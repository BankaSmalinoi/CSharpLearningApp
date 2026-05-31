from fastapi import FastAPI
from pydantic import BaseModel
from pathlib import Path
import subprocess
import tempfile
import shutil
import json
import re
import os

app = FastAPI()

TASKS_PATH = Path("tasks.json")


class CheckRequest(BaseModel):
    taskId: str
    code: str


class TestResult(BaseModel):
    input: str
    expectedOutput: str
    actualOutput: str
    passed: bool
    error: str | None = None


class CheckResponse(BaseModel):
    success: bool
    status: str
    message: str
    results: list[TestResult]


def load_tasks():
    with open(TASKS_PATH, "r", encoding="utf-8") as file:
        data = json.load(file)

    if isinstance(data, dict):
        return data.get("tasks") or data.get("practices") or []

    return data


def find_task(task_id: str):
    for task in load_tasks():
        if task.get("id") == task_id:
            return task
    return None


def normalize_output(value: str) -> str:
    if value is None:
        return ""

    value = value.replace("\r\n", "\n").replace("\r", "\n")
    lines = [line.rstrip() for line in value.split("\n")]
    return "\n".join(lines).strip()


def has_forbidden_code(code: str) -> str | None:
    forbidden_patterns = [
        r"\bSystem\.IO\b",
        r"\bFile\b",
        r"\bDirectory\b",
        r"\bProcess\b",
        r"\bThread\b",
        r"\bTask\b",
        r"\bEnvironment\b",
        r"\bReflection\b",
        r"\bDllImport\b",
        r"\bunsafe\b",
        r"while\s*\(\s*true\s*\)",
        r"for\s*\(\s*;\s*;\s*\)"
    ]

    for pattern in forbidden_patterns:
        if re.search(pattern, code):
            return f"Запрещённая конструкция: {pattern}"

    return None


def indent_code(code: str, spaces: int) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line for line in code.splitlines())


def ensure_using_system_text(code: str) -> str:
    if "System.Text" in code:
        return code

    if "using System;" in code:
        return code.replace("using System;", "using System;\nusing System.Text;", 1)

    return "using System;\nusing System.Text;\n\n" + code


def inject_utf8_encoding_into_main(code: str) -> str:
    if "Console.OutputEncoding" in code and "Console.InputEncoding" in code:
        return code

    code = ensure_using_system_text(code)

    injection = (
        "Console.OutputEncoding = Encoding.UTF8;\n"
        "        Console.InputEncoding = Encoding.UTF8;\n"
    )

    patterns = [
        r"(static\s+void\s+Main\s*\([^)]*\)\s*\{\s*)",
        r"(static\s+int\s+Main\s*\([^)]*\)\s*\{\s*)",
        r"(public\s+static\s+void\s+Main\s*\([^)]*\)\s*\{\s*)",
        r"(public\s+static\s+int\s+Main\s*\([^)]*\)\s*\{\s*)",
    ]

    for pattern in patterns:
        if re.search(pattern, code, flags=re.MULTILINE):
            return re.sub(pattern, r"\1\n        " + injection, code, count=1, flags=re.MULTILINE)

    return code


def wrap_code_if_needed(code: str) -> str:
    if "class Program" in code or "static void Main" in code or "static int Main" in code:
        return inject_utf8_encoding_into_main(code)

    wrapped = f"""using System;
using System.Text;

class Program
{{
    static void Main()
    {{
        Console.OutputEncoding = Encoding.UTF8;
        Console.InputEncoding = Encoding.UTF8;

{indent_code(code, 8)}
    }}
}}
"""
    return wrapped


def create_project(temp_dir: Path, code: str):
    csproj = temp_dir / "UserSolution.csproj"
    program = temp_dir / "Program.cs"

    csproj.write_text(
        """<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <OutputType>Exe</OutputType>
    <TargetFramework>net8.0</TargetFramework>
    <ImplicitUsings>disable</ImplicitUsings>
    <Nullable>disable</Nullable>
    <ConsoleOutputEncoding>utf-8</ConsoleOutputEncoding>
  </PropertyGroup>
</Project>
""",
        encoding="utf-8"
    )

    program.write_text(wrap_code_if_needed(code), encoding="utf-8")


def run_user_code(code: str, input_data: str, build_timeout_seconds: int = 20, run_timeout_seconds: int = 5):
    temp_dir = Path(tempfile.mkdtemp(prefix="csharp_check_"))

    env = os.environ.copy()
    env["DOTNET_CLI_UI_LANGUAGE"] = "ru"
    env["DOTNET_NOLOGO"] = "1"

    try:
        create_project(temp_dir, code)

        build_process = subprocess.run(
            ["dotnet", "build", "--nologo"],
            cwd=temp_dir,
            text=True,
            encoding="utf-8",
            errors="replace",
            capture_output=True,
            timeout=build_timeout_seconds,
            env=env
        )

        if build_process.returncode != 0:
            return "", build_process.stderr + build_process.stdout

        run_process = subprocess.run(
            ["dotnet", "run", "--no-build", "--nologo"],
            cwd=temp_dir,
            input=input_data,
            text=True,
            encoding="utf-8",
            errors="replace",
            capture_output=True,
            timeout=run_timeout_seconds,
            env=env
        )

        if run_process.returncode != 0:
            return run_process.stdout, run_process.stderr

        return run_process.stdout, None

    except subprocess.TimeoutExpired:
        return "", "Программа выполнялась слишком долго. Возможно, есть бесконечный цикл."

    finally:
        shutil.rmtree(temp_dir, ignore_errors=True)


@app.get("/")
def root():
    return {"message": "C# checker backend is running"}


@app.post("/check", response_model=CheckResponse)
def check_solution(request: CheckRequest):
    task = find_task(request.taskId)

    if task is None:
        return CheckResponse(
            success=False,
            status="task_not_found",
            message="Задание не найдено",
            results=[]
        )

    forbidden_error = has_forbidden_code(request.code)
    if forbidden_error:
        return CheckResponse(
            success=False,
            status="forbidden_code",
            message=forbidden_error,
            results=[]
        )

    tests = task.get("tests") or [
        {
            "input": task.get("inputExample", ""),
            "expectedOutput": task.get("outputExample", "")
        }
    ]

    results = []
    all_passed = True

    for test in tests:
        input_data = test.get("input", "")
        expected = normalize_output(test.get("expectedOutput", ""))

        actual_raw, error = run_user_code(request.code, input_data)
        actual = normalize_output(actual_raw)

        passed = error is None and actual == expected

        if not passed:
            all_passed = False

        results.append(TestResult(
            input=input_data,
            expectedOutput=expected,
            actualOutput=actual,
            passed=passed,
            error=error
        ))

    return CheckResponse(
        success=all_passed,
        status="accepted" if all_passed else "wrong_answer",
        message="Решение принято" if all_passed else "Решение не прошло проверку",
        results=results
    )

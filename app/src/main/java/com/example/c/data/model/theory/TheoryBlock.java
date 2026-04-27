package com.example.c.data.model.theory;

public class TheoryBlock {
    public String type;
    public String text;
    public String code;
    public String language;

    public TheoryBlock() {
    }

    public String getType() {
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        if (text != null) {
            return text;
        }
        return code != null ? code : "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code != null ? code : "";
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language != null ? language : "";
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

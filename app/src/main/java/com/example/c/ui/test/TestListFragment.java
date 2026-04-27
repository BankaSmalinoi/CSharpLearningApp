package com.example.c.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.model.test.TestModel;
import com.example.c.viewmodel.TestViewModel;
import com.example.c.viewmodel.TheoryViewModel;

import java.util.List;

public class TestListFragment extends Fragment {

    private TestViewModel testViewModel;
    private TheoryViewModel theoryViewModel;
    private TestListAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout root = new FrameLayout(requireContext());

        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setClipToPadding(false);
        recyclerView.setPadding(0, 8, 0, 16);
        root.addView(recyclerView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        emptyView = new TextView(requireContext());
        emptyView.setText("Тесты не найдены");
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setVisibility(View.GONE);
        root.addView(emptyView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        testViewModel = new ViewModelProvider(this).get(TestViewModel.class);
        theoryViewModel = new ViewModelProvider(this).get(TheoryViewModel.class);

        adapter = new TestListAdapter(requireContext(), new TestListAdapter.OnTestClickListener() {
            @Override
            public void onTestClick(TestModel test) {
                openTestIfAvailable(test);
            }
        });
        recyclerView.setAdapter(adapter);

        List<TestModel> tests = testViewModel.getAllTests();
        adapter.setTests(tests);
        emptyView.setVisibility(tests == null || tests.isEmpty() ? View.VISIBLE : View.GONE);

        theoryViewModel.observeAllProgress().observe(getViewLifecycleOwner(), new Observer<List<TopicProgressEntity>>() {
            @Override
            public void onChanged(List<TopicProgressEntity> progress) {
                adapter.setProgressList(progress);
            }
        });
    }

    private void openTestIfAvailable(TestModel test) {
        if (test == null) {
            return;
        }

        if (test.topicId != null && !test.topicId.trim().isEmpty() && !theoryViewModel.isTheoryRead(test.topicId)) {
            Toast.makeText(requireContext(), "Сначала прочитайте теорию по этой теме", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), TestPassingActivity.class);
        intent.putExtra(TestPassingActivity.EXTRA_TEST_ID, test.id);
        startActivity(intent);
    }
}

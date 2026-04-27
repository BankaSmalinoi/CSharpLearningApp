package com.example.c.ui.theory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.model.theory.TheoryTopic;
import com.example.c.viewmodel.TheoryViewModel;

import java.util.List;

public class TheoryListFragment extends Fragment {

    private TheoryViewModel viewModel;
    private TheoryTopicAdapter adapter;
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
        emptyView.setText("Темы не найдены");
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
        viewModel = new ViewModelProvider(this).get(TheoryViewModel.class);

        adapter = new TheoryTopicAdapter(requireContext(), new TheoryTopicAdapter.OnTopicClickListener() {
            @Override
            public void onTopicClick(TheoryTopic topic) {
                Intent intent = new Intent(requireContext(), TheoryTopicActivity.class);
                intent.putExtra(TheoryTopicActivity.EXTRA_TOPIC_ID, topic.id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        List<TheoryTopic> topics = viewModel.getTopics();
        adapter.setTopics(topics);
        emptyView.setVisibility(topics == null || topics.isEmpty() ? View.VISIBLE : View.GONE);

        viewModel.observeAllProgress().observe(getViewLifecycleOwner(), new Observer<List<TopicProgressEntity>>() {
            @Override
            public void onChanged(List<TopicProgressEntity> progress) {
                adapter.setProgressList(progress);
            }
        });
    }
}

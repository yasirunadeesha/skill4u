package com.s23010269.skill4u;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private List<Task> tasks;
    private OnTaskClickListener listener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategory;
        TextView tvTitle, tvDateTime;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategory = itemView.findViewById(R.id.iv_task_category);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDateTime = itemView.findViewById(R.id.tv_task_datetime);
        }

        void bind(Task task) {
            // Set category icon based on task.category
            int iconRes = itemView.getContext()
                    .getResources()
                    .getIdentifier("ic_category_" + task.category.toLowerCase(),
                            "drawable", itemView.getContext().getPackageName());
            ivCategory.setImageResource(iconRes);

            tvTitle.setText(task.title);
            tvDateTime.setText(task.date + " " + task.time);
        }
    }
}

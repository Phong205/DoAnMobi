package com.example.quanlydeadline.adapters;

import android.graphics.Paint;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.R;
import com.example.quanlydeadline.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onTaskCheckedChange(Task task, boolean isChecked);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
    }

    private List<Task> tasks = new ArrayList<>();
    private final OnTaskActionListener listener;

    public TaskAdapter(OnTaskActionListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> newTasks) {
        this.tasks = newTasks != null ? newTasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        // Tránh trigger listener khi set lại trạng thái checkbox do recycle view
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone);
        applyDoneStyle(holder, task.isDone);

        holder.tvTitle.setText(task.title);

        if (task.note != null && !task.note.trim().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(task.note);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        if (task.dueDate > 0) {
            String dateStr = DateFormat.format("dd/MM/yyyy HH:mm", task.dueDate).toString();
            holder.tvDueDate.setText(dateStr);
        } else {
            holder.tvDueDate.setText("Chưa đặt hạn");
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyDoneStyle(holder, isChecked);
            if (listener != null) listener.onTaskCheckedChange(task, isChecked);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onTaskEdit(task);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onTaskDelete(task);
        });
    }

    private void applyDoneStyle(TaskViewHolder holder, boolean isDone) {
        if (isDone) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTitle, tvNote, tvDueDate;
        View btnEdit, btnDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbTaskDone);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvNote = itemView.findViewById(R.id.tvTaskNote);
            tvDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            btnEdit = itemView.findViewById(R.id.btnEditTask);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}

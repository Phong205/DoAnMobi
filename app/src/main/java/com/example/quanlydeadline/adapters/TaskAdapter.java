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
    private android.content.Context context;
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
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone);
        applyDoneStyle(holder, task.isDone);

        holder.tvTitle.setText(task.title);
        if (holder.layoutFileAttach != null) {
            if (task.fileName != null && !task.fileName.trim().isEmpty()) {
                holder.layoutFileAttach.setVisibility(View.VISIBLE);
                if (holder.tvFileName != null) {
                    holder.tvFileName.setText("📎 " + task.fileName);
                    holder.tvFileName.setOnClickListener(v -> {

                        if (task.fileUrl == null) return;

                        java.io.File file = new java.io.File(task.fileUrl);

                        if (!file.exists()) {
                            android.widget.Toast.makeText(context,
                                    "Không tìm thấy file",
                                    android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }

                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                context.getPackageName() + ".provider",
                                file
                        );

                        android.content.Intent intent =
                                new android.content.Intent(android.content.Intent.ACTION_VIEW);

                        intent.setDataAndType(uri, "*/*");
                        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        context.startActivity(intent);

                    });
                }
            } else {
                holder.layoutFileAttach.setVisibility(View.GONE);
            }
        }
        // Note badge
        if (task.note != null && !task.note.trim().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(task.note);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        // Due date
        if (task.dueDate > 0) {
            holder.tvDueDate.setText("📅 " + DateFormat.format("dd/MM/yyyy HH:mm", task.dueDate));
        } else {
            holder.tvDueDate.setText("📅 Chưa đặt hạn");
        }

        // ✅ Priority badge
        switch (task.priority) {
            case 2: // Cao
                holder.tvPriorityBadge.setText("Cao");
                holder.tvPriorityBadge.setTextColor(0xFFEF4444);
                holder.tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_high);
                break;
            case 1: // Trung bình
                holder.tvPriorityBadge.setText("Trung bình");
                holder.tvPriorityBadge.setTextColor(0xFFF59E0B);
                holder.tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_medium);
                break;
            default: // 0 = Thấp
                holder.tvPriorityBadge.setText("Thấp");
                holder.tvPriorityBadge.setTextColor(0xFF16A34A);
                holder.tvPriorityBadge.setBackgroundResource(R.drawable.badge_priority_low);
                break;
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
        TextView tvPriorityBadge; // ✅ thêm field này
        View btnEdit, btnDelete;
        View layoutFileAttach;
        TextView tvFileName;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbTaskDone);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvNote = itemView.findViewById(R.id.tvTaskNote);
            tvDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge); // ✅ thêm dòng này
            btnEdit = itemView.findViewById(R.id.btnEditTask);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
            layoutFileAttach = itemView.findViewById(R.id.layoutFileAttach);
            tvFileName = itemView.findViewById(R.id.tvFileName);
        }
    }

}
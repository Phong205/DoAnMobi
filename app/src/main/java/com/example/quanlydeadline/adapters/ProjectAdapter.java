package com.example.quanlydeadline.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.R;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectActionListener {
        void onProjectClick(Project project);
        void onProjectEdit(Project project);
        void onProjectDelete(Project project);
    }

    private List<Project> projects = new ArrayList<>();
    private final OnProjectActionListener listener;
    private final TaskDao taskDao; // ✅ thêm TaskDao

    public ProjectAdapter(OnProjectActionListener listener, TaskDao taskDao) {
        this.listener = listener;
        this.taskDao = taskDao;
    }

    public void setProjects(List<Project> newProjects) {
        this.projects = newProjects != null ? newProjects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);

        holder.tvName.setText(project.name);

        // Mô tả
        if (project.description != null && !project.description.trim().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(project.description);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Hạn chót
        if (project.dueDate > 0) {
            String dateStr = DateFormat.format("dd/MM/yyyy", project.dueDate).toString();
            holder.tvDueDate.setText("Hạn chót: " + dateStr);
            holder.tvDueDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDueDate.setVisibility(View.GONE);
        }

        // ✅ Đếm task từ DB và cập nhật UI
        if (taskDao != null) {
            int total = taskDao.countAllTasks(project.id);
            int done = taskDao.countDoneTasks(project.id);
            int percent = total > 0 ? (done * 100 / total) : 0;

            holder.tvTaskCount.setText(done + "/" + total + " tasks");
            holder.tvPercent.setText(percent + "%");
            holder.progressBar.setProgress(percent);

            // Màu % theo tiến độ
            if (percent == 100) {
                holder.tvPercent.setTextColor(0xFF16A34A); // xanh lá
            } else if (percent >= 50) {
                holder.tvPercent.setTextColor(0xFF2962FF); // xanh dương
            } else {
                holder.tvPercent.setTextColor(0xFFF59E0B); // vàng
            }
        }

        // Click vào card → mở ProjectDetail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProjectClick(project);
        });

        // ✅ Nút 3 chấm → popup menu Sửa/Xóa
        holder.btnArrow.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 1, 0, "✏️ Sửa đồ án");
            popup.getMenu().add(0, 2, 1, "🗑️ Xóa đồ án");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    if (listener != null) listener.onProjectEdit(project);
                    return true;
                } else if (item.getItemId() == 2) {
                    if (listener != null) listener.onProjectDelete(project);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvDueDate, tvTaskCount, tvPercent;
        ProgressBar progressBar;
        ImageButton btnArrow;
        View btnEdit, btnDelete;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProjectName);
            tvDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvDueDate = itemView.findViewById(R.id.tvProjectDueDate);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);       // ✅
            tvPercent = itemView.findViewById(R.id.tvProjectPercent);    // ✅
            progressBar = itemView.findViewById(R.id.progressProject);   // ✅
            btnArrow = itemView.findViewById(R.id.btnArrow);             // ✅
            btnEdit = itemView.findViewById(R.id.btnEditProject);
            btnDelete = itemView.findViewById(R.id.btnDeleteProject);
        }
    }
}
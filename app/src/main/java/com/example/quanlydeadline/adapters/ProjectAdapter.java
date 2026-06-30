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
import com.example.quanlydeadline.models.Project;
import com.example.quanlydeadline.models.ProjectWithProgress;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectActionListener {
        void onProjectClick(Project project);
        void onProjectEdit(Project project);
        void onProjectDelete(Project project);
    }

    private List<ProjectWithProgress> projects = new ArrayList<>();
    private final OnProjectActionListener listener;

    public ProjectAdapter(OnProjectActionListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<ProjectWithProgress> newProjects) {
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
        ProjectWithProgress item = projects.get(position);
        Project project = item.project;

        holder.tvName.setText(project.name);
        holder.tvDescription.setText(project.description);

        if (project.dueDate > 0) {
            holder.tvDueDate.setVisibility(View.VISIBLE);
            holder.tvDueDate.setText("Hạn chót: " + DateFormat.format("dd/MM/yyyy", project.dueDate));
        } else {
            holder.tvDueDate.setVisibility(View.GONE);
        }

        int total = item.totalTasks;
        int done = item.doneTasks;
        int percent = item.getProgressPercentage();

        holder.tvTaskCount.setText(done + "/" + total + " công việc");
        holder.tvPercent.setText(percent + "%");
        holder.progressBar.setProgress(percent);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProjectClick(project);
        });

        holder.btnArrow.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnArrow);
            popup.getMenu().add(1, 1, 1, "Sửa đồ án");
            popup.getMenu().add(1, 2, 2, "Xóa đồ án");

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 1) {
                    if (listener != null) listener.onProjectEdit(project);
                    return true;
                } else if (menuItem.getItemId() == 2) {
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

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProjectName);
            tvDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvDueDate = itemView.findViewById(R.id.tvProjectDueDate);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);
            tvPercent = itemView.findViewById(R.id.tvProjectPercent);
            progressBar = itemView.findViewById(R.id.progressProject);
            btnArrow = itemView.findViewById(R.id.btnArrow);
        }
    }
}
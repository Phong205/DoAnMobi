package com.example.quanlydeadline.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.R;
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

    public ProjectAdapter(OnProjectActionListener listener) {
        this.listener = listener;
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

        if (project.description != null && !project.description.trim().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(project.description);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        if (project.dueDate > 0) {
            String dateStr = DateFormat.format("dd/MM/yyyy", project.dueDate).toString();
            holder.tvDueDate.setText("Hạn chót: " + dateStr);
            holder.tvDueDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDueDate.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProjectClick(project);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onProjectEdit(project);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onProjectDelete(project);
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvDueDate;
        View btnEdit, btnDelete;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProjectName);
            tvDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvDueDate = itemView.findViewById(R.id.tvProjectDueDate);
            btnEdit = itemView.findViewById(R.id.btnEditProject);
            btnDelete = itemView.findViewById(R.id.btnDeleteProject);
        }
    }
}

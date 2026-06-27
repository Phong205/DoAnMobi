package com.example.quanlydeadline.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.R;

import java.util.ArrayList;
import java.util.List;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.StatsViewHolder> {

    public static class ProjectStat {
        public String name;
        public int totalTasks;
        public int doneTasks;

        public ProjectStat(String name, int totalTasks, int doneTasks) {
            this.name = name;
            this.totalTasks = totalTasks;
            this.doneTasks = doneTasks;
        }

        public int getPercent() {
            if (totalTasks == 0) return 0;
            return (int) ((doneTasks * 100.0) / totalTasks);
        }
    }

    private List<ProjectStat> stats = new ArrayList<>();

    public void setStats(List<ProjectStat> newStats) {
        this.stats = newStats != null ? newStats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stats_project, parent, false);
        return new StatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
        ProjectStat stat = stats.get(position);
        holder.tvProjectName.setText(stat.name);
        holder.tvPercent.setText(stat.getPercent() + "%");
        holder.progressBar.setProgress(stat.getPercent());
        holder.tvTaskCount.setText(stat.doneTasks + "/" + stat.totalTasks + " tasks hoàn thành");
    }

    @Override
    public int getItemCount() { return stats.size(); }

    static class StatsViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvPercent, tvTaskCount;
        ProgressBar progressBar;

        StatsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
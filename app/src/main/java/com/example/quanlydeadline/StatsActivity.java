package com.example.quanlydeadline;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.adapters.StatsAdapter;
import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.ProjectDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.ProjectWithProgress;
import com.example.quanlydeadline.models.Task;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private TextView tvTotalProjects, tvTotalTasks, tvDueCount, tvOverdueCount;
    private TextView tvDoneCount, tvInProgressCount;
    private TextView tvCompletionPct, tvCompletionCircle, tvCompletionDetail, tvTotalLabel;
    private ProgressBar progressCompletion;
    private PieChart pieChart;
    private StatsAdapter statsAdapter;

    private TaskDao taskDao;
    private ProjectDao projectDao;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        taskDao = AppDatabase.getDatabase(this).taskDao();
        projectDao = AppDatabase.getDatabase(this).projectDao();
        currentUserId = new SessionManager(this).getUserId();

        tvTotalProjects    = findViewById(R.id.tvTotalProjects);
        tvTotalTasks       = findViewById(R.id.tvTotalTasks);
        tvDueCount         = findViewById(R.id.tvDueCount);
        tvOverdueCount     = findViewById(R.id.tvOverdueCount);
        tvDoneCount        = findViewById(R.id.tvDoneCount);
        tvInProgressCount  = findViewById(R.id.tvInProgressCount);
        tvCompletionPct    = findViewById(R.id.tvCompletionPct);
        tvCompletionCircle = findViewById(R.id.tvCompletionCircle);
        tvCompletionDetail = findViewById(R.id.tvCompletionDetail);
        tvTotalLabel       = findViewById(R.id.tvTotalLabel);
        progressCompletion = findViewById(R.id.progressCompletion);
        pieChart           = findViewById(R.id.pieChart);

        RecyclerView recyclerStats = findViewById(R.id.recyclerStats);
        recyclerStats.setLayoutManager(new LinearLayoutManager(this));
        statsAdapter = new StatsAdapter();
        recyclerStats.setAdapter(statsAdapter);

        loadStatsFromRoom();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_stats);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_projects) {
                startActivity(new Intent(this, ProjectListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_stats) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadStatsFromRoom() {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            long in3days = now + (3L * 24 * 60 * 60 * 1000);

            List<ProjectWithProgress> projects = projectDao.getProjectsWithProgress(currentUserId);
            List<Task> allTasks = taskDao.getAllTasksByUser(currentUserId);

            int totalProjects = projects.size();
            int total = allTasks.size();
            int done = 0, due = 0, overdue = 0, inProgress = 0, todo = 0;

            for (Task t : allTasks) {
                if (t.isDone) {
                    done++;
                } else if (t.dueDate > 0) {
                    if (t.dueDate < now) overdue++;
                    else if (t.dueDate <= in3days) due++;
                    else inProgress++;
                } else {
                    todo++;
                }
            }

            List<StatsAdapter.ProjectStat> statList = new ArrayList<>();
            for (ProjectWithProgress p : projects) {
                statList.add(new StatsAdapter.ProjectStat(
                        p.project.name, p.totalTasks, p.doneTasks));
            }

            int finalTotal = total, finalDone = done, finalDue = due,
                    finalOverdue = overdue, finalInProgress = inProgress, finalTodo = todo;

            runOnUiThread(() -> {
                tvTotalProjects.setText(String.valueOf(totalProjects));
                updateUI(finalTotal, finalTodo, finalInProgress, finalDue, finalOverdue, finalDone, statList);
            });
        }).start();
    }

    private void updateUI(int total, int todo, int inProgress, int due, int overdue, int done,
                          List<StatsAdapter.ProjectStat> statList) {
        tvTotalTasks.setText(String.valueOf(total));
        tvDueCount.setText(String.valueOf(due));
        tvOverdueCount.setText(String.valueOf(overdue));
        tvDoneCount.setText(String.valueOf(done));
        tvInProgressCount.setText(String.valueOf(inProgress));

        int pct = total == 0 ? 0 : (int)(done * 100.0 / total);
        tvCompletionPct.setText(pct + "%");
        tvCompletionCircle.setText(pct + "%");
        tvCompletionDetail.setText(done + " / " + total + " tasks đã hoàn thành");
        progressCompletion.setProgress(pct);

        setLegend(R.id.legendTodo,       "Todo",       todo,     "#9CA3AF");
        setLegend(R.id.legendInProgress, "In Progress", inProgress, "#3B82F6");
        setLegend(R.id.legendDue,        "Sắp hết hạn", due,     "#F59E0B");
        setLegend(R.id.legendOverdue,    "Quá hạn",    overdue,  "#EF4444");
        setLegend(R.id.legendDone,       "Hoàn thành", done,     "#22C55E");
        tvTotalLabel.setText("Tổng: " + total + " tasks");

        setupPieChart(todo, inProgress, due, overdue, done);

        statsAdapter.setStats(statList);
    }

    private void setLegend(int legendId, String label, int value, String colorHex) {
        View legendView = findViewById(legendId);
        if (legendView == null) return;
        View dot = legendView.findViewById(R.id.legendColor);
        TextView tvLabel = legendView.findViewById(R.id.legendLabel);
        TextView tvValue = legendView.findViewById(R.id.legendValue);
        dot.getBackground().mutate().setTint(Color.parseColor(colorHex));
        tvLabel.setText(label);
        tvValue.setText(String.valueOf(value));
    }

    private void setupPieChart(int todo, int inProgress, int due, int overdue, int done) {
        List<PieEntry> entries = new ArrayList<>();
        if (todo > 0)       entries.add(new PieEntry(todo, ""));
        if (inProgress > 0) entries.add(new PieEntry(inProgress, ""));
        if (due > 0)        entries.add(new PieEntry(due, ""));
        if (overdue > 0)    entries.add(new PieEntry(overdue, ""));
        if (done > 0)       entries.add(new PieEntry(done, ""));

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        List<Integer> colors = new ArrayList<>();
        if (todo > 0)       colors.add(Color.parseColor("#9CA3AF"));
        if (inProgress > 0) colors.add(Color.parseColor("#3B82F6"));
        if (due > 0)        colors.add(Color.parseColor("#F59E0B"));
        if (overdue > 0)    colors.add(Color.parseColor("#EF4444"));
        if (done > 0)       colors.add(Color.parseColor("#22C55E"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatsFromRoom();
    }
}
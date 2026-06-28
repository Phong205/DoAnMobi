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
import com.example.quanlydeadline.database.SessionManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsActivity extends AppCompatActivity {

    private TextView tvTotalProjects, tvTotalTasks, tvDueCount, tvOverdueCount;
    private TextView tvDoneCount, tvInProgressCount;
    private TextView tvCompletionPct, tvCompletionCircle, tvCompletionDetail, tvTotalLabel;
    private ProgressBar progressCompletion;
    private PieChart pieChart;
    private StatsAdapter statsAdapter;
    private FirebaseFirestore db;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        db = FirebaseFirestore.getInstance();
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

        loadStatsFromFirebase();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_stats);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { finish(); return true; }
            else if (id == R.id.nav_projects) {
                startActivity(new Intent(this, ProjectListActivity.class));
                return true;
            }
            else if (id == R.id.nav_stats) return true;
            else if (id == R.id.nav_profile) return true;
            return false;
        });
    }

    private void loadStatsFromFirebase() {
        long now = System.currentTimeMillis();
        long in3days = now + (3L * 24 * 60 * 60 * 1000);

        db.collection("projects")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(projectSnap -> {
                    int totalProjects = projectSnap.size();
                    tvTotalProjects.setText(String.valueOf(totalProjects));

                    if (totalProjects == 0) {
                        updateUI(0, 0, 0, 0, 0, 0, new ArrayList<>());
                        return;
                    }

                    List<StatsAdapter.ProjectStat> statList = new ArrayList<>();
                    AtomicInteger processed      = new AtomicInteger(0);
                    AtomicInteger totalTasks     = new AtomicInteger(0);
                    AtomicInteger dueTasks       = new AtomicInteger(0);
                    AtomicInteger overdueTasks   = new AtomicInteger(0);
                    AtomicInteger doneTasks      = new AtomicInteger(0);
                    AtomicInteger inProgressTask = new AtomicInteger(0);

                    for (QueryDocumentSnapshot projectDoc : projectSnap) {
                        String projectId   = projectDoc.getId();
                        String projectName = projectDoc.getString("name");

                        db.collection("tasks")
                                .whereEqualTo("projectId", Integer.parseInt(projectId))
                                .get()
                                .addOnSuccessListener(taskSnap -> {
                                    int total = taskSnap.size();
                                    int done = 0, due = 0, overdue = 0, inProgress = 0;

                                    for (QueryDocumentSnapshot taskDoc : taskSnap) {
                                        Boolean isDone = taskDoc.getBoolean("isDone");
                                        Long dueDate   = taskDoc.getLong("dueDate");

                                        if (Boolean.TRUE.equals(isDone)) {
                                            done++;
                                        } else if (dueDate != null) {
                                            if (dueDate < now) overdue++;
                                            else if (dueDate <= in3days) due++;
                                            else inProgress++;
                                        } else {
                                            inProgress++;
                                        }
                                    }

                                    totalTasks.addAndGet(total);
                                    dueTasks.addAndGet(due);
                                    overdueTasks.addAndGet(overdue);
                                    doneTasks.addAndGet(done);
                                    inProgressTask.addAndGet(inProgress);

                                    statList.add(new StatsAdapter.ProjectStat(
                                            projectName != null ? projectName : "Không tên",
                                            total, done));

                                    if (processed.incrementAndGet() == totalProjects) {
                                        int t  = totalTasks.get();
                                        int du = dueTasks.get();
                                        int ov = overdueTasks.get();
                                        int dn = doneTasks.get();
                                        int ip = inProgressTask.get();
                                        int todo = t - du - ov - dn - ip;

                                        runOnUiThread(() -> updateUI(t, todo, ip, du, ov, dn, statList));
                                    }
                                });
                    }
                });
    }

    private void updateUI(int total, int todo, int inProgress, int due, int overdue, int done,
                          List<StatsAdapter.ProjectStat> statList) {
        tvTotalTasks.setText(String.valueOf(total));
        tvDueCount.setText(String.valueOf(due));
        tvOverdueCount.setText(String.valueOf(overdue));
        tvDoneCount.setText(String.valueOf(done));
        tvInProgressCount.setText(String.valueOf(inProgress));

        // Tỉ lệ hoàn thành
        int pct = total == 0 ? 0 : (int)(done * 100.0 / total);
        tvCompletionPct.setText(pct + "%");
        tvCompletionCircle.setText(pct + "%");
        tvCompletionDetail.setText(done + " / " + total + " tasks đã hoàn thành");
        progressCompletion.setProgress(pct);

        // Legend
        setLegend(R.id.legendTodo,       "Todo",       todo,     "#9CA3AF");
        setLegend(R.id.legendInProgress, "In Progress", inProgress, "#3B82F6");
        setLegend(R.id.legendDue,        "Sắp hết hạn", due,     "#F59E0B");
        setLegend(R.id.legendOverdue,    "Quá hạn",    overdue,  "#EF4444");
        setLegend(R.id.legendDone,       "Hoàn thành", done,     "#22C55E");
        tvTotalLabel.setText("Tổng: " + total + " tasks");

        // Pie chart
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
        loadStatsFromFirebase();
    }
}
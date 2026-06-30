package com.example.quanlydeadline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.NotificationHelper;
import com.example.quanlydeadline.database.NotificationSettingsDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.DeadlineNotification;
import com.example.quanlydeadline.models.NotificationSettings;
import com.example.quanlydeadline.models.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerUnread, recyclerRead;
    private TextView tvUnreadCount, tvReadCount, tvMarkAllRead;
    private TextView tvEmptyState;
    private List<DeadlineNotification> allNotifications = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvUnreadCount = findViewById(R.id.tvUnreadCount);
        tvReadCount = findViewById(R.id.tvReadCount);
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
        tvEmptyState = findViewById(R.id.tvEmptyNotification);
        recyclerUnread = findViewById(R.id.recyclerUnread);
        recyclerRead = findViewById(R.id.recyclerRead);

        recyclerUnread.setLayoutManager(new LinearLayoutManager(this));
        recyclerRead.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();

        tvMarkAllRead.setOnClickListener(v -> {
            for (DeadlineNotification n : allNotifications) n.isRead = true;
            renderNotifications();
        });
    }

    private void loadNotifications() {
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();
        TaskDao taskDao = AppDatabase.getDatabase(this).taskDao();
        NotificationSettingsDao settingsDao = AppDatabase.getDatabase(this).notificationSettingsDao();

        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasksByUser(userId);
            NotificationSettings settings = settingsDao.getSettings(userId);
            allNotifications = NotificationHelper.generateNotifications(tasks, settings);
            runOnUiThread(this::renderNotifications);
        }).start();
    }

    private void renderNotifications() {
        List<DeadlineNotification> unread = new ArrayList<>();
        List<DeadlineNotification> read = new ArrayList<>();

        for (DeadlineNotification n : allNotifications) {
            if (n.isRead) read.add(n);
            else unread.add(n);
        }

        tvUnreadCount.setText("CHƯA ĐỌC (" + unread.size() + ")");
        tvReadCount.setText("ĐÃ ĐỌC (" + read.size() + ")");

        tvEmptyState.setVisibility(allNotifications.isEmpty() ? View.VISIBLE : View.GONE);

        recyclerUnread.setAdapter(new NotificationAdapter(unread, false));
        recyclerRead.setAdapter(new NotificationAdapter(read, true));
    }

    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
        private final List<DeadlineNotification> list;
        private final boolean isRead;

        NotificationAdapter(List<DeadlineNotification> list, boolean isRead) {
            this.list = list;
            this.isRead = isRead;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            DeadlineNotification n = list.get(position);

            holder.tvMessage.setText(n.message);
            holder.tvTime.setText(n.timeLabel);

            if (n.type == NotificationHelper.TYPE_URGENT) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.ivIcon.setColorFilter(0xFFEF4444); // đỏ
                holder.itemView.setBackgroundColor(isRead ? 0xFFFFFFFF : 0xFFFFF0F0);
            } else if (n.type == NotificationHelper.TYPE_WARNING) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.ivIcon.setColorFilter(0xFFF59E0B); // vàng
                holder.itemView.setBackgroundColor(isRead ? 0xFFFFFFFF : 0xFFFFFBF0);
            } else {
                holder.ivIcon.setImageResource(android.R.drawable.ic_popup_reminder);
                holder.ivIcon.setColorFilter(0xFF2962FF); // xanh
                holder.itemView.setBackgroundColor(isRead ? 0xFFFFFFFF : 0xFFF0F4FF);
            }

            holder.dotUnread.setVisibility(isRead ? View.GONE : View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                n.isRead = true;
                renderNotifications();
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvMessage, tvTime;
            View dotUnread;

            VH(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivNotifIcon);
                tvMessage = itemView.findViewById(R.id.tvNotifMessage);
                tvTime = itemView.findViewById(R.id.tvNotifTime);
                dotUnread = itemView.findViewById(R.id.dotUnread);
            }
        }
    }
}
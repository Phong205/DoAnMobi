package com.example.quanlydeadline.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.quanlydeadline.models.NotificationSettings;
import com.example.quanlydeadline.models.Project;
import com.example.quanlydeadline.models.Task;
import com.example.quanlydeadline.models.User;

// ✅ Tăng version từ 5 -> 6 vì Project và Task thêm cột "updated_at"
@Database(entities = {User.class, Project.class, Task.class, NotificationSettings.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ProjectDao projectDao();
    public abstract TaskDao taskDao();
    public abstract NotificationSettingsDao notificationSettingsDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "quanlydeadline_db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

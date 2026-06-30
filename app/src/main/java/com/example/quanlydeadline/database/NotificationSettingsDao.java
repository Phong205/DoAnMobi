package com.example.quanlydeadline.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quanlydeadline.models.NotificationSettings;

@Dao
public interface NotificationSettingsDao {

    @Query("SELECT * FROM notification_settings WHERE userId = :userId LIMIT 1")
    NotificationSettings getSettings(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(NotificationSettings settings);

    @Update
    void update(NotificationSettings settings);
}
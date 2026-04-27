package com.example.c.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.c.data.db.dao.AppStatisticsDao;
import com.example.c.data.db.dao.PracticeProgressDao;
import com.example.c.data.db.dao.TestProgressDao;
import com.example.c.data.db.dao.TopicProgressDao;
import com.example.c.data.db.dao.UserDao;
import com.example.c.data.db.entity.AppStatisticsEntity;
import com.example.c.data.db.entity.PracticeAttemptEntity;
import com.example.c.data.db.entity.PracticeProgressEntity;
import com.example.c.data.db.entity.TestAttemptEntity;
import com.example.c.data.db.entity.TestProgressEntity;
import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.db.entity.UserEntity;

@Database(
        entities = {
                UserEntity.class,
                TopicProgressEntity.class,
                TestProgressEntity.class,
                TestAttemptEntity.class,
                PracticeProgressEntity.class,
                PracticeAttemptEntity.class,
                AppStatisticsEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public abstract UserDao userDao();
    public abstract TopicProgressDao topicProgressDao();
    public abstract TestProgressDao testProgressDao();
    public abstract PracticeProgressDao practiceProgressDao();
    public abstract AppStatisticsDao appStatisticsDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "csharp_learning_db"
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static AppDatabase getDatabase(Context context) {
        return getInstance(context);
    }
}

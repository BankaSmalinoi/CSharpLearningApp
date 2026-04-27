package com.example.c.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.c.data.db.AppDatabase;
import com.example.c.data.db.dao.UserDao;
import com.example.c.data.db.entity.UserEntity;

public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        userDao = database.userDao();
    }

    public LiveData<UserEntity> observeUser() {
        return userDao.observeUser();
    }

    public void insertUser(UserEntity user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public void updateUser(UserEntity user) {
        executorService.execute(() -> userDao.update(user));
    }
}

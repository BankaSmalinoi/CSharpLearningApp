package com.example.c.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.c.data.db.entity.UserEntity;
import com.example.c.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final LiveData<UserEntity> user;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        user = repository.observeUser();
    }

    public LiveData<UserEntity> getUser() {
        return user;
    }

    public void createUser(String fullName, String photoUri) {
        UserEntity user = new UserEntity(fullName, photoUri);
        repository.insertUser(user);
    }

    public void updateUser(UserEntity user) {
        repository.updateUser(user);
    }
}

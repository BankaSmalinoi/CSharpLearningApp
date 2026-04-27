package com.example.c.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "display_name")
    public String displayName;

    @ColumnInfo(name = "photo_uri")
    public String photoUri;

    public UserEntity(String displayName, String photoUri) {
        this.displayName = displayName;
        this.photoUri = photoUri;
    }
}

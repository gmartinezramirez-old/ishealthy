package com.gmartinezramirez.healthyfood;

import android.content.Context;

import java.util.Date;
import java.util.UUID;

public class Food {

    private UUID mId;
    private String mPhotoPath;
    private boolean mIsHealthy;
    private boolean mIsFood;
    private long mTimestamp;

    public Food(UUID uuid) {
        Date date = new Date();
        mId = uuid;
        mIsFood = true;
        mTimestamp = date.getTime();
    }

    public Food() {
        this(UUID.randomUUID());
    }

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.mPhotoPath = photoPath;
    }

    public boolean isHealthy() {
        return mIsHealthy;
    }

    public void setHealthy(boolean healthy) {
        mIsHealthy = healthy;
    }

    public UUID getId() {
        return mId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public boolean isFood() {
        return mIsFood;
    }

    public void setFood(boolean food) {
        mIsFood = food;
    }
}
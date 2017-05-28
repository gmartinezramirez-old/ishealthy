package com.gmartinezramirez.healthyfood.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.gmartinezramirez.healthyfood.Food;
import com.gmartinezramirez.healthyfood.database.FoodDbSchema.FoodTable;

import java.util.UUID;

public class FoodCursorWrapper extends CursorWrapper {
    public FoodCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Food getFood() {
        String uuidString = getString(getColumnIndex(FoodTable.Cols.UUID));
        String photoPath = getString(getColumnIndex(FoodTable.Cols.PHOTO_PATH));
        int isHealthy = getInt(getColumnIndex(FoodTable.Cols.IS_HEALTHY));
        int isFood = getInt(getColumnIndex(FoodTable.Cols.IS_FOOD));
        long timestamp = getLong(getColumnIndex(FoodTable.Cols.TIMESTAMP));

        Food food = new Food(UUID.fromString(uuidString));
        food.setPhotoPath(photoPath);
        food.setHealthy(isHealthy != 0);
        food.setFood(isFood != 0);
        food.setTimestamp(timestamp);

        return food;
    }

}

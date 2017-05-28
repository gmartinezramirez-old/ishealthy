package com.gmartinezramirez.healthyfood;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gmartinezramirez.healthyfood.database.FoodBaseHelper;
import com.gmartinezramirez.healthyfood.database.FoodCursorWrapper;
import com.gmartinezramirez.healthyfood.database.FoodDbSchema.FoodTable;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class FoodLab {
    private static FoodLab sFoodLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static FoodLab get(Context context) {
        if (sFoodLab == null) {
            sFoodLab = new FoodLab(context);
        }
        return sFoodLab;
    }

    public ArrayList<Food> getFoods() {
        ArrayList<Food> foods = new ArrayList<>();
        try (FoodCursorWrapper cursor = queryFoods(null, null, FoodTable.Cols.TIMESTAMP + " DESC")) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                foods.add(cursor.getFood());
                cursor.moveToNext();
            }
        }
        return foods;
    }

    public void addFood(Food c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(FoodTable.NAME, null, values);
    }

    public void updateFood(Food food) {
        String uuidString = food.getId().toString();
        ContentValues values = getContentValues(food);
        String[] args = { uuidString };

        mDatabase.update(FoodTable.NAME, values, FoodTable.Cols.UUID + " = ?", args);
    }

    public Food getFood(UUID id) {
        String[] args = { id.toString() };
        try (FoodCursorWrapper cursor = queryFoods(FoodTable.Cols.UUID + " = ?", args, null)) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getFood();
        }
    }

    public void deleteFood(UUID id) {
        String[] args = { id.toString() };
        mDatabase.delete(FoodTable.NAME, FoodTable.Cols.UUID + " = ?", args);
    }

    public File getPhotoFile(Food food) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, food.getPhotoFilename());
    }

    /* 
     * query:
     *    * Name of the table
     *    * Columns
     *    * whereClause
     *    * whereArgs 
     *    * GroupBy
     *    * Having 
     *    * Orderby
     *
    */
    private FoodCursorWrapper queryFoods(String whereClause, String[] whereArgs, String orderArgs) {
        Cursor cursor = mDatabase.query(
                FoodTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderArgs
        );
        return new FoodCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Food food) {
        ContentValues values = new ContentValues();
        values.put(FoodTable.Cols.UUID, food.getId().toString());
        values.put(FoodTable.Cols.PHOTO_PATH, food.getPhotoPath());
        values.put(FoodTable.Cols.IS_HEALTHY, food.isHealthy() ? 1 : 0);
        values.put(FoodTable.Cols.IS_FOOD, food.isFood() ? 1 : 0);
        values.put(FoodTable.Cols.TIMESTAMP, food.getTimestamp());
        return values;
    }

    private FoodLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new FoodBaseHelper(mContext).getWritableDatabase();
    }
}

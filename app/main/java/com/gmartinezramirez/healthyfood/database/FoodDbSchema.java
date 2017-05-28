package com.gmartinezramirez.healthyfood.database;

public class FoodDbSchema {
    public static final class FoodTable {

        public static final String NAME = "foods";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String IS_HEALTHY = "isHealthy";
            public static final String IS_FOOD = "isFood";
            public static final String PHOTO_PATH = "photoPath";
            public static final String TIMESTAMP = "createdAt";
        }
    }
}

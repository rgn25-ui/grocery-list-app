package com.grocerylist.app.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.utils.Constants;

@Database(
        entities = {GroceryList.class, GroceryItem.class},
        version = 4,
        exportSchema = false
)
public abstract class GroceryDatabase extends RoomDatabase {

    public abstract GroceryDao groceryDao();

    private static volatile GroceryDatabase instance;

    public static GroceryDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (GroceryDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GroceryDatabase.class,
                                    Constants.DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration() // KEY FIX: Allows database recreation
                            .build();
                }
            }
        }
        return instance;
    }
}
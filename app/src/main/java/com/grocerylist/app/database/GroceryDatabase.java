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
        version = 5, // 5: pendingSync columns. Destructive migration is accepted: data is restored from the backend
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
                            .fallbackToDestructiveMigration(true) // Recreates the database on version changes; data is restored from the backend
                            .build();
                }
            }
        }
        return instance;
    }
}
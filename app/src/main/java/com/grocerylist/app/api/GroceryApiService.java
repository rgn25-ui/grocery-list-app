package com.grocerylist.app.api;

import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.models.SyncData;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroceryApiService {

    @GET("api/sync")
    Single<SyncData> getAllData(@Query("userId") String userId);

    @POST("api/lists")
    Single<GroceryList> createList(@Body GroceryList list);

    @DELETE("api/lists/{id}")
    Single<Void> deleteList(@Path("id") String id);

    @POST("api/items")
    Single<GroceryItem> createItem(@Body GroceryItem item);

    @DELETE("api/items/{id}")
    Single<Void> deleteItem(@Path("id") String id);

    @DELETE("api/lists/{listId}/completed-items")
    Single<Void> clearCompletedItems(@Path("listId") String listId);

    @DELETE("admin/clear-all")
    Single<String> clearAllData(@Query("userId") String userId, @Query("confirmToken") String confirmToken);
}
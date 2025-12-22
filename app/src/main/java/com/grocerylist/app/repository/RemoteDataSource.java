package com.grocerylist.app.repository;

import com.grocerylist.app.api.GroceryApiService;
import com.grocerylist.app.models.GroceryItem;
import com.grocerylist.app.models.GroceryList;
import com.grocerylist.app.models.SyncData;
import com.grocerylist.app.utils.Constants;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Handles all remote API operations (Backend communication)
 * Responsible for network calls to the Spring Boot backend
 */
public class RemoteDataSource {
    private final GroceryApiService apiService;
    private final CompositeDisposable disposables;
    private static final int TIMEOUT_SECONDS = 60;

    public RemoteDataSource() {
        this.disposables = new CompositeDisposable();

        // Create OkHttpClient with longer timeouts for cold starts
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        this.apiService = retrofit.create(GroceryApiService.class);
    }

    // ===== SYNC OPERATIONS =====

    public Single<SyncData> getAllData(String userId) {
        return apiService.getAllData(userId);
    }

    // ===== LIST OPERATIONS =====

    public Single<GroceryList> createList(GroceryList list) {
        return apiService.createList(list);
    }

    public Single<Void> deleteList(String listId) {
        return apiService.deleteList(listId);
    }

    // ===== ITEM OPERATIONS =====

    public Single<GroceryItem> createItem(GroceryItem item) {
        return apiService.createItem(item);
    }

    public Single<Void> deleteItem(String itemId) {
        return apiService.deleteItem(itemId);
    }

    public Single<Void> clearCompletedItems(String listId) {
        return apiService.clearCompletedItems(listId);
    }

    // ===== ADMIN OPERATIONS =====

    public Single<String> clearAllData(String userId, String confirmToken) {
        return apiService.clearAllData(userId, confirmToken);
    }

    // ===== CLEANUP =====

    public void cleanup() {
        disposables.dispose();
    }

    public CompositeDisposable getDisposables() {
        return disposables;
    }

    // Version control test - this is a test commit
    // Version control test - this is a test commit
    // Version control test - this is a test commit
}
package com.grocerylist.app.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.grocerylist.app.database.GroceryDatabase;

import java.util.concurrent.TimeUnit;

/**
 * Uploads pending local changes via WorkManager, so they reach the backend even if the app is
 * closed before the upload finishes (e.g. while Cloud Run is cold-starting).
 * Runs only with a network connection and retries with exponential backoff when it stops early.
 */
public class UploadWorker extends Worker {

    private static final String UNIQUE_WORK_NAME = "upload-pending-changes";
    private static final long INITIAL_BACKOFF_SECONDS = 30;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Queues an upload. APPEND_OR_REPLACE: a change made while an upload is running gets its own
     * pass afterward, so it is never missed; already-finished or failed work is replaced.
     */
    static void enqueue(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, INITIAL_BACKOFF_SECONDS, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        LocalDataSource localDataSource = new LocalDataSource(GroceryDatabase.getDatabase(context).groceryDao());
        RemoteDataSource remoteDataSource = new RemoteDataSource();
        try {
            boolean completed = new SyncManager(localDataSource, remoteDataSource, context).uploadPendingChanges();
            // Rejected items (4xx) count as handled; they stay pending and are tried on the next upload
            return completed ? Result.success() : Result.retry();
        } finally {
            remoteDataSource.cleanup();
        }
    }
}

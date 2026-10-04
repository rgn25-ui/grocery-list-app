package com.grocerylist.app.repository;

/**
 * Decides what to do when uploading one pending change fails.
 * Kept free of Android and Retrofit so it can be unit tested.
 */
final class UploadPolicy {

    enum Outcome {
        /** Sent and confirmed by the backend: clear the pending flag. */
        SENT,
        /** Rejected by the backend (bad data): keep it pending, but continue with the rest. */
        SKIP,
        /** Network error, rate limit or server error: stop and try everything again later. */
        STOP
    }

    private UploadPolicy() {
    }

    /** @param httpCode the HTTP status, or null when no response was received (offline, timeout) */
    static Outcome outcomeForFailure(Integer httpCode) {
        if (httpCode == null || httpCode == 429) {
            return Outcome.STOP;
        }
        if (httpCode >= 400 && httpCode < 500) {
            return Outcome.SKIP;
        }
        return Outcome.STOP;
    }
}

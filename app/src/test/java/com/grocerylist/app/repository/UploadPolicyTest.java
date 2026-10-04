package com.grocerylist.app.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UploadPolicyTest {

    @Test
    public void noResponse_stopsAndRetriesLater() {
        assertEquals(UploadPolicy.Outcome.STOP, UploadPolicy.outcomeForFailure(null));
    }

    @Test
    public void rateLimited_stopsAndRetriesLater() {
        assertEquals(UploadPolicy.Outcome.STOP, UploadPolicy.outcomeForFailure(429));
    }

    @Test
    public void serverError_stopsAndRetriesLater() {
        assertEquals(UploadPolicy.Outcome.STOP, UploadPolicy.outcomeForFailure(500));
        assertEquals(UploadPolicy.Outcome.STOP, UploadPolicy.outcomeForFailure(503));
    }

    @Test
    public void rejectedData_isSkippedSoTheRestCanContinue() {
        assertEquals(UploadPolicy.Outcome.SKIP, UploadPolicy.outcomeForFailure(400));
        assertEquals(UploadPolicy.Outcome.SKIP, UploadPolicy.outcomeForFailure(404));
    }
}

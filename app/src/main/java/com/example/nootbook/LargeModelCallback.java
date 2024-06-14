package com.example.nootbook;

public interface LargeModelCallback {
    void onSummaryGenerated(String summary);
    void onError(Exception e);
}

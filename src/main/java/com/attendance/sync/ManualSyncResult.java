package com.attendance.sync;

/**
 * Result summary for a manual date-range sync run from the GUI.
 */
public class ManualSyncResult {

    private int totalRecords;
    private int successCount;
    private int failedCount;
    private String errorMessage;

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean hasFatalError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}

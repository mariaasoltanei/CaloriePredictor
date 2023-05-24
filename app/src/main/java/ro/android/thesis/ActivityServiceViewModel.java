package ro.android.thesis;

import android.content.ServiceConnection;

import androidx.lifecycle.ViewModel;

public class ActivityServiceViewModel extends ViewModel {
    private ServiceConnection activityServiceConnection;
    private boolean isServiceBound;

    public boolean isServiceBound() {
        return isServiceBound;
    }

    public void setServiceBound(boolean serviceBound) {
        isServiceBound = serviceBound;
    }

    public ServiceConnection getActivityServiceConnection() {
        return activityServiceConnection;
    }

    public void setActivityServiceConnection(ServiceConnection activityServiceConnection) {
        this.activityServiceConnection = activityServiceConnection;
    }
}

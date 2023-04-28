package ro.android.thesis;

import android.content.ServiceConnection;

import androidx.lifecycle.ViewModel;

public class StepServiceViewModel extends ViewModel {
    private ServiceConnection stepServiceConnection;
    private boolean isServiceBound;

    public boolean isServiceBound() {
        return isServiceBound;
    }

    public void setServiceBound(boolean serviceBound) {
        isServiceBound = serviceBound;
    }

    public ServiceConnection getStepServiceConnection() {
        return stepServiceConnection;
    }

    public void setStepServiceConnection(ServiceConnection stepServiceConnection) {
        this.stepServiceConnection = stepServiceConnection;
    }
}

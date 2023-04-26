package ro.android.thesis;

import android.content.ServiceConnection;

import androidx.lifecycle.ViewModel;

public class StepServiceViewModel extends ViewModel {
    private ServiceConnection stepServiceConnection;

    public ServiceConnection getStepServiceConnection() {
        return stepServiceConnection;
    }

    public void setStepServiceConnection(ServiceConnection stepServiceConnection) {
        this.stepServiceConnection = stepServiceConnection;
    }
}

//package ro.android.thesis.utils;
//
//import java.util.Calendar;
//import java.util.TimerTask;
//
//import ro.android.thesis.services.StepService;
//
//public class ResetStepCountTask extends TimerTask {
//    private StepService stepService;
//
//    public ResetStepCountTask(StepService stepService) {
//        this.stepService = stepService;
//    }
//    @Override
//    public void run() {
//        Calendar now = Calendar.getInstance();
//        if (now.get(Calendar.HOUR_OF_DAY) == 20 && now.get(Calendar.MINUTE) == 53) {
//            stepService.resetStepCount();
//        }
//    }
//}
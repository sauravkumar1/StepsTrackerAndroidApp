package com.example.android.stepstracker.Widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class StepsTrackerUpdateService  extends IntentService {



    public StepsTrackerUpdateService() {
        super("StepsTrackerUpdateService");
    }

    public static void startStepsService(Context context, String steps) {
        Intent intent = new Intent(context, StepsTrackerUpdateService.class);
        intent.putExtra("STEPS",steps);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String steps = intent.getExtras().getString("STEPS");
            handleActionUpdateBakingWidgets(steps);
        }
    }



    private void handleActionUpdateBakingWidgets(String steps) {
        Intent intent = new Intent();
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        intent.putExtra("STEPS",steps);
        sendBroadcast(intent);
    }

}

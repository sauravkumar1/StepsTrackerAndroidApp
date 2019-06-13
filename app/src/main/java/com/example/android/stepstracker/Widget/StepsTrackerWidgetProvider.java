package com.example.android.stepstracker.Widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.stepstracker.MainActivity;
import com.example.android.stepstracker.R;

public class StepsTrackerWidgetProvider  extends AppWidgetProvider {


    static String stepsString ;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_view);
        //call activity when widget is clicked, but resume activity from stack so you do not pass intent.extras afresh
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.addCategory(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.tv_widget_steps, appPendingIntent);

        // Set the GridWidgetService intent to act as the adapter for the GridView
      Intent intent = new Intent(context, StepsTrackerWidgetService.class);
      //views.setRemoteAdapter(R.id.tv_widget_steps, intent);
      views.setTextViewText(R.id.tv_widget_steps, stepsString);
        // Instruct the widget manager to update the widget

        Intent configIntent = new Intent(context, MainActivity.class);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        views.setOnClickPendingIntent(R.id.tv_widget_steps, configPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);



    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateBakingWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, StepsTrackerWidgetProvider.class));

        final String action = intent.getAction();

        if (action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {
            stepsString = intent.getExtras().getString("STEPS");
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.tv_widget_steps);
            //Now update all widgets
            StepsTrackerWidgetProvider.updateBakingWidgets(context, appWidgetManager, appWidgetIds);
            super.onReceive(context, intent);
        }
    }

}


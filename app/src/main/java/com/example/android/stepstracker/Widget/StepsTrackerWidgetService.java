package com.example.android.stepstracker.Widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.stepstracker.R;

import java.util.ArrayList;

import static com.example.android.stepstracker.Widget.StepsTrackerWidgetProvider.stepsString;

public class StepsTrackerWidgetService extends RemoteViewsService {
    String remoteViewSteps;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory(this.getApplicationContext(),intent);
    }


    class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        Context mContext = null;

        public RemoteViewsFactory(Context context,Intent intent) {
            mContext = context;

        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            remoteViewSteps = stepsString;
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return 1 ;
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_view);


            views.setTextViewText(R.id.tv_widget_steps, stepsString);

            Intent fillInIntent = new Intent();
            //fillInIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.tv_widget_steps, fillInIntent);


            return null;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount()
        {
            return 1;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }




    }


}


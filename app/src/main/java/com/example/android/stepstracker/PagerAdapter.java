package com.example.android.stepstracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.android.stepstracker.Tabs.ReportsTab;
import com.example.android.stepstracker.Tabs.SettingsTab;
import com.example.android.stepstracker.Tabs.TodayTab;

public class PagerAdapter extends FragmentStatePagerAdapter {

    int mNoOfTabs;

    public PagerAdapter(FragmentManager fm, int NumberOfTabs)
    {
        super(fm);
        this.mNoOfTabs = NumberOfTabs;
    }


    @Override
    public Fragment getItem(int position) {
        switch(position)
        {

            case 0:
                TodayTab todaysTab = new TodayTab();
                return todaysTab;
            case 1:
                ReportsTab reportsTab = new ReportsTab();
                return  reportsTab;
            case 2:
                SettingsTab settingsTab = new SettingsTab();
                return  settingsTab;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNoOfTabs;
    }
}

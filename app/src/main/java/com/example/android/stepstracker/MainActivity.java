package com.example.android.stepstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;

import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.example.android.stepstracker.Tabs.ReportsTab;
import com.example.android.stepstracker.Tabs.SettingsTab;
import com.example.android.stepstracker.Tabs.TodayTab;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;



public class MainActivity extends AppCompatActivity implements TodayTab.OnFragmentInteractionListener,
        ReportsTab.OnFragmentInteractionListener,SettingsTab.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetSharedPreference();



        TabLayout tabLayout = (TabLayout)findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.today)).setIcon(R.drawable.today));//.setIcon(R.id));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reports)).setIcon(R.drawable.chart));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.settings)).setIcon(R.drawable.setting));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
        tabLayout.setSelectedTabIndicatorHeight(10);

        final ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));



        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setPageTransformer(true, new DepthPageTransformer());


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }




    @Override
    public  void onStop()
    {
        SharedPreferences sharedPrefs = getSharedPreferences(getResources().getString(R.string.stepstrackersharedpref),MODE_PRIVATE);
        SharedPreferences.Editor ed;
        ed = sharedPrefs.edit();
        ed.putBoolean(getResources().getString(R.string.isplaybuttononsharedpref), false);
        ed.commit();
        super.onStop();

    }


    @Override
    public  void onDestroy()
    {
        super.onDestroy();

    }



    private void SetSharedPreference()
    {
        SharedPreferences sharedPrefs = this.getSharedPreferences(getResources().getString(R.string.stepstrackersharedpref), this.MODE_PRIVATE);

        boolean hasBeenInitialised  = sharedPrefs.getBoolean(getResources().getString(R.string.isinitialisedsharedpref),false );

        if(!hasBeenInitialised)
        {
            SharedPreferences.Editor ed;
            ed = sharedPrefs.edit();
            ed.putInt(getResources().getString(R.string.date), Util.getDate(0));
            ed.putBoolean(getResources().getString(R.string.isinitialisedsharedpref), true);
            ed.putBoolean(getResources().getString(R.string.gendersharedpref), true);
            ed.putInt(getResources().getString(R.string.heightsharedpref), 172);
            ed.putInt(getResources().getString(R.string.weightsharedpref), 65);
            ed.putInt(getResources().getString(R.string.sensitivitysharedpref), 1);
            ed.putInt(getResources().getString(R.string.steplengthsharedpref), 75);
            ed.putInt(getResources().getString(R.string.pacesharedpref), 60);
            ed.putInt(getResources().getString(R.string.stepstodaysharedpref), 0);

            ed.commit();
        }





    }
    private  void fatalError(){
        throw new NullPointerException();
    }
}

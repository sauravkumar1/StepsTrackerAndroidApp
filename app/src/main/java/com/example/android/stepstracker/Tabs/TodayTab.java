package com.example.android.stepstracker.Tabs;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import com.example.android.stepstracker.Data.TaskContract;
import com.example.android.stepstracker.Data.TaskDbHelper;
import com.example.android.stepstracker.MainActivity;
import com.example.android.stepstracker.R;
import com.example.android.stepstracker.Sensor.StepDetector;
import com.example.android.stepstracker.Sensor.StepListener;
import com.example.android.stepstracker.Util;
import com.example.android.stepstracker.Widget.StepsTrackerUpdateService;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.LocalDate;
import java.util.Calendar;
import butterknife.BindView;
import butterknife.ButterKnife;

import  java.util.Date;
import java.util.List;
import java.util.Random;


import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;

public class TodayTab extends Fragment implements  SensorEventListener,StepListener  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  boolean isPlaybButtonOn = false;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private int numSteps;
    private  int steplength ;
    private  int pace ;
    private  int mWeight;
    private int mSpeed;
    private int mHeight;
    private MediaPlayer mActivityStarted;
    private MediaPlayer mActivityPaused;
    private  boolean mIsSoundEnabled;

    private SharedPreferences mSettingsPreference;

    private String mParam1;
    private String mParam2;
    private  View mRootView ;
    @BindView(R.id.tv_steps) TextView mTextViewStepsCount ;
    @BindView(R.id.tv_kcal) TextView mTVKcal;
    @BindView(R.id.tv_kilometers) TextView mKm;
    @BindView(R.id.tv_time)  TextView mMinutes;
     private ContentResolver mContentResolver;
    private OnFragmentInteractionListener mListener;

    public TodayTab() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Tab1.
     */
    // TODO: Rename and change types and number of parameters
    public static TodayTab newInstance(String param1, String param2) {
        TodayTab fragment = new TodayTab();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_today, container, false);
        ButterKnife.bind(this, mRootView);
        mActivityPaused= MediaPlayer.create(getContext(),R.raw.activitypaused);

        mActivityStarted= MediaPlayer.create(getContext(),R.raw.activitystarted);

        sensorManager = (SensorManager) mRootView.getContext().getSystemService(mRootView.getContext().SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        mSettingsPreference=   mRootView.getContext().getSharedPreferences(getResources().getString(R.string.stepstrackersharedpref),    mRootView.getContext().MODE_PRIVATE);
        mContentResolver = getContext().getContentResolver();
        Initialise();
        return  mRootView;
    }


   private  void  Initialise(){

       InitialiseSteps();


       mTextViewStepsCount.setText(String.valueOf(numSteps));

       isPlaybButtonOn = mSettingsPreference.getBoolean(getResources().getString(R.string.isplaybuttononsharedpref),false );
       steplength = mSettingsPreference.getInt(getResources().getString(R.string.steplengthsharedpref), 75);
       mWeight = mSettingsPreference.getInt(getResources().getString(R.string.weightsharedpref), 65);
       pace = mSettingsPreference.getInt(getResources().getString(R.string.pacesharedpref), 60);
       mHeight =mSettingsPreference.getInt(getResources().getString(R.string.heightsharedpref), 60);
       mIsSoundEnabled= mSettingsPreference.getBoolean(getResources().getString(R.string.issoundenabled),false );
       if(isPlaybButtonOn)
           RegisterListener();
       SetPlayPauseButton();
       UpdateTotalDistance();
       UpdateCaloriesBurnt();
       UpdateTotalTimeWalked();
       mSpeed = (int)(( (double)pace*(double) steplength)/100)*60;
    }


    private  void InitialiseSteps()
    {

        int date =   mSettingsPreference.getInt(getResources().getString(R.string.date), 0);
        int steps =   mSettingsPreference.getInt(getResources().getString(R.string.stepstodaysharedpref), 0);
        if(date == Util.getDate(0))
                  numSteps = steps;
        else {

            SharedPreferences.Editor ed;
            ed = mSettingsPreference.edit();
            ed.putInt(getResources().getString(R.string.date),date);
            ed.putInt(getResources().getString(R.string.stepstodaysharedpref),steps);
            ed.commit();

            int todayDate = Util.getDate(0);

            ContentValues contentValues = new ContentValues();
            // Put the task description and selected mPriority into the ContentValues
            contentValues.put(TaskContract.TaskEntry.COLUMN_DATE, date);
            contentValues.put(TaskContract.TaskEntry.COLUMN_STEPS, steps);

            //first Delete any data present for today
            Uri uri= TaskContract.TaskEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(todayDate )).build();
            mContentResolver.delete(uri, null, null);

            mContentResolver.insert(uri, contentValues);

            numSteps =0;
        }


    }





    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {

        SharedPreferences.Editor ed;
        ed = mSettingsPreference.edit();
        ed.putBoolean(getResources().getString(R.string.isplaybuttononsharedpref), isPlaybButtonOn);
        ed.putInt(getResources().getString(R.string.date), Util.getDate(0));
        ed.putInt(getResources().getString(R.string.stepstodaysharedpref), numSteps);
        ed.commit();
        super.onPause();

    }


    @Override
    public void onResume() {

        Initialise();

        super.onResume();

    }

    @Override
    public  void onDestroy()
    {   SharedPreferences.Editor ed;
        ed = mSettingsPreference.edit();
        ed.putBoolean(getResources().getString(R.string.isplaybuttononsharedpref), isPlaybButtonOn);
        ed.putInt(getResources().getString(R.string.date), Util.getDate(0));
        ed.putInt(getResources().getString(R.string.stepstodaysharedpref), numSteps);
        ed.commit();
        DeleteExistingStepsThenInsert();
        super.onDestroy();
        UnRegisterListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    private  void DeleteExistingStepsThenInsert()
    {




        int todayDate = Util.getDate(0);

        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(TaskContract.TaskEntry.COLUMN_DATE, todayDate);
        contentValues.put(TaskContract.TaskEntry.COLUMN_STEPS, numSteps);


        Uri uri = TaskContract.TaskEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(todayDate )).build();
         mContentResolver.delete(uri, null, null);

        mContentResolver.insert(uri, contentValues);

    }


    private  void UpdateTotalDistance()
    {

       double distance = ((double)numSteps* (double)steplength)/100/1000;
       distance = Math.round(distance*100.0)/100.0;
        mKm.setText(String.valueOf(distance));

    }
    private  void UpdateCaloriesBurnt()
    {

        int calories = (int) (0.035 * (double)mWeight + (((double)( mSpeed * mSpeed )/(double)mHeight)/1000) *0.029 * (double)mWeight);

        mTVKcal.setText(String.valueOf(calories));

    }
    private  void UpdateTotalTimeWalked()
    {
        int time  = (int)((numSteps+1)/pace);
        mMinutes.setText(String.valueOf(time));

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {
        numSteps++;

        int date =  Util.getDate(0);

        SharedPreferences.Editor ed;
        ed = mSettingsPreference.edit();
        ed.putInt(getResources().getString(R.string.date), date);
        ed.putInt(getResources().getString(R.string.stepstodaysharedpref), numSteps);
        ed.commit();

        Context thisContext = mRootView.getContext();

        mTextViewStepsCount.setText(String.valueOf(numSteps));
        if(thisContext != null)
            StepsTrackerUpdateService.startStepsService(thisContext,String.valueOf(numSteps));

        UpdateTotalDistance();
        UpdateCaloriesBurnt();
        UpdateTotalTimeWalked();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private  void RegisterListener()
    {
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);

    }



    private  void UnRegisterListener()
    {
        sensorManager.unregisterListener(this);

    }



    private  void SetPlayPauseButton() {

        PlayPauseButton playPauseButton  = (PlayPauseButton) mRootView.findViewById(R.id.main_play_pause_button);
        playPauseButton.setColor((getResources().getColor(R.color.colorAccent)));// Color.parseColor("#000000"));

        playPauseButton.setOnControlStatusChangeListener(new PlayPauseButton.OnControlStatusChangeListener() {
            @Override public void onStatusChange(View view, boolean state) {
                if(state) {

                    RegisterListener();
                    Toast.makeText(getActivity(), getResources().getString(R.string.activitysatarted),
                            Toast.LENGTH_SHORT).show();

                                 if(mIsSoundEnabled)
                                 mActivityStarted.start();

                    isPlaybButtonOn= true;
                } else {
                    UnRegisterListener();
                    Toast.makeText(getActivity(), getResources().getString(R.string.activitypaused),
                            Toast.LENGTH_SHORT).show();
                    if(mIsSoundEnabled)
                    mActivityPaused.start();
                    isPlaybButtonOn= false;
                }
            }
        });
    }




}

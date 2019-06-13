package com.example.android.stepstracker.Tabs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stepstracker.Data.TaskContract;
import com.example.android.stepstracker.Data.TaskDbHelper;
import com.example.android.stepstracker.R;
import com.example.android.stepstracker.Sensor.StepDetector;
import com.example.android.stepstracker.Sensor.StepListener;
import com.example.android.stepstracker.Util;
import com.example.android.stepstracker.Widget.StepsTrackerUpdateService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsTab extends Fragment implements
  SensorEventListener, StepListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private OnFragmentInteractionListener mListener;
    private View mRootView;
    @BindView(R.id.tv_weight) TextView mTextViewWeight;
    @BindView(R.id.tv_height) TextView mTextViewHeight;
    @BindView(R.id.tv_stepLength) TextView mTextViewStepLength;
    @BindView(R.id.tv_pace) TextView mTextViewPace;
    @BindView(R.id.radio_male) RadioButton mMale;
    @BindView(R.id.radio_female) RadioButton mFemale;
    private boolean mIsPlayButtonOn;
    @BindView(R.id.iv_soundcontrol) ImageView mSoundImageView;
    private SharedPreferences mSettingsPreference;
    private  boolean mIsSoundEnabled;
    private int numSteps;
    OnSensitivityChangedListenver  mCallback;
    private ContentResolver mContentResolver;

    public interface OnSensitivityChangedListenver {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onSensitivityChanged(int sensitivity);
    }


    public SettingsTab() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Tab3.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsTab newInstance(String param1, String param2) {
        SettingsTab fragment = new SettingsTab();
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
        // Inflate the layout for this fragment
          mRootView=  inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, mRootView);
        mSettingsPreference=   mRootView.getContext().getSharedPreferences(getResources().getString(R.string.stepstrackersharedpref),    mRootView.getContext().MODE_PRIVATE);

        mContentResolver = getContext().getContentResolver();
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        SetSteps();
        SetGender();
        SetHeightTextView();
        SetWeightTextView();
        SetSensitivitySpinner();
        SetStepLength();
        SetPace();
        SetSound();

        return  mRootView;
    }



    private  void InitialiseSteps()
    {
        numSteps = mSettingsPreference.getInt(getResources().getString(R.string.stepstodaysharedpref), 0);

    }


    private  void SetSound()
    {
        final SharedPreferences sharedPrefs = mRootView.getContext().getSharedPreferences(getResources().getString(R.string.stepstrackersharedpref), mRootView.getContext().MODE_PRIVATE);
        mIsSoundEnabled= sharedPrefs.getBoolean(getResources().getString(R.string.issoundenabled),false );
        UpdateSoundImageIcon();

        mSoundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor ed;
                ed = sharedPrefs.edit();
                mIsSoundEnabled = !mIsSoundEnabled;
                ed.putBoolean(getResources().getString(R.string.issoundenabled), mIsSoundEnabled);
                ed.commit();
                UpdateSoundImageIcon();
            }
        });

    }

    private  void UpdateSoundImageIcon()
    {
        if(mIsSoundEnabled)
            mSoundImageView.setBackgroundResource(R.drawable.sound);
        else
            mSoundImageView.setBackgroundResource(R.drawable.mute);

    }
    private void SetSteps() {
        SharedPreferences sharedPrefs = mRootView.getContext().getSharedPreferences(getResources().getString(R.string.stepstrackersharedpref), mRootView.getContext().MODE_PRIVATE);
        mIsPlayButtonOn= sharedPrefs.getBoolean(getResources().getString(R.string.isplaybuttononsharedpref),false );
        if(mIsPlayButtonOn) {

                InitialiseSteps();

           sensorManager = (SensorManager) mRootView.getContext().getSystemService(mRootView.getContext().SENSOR_SERVICE);
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            //  simpleStepDetector.
            simpleStepDetector.registerListener(this);

            RegisterListener();
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
    public void onStop() {
        super.onStop();
        if(mIsPlayButtonOn) {
            DeleteExistingStepsThenInsert();
        }

        UnRegisterListener();
        mListener = null;
    }

    @Override
    public  void onDestroy()
    {

        super.onDestroy();

    }




    private  void DeleteExistingStepsThenInsert()
    {

        SharedPreferences.Editor ed;
        ed = mSettingsPreference.edit();
        ed.putInt(getResources().getString(R.string.date), Util.getDate(0));
        ed.putInt(getResources().getString(R.string.stepstodaysharedpref),numSteps);
        ed.commit();

        int todayDate = Util.getDate(0);

        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(TaskContract.TaskEntry.COLUMN_DATE, todayDate);
        contentValues.put(TaskContract.TaskEntry.COLUMN_STEPS, numSteps);

        //first Delete any data present for today
        Uri uri= TaskContract.TaskEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(todayDate )).build();
         mContentResolver.delete(uri, null, null);

        mContentResolver.insert(uri, contentValues);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        int date =   Util.getDate(0);
        SharedPreferences.Editor ed;
        ed = mSettingsPreference.edit();
        ed.putInt(getResources().getString(R.string.date), date);
        ed.putInt(getResources().getString(R.string.stepstodaysharedpref), numSteps);
        ed.commit();


        Context thisContext = mRootView.getContext();
        if(thisContext != null)
            StepsTrackerUpdateService.startStepsService(thisContext,String.valueOf(numSteps));
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


private void SetGender() {
    boolean isMale = mSettingsPreference.getBoolean(getResources().getString(R.string.gendersharedpref), true);
    if(isMale)
    mMale.setChecked(true);
    else
    mFemale.setChecked(true);

    GenderChangeHandler();

}

private void GenderChangeHandler()
{
    RadioGroup rb = (RadioGroup) mRootView.findViewById(R.id.rg_gender);

    rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {


            switch (checkedId) {
                case R.id.radio_male:
                {
                    boolean checked = ((RadioButton)mMale).isChecked();
                    if(checked)
                    {
                        SharedPreferences.Editor ed;
                        ed = mSettingsPreference.edit();

                        ed.putBoolean(getResources().getString(R.string.gender), true);
                        ed.commit();
                    }
                }
                // Pirates are the best
                break;
                case R.id.radio_female:
                {
                    boolean checked = ((RadioButton)mFemale).isChecked();
                    if(checked)
                    {
                        SharedPreferences.Editor ed;
                        ed = mSettingsPreference.edit();

                        ed.putBoolean(getResources().getString(R.string.gender), false);
                        ed.commit();
                    }
                }
                break;

            }
        }

    });
}

    private  void SetWeightTextView() {

        mTextViewWeight.setText( String.valueOf(mSettingsPreference.getInt(getResources().getString(R.string.weightsharedpref),65)));

        mTextViewWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeightChangeHandler();
            }
        });
    }

    private void WeightChangeHandler()
    {

        Integer initialWeight =  mSettingsPreference.getInt(getResources().getString(R.string.weightsharedpref),45);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        final NumberPicker np = new NumberPicker(getActivity());
        np.setMinValue(45);
        np.setMaxValue(130);
        np.setValue(initialWeight);
        builder.setView(np);
        builder.setTitle(getResources().getString(R.string.editweight));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                np.clearFocus();
                Integer finalWeight = np.getValue();

                SharedPreferences.Editor ed;
                ed = mSettingsPreference.edit();

                ed.putInt(getResources().getString(R.string.weightsharedpref), finalWeight);
                ed.commit();
                mTextViewWeight.setText(Integer.toString(finalWeight));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();


    }

    @Override
    public void onResume() {

        SetSteps() ;

        super.onResume();

    }

    @Override
    public void onPause() {

        SharedPreferences.Editor ed;
        ed = mSettingsPreference.edit();
        ed.putInt(getResources().getString(R.string.date), Util.getDate(0));
        ed.putInt(getResources().getString(R.string.stepstodaysharedpref), numSteps);
        ed.putBoolean(getResources().getString(R.string.sound), mIsSoundEnabled);
        ed.commit();
        super.onPause();

    }

    private  void SetHeightTextView() {

        mTextViewHeight.setText( String.valueOf(mSettingsPreference.getInt(getResources().getString(R.string.heightsharedpref),172)));


        mTextViewHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HeightChangeHandler();

            }
        });

    }


    private void HeightChangeHandler()
    {

        Integer initialAge = mSettingsPreference.getInt(getResources().getString(R.string.heightsharedpref),172);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        final NumberPicker np = new NumberPicker(getActivity());
        np.setMinValue(100);
        np.setMaxValue(300);
        np.setValue(initialAge);
        builder.setView(np);
        builder.setTitle(getResources().getString(R.string.editheight));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                np.clearFocus();
                Integer height = np.getValue();

                SharedPreferences.Editor ed;
                ed = mSettingsPreference.edit();

                ed.putInt(getResources().getString(R.string.heightsharedpref), height);
                ed.commit();

                mTextViewHeight.setText(Integer.toString(height));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();

    }


    private  void SetSensitivitySpinner() {
        List<String> arraySpinner = new ArrayList<String>();
        String[] myStrings = getResources().getStringArray(R.array.sensitivityNames);
        for(int i=0;i<myStrings.length;i++)
        {
            arraySpinner.add(myStrings[i]);
        }

        Spinner spin = (Spinner) mRootView.findViewById(R.id.sp_sensitivity);

        int sensitivityIndex = mSettingsPreference.getInt(getResources().getString(R.string.sensitivitysharedpref),2);

        ArrayAdapter<String> aa = new ArrayAdapter(this.getContext(), R.layout.spinner_text, arraySpinner );
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(aa);
       spin.setSelection(sensitivityIndex,true);


        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                    {
                         UpdateSensitivity(0);
                    }
                     break;
                    case 1:
                        UpdateSensitivity(1);
                        break;
                    case 2:
                        UpdateSensitivity(2);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });
    }


    private void UpdateSensitivity(int index)
    {

        String[] myStrings = getResources().getStringArray(R.array.sensitivityValues);
         int SensitivityValue = Integer.parseInt(myStrings[index]);

        SharedPreferences.Editor ed;
        ed = mSettingsPreference.edit();
        ed.putInt(getResources().getString(R.string.sensitivitysharedpref), index);
        ed.commit();
        simpleStepDetector.onSensitivityChanged(SensitivityValue);

    }

    private  void SetStepLength() {

        mTextViewStepLength.setText( String.valueOf(mSettingsPreference.getInt(getResources().getString(R.string.steplengthsharedpref),75)));

        mTextViewStepLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer initialAge = mSettingsPreference.getInt(getResources().getString(R.string.steplengthsharedpref),25);

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(getActivity());
                final NumberPicker np = new NumberPicker(getActivity());
                np.setMinValue(30);
                np.setMaxValue(150);
                np.setValue(initialAge);
                builder.setView(np);
                builder.setTitle(getResources().getString(R.string.editsteplength));

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        np.clearFocus();
                        Integer finalStepLength = np.getValue();
                        mTextViewStepLength.setText(Integer.toString(finalStepLength));
                        SharedPreferences.Editor ed;
                        ed = mSettingsPreference.edit();
                        ed.putInt(getResources().getString(R.string.steplengthsharedpref), finalStepLength);
                        ed.commit();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();


            }
        });
    }

    private  void SetPace() {
        mTextViewPace.setText( String.valueOf(mSettingsPreference.getInt(getResources().getString(R.string.pacesharedpref),60)));


        mTextViewPace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer len = mTextViewPace.getText().length();
                String stepLength = mTextViewPace.getText().toString();
                Integer initialAge= Integer.parseInt(stepLength);

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(getActivity());
                final NumberPicker np = new NumberPicker(getActivity());
                np.setMinValue(40);
                np.setMaxValue(300);
                np.setValue(initialAge);
                builder.setView(np);
                builder.setTitle(getResources().getString(R.string.editpace));

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        np.clearFocus();
                        Integer pace = np.getValue();
                        mTextViewPace.setText(Integer.toString(pace));
                        SharedPreferences.Editor ed;
                        ed = mSettingsPreference.edit();
                        ed.putInt(getResources().getString(R.string.pacesharedpref), pace);
                        ed.commit();

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();


            }
        });
    }

    private  void RegisterListener()
    {
        if(sensorManager!=null)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }



    private  void UnRegisterListener()
    {
        if(sensorManager!=null)
        sensorManager.unregisterListener(this);
    }




}
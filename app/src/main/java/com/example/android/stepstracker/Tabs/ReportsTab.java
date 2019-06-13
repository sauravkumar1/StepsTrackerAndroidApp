package com.example.android.stepstracker.Tabs;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.InstrumentationInfo;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stepstracker.Data.TaskContract;
import com.example.android.stepstracker.R;
import com.example.android.stepstracker.Util;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

public class ReportsTab extends Fragment
     implements LoaderCallbacks<Cursor> {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.chart_month)
    BarChart mChartMonth;
    @BindView(R.id.chart_week)
    BarChart mChartWeek;
    private View mRootView;

    private String mParam1;
    private String mParam2;
    private String[] mWeekDays;
    private String[] mMonths;
    private int mThisMonthIndex;
    private int mWeekTodayIndex;
    private int mMonthTodayDateIndex;
    @BindView(R.id.tv_week)
    TextView mWeekTextView;
    @BindView(R.id.tv_month)
    TextView mMonthTextView;
    private int mStepsToday;
    private SharedPreferences mSharedPrefs;
    @BindView(R.id.tv_noStepsWeek)
    TextView mNoStepsWeek;
    @BindView(R.id.tv_noStepsMonth)
    TextView mNoStepsMonth;
    @BindView(R.id.iv_next_month)
    ImageView mNextMonthIv;
    @BindView(R.id.iv_previous_month)
    ImageView mPreviousMonthIv;
    @BindView(R.id.iv_next_week)
    ImageView mNextWeekIv;
    @BindView(R.id.iv_previous_week)
    ImageView mPreviousWeekIv;
    private int mMonthDelta = 0;
    private int mWeekDelta = 0;
    private int mFirstWeekDelta = 0;
    private int mFirstMonthDelta = 0;
    @BindView(R.id.pb_week)
    ProgressBar weekPb;
    @BindView(R.id.pb_month)
    ProgressBar monthPb;
    private MediaPlayer mNoDataWeek;

    private MediaPlayer mNoDataMonth;

    private MediaPlayer mJourneyStartedMonth;

    private MediaPlayer mJourneyStartedWeek;

    private MediaPlayer mFutureData;
    private OnFragmentInteractionListener mListener;

    private final String WEEK = "week";
    private final String MONTH = "month";
    private static final String[] PROJECTION = {TaskContract.TaskEntry.COLUMN_DATE, TaskContract.TaskEntry.COLUMN_STEPS};
    private static final String SELECTION = TaskContract.TaskEntry.COLUMN_DATE;
    /**
     * Sort order for the query. Sorted by primary name in ascending order.
     */
    private static final String ORDER = TaskContract.TaskEntry.COLUMN_DATE + " ASC";
    private static final int WEEK_LOADER_ID = 1;
    private static final int MONTH_LOADER_ID = 2;

    private ContentResolver mContentResolver;

    public ReportsTab() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Tab2.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportsTab newInstance(String param1, String param2) {
        ReportsTab fragment = new ReportsTab();
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
        View rootView = inflater.inflate(R.layout.fragment_reports, container, false);
        mRootView = rootView;

        mContentResolver = getContext().getContentResolver();
        ButterKnife.bind(this, rootView);
        mWeekDelta = 0;
        mMonthDelta = 0;
        mWeekTodayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        mMonthTodayDateIndex = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        mWeekDays = getResources().getStringArray(R.array.weekdays);
        mMonths = getResources().getStringArray(R.array.months);
        mThisMonthIndex = Calendar.getInstance().get(Calendar.MONTH) + 1;

        mNoDataWeek = MediaPlayer.create(getContext(), R.raw.nostepsweek);

        mNoDataMonth = MediaPlayer.create(getContext(), R.raw.nostepsmonth);

        mJourneyStartedMonth = MediaPlayer.create(getContext(), R.raw.journeystartedmonth);
        mJourneyStartedWeek = MediaPlayer.create(getContext(), R.raw.journeystartedweek);

        mFutureData = MediaPlayer.create(getContext(), R.raw.future);

        mSharedPrefs = mRootView.getContext().getSharedPreferences(getResources().getString(R.string.stepstrackersharedpref), mRootView.getContext().MODE_PRIVATE);
        mStepsToday = mSharedPrefs.getInt(getResources().getString(R.string.stepstodaysharedpref), 0);
        SetDeltas();
        SetNextMonth();
        SetPreviousMonth();
        SetPreviousWeek();
        SetNextWeek();


        getLoaderManager().initLoader(WEEK_LOADER_ID, null, this);
        getLoaderManager().initLoader(MONTH_LOADER_ID, null, this);

        return rootView;
    }

    private void SetDeltas() {

        int firstDate = 0;
        Uri uriToSelect = TaskContract.TaskEntry.CONTENT_URI;
        Cursor cursor = mContentResolver.query(uriToSelect, new String[]{"MIN(date) AS date"}, null,
                null, null);
        if (null != cursor) {
            cursor.moveToFirst();
            firstDate = cursor.getInt(0);
            cursor.close();
        }

        if (firstDate == 0)
            firstDate = Util.getDate(0);
        //  db.close();


        int todayDate = Util.getDate(0);


        for (int i = 0; i < 480; i++) {
            int sundayIndex = 7 * i + mWeekTodayIndex - 1;
            int sundayDate = Util.getDate(-sundayIndex);
            if (sundayDate <= firstDate) {
                mFirstWeekDelta = i;
                break;
            }
        }


        int firstDateYear = Integer.parseInt(Integer.toString(firstDate).substring(0, 4)); //(int) firstDate / 10000;
        int todayDateYear = Integer.parseInt(Integer.toString(todayDate).substring(0, 4)); //(int) todayDate / 10000;

        int firstMonthIndex = Integer.parseInt(Integer.toString(firstDate).substring(4, 6));  // firstMonthandYear - firstDateYear * 100;
        if (firstDateYear != todayDateYear) {
            mFirstMonthDelta += (todayDateYear - firstDateYear - 1) * 12;
            mFirstMonthDelta += mThisMonthIndex - 1;
            mFirstMonthDelta += 12 - firstMonthIndex;

        } else {

            mFirstMonthDelta += mThisMonthIndex - firstMonthIndex;
        }

    }

    private String[] GenerateWeekDatesToFetchDatafor() {
        String[] datesArray = new String[7];

        int startDelta = mWeekDelta * 7 + mWeekTodayIndex - 1;

        for (int i = 1; i < 8; i++) {
            int date = Util.getDate(-(startDelta - i + 1));

            datesArray[i - 1] = String.valueOf(date);
        }


        return datesArray;
    }

    private String[] GenerateMonthDatesToFetchDatafor() {
        int startDate = 0;
        int endDate = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        if (mMonthDelta > 0)
            cal.add(Calendar.MONTH, -mMonthDelta);
        int numDays = cal.getActualMaximum(Calendar.DATE);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        startDate = year * 10000 + month * 100 + 1;
        endDate = year * 10000 + month * 100 + numDays;
        int days = endDate - startDate + 1;

        String[] dates = new String[days];
        int index = 0;
        for (int i = startDate; i <= endDate; i++) {
            dates[index++] = String.valueOf(i);
        }

        return dates;
    }

    private ArrayList<Pair<Integer, Integer>> FetchWeekDataFromDb() {
        ArrayList<Pair<Integer, Integer>> weeksData = new ArrayList<>();
        int startDelta = mWeekDelta * 7 + mWeekTodayIndex - 1;

        for (int i = 1; i < 8; i++) {
            int date = Util.getDate(-(startDelta - i + 1));
            int steps = 0;

            Uri uriToSelect = TaskContract.TaskEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(date)).build();
            steps = 0;

            Cursor cursor = mContentResolver.query(uriToSelect, null, null,
                    null, TaskContract.TaskEntry.COLUMN_STEPS);//  .query(uriToSelect, null, null);
            if (null != cursor) {
                int index = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_STEPS);
                if (cursor.moveToFirst())
                    steps = cursor.getInt(index);
                cursor.close();
            }


            weeksData.add(new Pair<>(date, steps));
        }

        return weeksData;
    }

    private ArrayList<Pair<Integer, Integer>> FetchMonthDataFromDb() {
        ArrayList<Pair<Integer, Integer>> monthData = new ArrayList<Pair<Integer, Integer>>();


        int startDate = 0;
        int endDate = 0;

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        if (mMonthDelta > 0)
            cal.add(Calendar.MONTH, -mMonthDelta);
        int numDays = cal.getActualMaximum(Calendar.DATE);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;


        startDate = year * 10000 + month * 100 + 1;

        endDate = year * 10000 + month * 100 + numDays;

        int todayDate = Util.getDate(0);
        for (int i = startDate; i <= endDate; i++) {

            int steps = 0;
            if (i == todayDate) {
                steps = mStepsToday;
            } else {//

                Uri uriToSelect = TaskContract.TaskEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(i)).build();
                steps = 0;

                Cursor cursor = mContentResolver.query(uriToSelect, null, null,
                        null, TaskContract.TaskEntry.COLUMN_STEPS);//  .query(uriToSelect, null, null);
                if (null != cursor) {
                    int index = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_STEPS);
                    if (cursor.moveToFirst())
                        steps = cursor.getInt(index);
                    cursor.close();
                }

            }
            monthData.add(new Pair<Integer, Integer>(i, steps));
        }

        return monthData;
    }


    private String Generateplaceholders(int count) {
        String result = " in (";

        for (int i = 0; i < count; i++) {
            if (i < count - 1)
                result += "?,";
            else
                result += "?";

        }

        result += ")";
        return result;

    }


    private void SetWeekData(ArrayList<Pair<Integer, Integer>> weekData) {

        boolean noDatafound = true;
        LinkedHashMap<Integer, Integer> dictionary = new LinkedHashMap<Integer, Integer>();

        String[] dates = GenerateWeekDatesToFetchDatafor();

        for (int i = 0; i < dates.length; i++) {
            Integer date = Integer.valueOf(dates[i]);
            dictionary.put(date, 0);

        }
        int today = Util.getDate(0);
        for (int i = 0; i < weekData.size(); i++) {

            int date = weekData.get(i).first;
            int steps = weekData.get(i).second;
            if (mWeekDelta == 0) {

                if (date == today)
                    steps = mSharedPrefs.getInt(getResources().getString(R.string.stepstodaysharedpref), steps);
            }
            if (steps > 0) noDatafound = false;

            if (dictionary.containsKey(date)) {
                dictionary.put(date, steps);
            }


        }


        mChartWeek.getDescription().setEnabled(false);
        mChartWeek.setMaxVisibleValueCount(60);
        mChartWeek.setPinchZoom(false);
        mChartWeek.setDrawBarShadow(false);
        mChartWeek.setDrawGridBackground(false);
        XAxis xAxis = mChartWeek.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        mChartWeek.getAxisLeft().setDrawGridLines(true);


        mChartWeek.animateY(800);
        mChartWeek.setDrawBarShadow(false);
        mChartWeek.setDrawValueAboveBar(true);
        mChartWeek.getLegend().setEnabled(false);

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < dates.length; i++) {
            int date = Integer.valueOf(dates[i]);
            if (dictionary.containsKey(date)) {
                int steps = dictionary.get(date);
                entries.add(new BarEntry(i, steps));
            } else
                entries.add(new BarEntry(i, 0));
        }

        String heading = "";

        String startDate = dates[0];
        String endDate = dates[6];
        int startDateMonthIndex = Integer.parseInt(startDate.substring(4, 6));
        int endDateMonthIndex = Integer.parseInt(endDate.substring(4, 6));


        if (startDate + 6 != endDate) {
            heading = startDate.substring(6) + " " + mMonths[startDateMonthIndex - 1].substring(0, 3) + " - " + endDate.substring(6) + " " + mMonths[endDateMonthIndex - 1].substring(0, 3);
        } else {
            heading = startDate.substring(6) + " - " + endDate.substring(6) + " " + mMonths[endDateMonthIndex - 1].substring(0, 3);
        }
        mWeekTextView.setText(heading);


        final String[] ds = new String[7];

        for (int i = 0; i < 7; i++) {
            ds[i] = mWeekDays[i];
        }


        if (mWeekDelta == 0)
            ds[mWeekTodayIndex - 1] = getResources().getString(R.string.today);


        if (noDatafound) {
            mNoStepsWeek.setVisibility(View.VISIBLE);
            mChartWeek.setVisibility(View.INVISIBLE);
            boolean IsSoundEnabled = mSharedPrefs.getBoolean(getResources().getString(R.string.issoundenabled), true);
            if (IsSoundEnabled) mNoDataWeek.start();

        } else {
            mChartWeek.setVisibility(View.VISIBLE);
            mNoStepsWeek.setVisibility(View.INVISIBLE);
        }

        mChartWeek.getXAxis().setValueFormatter(new IndexAxisValueFormatter(ds));
        mChartWeek.getXAxis().setValueFormatter(new LabelFormatter(ds));
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(getResources().getColor(R.color.colorPrimary));
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(10f);
        data.setBarWidth(0.8f);
        dataSet.setDrawValues(true);

        data.setHighlightEnabled(true);

        mChartWeek.setTouchEnabled(false);
        mChartWeek.setData(data);
        mChartWeek.getLegend().setEnabled(false);

    }


    private void SetMonthData(ArrayList<Pair<Integer, Integer>> monthData) {

        boolean noDatafound = true;
        LinkedHashMap<Integer, Integer> dictionary = new LinkedHashMap<Integer, Integer>();

        String[] dates = GenerateMonthDatesToFetchDatafor();

        for (int i = 0; i < dates.length; i++) {
            Integer date = Integer.valueOf(dates[i]);
            dictionary.put(date, 0);

        }
        int today = Util.getDate(0);
        for (int i = 0; i < monthData.size(); i++) {

            int date = monthData.get(i).first;
            int steps = monthData.get(i).second;
            if (mMonthDelta == 0) {

                if (date == today)
                    steps = mSharedPrefs.getInt(getResources().getString(R.string.stepstodaysharedpref), steps);
            }
            if (steps > 0) noDatafound = false;

            if (dictionary.containsKey(date)) {
                dictionary.put(date, steps);
            }


        }

        mChartMonth.getDescription().setEnabled(false);

        mChartMonth.setMaxVisibleValueCount(60);
        mChartMonth.setPinchZoom(false);

        mChartMonth.setDrawBarShadow(false);
        mChartMonth.setDrawGridBackground(false);

        XAxis xAxis = mChartMonth.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        mChartMonth.getAxisLeft().setDrawGridLines(false);
        mChartMonth.animateY(800);

        mChartMonth.getLegend().setEnabled(false);

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < dates.length; i++) {
            int date = Integer.valueOf(dates[i]);
            if (dictionary.containsKey(date)) {
                int steps = dictionary.get(date);
                entries.add(new BarEntry(i, steps));
            } else
                entries.add(new BarEntry(i, 0));
        }

        String heading = "";

        int numofDays = dates.length;
        String startDate = dates[0];
        String endDate = dates[numofDays - 1];


        int startDateMonthIndex = Integer.parseInt(startDate.substring(4, 6));


        heading = mMonths[startDateMonthIndex - 1];

        mMonthTextView.setText(heading);


        final String[] ds = new String[numofDays + 1];
        ds[0] = "extra";

        for (int i = 1; i <= numofDays; i++) {
            ds[i] = Integer.toString(i);
        }


        if (noDatafound) {
            mNoStepsMonth.setVisibility(View.VISIBLE);
            mChartMonth.setVisibility(View.INVISIBLE);
            boolean IsSoundEnabled = mSharedPrefs.getBoolean(getResources().getString(R.string.issoundenabled), true);
            if (IsSoundEnabled) mNoDataMonth.start();

        } else {

            mChartMonth.setVisibility(View.VISIBLE);
            mNoStepsMonth.setVisibility(View.INVISIBLE);
        }

        mChartMonth.getXAxis().setValueFormatter(new IndexAxisValueFormatter(ds));
        mChartMonth.getXAxis().setValueFormatter(new LabelFormatter(ds));
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(getResources().getColor(R.color.colorPrimary));
        dataSet.getValueTextColor(R.color.colorWhite);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);
        dataSet.setDrawValues(false);
        mChartMonth.setTouchEnabled(false);
        mChartMonth.setData(data);
        mChartMonth.getLegend().setEnabled(false);
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
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        switch (loaderId) {
            case WEEK_LOADER_ID:
                String selectionWeek = SELECTION + Generateplaceholders(7);
                String[] datesWeek = GenerateWeekDatesToFetchDatafor();
                return new CursorLoader(getActivity(), TaskContract.TaskEntry.CONTENT_URI, PROJECTION,
                        selectionWeek, datesWeek, ORDER);
            case MONTH_LOADER_ID:

                String[] datesMonth = GenerateMonthDatesToFetchDatafor();
                int count = datesMonth.length;
                String selectionMonth = SELECTION + Generateplaceholders(count);
                return new CursorLoader(getActivity(), TaskContract.TaskEntry.CONTENT_URI, PROJECTION,
                        selectionMonth, datesMonth, ORDER);

        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        ArrayList<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>();


        if (null != cursor) {
            while (cursor.moveToNext()) {
                int date = cursor.getInt(0);
                int steps = cursor.getInt(1);
                result.add(new Pair<>(date, steps));
            }
            cursor.close();
        }


        switch (loaderId) {
            case WEEK_LOADER_ID:
                SetWeekData(result);
                weekPb.setVisibility(View.INVISIBLE);
                break;
            case MONTH_LOADER_ID:
                SetMonthData(result);
                monthPb.setVisibility(View.INVISIBLE);
                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class LabelFormatter implements IAxisValueFormatter {
        private final String[] mLabels;

        public LabelFormatter(String[] labels) {
            mLabels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if (value < mLabels.length)
                return mLabels[(int) value];

            return "";

        }
    }

    private void RestartLoader(int loaderId) {
        getLoaderManager().restartLoader(loaderId, null, this);

    }

    private void SetNextMonth() {


        mNextMonthIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMonthDelta == 0) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.futuremessage),
                            Toast.LENGTH_SHORT);


                    boolean IsSoundEnabled = mSharedPrefs.getBoolean(getResources().getString(R.string.issoundenabled), true);

                    if (IsSoundEnabled)
                        mFutureData.start();
                    mNextMonthIv.setVisibility(View.INVISIBLE);

                } else {
                    mMonthDelta--;
                    //    SetMonthData(FetchMonthDataFromDb());
                    monthPb.setVisibility(View.VISIBLE);
                    RestartLoader(MONTH_LOADER_ID);
                    //   new AsyncDataLoad().execute(MONTH);

                    mPreviousMonthIv.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void SetPreviousMonth() {

        mPreviousMonthIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFirstMonthDelta > mMonthDelta) {
                    mMonthDelta++;
                    monthPb.setVisibility(View.VISIBLE);
                    RestartLoader(MONTH_LOADER_ID);
                    mNextMonthIv.setVisibility(View.VISIBLE);
                } else if (mFirstMonthDelta == mMonthDelta) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.firstmonth),
                            Toast.LENGTH_SHORT).show();

                    boolean IsSoundEnabled = mSharedPrefs.getBoolean(getResources().getString(R.string.issoundenabled), true);
                    if (IsSoundEnabled)
                        mJourneyStartedMonth.start();

                    mPreviousMonthIv.setVisibility(View.INVISIBLE);
                }


            }
        });


    }

    private void SetPreviousWeek() {
        mPreviousWeekIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFirstWeekDelta > mWeekDelta) {
                    mWeekDelta++;
                    weekPb.setVisibility(View.VISIBLE);
                    RestartLoader(WEEK_LOADER_ID);
                    mNextWeekIv.setVisibility(View.VISIBLE);
                } else if (mFirstWeekDelta == mWeekDelta) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.firstweek),
                            Toast.LENGTH_SHORT).show();
                    boolean IsSoundEnabled = mSharedPrefs.getBoolean(getResources().getString(R.string.issoundenabled), true);
                    if (IsSoundEnabled)
                        mJourneyStartedWeek.start();
                    mPreviousWeekIv.setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    private void SetNextWeek() {
        mNextWeekIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mWeekDelta == 0) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.futuremessage),
                            Toast.LENGTH_SHORT).show();
                    boolean IsSoundEnabled = mSharedPrefs.getBoolean(getResources().getString(R.string.issoundenabled), true);
                    if (IsSoundEnabled)
                        mFutureData.start();
                    mNextWeekIv.setVisibility(View.INVISIBLE);

                } else {
                    mWeekDelta--;

                    weekPb.setVisibility(View.VISIBLE);

                    RestartLoader(WEEK_LOADER_ID);
                    mPreviousWeekIv.setVisibility(View.VISIBLE);
                }

            }
        });
    }
}








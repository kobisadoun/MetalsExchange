package com.kobi.metalsexchange.app;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kobi.metalsexchange.app.data.MetalsContract;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.ArrayList;

public class TrendGraphFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,OnChartGestureListener, OnChartValueSelectedListener {

    private static final String LOG_TAG = TrendGraphFragment.class.getSimpleName();
    static final String TREND_GRAPH_URI = "URI";

    private static final int TREND_GRAPH_LOADER = 0;

    private static final String[] TREND_GRAPH_COLUMNS = {
            MetalsContract.MetalsRateEntry.TABLE_NAME + "." + MetalsContract.MetalsRateEntry._ID,
            MetalsContract.MetalsRateEntry.COLUMN_DATE,
            MetalsContract.MetalsRateEntry.COLUMN_METAL_ID,
            MetalsContract.MetalsRateEntry.COLUMN_ILS_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_USD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_GBP_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_EUR_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_CAD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_DKK_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_NOK_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_SEK_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_CHF_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_JOD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_EGP_RATE
    };

    // These indices are tied to TREND_GRAPH_COLUMNS.  If TREND_GRAPH_COLUMNS changes, these
    // must change.
    public static final int COL_RATE_ID = 0;
    public static final int COL_RATE_DATE = 1;
    public static final int COL_RATE_METAL_ID = 2;
    public static final int COL_RATE_NIS_RATE = 3;
    public static final int COL_RATE_USD_RATE = 4;
    public static final int COL_RATE_GBP_RATE = 5;
    public static final int COL_RATE_EUR_RATE = 6;
    public static final int COL_RATE_CAD_RATE = 7;
    public static final int COL_RATE_DKK_RATE = 8;
    public static final int COL_RATE_NOK_RATE = 9;
    public static final int COL_RATE_SEK_RATE = 10;
    public static final int COL_RATE_CHF_RATE = 11;
    public static final int COL_RATE_JOD_RATE = 12;
    public static final int COL_RATE_EGP_RATE = 13;

    //private ImageView mIconView;
    private LineChart mChart;
    private long mHighlightedDate;

    public TrendGraphFragment() {
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            Uri uri = arguments.getParcelable(TrendGraphFragment.TREND_GRAPH_URI);
            mHighlightedDate = MetalsContract.MetalsRateEntry.getDateFromUri(uri);
        }

        View rootView = inflater.inflate(R.layout.fragment_trend_graph, container, false);
        // mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mChart = (LineChart) rootView.findViewById(R.id.trend_chart);

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        //mChart.setDescription("test");
        mChart.setNoDataTextDescription("");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TREND_GRAPH_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        //need to check why we have to call this
        getLoaderManager().restartLoader(TREND_GRAPH_LOADER, null, this);
    }

    void onCurrencyOrWeightChanged() {
        getLoaderManager().restartLoader(TREND_GRAPH_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mHighlightedDate >=0 ) {
            String sortOrder = MetalsContract.MetalsRateEntry.COLUMN_DATE + " ASC";

            String metalId = Utility.getCurrentMetalId(getActivity());
            Uri ratesByMetalUri = MetalsContract.MetalsRateEntry.buildMetalsRateWithStartDate(
                    metalId, System.currentTimeMillis());

            return new CursorLoader(getActivity(),
                    ratesByMetalUri,
                    TREND_GRAPH_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            int xAxisIdx = 0;
            int xAxisHighlighted = -1;
            ArrayList<String> xVals = new ArrayList();
            ArrayList<Entry> yVals = new ArrayList();
            float maxVal=0;
            float minVal=Float.MAX_VALUE;
            while (cursor.moveToNext()) {
                // Read metal ID from cursor
                //String metalId = data.getString(COL_RATE_METAL_ID);
                long date = cursor.getLong(COL_RATE_DATE);
                if(mHighlightedDate == date){
                    xAxisHighlighted = xAxisIdx;
                }
                String shortDateText = Utility.getShortFormattedMonthDay(getActivity(), date);
                xVals.add(shortDateText);

                float rate = cursor.getFloat(Utility.getTrendPreferredCurrencyColumnId(Utility.getPreferredCurrency(getActivity())));
                if(!Utility.isGrams(getActivity())) {
                    rate *= Utility.GRAMS_IN_OUNCE;
                }

                maxVal = rate>maxVal ? rate : maxVal;
                minVal = rate<minVal ? rate : minVal;
                yVals.add(new Entry(rate, xAxisIdx++));
            }

            // mChart.setVisibleXRange(20);
            // create a dataset and give it a type
            LineDataSet set1 = new LineDataSet(yVals, "");
            set1.setFillAlpha(110);
            set1.setFillColor(Color.RED);

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
//            set1.setColor(Color.GRAY);
//            set1.setCircleColor(Color.BLACK);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(getResources().getColor(R.color.primary_dark));
            set1.setLineWidth(2f);
            set1.setCircleSize(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setFillAlpha(65);
            set1.setFillColor(Color.BLACK);
            //set1.setDrawCubic(true);
            set1.setHighLightColor(Color.RED);
            // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
            // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

            ArrayList<LineDataSet> dataSets = new ArrayList();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(xVals, dataSets);

//        LimitLine ll1 = new LimitLine(130f, "Upper Limit");
//        ll1.setLineWidth(4f);
//        ll1.enableDashedLine(10f, 10f, 0f);
//        ll1.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
//        ll1.setTextSize(10f);
//
//        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
//        ll2.setLineWidth(4f);
//        ll2.enableDashedLine(10f, 10f, 0f);
//        ll2.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
//        ll2.setTextSize(10f);

            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setAxisMaxValue(maxVal*1.005f);
            leftAxis.setStartAtZero(false);
            leftAxis.setAxisMinValue(minVal*0.995f);
            // leftAxis.setStartAtZero(false);

            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return Utility.getFormattedCurrency(Float.valueOf(value).doubleValue(), Utility.getPreferredCurrency(getActivity()), getActivity(), false);
                }
            });

            //leftAxis.setSpaceTop(20f);
            //leftAxis.setSpaceBottom(20f);

            mChart.getAxisRight().setEnabled(false);
            mChart.getXAxis().setAvoidFirstLastClipping(true);
            mChart.getXAxis().setAdjustXLabels(true);
            // set data
            mChart.setData(data);
            mChart.getLegend().setEnabled(false);   // Hide the legend

            mChart.animateX(1500);

            mChart.highlightValue(xAxisHighlighted, 0);
           // mChart.setHighlightLineWidth(15f);

             mChart.setDescription("");
            mChart.setNoDataTextDescription("");
            mChart.setHighlightEnabled(true);
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            mChart.setPinchZoom(true);

            mChart.setHighlightIndicatorEnabled(true);
            mChart.setDrawGridBackground(false);

            //  mChart.setVisibleYRange(30, YAxis.AxisDependency.LEFT);

            // // restrain the maximum scale-out factor
            // mChart.setScaleMinima(3f, 3f);
            //
            // // center the view to a specific position inside the chart
            // mChart.centerViewPort(10, 50, AxisDependency.LEFT);

            // get the legend (only possible after setting data)
            //Legend l = mChart.getLegend();

            // modify the legend ...
            // l.setPosition(LegendPosition.LEFT_OF_CHART);
            //l.setForm(Legend.LegendForm.LINE);
            // // dont forget to refresh the drawing
            //mChart.invalidate();
        }

    }

//    private String getRateForCurrency(Cursor data, String currencyId){
//        double rateRaw = data.getDouble(Utility.getPreferredCurrencyColumnId(currencyId));
//        String rate = Utility.getFormattedCurrency( rateRaw, currencyId, getActivity());
//        return rate;
//    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
        // Log.i("", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

}

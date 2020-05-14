package com.example.tathinkaccapp.Util;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class MpChart implements OnChartValueSelectedListener {
    public LineChart mLineChart;

    public MpChart(LineChart lineChart){
        mLineChart = lineChart;
    }

    public void chartInit(){
        mLineChart.setOnChartValueSelectedListener(this);
        // enable description text
        mLineChart.getDescription().setEnabled(false);
        // enable touch gestures
        mLineChart.setTouchEnabled(false);
        // enable scaling and dragging
        mLineChart.setDragEnabled(false);
        mLineChart.setScaleEnabled(true);
        mLineChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(false);
        // set an alternative background color
        mLineChart.setBackgroundColor(Color.parseColor("#f4f4f4"));
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        // add empty data
        mLineChart.setData(data);
        mLineChart.setDescription(null);    // Hide the description
        mLineChart.getAxisLeft().setDrawLabels(false);
        mLineChart.getXAxis().setDrawAxisLine(false);
        mLineChart.getAxisLeft().setDrawAxisLine(false);
        mLineChart.setViewPortOffsets(0f, 0f, 0f, 0f);
        mLineChart.getAxisRight().setDrawLabels(true);
        mLineChart.getXAxis().setDrawLabels(false);
        mLineChart.getAxisLeft().setDrawGridLines(false);
        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getLegend().setEnabled(false);   // Hide the legend
        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }


    private boolean moveToLastEntry = true;

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.parseColor("#F96C69"));
        set.setDrawCircles(false);
        set.setDrawFilled(true);
        set.setLineWidth(2f);
        set.setFillColor(Color.parseColor("#F96C69"));
        set.setDrawValues(false);
        return set;
    }

    public void addWalkEntry(double svm) {
        LineData data = mLineChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.setValueFormatter(new DefaultAxisValueFormatter(0));
            data.addEntry(new Entry(set.getEntryCount(), (float) svm), 0);
            data.notifyDataChanged();
            // let the chart know it's data has changed
            mLineChart.notifyDataSetChanged();
            // limit the number of visible entries
            mLineChart.setVisibleXRangeMaximum(20);
            // lineChart.setVisibleYRange(30, AxisDependency.LEFT);
            if (moveToLastEntry) {
                // move to the latest entry
                mLineChart.moveViewToX(data.getEntryCount());
            }
            if(data.getEntryCount() > 300){
                data.removeDataSet(0);
            }
        }
    }

    public void addIsWorkEntry(float leftPer, float rightPer) {
        LineData data = mLineChart.getData();
        if (data != null) {
            data.removeDataSet(0);
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            mLineChart.getData().getDataSetByIndex(0);
            data.setValueFormatter(new DefaultAxisValueFormatter(0));
            data.addEntry(new Entry(set.getEntryCount(), leftPer), 0);
            data.addEntry(new Entry(set.getEntryCount(), rightPer), 0);
            data.notifyDataChanged();
            // let the chart know it's data has changed
            mLineChart.notifyDataSetChanged();
            if (moveToLastEntry) {
//                // move to the latest entry
                mLineChart.moveViewToX(data.getEntryCount());
            }
        }
    }



    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }
    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}

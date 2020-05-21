package com.example.tathinkaccapp.Util;

import android.content.Context;
import android.util.Log;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import java.util.ArrayList;

public class MpChart{
    public BarChart mBarChart;
    public Context mContext;

    public MpChart(BarChart barChart, Context context){
        mBarChart = barChart;
        mContext = context;
    }

    public void chartInit(){
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawValueAboveBar(true);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setMaxVisibleValueCount(60);
        mBarChart.setPinchZoom(false);
        mBarChart.setDrawGridBackground(false);
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setCenterAxisLabels(false);
        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setDrawLabels(false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(3f);
        leftAxis.setEnabled(false);

        YAxis rightAxis = mBarChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(3f);
        rightAxis.setDrawLabels(false);
        mBarChart.setViewPortOffsets(0f, 0f, 0f, 0f);
        mBarChart.getAxisLeft().setDrawGridLines(false);
        mBarChart.getXAxis().setDrawGridLines(false);
        mBarChart.getLegend().setEnabled(false);
    }

    public void setWalkEntry(double leftStep, double rightStep){
        mBarChart.notifyDataSetChanged();
        mBarChart.invalidate();
        ArrayList<BarEntry> values = new ArrayList<>();
        values.add(new BarEntry(0, (float)leftStep));
        values.add(new BarEntry(1, (float)rightStep));
        BarDataSet set1;
        if (mBarChart.getData() != null && mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.setDrawValues(false);
        } else {
            set1 = new BarDataSet(values, null);
            set1.setDrawIcons(false);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);
            mBarChart.setData(data);
            set1.setDrawValues(false);
        }
    }
}

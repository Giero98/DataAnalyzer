package com.example.masterthesis;

import  androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;

import com.example.masterthesis.bluetooh.ConnectBtClientThread;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class Graph extends AppCompatActivity {

    private BarChart barChart;
    String fileName;
    ArrayList<Integer> fileUploadNumberList = new ArrayList<>(), qualityRangeList = new ArrayList<>();
    ArrayList<Float> sendingTimeList = new ArrayList<>(), uploadSpeedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setTitle("Graphs");

        Button buttonBack = findViewById(R.id.button_back);
        barChart = findViewById(R.id.barChart);
        RadioButton graph1 = findViewById(R.id.radioButton),
                    graph2 = findViewById(R.id.radioButton2),
                    graph3 = findViewById(R.id.radioButton3);

        buttonBack.setOnClickListener(v -> finish());
        graph1.setOnClickListener(v -> drawGraph1());
        graph2.setOnClickListener(v -> drawGraph2());
        graph3.setOnClickListener(v -> drawGraph3());


        for(String list : ConnectBtClientThread.getMeasurementDataList())
        {
            if(list.contains(","))
            {
                String[] dataArrayFileFirstData = list.split(",");
                String fileUploadNumber = dataArrayFileFirstData[0];
                try {
                    Integer.parseInt(fileUploadNumber);
                    String qualityRange = dataArrayFileFirstData[3];
                    String sendingTime = dataArrayFileFirstData[4];
                    String uploadSpeed = dataArrayFileFirstData[5];

                    fileUploadNumberList.add(Integer.parseInt(fileUploadNumber));
                    qualityRangeList.add(Integer.parseInt(qualityRange));
                    sendingTimeList.add(Float.parseFloat(sendingTime));
                    uploadSpeedList.add(Float.parseFloat(uploadSpeed));
                } catch (NumberFormatException ignored) {}
            } else {
                if(!fileUploadNumberList.isEmpty())
                    fileUploadNumberList.clear();
                if(!qualityRangeList.isEmpty())
                    qualityRangeList.clear();
                if(!sendingTimeList.isEmpty())
                    sendingTimeList.clear();
                if(!uploadSpeedList.isEmpty())
                    uploadSpeedList.clear();
                fileName = list;
            }
        }


        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setBackgroundColor(Color.WHITE);
        barChart.setDoubleTapToZoomEnabled(false);
        //barChart.setDragEnabled(true); // włącz przewijanie

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setLabelCount(fileUploadNumberList.size()); // liczba etykiet na osi X

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGridDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));
        leftAxis.setAxisMinimum(0f); // minimalna wartość osi Y
        leftAxis.setGranularity(0.01f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(16f);

        barChart.getAxisRight().setEnabled(false);
    }

    //OSX: fileUploadNumberList , OSY: sendingTimeList
    private void drawGraph1() {
        // przykładowe dane
        List<BarEntry> barEntries = new ArrayList<>();

        for(int i=0; i<fileUploadNumberList.size(); i++)
        {
            barEntries.add(new BarEntry(fileUploadNumberList.get(i),sendingTimeList.get(i)));
        }
        drawGraph(barEntries);
    }

    //OSX: fileUploadNumberList , OSY: qualityRangeList
    private void drawGraph2() {
        // przykładowe dane
        List<BarEntry> barEntries = new ArrayList<>();

        for(int i=0; i<fileUploadNumberList.size(); i++)
        {
            barEntries.add(new BarEntry(fileUploadNumberList.get(i),qualityRangeList.get(i)));
        }
        drawGraph(barEntries);
    }

    //OSX: fileUploadNumberList , OSY: qualityRangeList
    private void drawGraph3() {
        // przykładowe dane
        List<BarEntry> barEntries = new ArrayList<>();

        for(int i=0; i<fileUploadNumberList.size(); i++)
        {
            barEntries.add(new BarEntry(fileUploadNumberList.get(i),uploadSpeedList.get(i)));
        }
        drawGraph(barEntries);
    }

    private void drawGraph(List<BarEntry> barEntries){
        // konfiguracja wykresu
        BarDataSet barDataSet = new BarDataSet(barEntries, fileName);
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setHighlightEnabled(false);
        barDataSet.setValueTextSize(16f);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Constants.decimalFormat.format(value);
            }
        });

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.7f); // ustawiamy stałą szerokość kolumn

        barChart.setData(barData);
        barChart.animateY(1000);
        barChart.setVisibleXRangeMaximum(4); // ustawiamy maksymalną widoczną ilość kolumn
        barChart.moveViewToX(0); // przesuwamy wykres do początku
        barChart.invalidate();
    }

    //Create a menu for your current activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem itemShowLog = menu.findItem(R.id.show_log);
        itemShowLog.setTitle("Show Log");
        return true;
    }
    //Create interactions for selecting items from the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_log) {
            Intent intent = new Intent(this, MainActivity_Log.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
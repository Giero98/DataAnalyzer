package com.example.masterthesis;

import  androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.masterthesis.bluetooh.ClientBt;
import com.example.masterthesis.wifi.ClientWiFi;
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
    public static String connectionDetails;
    BarChart barChart;
    String fileName, columnUnit;
    ArrayList<Integer> sentFileNumber = new ArrayList<>(), qualitySignal = new ArrayList<>();
    ArrayList<Float> fileUploadTime = new ArrayList<>(), uploadSpeed = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setTitle("Graphs");

        Button buttonBack = findViewById(R.id.button_back);
        barChart = findViewById(R.id.barChart);
        RadioButton graphUploadTime = findViewById(R.id.radioButton_uploadTime),
                    graphQualitySignal = findViewById(R.id.radioButton_qualitySignal),
                    graphUploadSpeed = findViewById(R.id.radioButton_uploadSpeed);

        if(connectionDetails.equals(Constants.connectionWiFi))
            graphQualitySignal.setVisibility(View.INVISIBLE);
        buttonBack.setOnClickListener(v -> finish());
        graphUploadTime.setOnClickListener(v -> selectDataUploadTime());
        graphQualitySignal.setOnClickListener(v -> selectDataQualitySignal());
        graphUploadSpeed.setOnClickListener(v -> selectDataUploadSpeed());

        selectData();
        setupGraph();
        setupXAxis();
        setupYAxis();
    }

    void selectData()
    {
        if(connectionDetails.equals(Constants.connectionBt))
            assignmentBtData();
        else if(connectionDetails.equals(Constants.connectionWiFi))
            assignmentWiFiData();
    }

    void assignmentBtData()
    {
        for(String measurementData : ClientBt.getMeasurementDataList())
        {
            if(measurementData.contains(","))
            {
                String[] dataArrayFileMeasurement = measurementData.split(",");
                String sentFileNumberTable = dataArrayFileMeasurement[0];
                loadingIntoTheDataList(sentFileNumberTable, dataArrayFileMeasurement);
            } else {
                clearingDataInLists();
                fileName = measurementData;
            }
        }
    }

    void assignmentWiFiData()
    {
        for(String measurementData : ClientWiFi.getMeasurementDataList())
        {
            if(measurementData.contains(","))
            {
                String[] dataArrayFileMeasurement = measurementData.split(",");
                String sentFileNumberTable = dataArrayFileMeasurement[0];
                loadingIntoTheDataList(sentFileNumberTable, dataArrayFileMeasurement);
            } else {
                clearingDataInLists();
                fileName = measurementData;
            }
        }
    }

    void loadingIntoTheDataList (String sentFileNumberTable, String[] dataArrayFileMeasurement)
    {
        try {
            Integer.parseInt(sentFileNumberTable);
            sentFileNumber.add(Integer.parseInt(sentFileNumberTable));
            qualitySignal.add(Integer.parseInt(dataArrayFileMeasurement[3]));
            fileUploadTime.add(Float.parseFloat(dataArrayFileMeasurement[4]));
            uploadSpeed.add(Float.parseFloat(dataArrayFileMeasurement[5]));
        } catch (NumberFormatException ignored) {}
    }

    void setupGraph()
    {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setBackgroundColor(Color.WHITE);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.getAxisRight().setEnabled(false);
    }

    void setupXAxis()
    {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(Constants.distanceBetweenXAxisData);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(Constants.axisValueSize);
        xAxis.setLabelCount(sentFileNumber.size());
    }

    void setupYAxis()
    {
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGridDashedLine(Constants.girdLineStyle);
        leftAxis.setAxisMinimum(Constants.minimumYAxisValue);
        leftAxis.setGranularity(Constants.distanceBetweenYAxisData);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(Constants.axisValueSize);
    }

    void clearingDataInLists()
    {
        if(!sentFileNumber.isEmpty())
            sentFileNumber.clear();
        if(!qualitySignal.isEmpty())
            qualitySignal.clear();
        if(!fileUploadTime.isEmpty())
            fileUploadTime.clear();
        if(!uploadSpeed.isEmpty())
            uploadSpeed.clear();
    }

    void selectDataUploadTime() {
        List<BarEntry> graphData = new ArrayList<>();
        for(int i=0; i < sentFileNumber.size(); i++)
        {
            graphData.add(new BarEntry(sentFileNumber.get(i),fileUploadTime.get(i)));
        }
        columnUnit = Constants.uploadTimeUnit;
        drawGraph(graphData);
    }

    void selectDataQualitySignal() {
        List<BarEntry> graphData = new ArrayList<>();
        for(int i=0; i < sentFileNumber.size(); i++)
        {
            graphData.add(new BarEntry(sentFileNumber.get(i),qualitySignal.get(i)));
        }
        columnUnit = Constants.qualitySignalUnit;
        drawGraph(graphData);
    }

    void selectDataUploadSpeed() {
        List<BarEntry> graphData = new ArrayList<>();
        for(int i=0; i < sentFileNumber.size(); i++)
        {
            graphData.add(new BarEntry(sentFileNumber.get(i),uploadSpeed.get(i)));
        }
        if(connectionDetails.equals(Constants.connectionBt))
            columnUnit = Constants.uploadBTSpeedUnit;
        else if(connectionDetails.equals(Constants.connectionWiFi))
            columnUnit = Constants.uploadWiFiSpeedUnit;
        drawGraph(graphData);
    }

    @SuppressLint("SetTextI18n")
    void drawGraph(List<BarEntry> graphData){
        TextView textViewGraph = findViewById(R.id.textView_graph);
        textViewGraph.setText("File details: " + fileName);
        textViewGraph.setGravity(Gravity.CENTER);

        BarDataSet totalGraphData = formatDataSet(graphData);
        BarData barData = formatData(totalGraphData);
        lastGraphSetting(barData);
    }

    BarDataSet formatDataSet(List<BarEntry> graphData)
    {
        BarDataSet totalGraphData = new BarDataSet(graphData, columnUnit);
        totalGraphData.setColors(ColorTemplate.MATERIAL_COLORS);
        totalGraphData.setValueTextColor(Color.BLACK);
        totalGraphData.setHighlightEnabled(false);
        totalGraphData.setValueTextSize(16f);
        totalGraphData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Constants.decimalFormat.format(value);
            }
        });
        return totalGraphData;
    }

    BarData formatData(BarDataSet totalGraphData)
    {
        BarData barData = new BarData(totalGraphData);
        barData.setBarWidth(Constants.columnWidth);
        return barData;
    }

    void lastGraphSetting(BarData barData)
    {
        barChart.setData(barData);
        barChart.animateY(Constants.graphAnimationDuration);
        barChart.setVisibleXRangeMaximum(Constants.maximumNumberOfColumnsOnTheScreen);
        barChart.moveViewToX(0); // graph moved to the beginning
        barChart.invalidate();
    }

    //Create a menu for your current activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem itemShowLog = menu.findItem(R.id.show_log);
        itemShowLog.setTitle(Constants.titleLog);
        return true;
    }
    //Create interactions for selecting items from the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_log) {
            Intent intent = new Intent(this, Logs.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
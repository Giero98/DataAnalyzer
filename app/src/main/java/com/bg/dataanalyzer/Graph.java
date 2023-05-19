package com.bg.dataanalyzer;

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

import com.bg.dataanalyzer.file.FileInformation;
import com.bg.dataanalyzer.file.SendingData;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class Graph extends AppCompatActivity {
    public static String connectionDetails;
    CombinedChart combinedChart;
    Button buttonBack;
    RadioButton graphUploadTime, graphQualitySignal, graphUploadSpeed;
    String fileName, columnUnit;
    float averageValue;
    ArrayList<Integer> sentFileNumber = new ArrayList<>(), qualitySignal = new ArrayList<>();
    ArrayList<Float> fileUploadTime = new ArrayList<>(), uploadSpeed = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setTitle(getString(R.string.title_graph));

        declarationButtonsAndChart();
        hideSignalQualityGraphForWifi();
        buttonsResponse();

        allocationData();
        setupGraph();
        setupXAxis();
        setupYAxis();
    }

    void declarationButtonsAndChart() {
        buttonBack = findViewById(R.id.button_back);
        combinedChart = findViewById(R.id.combinedChart);
        graphUploadTime = findViewById(R.id.radioButton_uploadTime);
        graphQualitySignal = findViewById(R.id.radioButton_qualitySignal);
        graphUploadSpeed = findViewById(R.id.radioButton_uploadSpeed);
    }

    void hideSignalQualityGraphForWifi() {
        if(connectionDetails.equals(Constants.connectionWiFi))
            graphQualitySignal.setVisibility(View.INVISIBLE);
    }

    void buttonsResponse() {
        buttonBack.setOnClickListener(v -> finish());
        graphUploadTime.setOnClickListener(v -> selectDataUploadTime());
        graphQualitySignal.setOnClickListener(v -> selectDataQualitySignal());
        graphUploadSpeed.setOnClickListener(v -> selectDataUploadSpeed());
    }

    public void allocationData() {
        for(String measurementData : SendingData.getMeasurementDataList())
        {
            if(measurementData.contains(",")) {
                String[] dataArrayFileMeasurement = measurementData.split(",");
                String sentFileNumberTable = dataArrayFileMeasurement[0];
                loadingIntoTheDataList(sentFileNumberTable, dataArrayFileMeasurement);
            }
            else {
                clearingDataInLists();
                fileName = measurementData;
            }
        }
    }

    void loadingIntoTheDataList (String sentFileNumberTable, String[] dataArrayFileMeasurement) {
        try {
            Integer.parseInt(sentFileNumberTable);

            if(SendingData.getModuleSelect().equals(Constants.connectionBt)) {
                sentFileNumber.add(Integer.parseInt(sentFileNumberTable));
                qualitySignal.add(Integer.parseInt(dataArrayFileMeasurement[4]));
                fileUploadTime.add(Float.parseFloat(dataArrayFileMeasurement[5]));
                uploadSpeed.add(Float.parseFloat(dataArrayFileMeasurement[6]));
            }
            else {
                sentFileNumber.add(Integer.parseInt(sentFileNumberTable));
                fileUploadTime.add(Float.parseFloat(dataArrayFileMeasurement[4]));
                uploadSpeed.add(Float.parseFloat(dataArrayFileMeasurement[5]));
            }
        }
        catch (NumberFormatException ignored) {}
    }

    void clearingDataInLists() {
        if(!sentFileNumber.isEmpty())
            sentFileNumber.clear();
        if(!qualitySignal.isEmpty())
            qualitySignal.clear();
        if(!fileUploadTime.isEmpty())
            fileUploadTime.clear();
        if(!uploadSpeed.isEmpty())
            uploadSpeed.clear();
    }

    void setupGraph() {
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawValueAboveBar(true);
        combinedChart.setBackgroundColor(Color.WHITE);
        combinedChart.setDoubleTapToZoomEnabled(false);
        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.BAR,
                CombinedChart.DrawOrder.LINE});
    }

    void setupXAxis() {
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(Constants.distanceBetweenXAxisData);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(Constants.axisValueSize);
        xAxis.setLabelCount(sentFileNumber.size());
    }

    void setupYAxis() {
        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setGridDashedLine(Constants.girdLineStyle);
        leftAxis.setAxisMinimum(Constants.minimumYAxisValue);
        leftAxis.setGranularity(Constants.distanceBetweenYAxisData);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(Constants.axisValueSize);
    }

    void selectDataUploadTime() {
        List<BarEntry> graphData = new ArrayList<>();
        List<Entry> averageValues = new ArrayList<>();
        averageValue = calculateAverage(fileUploadTime);

        for(int i=0; i < sentFileNumber.size(); i++) {
            graphData.add(new BarEntry(sentFileNumber.get(i),fileUploadTime.get(i)));
            averageValues.add(new Entry(sentFileNumber.get(i), averageValue));
        }
        columnUnit = Constants.uploadTimeUnit;
        drawGraph(graphData, averageValues);
    }

    void selectDataQualitySignal() {
        List<BarEntry> graphData = new ArrayList<>();
        List<Entry> averageValues = new ArrayList<>();
        averageValue = calculateIntAverage(qualitySignal);

        for(int i=0; i < sentFileNumber.size(); i++) {
            graphData.add(new BarEntry(sentFileNumber.get(i),qualitySignal.get(i)));
            averageValues.add(new Entry(sentFileNumber.get(i), averageValue));
        }
        columnUnit = Constants.qualitySignalUnit;
        drawGraph(graphData, averageValues);
    }

    void selectDataUploadSpeed() {
        List<BarEntry> graphData = new ArrayList<>();
        List<Entry> averageValues = new ArrayList<>();
        averageValue = calculateAverage(uploadSpeed);

        for(int i=0; i < sentFileNumber.size(); i++) {
            graphData.add(new BarEntry(sentFileNumber.get(i),uploadSpeed.get(i)));
            averageValues.add(new Entry(sentFileNumber.get(i), averageValue));
        }
        columnUnit = "[" + FileInformation.getFileSizeUnit(FileInformation.getFileSizeBytes()) + "/s]";
        drawGraph(graphData, averageValues);
    }

    @SuppressLint("SetTextI18n")
    void drawGraph(List<BarEntry> graphData, List<Entry> averageValues) {
        TextView textViewGraph = findViewById(R.id.textView_graph);
        textViewGraph.setText(getString(R.string.file_details) + ": " + fileName);
        textViewGraph.setGravity(Gravity.CENTER);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(generateLineData(averageValues));
        combinedData.setData(generateBarData(graphData));

        combinedChart.getXAxis().setAxisMinimum(0.5f);
        combinedChart.getXAxis().setAxisMaximum(sentFileNumber.size() + 0.5f);


        combinedChart.setData(combinedData);
        combinedChart.animateY(Constants.graphAnimationDuration);
        combinedChart.setVisibleXRangeMaximum(Constants.maximumNumberOfColumnsOnTheScreen);
        combinedChart.invalidate();
    }

    BarData generateBarData(List<BarEntry> graphData) {
        BarDataSet barDataSet = new BarDataSet(graphData, columnUnit);
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
        barData.setBarWidth(Constants.columnWidth);
        return barData;
    }

    LineData generateLineData(List<Entry> averageValues) {
        LineDataSet lineDataSet = new LineDataSet(averageValues, getString(R.string.average) +
                "= " + Constants.decimalFormat.format(averageValue));
        lineDataSet.setColor(Color.DKGRAY);
        lineDataSet.setCircleColor(Color.DKGRAY);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(0f);

        return new LineData(lineDataSet);
    }

    float calculateIntAverage(ArrayList<Integer> values) {
        float sum = 0;
        for (float value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    float calculateAverage(List<Float> values) {
        float sum = 0;
        for (float value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem    showLog = menu.findItem(R.id.show_log),
                    aboutAuthor = menu.findItem(R.id.about_author),
                    changeLanguage = menu.findItem(R.id.change_language);
        showLog.setTitle(getString(R.string.title_log));
        aboutAuthor.setVisible(false);
        changeLanguage.setVisible(false);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_log) {
            Intent intent = new Intent(this, Logs.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
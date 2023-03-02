package com.example.masterthesis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;

import com.example.masterthesis.bluetooh.ConnectBtClientThread;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class Graph extends AppCompatActivity {

    private LineChart mLineChart;
    ArrayList<Integer> fileUploadNumberList = new ArrayList<>(), qualityRangeList = new ArrayList<>();
    ArrayList<Float> sendingTimeList = new ArrayList<>(), uploadSpeedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setTitle("Graphs");

        Button buttonBack = findViewById(R.id.button_back);
        mLineChart = findViewById(R.id.lineChart);
        RadioButton graph1 = findViewById(R.id.radioButton),
                    graph2 = findViewById(R.id.radioButton2),
                    graph3 = findViewById(R.id.radioButton3);

        buttonBack.setOnClickListener(v -> finish());
        graph1.setOnClickListener(v -> drawGraph1());
        graph2.setOnClickListener(v -> drawGraph2());
        graph3.setOnClickListener(v -> drawGraph3());


        for(String list : ConnectBtClientThread.getMeasurementDataList())
        {
            String[] dataArrayFileFirstData = list.split(",");
            String fileUploadNumber = dataArrayFileFirstData[0];
            try{
                Integer.parseInt(fileUploadNumber);
                String qualityRange = dataArrayFileFirstData[3];
                String sendingTime = dataArrayFileFirstData[4];
                String uploadSpeed = dataArrayFileFirstData[5];

                fileUploadNumberList.add(Integer.parseInt(fileUploadNumber));
                qualityRangeList.add(Integer.parseInt(qualityRange));
                sendingTimeList.add(Float.parseFloat(sendingTime));
                uploadSpeedList.add(Float.parseFloat(uploadSpeed));


            } catch (NumberFormatException ignored){}

        }


    }

    //OSX: fileUploadNumberList , OSY: sendingTimeList
    private void drawGraph1() {
        // przykładowe dane
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<fileUploadNumberList.size(); i++)
        {
            entries.add(new Entry(fileUploadNumberList.get(i),sendingTimeList.get(i)));
        }
        drawGraph(entries);
    }

    //OSX: fileUploadNumberList , OSY: qualityRangeList
    private void drawGraph2() {
        // przykładowe dane
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<fileUploadNumberList.size(); i++)
        {
            entries.add(new Entry(fileUploadNumberList.get(i),qualityRangeList.get(i)));
        }
        drawGraph(entries);
    }

    //OSX: fileUploadNumberList , OSY: qualityRangeList
    private void drawGraph3() {
        // przykładowe dane
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<fileUploadNumberList.size(); i++)
        {
            entries.add(new Entry(fileUploadNumberList.get(i),uploadSpeedList.get(i)));
        }
        drawGraph(entries);
    }

    private void drawGraph(ArrayList<Entry> entries){
        // konfiguracja wykresu
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLUE);
        dataSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);
        mLineChart.invalidate(); // odświeżenie wykresu
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
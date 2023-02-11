package com.example.masterthesis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity_Log extends AppCompatActivity {

    public static final ArrayList<String> listLog = new ArrayList<>();
    public static ArrayAdapter<String> listAdapterLog;

    public ListView listViewLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_log);
        setTitle("Master Thesis - LOG");

        listViewLog = findViewById(R.id.ListViewLog);

        listAdapterLog = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listLog);
        listViewLog.setAdapter(listAdapterLog);
        listAdapterLog.notifyDataSetChanged();
    }

    public static class ListLog extends MainActivity_Log
    {

        public ListLog() {

        }

        public void addLog(Date date, String description, String errorCode)
        {
            listLog.add(date + "\n" + description + "\n" + errorCode);
        }
        public void addLog(Date date, String description)
        {
            listLog.add(date + "\n" + description);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem showLog = menu.findItem(R.id.show_log);
        showLog.setTitle("Back");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_log) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
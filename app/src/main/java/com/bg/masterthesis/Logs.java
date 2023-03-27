package com.bg.masterthesis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

public class Logs extends AppCompatActivity {
    static final ArrayList<String> listLog = new ArrayList<>(), listLogError = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        setTitle("LOG");

        ListView logListView = findViewById(R.id.logListView);
        Button buttonInfLog = findViewById(R.id.button_inf_log);
        Button buttonErrorLog = findViewById(R.id.button_error_log);

        ArrayAdapter<String> listAdapterLog = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listLog);
        ArrayAdapter<String> listAdapterLogError = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listLogError);

        logListView.setAdapter(listAdapterLog);
        listAdapterLog.notifyDataSetChanged();


        buttonInfLog.setOnClickListener(v ->{
            logListView.setAdapter(listAdapterLog);
            listAdapterLog.notifyDataSetChanged();
        });

        buttonErrorLog.setOnClickListener(v->{
            logListView.setAdapter(listAdapterLogError);
            listAdapterLogError.notifyDataSetChanged();
        });
    }

    public static class ListLog extends Logs
    {
        public void addLog(String description, String errorCode)
        {
            listLogError.add(currentDate()  + "\n" + description + "\n" + errorCode);
        }

        public void addLog(String description)
        {
            listLog.add(currentDate() + "\n" + description);
        }

        Date currentDate()
        {
            return new Date(System.currentTimeMillis());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem showLog = menu.findItem(R.id.show_log);
        showLog.setTitle(Constants.back);
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
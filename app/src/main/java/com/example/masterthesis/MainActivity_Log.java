package com.example.masterthesis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

/**
 * View of application logs
 */
public class MainActivity_Log extends AppCompatActivity {

    //A list of information and error logs
    public static final ArrayList<String>
            listLog = new ArrayList<>(),
            listLogError = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_log);
        setTitle("LOG");

        ListView listViewLog = findViewById(R.id.ListViewLog);
        Button buttonInfLog = findViewById(R.id.button_inf_log);
        Button buttonErrorLog = findViewById(R.id.button_error_log);

        ArrayAdapter<String> listAdapterLog = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listLog);
        ArrayAdapter<String> listAdapterLogError = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listLogError);
        listViewLog.setAdapter(listAdapterLog);
        listAdapterLog.notifyDataSetChanged();

        //Selecting a list of information logs
        buttonInfLog.setOnClickListener(v ->{
            listViewLog.setAdapter(listAdapterLog);
            listAdapterLog.notifyDataSetChanged();
        });

        //Selecting a list of error logs
        buttonErrorLog.setOnClickListener(v->{
            listViewLog.setAdapter(listAdapterLogError);
            listAdapterLogError.notifyDataSetChanged();
        });
    }

    //A class used in other activities to add logs
    public static class ListLog extends MainActivity_Log
    {
        //ListLog class constructor
        public ListLog() {
        }

        //method of adding error logs
        public void addLog(Date date, String description, String errorCode)
        {
            listLogError.add(date + "\n" + description + "\n" + errorCode);
        }

        //method of adding information logs
        public void addLog(Date date, String description)
        {
            listLog.add(date + "\n" + description);

        }

        //method where the current date is retrieved
        public Date currentDate()
        {
            return new Date(System.currentTimeMillis());
        }
    }

    //Create a menu for your current activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem showLog = menu.findItem(R.id.show_log);
        showLog.setTitle("Back");
        return true;
    }

    //Create interactions for selecting items from the menu
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
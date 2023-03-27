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
    final ArrayList<String> LIST_LOG = new ArrayList<>(), LIST_LOG_ERROR = new ArrayList<>();
    ListView logListView;
    Button buttonInfLog, buttonErrorLog;
    ArrayAdapter<String> listAdapterLog, listAdapterLogError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        setTitle(Constants.titleLogActivity);

        declarationButtonsAndListView();
        declarationArrayAdapter();
        setAndUpdateLogList();
        buttonsResponses();
    }

    void declarationButtonsAndListView()
    {
        logListView = findViewById(R.id.logListView);
        buttonInfLog = findViewById(R.id.button_inf_log);
        buttonErrorLog = findViewById(R.id.button_error_log);
    }

    void declarationArrayAdapter()
    {
        listAdapterLog = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, LIST_LOG);
        listAdapterLogError = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, LIST_LOG_ERROR);
    }

    void setAndUpdateLogList()
    {
        logListView.setAdapter(listAdapterLog);
        listAdapterLog.notifyDataSetChanged();
    }

    void setAndUpdateErrorLogList()
    {
        logListView.setAdapter(listAdapterLogError);
        listAdapterLogError.notifyDataSetChanged();
    }

    void buttonsResponses()
    {
        buttonInfLog.setOnClickListener(v -> setAndUpdateLogList());
        buttonErrorLog.setOnClickListener(v-> setAndUpdateErrorLogList());
    }

    public static class ListLog extends Logs
    {
        public void addLog(String description, String errorCode)
        {
            LIST_LOG_ERROR.add(currentDate()  + "\n" + description + "\n" + errorCode);
        }

        public void addLog(String description)
        {
            LIST_LOG.add(currentDate() + "\n" + description);
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
        showLog.setTitle(Constants.textBack);
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
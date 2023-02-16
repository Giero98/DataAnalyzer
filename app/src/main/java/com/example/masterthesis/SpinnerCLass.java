package com.example.masterthesis;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * A class that is responsible for creating a spinner and reacting to value changes
 */
public class SpinnerCLass extends MainActivity_BT{
    private final Context context;
    private final Spinner spinnerBufferSize, spinnerNumberOfFile;
    public static int bufferSize, numberOfFile;

    //SpinnerCLass class constructor
    public SpinnerCLass(MainActivity_BT newContext, Spinner newSpinnerBufferSize, Spinner newSpinnerNumberOfFile) {
        context = newContext;
        spinnerBufferSize = newSpinnerBufferSize;
        spinnerNumberOfFile = newSpinnerNumberOfFile;
        start();
    }

    //The method where setting values in spinners and responding to changes begins
    private void start(){

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence>
                adapterBufferSize = ArrayAdapter.createFromResource(context,
                    R.array.buffer_size_array, android.R.layout.simple_spinner_item),
                adapterNumberOfFile = ArrayAdapter.createFromResource(context,
                    R.array.number_of_files_to_send_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBufferSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterNumberOfFile.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBufferSize.setAdapter(adapterBufferSize);
        spinnerNumberOfFile.setAdapter(adapterNumberOfFile);

        //Reacting to a change in the spinner responsible for the buffer
        spinnerBufferSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: //4KB
                        bufferSize = 1024 * 4;
                        break;
                    case 1: //16KB
                        bufferSize = 1024 * 16;
                        break;
                    case 2: //32KB
                        bufferSize = 1024 * 32;
                        break;
                    case 3: //10% OF FILE
                        bufferSize = 0;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //Reacting to changes in the spinner responsible for the number of files
        spinnerNumberOfFile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: //1
                        numberOfFile = 1;
                        break;
                    case 1: //3
                        numberOfFile = 3;
                        break;
                    case 2: //5
                        numberOfFile = 5;
                        break;
                    case 3: //10
                        numberOfFile = 10;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}

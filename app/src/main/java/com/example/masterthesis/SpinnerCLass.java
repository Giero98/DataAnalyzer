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
    private final Spinner spinnerBufferSize;
    public static int bufferSize;

    //SpinnerCLass class constructor
    public SpinnerCLass(MainActivity_BT newContext, Spinner newSpinnerBufferSize) {
        context = newContext;
        spinnerBufferSize = newSpinnerBufferSize;
        start();
    }

    //The method where setting values in spinners and responding to changes begins
    private void start(){

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence>
                adapterBufferSize = ArrayAdapter.createFromResource(context,
                    R.array.buffer_size_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBufferSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBufferSize.setAdapter(adapterBufferSize);

        //Reacting to a change in the spinner responsible for the buffer
        spinnerBufferSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: //4KB
                        bufferSize = 1024 * 4;
                        break;
                    case 1: //8KB
                        bufferSize = 1024 *8;
                        break;
                    case 2: //16KB
                        bufferSize = 1024 * 16;
                        break;
                    case 3: //32KB
                        bufferSize = 1024 * 32;
                        break;
                    case 4:
                        bufferSize = 1024 * 64;
                        break;
                    case 5:
                        bufferSize = 1024 * 128;
                        break;
                    case 6:
                        bufferSize = 1024 * 256;
                        break;
                    case 7: //10% OF FILE
                        bufferSize = 0;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}

package com.example.masterthesis;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.masterthesis.bluetooh.Bluetooth;

public class Buffer {
    final Context contextBt;
    final Spinner selectBuffer;
    public static int bufferSize;

    public Buffer(Bluetooth contextBt, Spinner selectBuffer) {
        this.contextBt = contextBt;
        this.selectBuffer = selectBuffer;
        settingBufferValue();
    }

    void settingBufferValue(){
        ArrayAdapter<CharSequence> adapterBufferSize = ArrayAdapter.createFromResource(contextBt,
                    R.array.buffer_size_array, android.R.layout.simple_spinner_item);
        adapterBufferSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectBuffer.setAdapter(adapterBufferSize);
        selectBuffer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: //4KB
                        bufferSize = Constants.size1Kb * 4;
                        break;
                    case 1:
                        bufferSize = Constants.size1Kb * 8;
                        break;
                    case 2:
                        bufferSize = Constants.size1Kb * 16;
                        break;
                    case 3:
                        bufferSize = Constants.size1Kb * 32;
                        break;
                    case 4:
                        bufferSize = Constants.size1Kb * 64;
                        break;
                    case 5:
                        bufferSize = Constants.size1Kb * 128;
                        break;
                    case 6:
                        bufferSize = Constants.size1Kb * 256;
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

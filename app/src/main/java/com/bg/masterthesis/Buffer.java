package com.bg.masterthesis;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Buffer {
    Context currentContext;
    Spinner selectBuffer;
    public static int bufferSize;

    public Buffer(Context context, Spinner selectBuffer) {
        currentContext = context;
        this.selectBuffer = selectBuffer;
        settingBufferValue();
    }

    void settingBufferValue() {
        ArrayAdapter<CharSequence> adapterBufferSize = createAndCustomizeAdapterForBufferList();

        selectBuffer.setAdapter(adapterBufferSize);
        selectBuffer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bufferSize = Constants.bufferSizes[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    ArrayAdapter<CharSequence> createAndCustomizeAdapterForBufferList() {
        int     textArray = R.array.buffer_size_array,
                textView = android.R.layout.simple_spinner_item,
                resource = android.R.layout.simple_spinner_dropdown_item;

        ArrayAdapter<CharSequence> adapterBufferSize = ArrayAdapter.createFromResource(currentContext, textArray, textView);
        adapterBufferSize.setDropDownViewResource(resource);

        return adapterBufferSize;
    }
}

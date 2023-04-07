package com.bg.masterthesis.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.bg.masterthesis.Constants;
import com.bg.masterthesis.Logs;
import com.bg.masterthesis.R;

public class NumberOfFileFromUI {

    @SuppressLint("SetTextI18n")
    public static void readNumberOfFilesToSent(Context context) {
        Logs LOG = new Logs();
        try {
            int number = getNumberFromUI();
            if(getNumberFromUI() < Constants.minimumNumberOfUploadFiles || number > Constants.maximumNumberOfUploadFiles) {
                Toast.makeText(context, context.getString(R.string.value_between_1_100), Toast.LENGTH_SHORT).show();
                if(number > Constants.maximumNumberOfUploadFiles)
                    DeclarationOfUIVar.multiple_file.setText(Integer.toString(Constants.maximumNumberOfUploadFiles));
            }
        }
        catch(NumberFormatException e) {
            Toast.makeText(context, context.getString(R.string.enter_value), Toast.LENGTH_SHORT).show();
            LOG.addLog(context.getString(R.string.incorrect_format), e.getMessage());
        }
    }

    public static int getNumberFromUI() {
        return Integer.parseInt(DeclarationOfUIVar.multiple_file.getText().toString());
    }

    @SuppressLint("SetTextI18n")
    public static void increasingNumberOfFilesToSent() {
        int number = getNumberFromUI();
        if(number < Constants.maximumNumberOfUploadFiles) {
            number += 1;
            DeclarationOfUIVar.multiple_file.setText(Integer.toString(number));
        }
    }

    @SuppressLint("SetTextI18n")
    public static void reducingNumberOfFilesToSent() {
        int number = getNumberFromUI();
        if(number > Constants.minimumNumberOfUploadFiles) {
            number -= 1;
            DeclarationOfUIVar.multiple_file.setText(Integer.toString(number));
        }
    }
}

package com.bg.masterthesis.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.bg.masterthesis.Constants;
import com.bg.masterthesis.Logs;

public class NumberOfFileFromUI {

    @SuppressLint("SetTextI18n")
    public static void readNumberOfFilesToSent(Context context) {
        Logs LOG = new Logs();
        try {
            int number = getNumberFromUI();
            if(getNumberFromUI() < Constants.minimumNumberOfUploadFiles || number > Constants.maximumNumberOfUploadFiles) {
                Toast.makeText(context, "Enter a value between 1-100", Toast.LENGTH_SHORT).show();
                if(number > Constants.maximumNumberOfUploadFiles)
                    DeclarationOfUIVar.multiple_file.setText(Integer.toString(Constants.maximumNumberOfUploadFiles));
            }
        }
        catch(NumberFormatException e) {
            Toast.makeText(context, "Enter a numeric value", Toast.LENGTH_SHORT).show();
            LOG.addLog("Incorrect format loaded", e.getMessage());
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

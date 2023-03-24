package com.example.masterthesis.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masterthesis.R;

@SuppressLint({"StaticFieldLeak","SetTextI18n"})
public class DeclarationOfUIVar {
    static Context context;
    public static TextView textView_connected, textView_percent,
            textView_inf, textView_qualitySignal;
    public static EditText multiple_file;
    public static Button button_devices, button_detect, button_chooseFile, button_sendData,
            button_saveMeasurementData, button_graph, button_disconnectBack;
    public static ImageButton button_upMultipleFile, button_downMultipleFile;
    public static ProgressBar progressBar;
    public static LinearLayout layoutPercent, parameterLayoutForFileUpload;

    public DeclarationOfUIVar(Context context)
    {
        DeclarationOfUIVar.context = context;
        assignReferences();
    }

    void assignReferences()
    {
        textView_connected = ((Activity) context).findViewById(R.id.textView_connected);
        textView_percent = ((Activity) context).findViewById(R.id.textView_percent);
        textView_inf = ((Activity) context).findViewById(R.id.textView_inf);
        ((Activity) context).runOnUiThread(() ->
                textView_inf.setMovementMethod(new ScrollingMovementMethod()));

        button_disconnectBack = ((Activity) context).findViewById(R.id.button_disconnectAndBack);
        button_devices = ((Activity) context).findViewById(R.id.button_devices);
        button_detect = ((Activity) context).findViewById(R.id.button_detect);
        button_chooseFile = ((Activity) context).findViewById(R.id.button_chooseFile);
        button_sendData = ((Activity) context).findViewById(R.id.button_sendData);
        button_saveMeasurementData = ((Activity) context).findViewById(R.id.button_saveMeasurementData);
        button_graph = ((Activity) context).findViewById(R.id.button_graph);
        multiple_file = ((Activity) context).findViewById(R.id.multiple_file);

        button_upMultipleFile = ((Activity) context).findViewById(R.id.button_upMultipleFile);
        button_downMultipleFile = ((Activity) context).findViewById(R.id.button_downMultipleFile);

        parameterLayoutForFileUpload = ((Activity) context).findViewById(R.id.parameterLayoutForFileUpload);
        layoutPercent = ((Activity) context).findViewById(R.id.layoutPercent);

        progressBar = ((Activity) context).findViewById(R.id.progressBar);

    }

    public static void assignReferenceQualitySignal()
    {
        textView_qualitySignal = ((Activity) context).findViewById(R.id.textView_qualitySignal);
        startVisibilityQualitySignal();
    }

    static void startVisibilityQualitySignal()
    {
        TextView qualitySignalText = ((Activity) context).findViewById(R.id.textView_qualitySignalText);
        qualitySignalText.setVisibility(View.VISIBLE);
    }

    public static void viewAfterSuccessConnectionOnServerBt()
    {
        ((Activity) context).runOnUiThread(() -> {
            textView_connected.setText("Connected as a Host");
            button_devices.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_disconnectBack.setVisibility(View.INVISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);});
    }

    public static void viewAfterSuccessConnectionOnClientBt()
    {
        ((Activity) context).runOnUiThread(() -> {
            textView_connected.setText("Connected as a Client");
            button_chooseFile.setVisibility(View.VISIBLE);
            button_devices.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_disconnectBack.setText("Disconnect");
            parameterLayoutForFileUpload.setVisibility(View.VISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);});
    }

    public static void updateViewWhenDisconnected()
    {
        ((Activity) context).runOnUiThread(() -> {
            textView_connected.setText("Disconnected");
            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
            button_disconnectBack.setText("Back");
            button_disconnectBack.setVisibility(View.VISIBLE);});
    }

    public static void updateViewWhenFileSent()
    {
        ((Activity) context).runOnUiThread(() -> {
            textView_inf.setText(textView_inf.getText() + "\n");
            Toast.makeText(context, "File sent", Toast.LENGTH_SHORT).show();
            button_saveMeasurementData.setVisibility(View.VISIBLE);
            button_graph.setVisibility(View.VISIBLE);
        });
    }

    public static void updateViewWhenStartServerWifi()
    {
        ((Activity) context).runOnUiThread(() ->{
            textView_connected.setText("Connected as a Host");
            button_devices.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);
        });
    }

    public static void updateViewWhenStartClientWifi()
    {
        ((Activity) context).runOnUiThread(() ->{
            textView_connected.setText("Connected as a Client");
            button_devices.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_chooseFile.setVisibility(View.VISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);
        });
    }
}

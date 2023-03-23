package com.example.masterthesis.wifi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.masterthesis.Buffer;
import com.example.masterthesis.Constants;
import com.example.masterthesis.Logs;
import com.example.masterthesis.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientWiFi extends Thread{
    static final ArrayList<String> measurementDataList = new ArrayList<>();
    public static String fileSizeUnit = Constants.fileSizeUnitBytes;
    @SuppressLint("StaticFieldLeak")
    static TextView textView_percent, textView_inf, textView_qualitySignal, textView_qualitySignalText;
    @SuppressLint("StaticFieldLeak")
    static Button button_deviceDisplay, button_chooseFile, button_detect, button_saveMeasurementData, button_graph;
    @SuppressLint("StaticFieldLeak")
    static ProgressBar progressBar;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout layoutPercent;
    static InputStream inputStream;
    static OutputStream outputStream;
    static long fileSizeBytes;
    static Socket socket = new Socket();
    String serverAddress;
    int port;
    static Logs.ListLog LOG;
    @SuppressLint("StaticFieldLeak")
    static Context context;

    public ClientWiFi(Logs.ListLog LOG, Context context, WifiP2pInfo wifiDirectInfo, String portNumber) {
        ClientWiFi.LOG = LOG;
        ClientWiFi.context = context;
        this.serverAddress = wifiDirectInfo.groupOwnerAddress.getHostAddress();
        port = Integer.parseInt(portNumber);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(serverAddress,port),5000);

            TextView textView_connected = ((Activity) context).findViewById(R.id.textView_connected);
            ((Activity) context).runOnUiThread(() ->
                    textView_connected.setText("Connected as a client"));
            changeUiButtonShow();
            getNetworkSignal();
        } catch(IOException e) {
            LOG.addLog("Client socket creation error with host", e.getMessage());
        }
    }

    void changeUiButtonShow()
    {
        textView_percent = ((Activity) context).findViewById(R.id.textView_percent);
        textView_inf = ((Activity) context).findViewById(R.id.textView_inf);
        ((Activity) context).runOnUiThread(() ->textView_inf.setMovementMethod(new ScrollingMovementMethod()));
        textView_qualitySignal = ((Activity) context).findViewById(R.id.textView_qualitySignal);
        textView_qualitySignalText = ((Activity) context).findViewById(R.id.textView_qualitySignalText);
        button_deviceDisplay = ((Activity) context).findViewById(R.id.button_deviceDisplay);
        button_detect = ((Activity) context).findViewById(R.id.button_detect);
        button_chooseFile = ((Activity) context).findViewById(R.id.button_chooseFile);
        button_saveMeasurementData = ((Activity) context).findViewById(R.id.button_saveMeasurementData);
        button_graph = ((Activity) context).findViewById(R.id.button_graph);
        progressBar = ((Activity) context).findViewById(R.id.progressBar);
        layoutPercent = ((Activity) context).findViewById(R.id.layoutPercent);
        ((Activity) context).runOnUiThread(() ->{
            button_deviceDisplay.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_chooseFile.setVisibility(View.VISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);
            textView_qualitySignalText.setVisibility(View.VISIBLE);
        });
    }

    void getNetworkSignal()
    {
        new Thread(() ->{
            @SuppressLint("WifiManagerPotentialLeak")
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            do {
                if (wifiManager != null) {
                    int rssi = wifiManager.getConnectionInfo().getRssi();
                    int level = WifiManager.calculateSignalLevel(rssi, 100);
                    ((Activity) context).runOnUiThread(() -> textView_qualitySignal.setText(String.valueOf(level)));
                }
            }while (socket!=null);
        }).start();
    }

    public static Socket getSocket()
    {
        return socket;
    }

    @SuppressLint("SetTextI18n")
    public static void sendData(Intent fileToSend, long fileSizeBytes, String fileName, int multipleFile)
    {
        ClientWiFi.fileSizeBytes = fileSizeBytes;
        openStream();

        Uri uri = fileToSend.getData();
        double fileSize = getFileSize(fileSizeBytes);
        int bufferSize;
        if(Buffer.bufferSize == 0)
            bufferSize = (int) (fileSizeBytes * 0.1); //buffer size is 10% of file size in bytes
        else
            bufferSize = Buffer.bufferSize;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        try {
            measurementDataList.add(fileName);
            measurementDataList.add(Constants.titleWiFiFileColumn);

            String fileDetails = fileName + ";" + fileSizeUnit + ";" + fileSizeBytes + ";" +
                    bufferSize + ";" + multipleFile;
            outputStream.write(fileDetails.getBytes());
            outputStream.flush();


            byte[] confirmBuffer = new byte[Constants.confirmBufferBytes];
            try {
                int bytesLoad = inputStream.read(confirmBuffer);
                String confirmMessage = new String(confirmBuffer, 0, bytesLoad);

                if (confirmMessage.equals("Confirmed")) {
                    LOG.addLog("Sending file information");
                } else {
                    LOG.addLog("Error sending file information");
                }
            } catch (IOException e) {
                LOG.addLog("Failed to create stream to receive message whether file information was delivered", e.getMessage());
            }
            Arrays.fill(confirmBuffer, 0, confirmBuffer.length, (byte) 0);


            @SuppressLint("Recycle")
            FileInputStream file = (FileInputStream) context.getContentResolver().openInputStream(uri);

            for(int repeat = 0 ; repeat < multipleFile; repeat++)
            {
                long fullBytes = 0;
                long startTime = System.currentTimeMillis();
                try {
                    while ((bytesRead = file.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, bytesRead);
                        fullBytes += bytesRead;
                        long percent = 100 * (fullBytes + fileSizeBytes * repeat) /
                                (fileSizeBytes * multipleFile);
                        ((Activity) context).runOnUiThread(() -> textView_percent.setText("Sent: " + percent + " %"));
                        progressBar.setProgress((int) percent);
                    }
                    outputStream.flush();
                    file.getChannel().position(0);
                    LOG.addLog("End of file upload number " + (repeat+1));
                    Arrays.fill(buffer, 0, buffer.length, (byte) 0);

                    try {
                        while (true) {
                            int bytesLoad = inputStream.read(confirmBuffer);
                            String confirmMessage = new String(confirmBuffer, 0, bytesLoad);

                            if (confirmMessage.equals("Confirmed")) {
                                long endTime = System.currentTimeMillis();
                                double resultTime = (double) (endTime - startTime) / 1000; //time change ms to s
                                double speedSend = fileSize / resultTime;
                                String sizeUnit = setSpeedSendUnit(speedSend);
                                updateTextInf(bufferSize,repeat,resultTime,speedSend,sizeUnit);
                                saveMeasurementDataToList(repeat,fileSize,resultTime,speedSend);
                                break;
                            } else if (confirmMessage.equals("NoneConfirmed")) {
                                LOG.addLog("Failed to save to the server");
                                ((Activity) context).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() + "\nFailed to save to the server"));
                                return;
                            }
                            Arrays.fill(confirmBuffer, 0, confirmBuffer.length, (byte) 0);
                        }
                    } catch (IOException e) {
                        LOG.addLog("Failed to create stream to receive message whether file was delivered", e.getMessage());
                    }
                } catch (IOException e) {
                    LOG.addLog("Failed to send file", e.getMessage());
                }
            }
            updateView();
            try {
                if (file != null) {
                    file.close();
                    LOG.addLog("Stream to file closed");
                }
            } catch (IOException e) {
                LOG.addLog("Failed to close stream to file", e.getMessage());
            }
        }
        catch (IOException e)
        {
            LOG.addLog("Failed to send basic file information", e.getMessage());
        }
    }

    static void openStream()
    {
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            LOG.addLog("Stream error", e.getMessage());
        }
    }

    public static void closeStream()
    {
        try{
            if(inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();
        } catch (IOException e) {
            LOG.addLog("Error close stream", e.getMessage());
        }
    }

    @SuppressLint("Range")
    public static double getFileSize(long fileSizeBytes)
    {
        double fileSize = fileSizeBytes;
        if(fileSize > Constants.size1Kb) {
            fileSize /= Constants.size1Kb;
            fileSizeUnit = Constants.fileSizeUnitKB;
            if(fileSize > Constants.size1Kb) {
                fileSize /= Constants.size1Kb;
                fileSizeUnit = Constants.fileSizeUnitMB;
            }
        }
        return fileSize;
    }

    static String setSpeedSendUnit(double speedSend)
    {
        String sizeUnit = fileSizeUnit;
        if(fileSizeUnit.equals(Constants.fileSizeUnitBytes) && speedSend > Constants.size1Kb) {
            speedSend /= Constants.size1Kb;
            sizeUnit = Constants.fileSizeUnitKB;
            if(speedSend > Constants.size1Kb) {
                sizeUnit = Constants.fileSizeUnitMB;
            }
        }
        else if (fileSizeUnit.equals(Constants.fileSizeUnitKB) && speedSend > Constants.size1Kb)
        {
            sizeUnit = Constants.fileSizeUnitMB;
        }
        return sizeUnit;
    }

    @SuppressLint("SetTextI18n")
    static void updateTextInf(int bufferSize, int repeat, double resultTime, double speedSend, String sizeUnit)
    {
        ((Activity) context).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() +
                "\nThe size of the set buffer: " +
                bufferSize + " Bytes" +
                "\nFile upload number: " +
                (repeat + 1) +
                "\nFile transfer time: " +
                Constants.decimalFormat.format(resultTime).replace(",", ".") + " s" +
                "\nUpload speed is: " +
                Constants.decimalFormat.format(speedSend).replace(",", ".") +
                " " + sizeUnit + "/s"));
    }

    static void saveMeasurementDataToList(int repeat, double fileSize, double resultTime, double speedSend)
    {
        measurementDataList.add((repeat + 1) + "," +
                fileSizeBytes + "," +
                Constants.decimalFormat.format(fileSize).replace(",", ".") + "," +
                textView_qualitySignal.getText() + "," +
                Constants.decimalFormat.format(resultTime).replace(",", ".") + "," +
                Constants.decimalFormat.format(speedSend).replace(",", "."));
    }

    @SuppressWarnings("SameReturnValue")
    public static ArrayList<String> getMeasurementDataList(){
        LOG.addLog("measurementDataList");
        LOG.addLog(String.valueOf(measurementDataList));
        return measurementDataList; }

    public static void saveMeasurementData(){

        AlertDialog.Builder viewToSaveData = new AlertDialog.Builder(context);
        viewToSaveData.setTitle(Constants.titleDialogToSaveData);
        EditText editText = new EditText(context);
        viewToSaveData.setView(editText);
        editText.requestFocus();

        viewToSaveData.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        viewToSaveData.setPositiveButton("OK", (dialog, which) -> {
            String dataFileName = String.valueOf(editText.getText());

            if(!dataFileName.isEmpty()) {
                File file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        dataFileName + ".csv");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);

                    for (String data : measurementDataList) {
                        osw.write(data);
                        osw.write("\n");
                    }

                    osw.close();
                    fos.close();

                    if (!measurementDataList.isEmpty()) {
                        measurementDataList.clear();
                    }

                    ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "The data has been saved", Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    LOG.addLog("Error writing measurement data to the file", e.getMessage());
                }
            }
            else{
                ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Enter the name of the file to save", Toast.LENGTH_SHORT).show());
            }
        });
        viewToSaveData.show();
    }

    @SuppressLint("SetTextI18n")
    static void updateView() {
        ((Activity) context).runOnUiThread(() -> {
            textView_inf.setText(textView_inf.getText() + "\n");
            Toast.makeText(context, "File sent", Toast.LENGTH_SHORT).show();
            button_saveMeasurementData.setVisibility(View.VISIBLE);
            button_graph.setVisibility(View.VISIBLE);
        });
    }
}
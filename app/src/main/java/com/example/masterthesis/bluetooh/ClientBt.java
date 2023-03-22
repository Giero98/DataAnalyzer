package com.example.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.masterthesis.Constants;
import com.example.masterthesis.Logs;
import com.example.masterthesis.R;
import com.example.masterthesis.Buffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientBt extends Thread {
    final Button button_chooseFile, button_foundDevice, button_detect, button_disconnectBack,
            button_saveMeasurementData, button_graph;
    final TextView textView_connected, textView_inf, textView_percent, textView_qualitySignal;
    final LinearLayout parameterLayoutForFileUpload, layoutPercent;
    final ProgressBar progressBar;
    static boolean dataSendFromClient = false, running;
    @SuppressLint("StaticFieldLeak")
    static Context BT;
    static BluetoothSocket socket;
    final BluetoothDevice device;
    static Intent fileToSend;
    static int multipleFile;
    static Logs.ListLog LOG;
    long fileSizeBytes;
    public static String fileSizeUnit = Constants.fileSizeUnitBytes , deviceName;
    OutputStream outputStream;
    static final ArrayList<String> measurementDataList = new ArrayList<>();

    public ClientBt(Context BT, BluetoothDevice device, Logs.ListLog LOG)
    {
        ClientBt.BT = BT;
        this.device = device;
        ClientBt.LOG = LOG;

        textView_connected = ((Activity) BT).findViewById(R.id.textView_connected);
        textView_inf = ((Activity) BT).findViewById(R.id.textView_inf);
        textView_inf.setMovementMethod(new ScrollingMovementMethod());
        textView_percent = ((Activity) BT).findViewById(R.id.textView_percent);
        textView_qualitySignal = ((Activity) BT).findViewById(R.id.textView_qualitySignal);
        button_chooseFile = ((Activity) BT).findViewById(R.id.button_chooseFile);
        button_foundDevice = ((Activity) BT).findViewById(R.id.button_foundDevice);
        button_detect = ((Activity) BT).findViewById(R.id.button_detect);
        button_disconnectBack = ((Activity) BT).findViewById(R.id.button_disconnectAndBack);
        button_saveMeasurementData = ((Activity) BT).findViewById(R.id.button_saveMeasurementData);
        button_graph = ((Activity) BT).findViewById(R.id.button_graph);
        parameterLayoutForFileUpload = ((Activity) BT).findViewById(R.id.parameterLayoutForFileUpload);
        layoutPercent = ((Activity) BT).findViewById(R.id.layoutPercent);
        progressBar = ((Activity) BT).findViewById(R.id.progressBar);
    }

    @SuppressLint("MissingPermission")
    public void run() {
        LOG.addLog("A client has started");
        try {
            socket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
        } catch (IOException e) {
            LOG.addLog("Socket's create() method failed", e.getMessage());
            return;
        }
        deviceName = device.getName();
        Constants.bluetoothAdapter.cancelDiscovery();
        try {
            socket.connect();
        } catch (IOException e) {
            LOG.addLog("Unable to connect", e.getMessage());
            closeSocketClient();
            return;
        }
        LOG.addLog("The connection attempt succeeded");
        changeViewOfApp();

        if(sendNameDevice()) {
            while (running) {
                if (dataSendFromClient)
                    sendData();
                if(!socket.isConnected()) {
                    closeSocketClient();
                    ((Activity) BT).runOnUiThread(() -> Toast.makeText(BT, "Disconnected", Toast.LENGTH_SHORT).show());
                    break;
                }
            }
        }
    }
    void closeSocketClient() {
        try {
            socket.close();
            LOG.addLog("Client socket closed");
        } catch (IOException e) {
            LOG.addLog("Could not close the client socket", e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    void changeViewOfApp()
    {
        ((Activity) BT).runOnUiThread(() -> {
            textView_connected.setText("Connected as a client with\n" + deviceName);
            button_chooseFile.setVisibility(View.VISIBLE);
            button_foundDevice.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_disconnectBack.setText("Disconnect");
            parameterLayoutForFileUpload.setVisibility(View.VISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);});

    }
    @SuppressLint("MissingPermission")
    private boolean sendNameDevice()
    {
        try {
            outputStream = socket.getOutputStream();
            try {
                @SuppressLint("HardwareIds")
                String deviceInfo = Constants.bluetoothAdapter.getName();
                outputStream.write(deviceInfo.getBytes());
                outputStream.flush();
                LOG.addLog("Device name sent");
                return true;
            }
            catch (IOException e) {
                LOG.addLog("Failed to send device name", e.getMessage());
                return false;
            }
        } catch (IOException ex) {
            LOG.addLog("Failed to create stream to send message", ex.getMessage());
            return false;
        }
    }

    @SuppressLint({"SetTextI18n", "Range", "Recycle"})
    void sendData()
    {
        Uri uri = fileToSend.getData();
        double fileSize = getFileSize(uri);
        int bufferSize;
        if(Buffer.bufferSize == 0)
            bufferSize = (int) (fileSizeBytes * 0.1); //buffer size is 10% of file size in bytes
        else
            bufferSize = Buffer.bufferSize;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        String fileName = getFileName(uri);

        try {
            measurementDataList.add(fileName);
            measurementDataList.add(Constants.titleFileColumn);

            String fileDetails = fileName + ";" + fileSizeUnit + ";" + fileSizeBytes + ";" +
                              bufferSize + ";" + multipleFile;
            outputStream.write(fileDetails.getBytes());
            outputStream.flush();
            LOG.addLog("Sending file information");

            InputStream inputStream = socket.getInputStream();
            FileInputStream file = (FileInputStream) BT.getContentResolver().openInputStream(uri);

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
                        ((Activity) BT).runOnUiThread(() -> textView_percent.setText("Sent: " + percent + " %"));
                        progressBar.setProgress((int) percent);
                    }
                    outputStream.flush();
                    file.getChannel().position(0);
                    LOG.addLog("End of file upload number " + (repeat+1));
                    Arrays.fill(buffer, 0, buffer.length, (byte) 0);

                    try {
                        byte[] confirmBuffer = new byte[Constants.confirmBufferBytes];
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
                                ((Activity) BT).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() + "\nFailed to save to the server"));
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
            updateBtView();
            dataSendFromClient = false;
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

    @SuppressLint("Range")
    public double getFileSize(Uri uri)
    {
        File file = new File(uri.getPath());
        double fileSize = 0;
        try (Cursor cursor = BT.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileSize = cursor.getDouble(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        if(fileSize == 0)
            fileSize = file.length();
        fileSizeBytes = (long) fileSize;
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

    @SuppressLint("Range")
    public String getFileName(Uri uri)
    {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = BT.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if(fileName != null) {
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    String setSpeedSendUnit(double speedSend)
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
    void updateTextInf(int bufferSize, int repeat, double resultTime, double speedSend, String sizeUnit)
    {
        ((Activity) BT).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() +
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

    void saveMeasurementDataToList(int repeat, double fileSize, double resultTime, double speedSend)
    {
        measurementDataList.add((repeat + 1) + "," +
                fileSizeBytes + "," +
                Constants.decimalFormat.format(fileSize).replace(",", ".") + "," +
                textView_qualitySignal.getText() + "," +
                Constants.decimalFormat.format(resultTime).replace(",", ".") + "," +
                Constants.decimalFormat.format(speedSend).replace(",", "."));
    }

    @SuppressLint("SetTextI18n")
    void updateBtView()
    {
        ((Activity) BT).runOnUiThread(() -> {
            textView_inf.setText(textView_inf.getText() + "\n");
            Toast.makeText(BT, "File sent", Toast.LENGTH_SHORT).show();
            button_saveMeasurementData.setVisibility(View.VISIBLE);
            button_graph.setVisibility(View.VISIBLE);
        });
    }
    public static String getFileSizeUnit()
    {
        return fileSizeUnit;
    }

    public static void dataSendFromClient(Boolean verity, Intent data, int numberMultipleFile)
    {
        dataSendFromClient = verity;
        fileToSend = data;
        multipleFile = numberMultipleFile;
    }

    public static BluetoothSocket getSocket()
    {
        return socket;
    }

    @SuppressWarnings("SameReturnValue")
    public static ArrayList<String> getMeasurementDataList(){ return measurementDataList; }
    
    public static void saveMeasurementData(){

        AlertDialog.Builder viewToSaveData = new AlertDialog.Builder(BT);
        viewToSaveData.setTitle(Constants.titleDialogToSaveData);
        EditText editText = new EditText(BT);
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

                            ((Activity) BT).runOnUiThread(() -> Toast.makeText(BT, "The data has been saved", Toast.LENGTH_SHORT).show());
                        } catch (IOException e) {
                            LOG.addLog("Error writing measurement data to the file", e.getMessage());
                        }
                    }
                    else{
                        ((Activity) BT).runOnUiThread(() -> Toast.makeText(BT, "Enter the name of the file to save", Toast.LENGTH_SHORT).show());
                    }
        });
        viewToSaveData.show();
    }
}

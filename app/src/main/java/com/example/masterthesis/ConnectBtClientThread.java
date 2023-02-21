package com.example.masterthesis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

public class ConnectBtClientThread extends Thread {


    public Button button_chooseFile, button_foundDevice, button_detect, button_disconnectBack,
            button_saveMeasurementData, button_graph;
    public TextView textView_connected, textView_inf, textView_percent;
    public LinearLayout linearSpinner;
    public ProgressBar progressBar;

    //A variable stating whether the file has been selected for upload
    public static boolean dataSendFromClient = false;
    private final Context BT;
    private BluetoothSocket socketClient;
    public static BluetoothSocket socketClientStatic;
    public BluetoothDevice device;
    //Intent to file
    public static Intent fileToSend;

    public static int multipleFile;

    //Log class reference
    public final MainActivity_Log.ListLog LOG;
    private long fileSizeBytes; //file size in bytes
    private String fileSizeUnit = "Bytes" , deviceName;
    private OutputStream outputStream;

    //ConnectBtClientThread class constructor
    public ConnectBtClientThread(Context BT, BluetoothDevice device, BluetoothSocket socketClient,
                                 MainActivity_Log.ListLog LOG) {
        this.BT = BT;
        this.device = device;
        this.socketClient = socketClient;
        this.LOG = LOG;


        textView_connected = ((Activity) BT).findViewById(R.id.textView_connected);
        textView_inf = ((Activity) BT).findViewById(R.id.textView_inf);
        textView_percent = ((Activity) BT).findViewById(R.id.textView_percent);
        button_chooseFile = ((Activity) BT).findViewById(R.id.button_chooseFile);
        button_foundDevice = ((Activity) BT).findViewById(R.id.button_foundDevice);
        button_detect = ((Activity) BT).findViewById(R.id.button_detect);
        button_disconnectBack = ((Activity) BT).findViewById(R.id.button_disconnectBack);
        button_saveMeasurementData = ((Activity) BT).findViewById(R.id.button_saveMeasurementData);
        button_graph = ((Activity) BT).findViewById(R.id.button_graph);
        linearSpinner = ((Activity) BT).findViewById(R.id.linearSpinner);
        progressBar = ((Activity) BT).findViewById(R.id.progressBar);

        textView_inf.setMovementMethod(new ScrollingMovementMethod());
    }

    //A method that is run when the start() method is called on an object representing a thread
    @SuppressLint({"MissingPermission", "SetTextI18n"})
    public void run() {
        LOG.addLog(new Date(System.currentTimeMillis()),"A client thread has started");
        try {
            socketClient = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
        } catch (IOException e) {
            LOG.addLog(LOG.currentDate(),"Socket's create() method failed", e.getMessage());
            return;
        }
        deviceName = device.getName();

        socketClientStatic = socketClient;

        // Cancel discovery because it otherwise slows down the connection.
        Constants.bluetoothAdapter.cancelDiscovery();
        try {
            // Connection to the device via the socket. This call blocks until it succeeds or throws an exception
            socketClient.connect();
        } catch (IOException e) {
            // Unable to connect, close the socket and return.
            LOG.addLog(LOG.currentDate(),"Unable to connect", e.getMessage());
            closeSocketClient();
            return;
        }
        LOG.addLog(LOG.currentDate(),"The connection attempt succeeded");
        //runOnUiThread() Used to run code on the main UI thread.
        ((Activity) BT).runOnUiThread(() -> {
            textView_connected.setText("Connected as a client with\n" + deviceName);
            button_chooseFile.setVisibility(View.VISIBLE);
            button_foundDevice.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_disconnectBack.setText("Disconnect");
            linearSpinner.setVisibility(View.VISIBLE);});
        if(sendNameDevice()) {
            //keep looping until the thread is stopped.
            while (!interrupted()) {
                if (dataSendFromClient)
                    sendData();
                if(!socketClient.isConnected()) {
                    closeSocketClient();
                    ((Activity) BT).runOnUiThread(() -> Toast.makeText(BT, "Disconnected", Toast.LENGTH_SHORT).show());
                    break;
                }
            }
        }
    }
    // Closes the client socket and causes the thread to finish.
    private void closeSocketClient() {
        try {
            socketClient.close();
            LOG.addLog(LOG.currentDate(),"Client socket closed");
        } catch (IOException e) {
            LOG.addLog(LOG.currentDate(),"Could not close the client socket", e.getMessage());
        }
    }

    //The method where the device name is sent
    @SuppressLint("MissingPermission")
    private boolean sendNameDevice()
    {
        try {
            outputStream = socketClient.getOutputStream();
            try {
                outputStream.write(Constants.bluetoothAdapter.getName().getBytes());
                outputStream.flush(); //flush() is used to push out all written bytes
                LOG.addLog(LOG.currentDate(),"Device name sent");
                return true;
            }
            catch (IOException e) {
                LOG.addLog(LOG.currentDate(),"Failed to send device name", e.getMessage());
                return false;
            }
        } catch (IOException ex) {
            LOG.addLog(LOG.currentDate(),"Failed to create stream to send message", ex.getMessage());
            return false;
        }
    }

    //The method where the file data and the file itself are sent
    @SuppressLint({"SetTextI18n", "Range", "Recycle"})
    private void sendData()
    {
        Uri uri = fileToSend.getData();
        double fileSize = getFileSize(uri);

        int bufferSize;
        if(SpinnerCLass.bufferSize == 0)
            bufferSize = (int) (fileSizeBytes * 0.1); //buffer size is 10% of file size in bytes
        else
            bufferSize = SpinnerCLass.bufferSize; //buffer size is 4/8/16/32/64/128/256KB

        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        String fileName = getFileName(uri);
        try {
            //Sending file information
            String fileData = fileName + ";" + fileSizeUnit + ";" + fileSizeBytes + ";"
                            + bufferSize + ";" + multipleFile;
            outputStream.write(fileData.getBytes());
            outputStream.flush();
            LOG.addLog(LOG.currentDate(), "Sending file information");
            FileInputStream file = null;
            InputStream inputStream = socketClient.getInputStream();
            long fullBytes = 0;
            for(int repeat = 0 ; repeat < multipleFile; repeat++)
            {
                //start counting the transfer time
                long startTime = System.currentTimeMillis();
                try {
                    file = (FileInputStream) BT.getContentResolver().openInputStream(uri);
                    //A loop that sends a file and displays the progress percentage
                    while ((bytesRead = file.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, bytesRead);
                        fullBytes += bytesRead;
                        int percent = ((int) (fullBytes * 100.0) / (int) fileSizeBytes) / multipleFile;
                        ((Activity) BT).runOnUiThread(() -> textView_percent.setText("Sent: " + percent + " %"));
                        progressBar.setProgress(percent);
                    }
                    outputStream.flush();
                    LOG.addLog(LOG.currentDate(), "Data file sent");
                    Arrays.fill(buffer, 0, buffer.length, (byte) 0); //clearing the buffer

                    try {
                        byte[] confirmBuffer = new byte[Constants.confirmBufferBytes];

                        //A loop in which it expects the server to confirm receipt of the file
                        while (true) {
                            int bytesLoad = inputStream.read(confirmBuffer);
                            String confirmMessage = new String(confirmBuffer, 0, bytesLoad);

                            if (confirmMessage.equals("Confirmed")) {
                                //end of upload time counting
                                long endTime = System.currentTimeMillis();
                                ((Activity) BT).runOnUiThread(() -> Toast.makeText(BT, "File sent", Toast.LENGTH_SHORT).show());
                                double resultTime = (double) (endTime - startTime) / 1000; //time change ms to s
                                double speedSend = fileSize / resultTime;
                                String sizeUnit = setSpeedSendUnit(speedSend);
                                ((Activity) BT).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() + "\nFile transfer time: " +
                                        Constants.decimalFormat.format(resultTime) + "\nUpload speed is: " +
                                        Constants.decimalFormat.format(speedSend) + " " + sizeUnit + "/s"));
                                Arrays.fill(confirmBuffer, 0, confirmBuffer.length, (byte) 0);
                                dataSendFromClient = false;
                                break;
                            } else if (confirmMessage.equals("NoneConfirmed")) {
                                LOG.addLog(LOG.currentDate(), "Failed to save to the server");
                                ((Activity) BT).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() + "\nFailed to save to the server"));
                                Arrays.fill(confirmBuffer, 0, confirmBuffer.length, (byte) 0);
                                dataSendFromClient = false;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        LOG.addLog(LOG.currentDate(), "Failed to create stream to receive message whether file was delivered", e.getMessage());
                    }
                } catch (IOException e) {
                    LOG.addLog(LOG.currentDate(), "Failed to send file", e.getMessage());
                }
            }
            ((Activity) BT).runOnUiThread(() -> {
                Toast.makeText(BT, "File sent", Toast.LENGTH_SHORT).show();
                button_saveMeasurementData.setVisibility(View.VISIBLE);
                button_graph.setVisibility(View.VISIBLE);
            });
            try {
                if (file != null) {
                    file.close();
                    LOG.addLog(LOG.currentDate(), "Stream to file closed");
                }
            } catch (IOException e) {
                LOG.addLog(LOG.currentDate(), "Failed to close stream to file", e.getMessage());
            }
        }
        catch (IOException e)
        {
            LOG.addLog(LOG.currentDate(),"Failed to send basic file information", e.getMessage());
        }
    }

    //method where the filename is retrieved
    @SuppressLint({"Range", "SetTextI18n"})
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

    //method where the file size is taken and then converted to the appropriate size
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
        if(fileSize > 1024) {
            fileSize /= 1024; //to KB
            fileSizeUnit = "KB";
            if(fileSize > 1024) {
                fileSize /= 1024; //to MB
                fileSizeUnit = "MB";
            }
        }
        return fileSize;
    }

    public String getFileSizeUnit()
    {
        return fileSizeUnit;
    }

    //method where the size of the variable data transfer rate is converted
    @SuppressWarnings("UnusedAssignment")
    private String setSpeedSendUnit(double speedSend)
    {
        String sizeUnit = fileSizeUnit;
        if(fileSizeUnit.equals("Bytes") && speedSend > 1024) {
            speedSend /= 1024; //to KB
            sizeUnit = "KB";
            if(speedSend > 1024) {
                speedSend /= 1024; //to MB
                sizeUnit = "MB";
            }
        }
        else if (fileSizeUnit.equals("KB") && speedSend >1024)
        {
            speedSend /= 1024; //to MB
            sizeUnit = "MB";
        }
        return sizeUnit;
    }

    //The method to get whether the file has been selected
    public static void dataSendFromClient(Boolean verity, Intent data, int numberMultipleFile)
    {
        dataSendFromClient = verity;
        fileToSend = data;
        multipleFile = numberMultipleFile;
    }

    public static BluetoothSocket getSocketClient()
    {
        return socketClientStatic;
    }
}

package com.example.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masterthesis.Constants;
import com.example.masterthesis.Logs;
import com.example.masterthesis.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ServerBt extends Thread {
    final Button button_foundDevice, button_detect, button_disconnectBack;
    final TextView textView_connected, textView_inf, textView_percent;
    final LinearLayout layoutPercent;
    final ProgressBar progressBar;
    static boolean running;
    final Logs.ListLog LOG;
    final Context BT;
    static BluetoothSocket socketServer;
    BluetoothServerSocket serverSocket;
    String fileName;

    public ServerBt(Context BT, Logs.ListLog LOG)
    {
        this.BT = BT;
        this.LOG = LOG;


        textView_connected = ((Activity) BT).findViewById(R.id.textView_connected);
        textView_inf = ((Activity) BT).findViewById(R.id.textView_inf);
        textView_inf.setMovementMethod(new ScrollingMovementMethod());
        textView_percent = ((Activity) BT).findViewById(R.id.textView_percent);
        button_foundDevice = ((Activity) BT).findViewById(R.id.button_foundDevice);
        button_detect = ((Activity) BT).findViewById(R.id.button_detect);
        button_disconnectBack = ((Activity) BT).findViewById(R.id.button_disconnectAndBack);
        layoutPercent = ((Activity) BT).findViewById(R.id.layoutPercent);
        progressBar = ((Activity) BT).findViewById(R.id.progressBar);
    }

    @SuppressLint({"SetTextI18n", "MissingPermission"})
    public void run() {
        LOG.addLog("A server thread has started listening");
        try {
            serverSocket = Constants.bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME, Constants.MY_UUID);
        } catch (IOException e) {
            LOG.addLog("Socket's listen() method failed", e.getMessage());
        }

        try {
            socketServer = serverSocket.accept();
        } catch (IOException e) {
            LOG.addLog("Socket's accept() method failed", e.getMessage());
        }
        if (socketServer != null) {
            LOG.addLog("The connection attempt succeeded");
            try {
                InputStream inputStream = socketServer.getInputStream();
                getData(inputStream);
                if(!socketServer.isConnected())
                    try {
                        updateTextWhenDisconnected();
                        running = false;
                        inputStream.close();
                        socketServer.close();
                    } catch (IOException ex) {
                        LOG.addLog("Error closing input stream and socket's", ex.getMessage());
                    }
            } catch (IOException e) {
                LOG.addLog("Failed to create stream to write data", e.getMessage());
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOG.addLog("Error closing output stream:", e.getMessage());
            }
        }
    }

    //The method where the file is downloaded and saved
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    private void getData(InputStream inputStream)
    {
        byte[] buffer = new byte[Constants.getBufferFirstInfOfFile];
        int bytes;

        try {
            bytes = inputStream.read(buffer);
            String deviceName = new String(buffer, 0, bytes);
            updateBtView(deviceName);
            Arrays.fill(buffer, 0, buffer.length, (byte) 0);

            try {
                OutputStream outputStream = socketServer.getOutputStream();

                while(running) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            
                            String fileDetails = new String(buffer, 0, bytes);
                            String[] dataArrayFileDetails = fileDetails.split(";");
                            fileName = dataArrayFileDetails[0];
                            String fileSizeUnit = dataArrayFileDetails[1];
                            String fileSizeString = dataArrayFileDetails[2];
                            String bufferSizeString = dataArrayFileDetails[3];
                            String multipleFileString = dataArrayFileDetails[4];
                            LOG.addLog("File information is being retrieved");
                            Arrays.fill(buffer, 0, buffer.length, (byte) 0);

                            long fileSizeBytes = Long.parseLong(fileSizeString);
                            int multipleFile = Integer.parseInt(multipleFileString);
                            int bufferSize = Integer.parseInt(bufferSizeString);

                            for (int repeat = 0; repeat < multipleFile; repeat++)
                            {
                                String confirmMessage;
                                FileOutputStream fileToSave = null;
                                long fullBytes = 0;
                                try {
                                    fileToSave = new FileOutputStream(setFilePlace());
                                    LOG.addLog("The file name has been set");
                                    byte[] bufferData = new byte[bufferSize];

                                    while ((bytes = inputStream.read(bufferData)) > 0) {
                                        fileToSave.write(bufferData, 0, bytes);
                                        fullBytes += bytes;
                                        long percent = 100 * (fullBytes + fileSizeBytes * repeat) /
                                                (fileSizeBytes * multipleFile);
                                        ((Activity) BT).runOnUiThread(() -> textView_percent.setText("Download: " + percent + " %"));
                                        progressBar.setProgress((int) percent);
                                        if (fullBytes == fileSizeBytes)
                                        {
                                            LOG.addLog("End of download file number " + (repeat+1));
                                            Arrays.fill(bufferData, 0, bufferData.length, (byte) 0);
                                            break;
                                        }
                                    }
                                    fileToSave.flush();
                                    LOG.addLog("The file has been downloaded and saved");
                                    confirmMessage = "Confirmed";
                                } catch (IOException e) {
                                    LOG.addLog("Error downloaded and saving file", e.getMessage());
                                    confirmMessage = "NoneConfirmed";
                                } finally {
                                    try {
                                        if (fileToSave != null) {
                                            fileToSave.close();
                                            LOG.addLog("Stream to file closed");
                                        }
                                    } catch (IOException e) {
                                        LOG.addLog("Error closing output stream:", e.getMessage());
                                    }
                                }
                                outputStream.write(confirmMessage.getBytes());
                                outputStream.flush();
                                LOG.addLog("Sending response to download and save file");

                                if (confirmMessage.equals("Confirmed")) {
                                    updateTextInf(fileName,fileSizeBytes,fileSizeUnit);
                                } else {
                                    ((Activity) BT).runOnUiThread(() -> textView_inf.setText("Error downloaded and saving file"));
                                    break;
                                }
                            }
                            ((Activity) BT).runOnUiThread(() -> Toast.makeText(BT, "Downloaded file", Toast.LENGTH_SHORT).show());
                        }
                    } catch (IOException e) {
                        LOG.addLog("The data could not be loaded", e.getMessage());
                        break;
                    }
                }
                outputStream.close();
            } catch (IOException e) {
                LOG.addLog("Failed to create stream to send data",e.getMessage());
            }
        } catch (IOException e) {
            LOG.addLog("The first data could not be loaded",e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    void updateBtView(String deviceName)
    {
        ((Activity) BT).runOnUiThread(() -> {
            textView_connected.setText("Connected as a server with\n" + deviceName);
            textView_percent.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            button_foundDevice.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_disconnectBack.setVisibility(View.INVISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);});
    }

    File setFilePlace()
    {
        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        "/" + fileName);
        int i = 1;
        while (file.exists()) {
            int dotIndex = fileName.lastIndexOf(".");
            String baseName = fileName.substring(0, dotIndex);
            String extension = fileName.substring(dotIndex);
            //Check if there is a file with the same name without a trailing number
            if (baseName.matches(".*\\(\\d+\\)$")) {
                int lastOpenParenIndex = baseName.lastIndexOf("(");
                String baseNameWithoutNumber = baseName.substring(0, lastOpenParenIndex);
                baseName = baseNameWithoutNumber + "(" + i + ")";
            } else {
                baseName = baseName + "(" + i + ")";
            }
            fileName = baseName + extension;
            file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                            "/" + fileName);
            i++;
        }
        return file;
    }

    @SuppressLint("SetTextI18n")
    void updateTextInf(String fileName, long fileSizeBytes, String fileSizeUnit)
    {
        double fileSize = conversionFileSize(fileSizeBytes, fileSizeUnit);
        ((Activity) BT).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() +
                "The name of the received file: " + fileName +
                "\nFile size: " +
                Constants.decimalFormat.format(fileSize).replace(",", ".") +
                " " + fileSizeUnit + "\n\n"));
    }

    double conversionFileSize(long fileSizeLong, String fileUnit)
    {
        double fileSize = (double) fileSizeLong;
        switch (fileUnit) {
            case Constants.fileSizeUnitMB:
                fileSize /= Constants.size1Kb; //to KB
                fileSize /= Constants.size1Kb; //to MB
                break;
            case Constants.fileSizeUnitKB:
                fileSize /= Constants.size1Kb; //to KB
                break;
        }
        return fileSize;
    }

    @SuppressLint("SetTextI18n")
    void updateTextWhenDisconnected()
    {
        ((Activity) BT).runOnUiThread(() -> {
            textView_connected.setText("Disconnected");
            Toast.makeText(BT, "Disconnected", Toast.LENGTH_SHORT).show();
            button_disconnectBack.setText("Back");
            button_disconnectBack.setVisibility(View.VISIBLE);});
    }

    public static BluetoothSocket getSocketServer() {return socketServer;}
}

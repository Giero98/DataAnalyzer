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
import com.example.masterthesis.MainActivity_Log;
import com.example.masterthesis.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

public class ConnectBtServerThread extends Thread {

    public Button button_foundDevice, button_detect, button_disconnectBack;
    public TextView textView_connected, textView_inf, textView_percent;
    public LinearLayout linearPercent;
    public ProgressBar progressBar;

    public final MainActivity_Log.ListLog LOG;
    private final Context BT;
    private BluetoothSocket socketServer;
    private BluetoothServerSocket serverSocket;

    //variable containing the name of the downloaded file
    private String fileName;

    //ConnectBtServerThread class constructor
    public ConnectBtServerThread(Context BT, BluetoothSocket socketServer, MainActivity_Log.ListLog LOG)
    {
        this.BT = BT;
        this.socketServer = socketServer;
        this.LOG = LOG;


        textView_connected = ((Activity) BT).findViewById(R.id.textView_connected);
        textView_inf = ((Activity) BT).findViewById(R.id.textView_inf);
        textView_percent = ((Activity) BT).findViewById(R.id.textView_percent);
        button_foundDevice = ((Activity) BT).findViewById(R.id.button_foundDevice);
        button_detect = ((Activity) BT).findViewById(R.id.button_detect);
        button_disconnectBack = ((Activity) BT).findViewById(R.id.button_disconnectBack);
        linearPercent = ((Activity) BT).findViewById(R.id.linearPercent);
        progressBar = ((Activity) BT).findViewById(R.id.progressBar);

        textView_inf.setMovementMethod(new ScrollingMovementMethod());
    }
    //A method that is run when the start() method is called on an object representing a thread
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    public void run() {
        LOG.addLog(new Date(System.currentTimeMillis()),"A server thread has started listening");
        try {
            serverSocket = Constants.bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME, Constants.MY_UUID);
        } catch (IOException e) {
            LOG.addLog(LOG.currentDate(),"Socket's listen() method failed", e.getMessage());
        }

        // Keep listening until exception occurs or a socket is returned.
        try {
            socketServer = serverSocket.accept();
        } catch (IOException e) {
            LOG.addLog(LOG.currentDate(),"Socket's accept() method failed", e.getMessage());
        }
        if (socketServer != null) {
            LOG.addLog(LOG.currentDate(),"The connection attempt succeeded");
            try {
                InputStream inputStream = socketServer.getInputStream();
                getData(inputStream);

                //if the connection is broken
                if(!socketServer.isConnected()) {
                    try {
                        ((Activity) BT).runOnUiThread(() -> {
                            textView_connected.setText("Disconnected");
                            Toast.makeText(BT, "Disconnected", Toast.LENGTH_SHORT).show();
                            button_disconnectBack.setText("Back");
                            button_disconnectBack.setVisibility(View.VISIBLE);});
                        inputStream.close();
                        socketServer.close();
                    } catch (IOException ex) {
                        LOG.addLog(LOG.currentDate(),"Error closing input stream and socket's", ex.getMessage());
                    }
                }
            } catch (IOException e) {
                LOG.addLog(LOG.currentDate(),"Failed to create stream to write data", e.getMessage());
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOG.addLog(LOG.currentDate(),"Error closing output stream:", e.getMessage());
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
            //runOnUiThread() Used to run code on the main UI thread.
            ((Activity) BT).runOnUiThread(() -> {
                textView_connected.setText("Connected as a server with\n" + deviceName);
                textView_percent.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                button_foundDevice.setVisibility(View.INVISIBLE);
                button_detect.setVisibility(View.INVISIBLE);
                button_disconnectBack.setVisibility(View.INVISIBLE);
                linearPercent.setVisibility(View.VISIBLE);});
            Arrays.fill(buffer, 0, buffer.length, (byte) 0);

            try {
                OutputStream outputStream = socketServer.getOutputStream();

                //The loop will be sent until it is stopped
                while(!interrupted()) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {

                            //File information is being retrieved
                            String fileFirstData = new String(buffer, 0, bytes);
                            String[] dataArrayFileFirstData = fileFirstData.split(";");
                            fileName = dataArrayFileFirstData[0];
                            String fileUnit = dataArrayFileFirstData[1];
                            String fileSizeString = dataArrayFileFirstData[2];
                            String bufferSize = dataArrayFileFirstData[3];
                            String multipleFile = dataArrayFileFirstData[4];
                            LOG.addLog(LOG.currentDate(), "File information is being retrieved");
                            Arrays.fill(buffer, 0, buffer.length, (byte) 0);

                            double fileSize = conversionFileSize(Long.parseLong(fileSizeString), fileUnit);
                            String confirmMessage;
                            FileOutputStream fileToSave = null;

                            progressBar.setMax((int)fileSize*Integer.parseInt(multipleFile));

                            for (int repeat = 0; repeat < Integer.parseInt(multipleFile); repeat++)
                            {
                                long fullBytes = 0;
                                try {
                                    fileToSave = new FileOutputStream(setFilePlace());
                                    LOG.addLog(LOG.currentDate(), "The file name has been set");
                                    byte[] bufferData = new byte[Integer.parseInt(bufferSize)];

                                    //loop where the file is fetched and the percentage of the file's saved data is displayed
                                    while ((bytes = inputStream.read(bufferData)) > 0) {
                                        fileToSave.write(bufferData, 0, bytes);
                                        fullBytes += bytes;
                                        int percent = (int) (fullBytes * 100.0) / (int) progressBar.getMax();
                                        ((Activity) BT).runOnUiThread(() -> textView_percent.setText("Download: " + percent + " %"));
                                        progressBar.setProgress(percent);
                                        if (progressBar.getProgress() == (progressBar.getMax() / (Integer.parseInt(multipleFile)-repeat)) )
                                        {
                                            LOG.addLog(LOG.currentDate(), "End of download file number " + (repeat+1));
                                            Arrays.fill(bufferData, 0, bufferData.length, (byte) 0);
                                            break;
                                        }
                                    }
                                    fileToSave.flush();
                                    ((Activity) BT).runOnUiThread(() -> Toast.makeText(BT, "Downloaded file", Toast.LENGTH_SHORT).show());
                                    LOG.addLog(new Date(System.currentTimeMillis()), "The file has been downloaded and saved");
                                    confirmMessage = "Confirmed";
                                } catch (IOException e) {
                                    LOG.addLog(LOG.currentDate(), "Error downloaded and saving file", e.getMessage());
                                    confirmMessage = "NoneConfirmed";
                                } finally {
                                    try {
                                        if (fileToSave != null) {
                                            fileToSave.close();
                                            LOG.addLog(LOG.currentDate(), "Stream to file closed");
                                        }
                                    } catch (IOException e) {
                                        LOG.addLog(LOG.currentDate(), "Error closing output stream:", e.getMessage());
                                    }
                                }
                                //sending response to download and save file
                                outputStream.write(confirmMessage.getBytes());
                                outputStream.flush();
                                LOG.addLog(LOG.currentDate(), "Sending response to download and save file");

                                if (confirmMessage.equals("Confirmed")) {
                                    ((Activity) BT).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() +
                                                    "The name of the received file: " + fileName +
                                                    "\nFile size: " +
                                                    Constants.decimalFormat.format(fileSize).replace(",", ".") +
                                                    " " + fileUnit + "\n"));
                                } else {
                                    ((Activity) BT).runOnUiThread(() -> textView_inf.setText("Error downloaded and saving file"));
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        LOG.addLog(LOG.currentDate(),"The data could not be loaded", e.getMessage());
                        break;
                    }
                }
                outputStream.close();
            } catch (IOException e) {
                LOG.addLog(LOG.currentDate(),"Failed to create stream to send data",e.getMessage());
            }
        } catch (IOException e) {
            LOG.addLog(LOG.currentDate(),"The first data could not be loaded",e.getMessage());
        }
    }

    //method where the location to save the downloaded file is chosen
    //and the name is set if it already exists in the given place
    private File setFilePlace()
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

    //method where the file size is converted according to the unit
    private double conversionFileSize(long fileSizeLong, String fileUnit)
    {
        double fileSize = (double) fileSizeLong;
        switch (fileUnit) {
            case "MB":
                fileSize /= 1024; //to KB
                fileSize /= 1024; //to MB
                break;
            case "KB":
                fileSize /= 1024; //to KB
                break;
        }
        return fileSize;
    }
}

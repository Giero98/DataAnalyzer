package com.example.masterthesis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class SendReceive extends Thread {
    static Socket socket;
    InputStream inputStream;
    static OutputStream outputStream;
    static TextView textView_percent;
    static TextView textView_inf;
    static TextView textView_qualitySignal;
    Button button_deviceDisplay;
    Button button_chooseFile;
    Button button_detect;
    static Button button_saveMeasurementData;
    static Button button_graph;
    static ProgressBar progressBar;
    String fileName;
    static String fileSizeUnit = Constants.fileSizeUnitBytes;
    static long fileSizeBytes;
    static final ArrayList<String> measurementDataList = new ArrayList<>();
    static Context context;
    static Logs.ListLog LOG;

    public SendReceive(Logs.ListLog LOG, Context context, Socket socket)
    {
        this.LOG = LOG;
        this.context = context;
        this.socket = socket;
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            LOG.addLog("stream error", e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        changeUiButtonShow();

        byte[] buffer = new byte[1024];
        int bytes;

        while (socket!=null)
        {
            try {
                bytes = inputStream.read(buffer);
                if(bytes>0)
                {
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
                                ((Activity) context).runOnUiThread(() -> textView_percent.setText("Download: " + percent + " %"));
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
                            ((Activity) context).runOnUiThread(() -> textView_inf.setText("Error downloaded and saving file"));
                            break;
                        }
                    }
                    ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Downloaded file", Toast.LENGTH_SHORT).show());

                }

            } catch (IOException e) {
                LOG.addLog("Data download error", e.getMessage());
            }
        }
    }

    void changeUiButtonShow()
    {
        textView_percent = ((Activity) context).findViewById(R.id.textView_percent);
        textView_inf = ((Activity) context).findViewById(R.id.textView_inf);
        textView_qualitySignal = ((Activity) context).findViewById(R.id.textView_qualitySignal);
        button_deviceDisplay = ((Activity) context).findViewById(R.id.button_deviceDisplay);
        button_detect = ((Activity) context).findViewById(R.id.button_detect);
        button_chooseFile = ((Activity) context).findViewById(R.id.button_chooseFile);
        button_saveMeasurementData = ((Activity) context).findViewById(R.id.button_saveMeasurementData);
        button_graph = ((Activity) context).findViewById(R.id.button_graph);
        progressBar = ((Activity) context).findViewById(R.id.progressBar);
        ((Activity) context).runOnUiThread(() ->{
            button_deviceDisplay.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            button_chooseFile.setVisibility(View.VISIBLE);
        });
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
        ((Activity) context).runOnUiThread(() -> textView_inf.setText(textView_inf.getText() +
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
    public static void sendData(Intent fileToSend, long fileSizeBytes, String fileName, int multipleFile)
    {
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
            measurementDataList.add(Constants.titleFileColumn);

            String fileDetails = fileName + ";" + fileSizeUnit + ";" + fileSizeBytes + ";" +
                    bufferSize + ";" + multipleFile;
            outputStream.write(fileDetails.getBytes());
            outputStream.flush();
            LOG.addLog("Sending file information");

            InputStream inputStream = socket.getInputStream();
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
            updateBtView();
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

    @SuppressLint("SetTextI18n")
    static void updateBtView()
    {
        ((Activity) context).runOnUiThread(() -> {
            textView_inf.setText(textView_inf.getText() + "\n");
            Toast.makeText(context, "File sent", Toast.LENGTH_SHORT).show();
            button_saveMeasurementData.setVisibility(View.VISIBLE);
            button_graph.setVisibility(View.VISIBLE);
        });
    }
}

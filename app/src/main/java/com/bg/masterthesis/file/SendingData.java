package com.bg.masterthesis.file;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bg.masterthesis.Buffer;
import com.bg.masterthesis.Constants;
import com.bg.masterthesis.Logs;
import com.bg.masterthesis.ui.DeclarationOfUIVar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class SendingData {
    public static ArrayList<String> measurementDataList = new ArrayList<>();
    static String moduleSelect;
    Logs LOG;
    DeclarationOfUIVar declarationUI;
    static InputStream inputStream;
    static OutputStream outputStream;
    Context context;
    public SendingData(Logs LOG, Context context, BluetoothSocket socket, Intent fileToSend, int multipleFile) {
        moduleSelect = Constants.connectionBt;
        this.LOG = LOG;
        this.context = context;
        openStream(socket);
        startSendingData(fileToSend,multipleFile);
    }

    public SendingData(Logs LOG, Context context, Socket socket, Intent fileToSend, int multipleFile) {
        moduleSelect = Constants.connectionWiFi;
        this.LOG = LOG;
        this.context = context;
        openStream(socket);
        startSendingData(fileToSend,multipleFile);
    }

    @SuppressLint("SetTextI18n")
    void startSendingData(Intent fileToSend, int multipleFile) {
        declarationUI = new DeclarationOfUIVar(context);

        double fileSize = FileInformation.getFileSize(fileToSend.getData(),context);
        long fileSizeBytes = FileInformation.getFileSizeBytes();
        String fileName = FileInformation.getFileName(fileToSend.getData(), context);
        String fileSizeUnit = FileInformation.getFileSizeUnit(fileSizeBytes);

        int bytesRead,bufferSize;
        if(Buffer.bufferSize == 0)
            bufferSize = (int) (fileSizeBytes * 0.1); //buffer size is 10% of file size in bytes
        else
            bufferSize = Buffer.bufferSize;
        byte[] buffer = new byte[bufferSize];

        try {
            measurementDataList.add(fileName);
            setTitleOfDataColumns(fileSizeUnit);

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
                }
                else {
                    LOG.addLog("Error sending file information");
                }
            }
            catch (IOException e) {
                LOG.addLog("Failed to create stream to receive message whether file information was delivered", e.getMessage());
            }
            Arrays.fill(confirmBuffer, 0, confirmBuffer.length, (byte) 0);

            @SuppressLint("Recycle")
            FileInputStream file = (FileInputStream) context.getContentResolver().openInputStream(fileToSend.getData());

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
                        ((Activity) context).runOnUiThread(() ->
                                declarationUI.textView_percent.setText("Sent: " + percent + " %"));
                        declarationUI.progressBar.setProgress((int) percent);
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
                                String sizeUnit = FileInformation.getFileSizeUnit(FileInformation.getFileSizeBytes());
                                updateTextInf(context,bufferSize,repeat,resultTime,speedSend,sizeUnit);
                                saveMeasurementDataToList(fileSizeBytes,repeat,fileSize,resultTime,speedSend);
                                break;
                            }
                            else if (confirmMessage.equals("NoneConfirmed")) {
                                LOG.addLog("Failed to save to the server");
                                ((Activity) context).runOnUiThread(() ->
                                        declarationUI.textView_inf.setText(declarationUI.textView_inf.getText() +
                                                "\nFailed to save to the server"));
                                return;
                            }
                            Arrays.fill(confirmBuffer, 0, confirmBuffer.length, (byte) 0);
                        }
                    }
                    catch (IOException e) {
                        LOG.addLog("Failed to create stream to receive message whether file was delivered", e.getMessage());
                    }
                }
                catch (IOException e) {
                    LOG.addLog("Failed to send file", e.getMessage());
                }
            }
            declarationUI.updateViewWhenFileSent();

            try {
                if (file != null) {
                    file.close();
                    LOG.addLog("Stream to file closed");
                }
            }
            catch (IOException e) {
                LOG.addLog("Failed to close stream to file", e.getMessage());
            }
        }
        catch (IOException e) {
            LOG.addLog("Failed to send basic file information", e.getMessage());
        }
    }

    void openStream(Socket socket) {
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }
        catch (IOException e) {
            LOG.addLog("Open stream error", e.getMessage());
        }
    }

    void openStream(BluetoothSocket socket) {
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }
        catch (IOException e) {
            LOG.addLog("Open stream error", e.getMessage());
        }
    }

    void setTitleOfDataColumns(String fileSizeUnit) {
        if(moduleSelect.equals(Constants.connectionBt)) {
            measurementDataList.add("File upload number" + "," +
                    "File size in bytes" + "," +
                    "File size in " + fileSizeUnit + "," +
                    "Quality range" + "," +
                    "Sending time [s]" + "," +
                    "Upload speed [" + fileSizeUnit + "/s]");
        }
        else {
            measurementDataList.add("File upload number" + "," +
                    "File size in bytes" + "," +
                    "File size in " + fileSizeUnit + "," +
                    "Sending time [s]" + "," +
                    "Upload speed [" + fileSizeUnit + "/s]");
        }
    }

    @SuppressLint("SetTextI18n")
    void updateTextInf(Context context, int bufferSize, int repeat, double resultTime, double speedSend, String sizeUnit) {
        ((Activity) context).runOnUiThread(() ->
                declarationUI.textView_inf.setText(declarationUI.textView_inf.getText() +
                        "\nThe size of the set buffer: " + bufferSize + " Bytes" +
                        "\nFile upload number: " + (repeat + 1) +
                        "\nFile transfer time: " +
                        Constants.decimalFormat.format(resultTime).replace(",", ".") + " s" +
                        "\nUpload speed is: " +
                        Constants.decimalFormat.format(speedSend).replace(",", ".") +
                        " " + sizeUnit + "/s"));
    }

    void saveMeasurementDataToList(long fileSizeBytes, int repeat, double fileSize, double resultTime, double speedSend) {
        if(moduleSelect.equals(Constants.connectionBt)) {
            String qualitySignal = (String) DeclarationOfUIVar.textView_qualitySignal.getText();
            measurementDataList.add((repeat + 1) + "," +
                    fileSizeBytes + "," +
                    Constants.decimalFormat.format(fileSize).replace(",", ".") + "," +
                    qualitySignal + "," +
                    Constants.decimalFormat.format(resultTime).replace(",", ".") + "," +
                    Constants.decimalFormat.format(speedSend).replace(",", "."));
        }
        else {
            measurementDataList.add((repeat + 1) + "," +
                    fileSizeBytes + "," +
                    Constants.decimalFormat.format(fileSize).replace(",", ".") + "," +
                    Constants.decimalFormat.format(resultTime).replace(",", ".") + "," +
                    Constants.decimalFormat.format(speedSend).replace(",", "."));
        }
    }

    public static void saveMeasurementData(Context context, Logs LOG) {
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
                }
                catch (IOException e) {
                    LOG.addLog("Error writing measurement data to the file", e.getMessage());
                }
            }
            else {
                ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Enter the name of the file to save", Toast.LENGTH_SHORT).show());
            }
        });
        viewToSaveData.show();
    }

    @SuppressWarnings("SameReturnValue")
    public static ArrayList<String> getMeasurementDataList() {
        return measurementDataList;
    }

    public static String getModuleSelect() {
        return moduleSelect;
    }

    public static void closeStream(Logs LOG) {
        try{
            if(inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();
        }
        catch (IOException e) {
            LOG.addLog("Error close stream", e.getMessage());
        }
    }
}

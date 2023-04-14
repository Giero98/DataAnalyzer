package com.bg.dataanalyzer.file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.bg.dataanalyzer.Constants;
import com.bg.dataanalyzer.R;
import com.bg.dataanalyzer.ui.DeclarationOfUIVar;
import com.bg.dataanalyzer.Logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class SavingData {
    String fileName;
    Logs LOG;
    DeclarationOfUIVar declarationUI;
    static InputStream inputStream;
    static OutputStream outputStream;
    Context context;

    public SavingData(Logs LOG, Context context, BluetoothSocket socket) {
        this.LOG = LOG;
        this.context = context;
        openStreams(socket);
        declarationUI = new DeclarationOfUIVar(context);
    }

    public SavingData(Logs LOG, Context context, Socket socket) {
        this.LOG = LOG;
        this.context = context;
        openStreams(socket);
        declarationUI = new DeclarationOfUIVar(context);
    }

    @SuppressLint("SetTextI18n")
    public void startSavingData() {
        byte[] buffer = new byte[Constants.getBufferFirstInfOfFile];

        try {
            int bytes = inputStream.read(buffer);
            if (bytes > 0) {
                String fileDetails = new String(buffer, 0, bytes);
                String[] dataArrayFileDetails = fileDetails.split(";");
                fileName = dataArrayFileDetails[0];
                String fileSizeUnit = dataArrayFileDetails[1];
                String fileSizeString = dataArrayFileDetails[2];
                String bufferSizeString = dataArrayFileDetails[3];
                String multipleFileString = dataArrayFileDetails[4];
                LOG.addLog(context.getString(R.string.file_inf_retrieved));
                Arrays.fill(buffer, 0, buffer.length, (byte) 0);

                long fileSizeBytes = Long.parseLong(fileSizeString);
                int multipleFile = Integer.parseInt(multipleFileString);
                int bufferSize = Integer.parseInt(bufferSizeString);

                String confirmMessage = "Confirmed";
                outputStream.write(confirmMessage.getBytes());
                outputStream.flush();

                for (int repeat = 0; repeat < multipleFile; repeat++) {
                    FileOutputStream fileToSave = null;
                    long fullBytes = 0;
                    try {
                        fileToSave = new FileOutputStream(setFilePlace());
                        LOG.addLog(context.getString(R.string.file_name_set));
                        byte[] bufferData = new byte[bufferSize];

                        while ((bytes = inputStream.read(bufferData)) > 0) {
                            fileToSave.write(bufferData, 0, bytes);
                            fullBytes += bytes;
                            long percent = 100 * (fullBytes + fileSizeBytes * repeat) /
                                    (fileSizeBytes * multipleFile);
                            ((Activity) context).runOnUiThread(() -> {
                                declarationUI.textView_percent.setText(context.getString(R.string.download) +": "+percent+" %");
                                declarationUI.progressBar.setProgress((int) percent);
                            });
                            if (fullBytes == fileSizeBytes) {
                                LOG.addLog(context.getString(R.string.end_download_file_number) +" "+ (repeat + 1));
                                Arrays.fill(bufferData, 0, bufferData.length, (byte) 0);
                                break;
                            }
                        }
                        fileToSave.flush();
                        LOG.addLog(context.getString(R.string.file_downloaded_and_saved));
                        confirmMessage = "Confirmed";
                    }
                    catch (IOException e) {
                        ((Activity) context).runOnUiThread(() ->
                                Toast.makeText(context,context.getString(R.string.file_downloaded_and_saved_error),Toast.LENGTH_SHORT).show());
                        LOG.addLog(context.getString(R.string.file_downloaded_and_saved_error), e.getMessage());
                        confirmMessage = "NoneConfirmed";
                    }
                    finally {
                        try {
                            if (fileToSave != null) {
                                fileToSave.close();
                                LOG.addLog(context.getString(R.string.close_stream_to_file));
                            }
                        }
                        catch (IOException e) {
                            ((Activity) context).runOnUiThread(() ->
                                    Toast.makeText(context,context.getString(R.string.close_output_stream_error),Toast.LENGTH_SHORT).show());
                            LOG.addLog(context.getString(R.string.close_output_stream_error), e.getMessage());
                        }
                    }
                    outputStream.write(confirmMessage.getBytes());
                    outputStream.flush();
                    LOG.addLog(context.getString(R.string.sending_response_to_download_and_save));

                    if (confirmMessage.equals("Confirmed")) {
                        updateTextInf(fileName,fileSizeBytes,fileSizeUnit);
                    }
                    else {
                        ((Activity) context).runOnUiThread(() ->
                                declarationUI.textView_inf.setText(context.getString(R.string.file_downloaded_and_saved_error)));
                        break;
                    }
                }
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, context.getString(R.string.download_file), Toast.LENGTH_SHORT).show());
            }
        }
        catch (Exception ignored) {
        }
    }

    void openStreams(Socket socket) {
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }
        catch (IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.open_stream_error),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.open_stream_error), e.getMessage());
        }
    }

    void openStreams(BluetoothSocket socket) {
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }
        catch (IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.open_stream_error),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.open_stream_error), e.getMessage());
        }
    }

    File setFilePlace() {
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
    void updateTextInf(String fileName, long fileSizeBytes, String fileSizeUnit) {
        double fileSize = conversionFileSize(fileSizeBytes, fileSizeUnit);
        ((Activity) context).runOnUiThread(() ->
                declarationUI.textView_inf.setText(declarationUI.textView_inf.getText() +
                        context.getString(R.string.name_received_file) +": "+ fileName +
                        "\n"+ context.getString(R.string.file_size) +": " +
                        Constants.decimalFormat.format(fileSize).replace(",", ".") +
                        " " + fileSizeUnit + "\n\n"));
    }

    double conversionFileSize(long fileSizeLong, String fileUnit) {
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

    public static void closeStreams(Logs LOG, Context context) {
        try{
            if(inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();
        }
        catch (IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.close_stream_error),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.close_stream_error), e.getMessage());
        }
    }
}

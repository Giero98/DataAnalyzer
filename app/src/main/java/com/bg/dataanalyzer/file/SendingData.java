package com.bg.dataanalyzer.file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bg.dataanalyzer.Buffer;
import com.bg.dataanalyzer.Constants;
import com.bg.dataanalyzer.Logs;
import com.bg.dataanalyzer.R;
import com.bg.dataanalyzer.ui.DeclarationOfUIVar;

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
                    LOG.addLog(context.getString(R.string.sending_file_inf));
                }
                else {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context,context.getString(R.string.sending_file_inf_error),Toast.LENGTH_SHORT).show());
                    LOG.addLog(context.getString(R.string.sending_file_inf_error));
                }
            }
            catch (IOException e) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context,context.getString(R.string.failed_create_stream_to_sending_file_inf),Toast.LENGTH_SHORT).show());
                LOG.addLog(context.getString(R.string.failed_create_stream_to_sending_file_inf), e.getMessage());
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
                                declarationUI.textView_percent.setText(context.getString(R.string.sent) + ": " + percent + " %"));
                        declarationUI.progressBar.setProgress((int) percent);
                    }
                    outputStream.flush();
                    file.getChannel().position(0);
                    LOG.addLog(context.getString(R.string.end_upload_file_number) +" "+ (repeat+1));
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
                                LOG.addLog(context.getString(R.string.failed_save_on_server));
                                ((Activity) context).runOnUiThread(() ->
                                        declarationUI.textView_inf.setText(declarationUI.textView_inf.getText() +
                                                "\n" + context.getString(R.string.failed_save_on_server)));
                                return;
                            }
                            Arrays.fill(confirmBuffer, 0, confirmBuffer.length, (byte) 0);
                        }
                    }
                    catch (IOException e) {
                        ((Activity) context).runOnUiThread(() ->
                                Toast.makeText(context,context.getString(R.string.failed_create_stream_to_receive_message),Toast.LENGTH_SHORT).show());
                        LOG.addLog(context.getString(R.string.failed_create_stream_to_receive_message), e.getMessage());
                    }
                }
                catch (IOException e) {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context,context.getString(R.string.failed_send_file),Toast.LENGTH_SHORT).show());
                    LOG.addLog(context.getString(R.string.failed_send_file), e.getMessage());
                }
            }
            declarationUI.updateViewWhenFileSent();

            try {
                if (file != null) {
                    file.close();
                    LOG.addLog(context.getString(R.string.close_stream_to_file));
                }
            }
            catch (IOException e) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context,context.getString(R.string.close_stream_to_file_error),Toast.LENGTH_SHORT).show());
                LOG.addLog(context.getString(R.string.close_stream_to_file_error), e.getMessage());
            }
        }
        catch (IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.sending_file_inf_error),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.sending_file_inf_error), e.getMessage());
        }
    }

    void openStream(Socket socket) {
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

    void openStream(BluetoothSocket socket) {
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

    void setTitleOfDataColumns(String fileSizeUnit) {
        if(moduleSelect.equals(Constants.connectionBt)) {
            measurementDataList.add(context.getString(R.string.file_upload_number) + "," +
                    context.getString(R.string.file_size_bytes) + "," +
                    context.getString(R.string.file_size_in) +" "+ fileSizeUnit + "," +
                    context.getString(R.string.signal_quality) + "," +
                    context.getString(R.string.sending_time) + "," +
                    context.getString(R.string.upload_speed) +" ["+ fileSizeUnit +"/s]");
        }
        else {
            measurementDataList.add(context.getString(R.string.file_upload_number) + "," +
                    context.getString(R.string.file_size_bytes) + "," +
                    context.getString(R.string.file_size_in) +" "+ fileSizeUnit + "," +
                    context.getString(R.string.sending_time) + "," +
                    context.getString(R.string.upload_speed) +" ["+ fileSizeUnit +"/s]");
        }
    }

    @SuppressLint("SetTextI18n")
    void updateTextInf(Context context, int bufferSize, int repeat, double resultTime, double speedSend, String sizeUnit) {
        ((Activity) context).runOnUiThread(() ->
                declarationUI.textView_inf.setText(declarationUI.textView_inf.getText() +
                        "\n"+ context.getString(R.string.size_set_buffer) +" " + bufferSize +" "+ Constants.fileSizeUnitBytes +
                        "\n"+ context.getString(R.string.file_upload_number) +": " + (repeat + 1) +
                        "\n"+ context.getString(R.string.upload_time) +": " +
                        Constants.decimalFormat.format(resultTime).replace(",", ".") + " s" +
                        "\n"+ context.getString(R.string.upload_speed_is) +": " +
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
        viewToSaveData.setTitle(context.getString(R.string.title_save_data));
        EditText editText = new EditText(context);
        viewToSaveData.setView(editText);
        editText.requestFocus();

        viewToSaveData.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
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

                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, context.getString(R.string.data_saved), Toast.LENGTH_SHORT).show());
                }
                catch (IOException e) {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context,context.getString(R.string.data_saved_error),Toast.LENGTH_SHORT).show());
                    LOG.addLog(context.getString(R.string.data_saved_error), e.getMessage());
                }
            }
            else {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, context.getString(R.string.name_of_file_to_save), Toast.LENGTH_SHORT).show());
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

    public static void closeStream(Logs LOG, Context context) {
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

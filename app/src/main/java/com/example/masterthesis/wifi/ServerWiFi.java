package com.example.masterthesis.wifi;

import static com.example.masterthesis.wifi.WiFi.wifiDirectChannel;
import static com.example.masterthesis.wifi.WiFi.wifiDirectManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerWiFi extends Thread{
    static Socket socket;
    static ServerSocket serverSocket;
    static Logs.ListLog LOG;
    int port;
    Context context;
    static InputStream inputStream;
    static OutputStream outputStream;
    String fileName;
    TextView textView_percent, textView_inf;
    Button button_deviceDisplay, button_detect;
    ProgressBar progressBar;
    LinearLayout layoutPercent;

    public ServerWiFi(Logs.ListLog LOG, Context context){
        ServerWiFi.LOG = LOG;
        this.context = context;
    }

    @Override
    public void run(){
        generatePort();
    }

    void generatePort()
    {
        int i=0;
        do {
            i++;
            port = (int) (Math.random() * 60000 + 1024);
            LOG.addLog("Port number "+i+" = "+port);
        }while(!createServerSocket());
    }

    boolean createServerSocket() {
        try (ServerSocket serverSocketTest = new ServerSocket(port)) {
            serverSocket = serverSocketTest;
            startRegistration();
            startConnect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void startRegistration() {
        Map record = new HashMap();
        record.put("PORT", String.valueOf(port));

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("Port", "WiFiDirect", record);

        wifiDirectManager.addLocalService(wifiDirectChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                LOG.addLog("Added network service providing port");
            }
            @Override
            public void onFailure(int arg0) {
                LOG.addLog("Failed added network service providing port", String.valueOf(arg0));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    void startConnect()
    {
        do {
            try {
                socket = serverSocket.accept();

                TextView textView_connected = ((Activity) context).findViewById(R.id.textView_connected);
                ((Activity) context).runOnUiThread(() ->
                        textView_connected.setText("Connected as a server"));
                writeData();
            } catch (IOException e) {
                LOG.addLog("Client connection error", e.getMessage());
            }
        }while(socket==null);
    }

    public static Socket getSocket()
    {
        return socket;
    }

    public static ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    @SuppressLint("SetTextI18n")
    void writeData() {
        changeUiButtonShow();
        openStream();

        byte[] buffer = new byte[Constants.getBufferFirstInfOfFile];
        int bytes;

        while (socket!=null)
        {
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

                        String confirmMessage;
                        confirmMessage = "Confirmed";
                        outputStream.write(confirmMessage.getBytes());
                        outputStream.flush();

                        for (int repeat = 0; repeat < multipleFile; repeat++) {
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
                                    if (fullBytes == fileSizeBytes) {
                                        LOG.addLog("End of download file number " + (repeat + 1));
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
                                updateTextInf(fileName, fileSizeBytes, fileSizeUnit);
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

    void openStream()
    {
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            LOG.addLog("Open stream error", e.getMessage());
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

    void changeUiButtonShow()
    {
        textView_percent = ((Activity) context).findViewById(R.id.textView_percent);
        textView_inf = ((Activity) context).findViewById(R.id.textView_inf);
        ((Activity) context).runOnUiThread(() ->textView_inf.setMovementMethod(new ScrollingMovementMethod()));
        button_deviceDisplay = ((Activity) context).findViewById(R.id.button_deviceDisplay);
        button_detect = ((Activity) context).findViewById(R.id.button_detect);
        progressBar = ((Activity) context).findViewById(R.id.progressBar);
        layoutPercent = ((Activity) context).findViewById(R.id.layoutPercent);
        ((Activity) context).runOnUiThread(() ->{
            button_deviceDisplay.setVisibility(View.INVISIBLE);
            button_detect.setVisibility(View.INVISIBLE);
            layoutPercent.setVisibility(View.VISIBLE);
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
}

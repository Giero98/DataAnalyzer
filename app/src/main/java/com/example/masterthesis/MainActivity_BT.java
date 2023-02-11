package com.example.masterthesis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * View of the application after successful connection via Bluetooth
 */
public class MainActivity_BT extends AppCompatActivity {
    //Variable containing the list of devices found
    private final ArrayList<String> discoveredDevices = new ArrayList<>();
    //Adapter connecting arrays with ListView
    private ArrayAdapter<String> listAdapter;
    //A unique UUID that will be used as a common identifier for both devices
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //Variable used as the name of the Bluetooth server
    private static final String NAME = "MASTER_THESIS";
    //A name that identifies the Log and is used to filter logs in the log console
    private BluetoothSocket socketClient = null, socketServer = null;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private boolean dataSendFromClient = false, closeTread = false;
    private Intent fileToSend;
    public MainActivity_Log.ListLog LOG = new MainActivity_Log.ListLog();
    final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Button button_sendData;
    public TextView textView_connected, textView_inf, textView_percent;
    private ListView listView;
    private ProgressBar progressBar;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bt);
        setTitle("Master Thesis - Bluetooth");

        Button button_disconnect = findViewById(R.id.button_disconnect);
        Button button_detect = findViewById(R.id.button_detect);
        Button button_foundDevice = findViewById(R.id.button_foundDevice);
        button_sendData = findViewById(R.id.button_sendData);
        textView_connected = findViewById(R.id.textView_connected);
        textView_inf = findViewById(R.id.textView_inf);
        textView_percent = findViewById(R.id.textView_percent);
        listView = findViewById(R.id.ListView);
        progressBar = findViewById(R.id.progressBar);

        textView_connected.setText("Not connected");

        //The invoked thread listening for the connection attempt
        ConnectBtServerThread threadServer = new ConnectBtServerThread();
        threadServer.start();

        LOG.addLog(new Date(System.currentTimeMillis()),"A server thread has started listening");


        //Button to detection by other devices
        button_detect.setOnClickListener(v -> discoverableBt());

        //Button to find device
        button_foundDevice.setOnClickListener((v -> foundDeviceBt()));

        //Button to send data
        button_sendData.setOnClickListener(v -> sendData());

        //Button to disconnect
        button_disconnect.setOnClickListener(v -> {
            closeBtConnection();
            Toast.makeText(MainActivity_BT.this, "Disconnect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity_BT.this, MainActivity.class);
            startActivity(intent);
        });

        //Select a found device for Bluetooth connection
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = (String) listView.getItemAtPosition(position);
            //deviceAddress holds the 17 characters from the end of the deviceInfo string
            String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            ConnectBtClientThread threadClient = new ConnectBtClientThread(device);
            threadClient.start();
            threadServer.interrupt();
            LOG.addLog(currentDate(),"The server thread has finished listening");
        });
    }

    //region BT detection

    //Calling intent enable discoverability
    private void discoverableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        ActivityDiscoverableBt.launch(intent);
    }

    //Reactions to permission response received discoverableBt
    final ActivityResultLauncher<Intent> ActivityDiscoverableBt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0)
                    Toast.makeText(MainActivity_BT.this, "The device is discoverable", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity_BT.this, "The device is undetectable", Toast.LENGTH_SHORT).show();
            });

    //endregion BT detection

    //region Searching for BT devices

    //configure discovery of Bluetooth devices
    @SuppressLint("MissingPermission") //Used to bypass validation re-verification
    private void foundDeviceBt() {
        bluetoothAdapter.startDiscovery();
        listDiscoverableDevices();
        intentActionFound();
        intentActionAclDisconnected();
    }

    //configuration of the list of discoverable devices
    private void listDiscoverableDevices()
    {
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevices);
        listAdapter.clear();
        listView.setAdapter(listAdapter);
    }

    //launching the intention to detect a new Bluetooth device
    private void intentActionFound()
    {
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intent);
    }

    //launching the intention about losing connection with the Bluetooth device
    private void intentActionAclDisconnected()
    {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
    }


    // Create a BroadcastReceiver for ACTION_FOUND or ACTION_ACL_DISCONNECTED.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                discoveredDevices.add(device.getName() + "\n" + device.getAddress());
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                discoveredDevices.remove(device.getName() + "\n" + device.getAddress());
            }
            listAdapter.notifyDataSetChanged();
        }
    };

    //endregion Searching for BT devices

    //region Connect as a Client

    private class ConnectBtClientThread extends Thread {
        private final BluetoothDevice device;
        private long fileSizeBytes;
        public String fileSizeUnit = "Bytes";

        //ConnectBtClientThread class constructor
        @SuppressLint("MissingPermission")
        public ConnectBtClientThread(BluetoothDevice newDevice) {
            device = newDevice;
            try {
                socketClient = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                LOG.addLog(currentDate(),"Socket's create() method failed", e.getMessage());
            }
        }

        //A method that is run when the start() method is called on an object representing a thread
        @SuppressLint({"MissingPermission", "SetTextI18n"})
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                socketClient.connect();
            } catch (IOException connectException) {
                // Unable to connect, close the socket and return.
                closeSocketClient();
                return;
            }
            // The connection attempt succeeded.
            //runOnUiThread() Used to run code on the main UI thread.
            runOnUiThread(() -> {
                            textView_connected.setText("Connected as a client with\n" + device.getName());
                            button_sendData.setVisibility(View.VISIBLE);});
            sendNameDevice();
            do {
                if (dataSendFromClient)
                    sendData();
            } while (!closeTread);
        }
        // Closes the client socket and causes the thread to finish.
        private void closeSocketClient() {
            try {
                socketClient.close();
            } catch (IOException e) {
                LOG.addLog(currentDate(),"Could not close the client socket", e.getMessage());
            }
        }
        @SuppressLint("MissingPermission")
        private void sendNameDevice()
        {
            try {
                OutputStream outputStream = socketClient.getOutputStream();
                outputStream.write(bluetoothAdapter.getName().getBytes());
                //flush() is used to push out all written bytes
                outputStream.flush();
            } catch (IOException e) {
                //Forwarding information about the exception to the place where it will be handled
                throw new RuntimeException(e);
            }
        }
        @SuppressLint({"SetTextI18n", "Range", "Recycle"})
        private void sendData()
        {
            try {
                Uri uri = fileToSend.getData();
                double fileSize = getFileSize(uri);

                int bufferSize = (int) (fileSizeBytes * 0.1); // 10% of file

                String fileName = getFileName(uri);
                OutputStream outputStream = socketClient.getOutputStream();
                String fileData = fileName + ";" + fileSizeUnit + ";" + fileSizeBytes + ";" + bufferSize;
                outputStream.write(fileData.getBytes());
                outputStream.flush();

                byte[] buffer = new byte[bufferSize];
                int bytesRead;

                long startTime = System.currentTimeMillis();

                FileInputStream fis = null;
                try {
                    fis = (FileInputStream) getContentResolver().openInputStream(uri);
                    long fullBytes=0;
                    while ((bytesRead = fis.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, bytesRead);
                        fullBytes+=bytesRead;
                        int percent = (int) (fullBytes * 100.0) / (int) fileSizeBytes;
                        runOnUiThread(() -> textView_percent.setText("Sent: " + percent + " %"));
                        progressBar.setProgress(percent);
                    }
                    outputStream.flush();
                    Arrays.fill(buffer, 0, buffer.length, (byte) 0);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                InputStream inputStream = socketClient.getInputStream();
                byte[] confirmationBuffer = new byte[100];
                while(true) {
                    int bytesReaded = inputStream.read(confirmationBuffer);
                    String confirmationMessage = new String(confirmationBuffer, 0, bytesReaded);
                    if(confirmationMessage.equals("Confirmed")) {
                        long endTime = System.currentTimeMillis();
                        runOnUiThread(() -> Toast.makeText(MainActivity_BT.this, "File sent", Toast.LENGTH_SHORT).show()) ;
                        double resultTime = (double) (endTime - startTime) / 1000; //ms to s
                        double speedSend = fileSize / resultTime;
                        String sizeUnit = setSpeedSendUnit(speedSend);
                        runOnUiThread(() -> textView_inf.setText(textView_inf.getText() + "\nFile transfer time: " +
                                decimalFormat.format(resultTime) + " s\nSize of the uploaded file: " +
                                decimalFormat.format(fileSize) + " " + fileSizeUnit + "\nUpload speed is: " +
                                decimalFormat.format(speedSend) + " " + sizeUnit + "/s"));
                        Arrays.fill(confirmationBuffer, 0, confirmationBuffer.length, (byte) 0);
                        dataSendFromClient = false;
                        return;
                    }
                    else if(confirmationMessage.equals("NoneConfirmed"))
                    {
                        runOnUiThread(() -> textView_inf.setText(textView_inf.getText() + "\nFailed to save to the server"));
                        Arrays.fill(confirmationBuffer, 0, confirmationBuffer.length, (byte) 0);
                        dataSendFromClient = false;
                        return;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @SuppressLint({"Range", "SetTextI18n"})
        private String getFileName(Uri uri)
        {
            String fileName = null;
            if (uri.getScheme().equals("content")) {
                try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            }
            if(fileName == null) {
                assert false;
                int cut = fileName.lastIndexOf('/');
                if (cut != -1) {
                    fileName = fileName.substring(cut + 1);
                }
            }
            String finalFileName = fileName;
            runOnUiThread(() -> textView_inf.setText("The name of the uploaded file: " + finalFileName));
            return finalFileName;
        }
        @SuppressLint("Range")
        private double getFileSize(Uri uri)
        {
            File file = new File(uri.getPath());
            double fileSize = 0;
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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
    }

    //endregion Connect as a Client

    //region Connect as a Server

    private class ConnectBtServerThread extends Thread {
        private BluetoothServerSocket serverSocket;

        //ConnectBtServerThread class constructor
        @SuppressLint("MissingPermission")
        public ConnectBtServerThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                long currentTimeMillis = System.currentTimeMillis();
                Date currentDate = new Date(currentTimeMillis);
                String descrpition = "Socket's listen() method failed";
                LOG.addLog(currentDate,descrpition, e.getMessage());
            }
        }
        //A method that is run when the start() method is called on an object representing a thread
        @SuppressLint("HardwareIds")
        public void run() {
            // Keep listening until exception occurs or a socket is returned.
            while (!isInterrupted()) {
                try {
                    socketServer = serverSocket.accept();
                } catch (IOException e) {
                    LOG.addLog(currentDate(),"Socket's accept() method failed", e.getMessage());
                    break;
                }
                if (socketServer != null) {
                    // A connection was accepted. Perform work associated with the connection in a separate thread.
                    try {
                        InputStream inputStream = socketServer.getInputStream();
                        getData(inputStream);
                    } catch (IOException e) {
                        LOG.addLog(currentDate(),"Error closing output stream:", e.getMessage());
                    }
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        LOG.addLog(currentDate(),"Error closing output stream:", e.getMessage());
                    }
                    break;
                }
            }
        }
        @SuppressLint("SetTextI18n")
        private void getData(InputStream inputStream)
        {
            byte[] buffer = new byte[1024];
            int bytes;
            try {
                bytes = inputStream.read(buffer);
                // Send the obtained bytes to the UI activity.
                String incomingMessage = new String(buffer, 0, bytes);
                //runOnUiThread() Used to run code on the main UI thread.
                runOnUiThread(() -> textView_connected.setText("Connected as a server with\n" + incomingMessage));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            OutputStream outputStream;
            try {
                outputStream = socketServer.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while(true) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {

                        String fileData = new String(buffer, 0, bytes);
                        String[] dataArray = fileData.split(";");
                        String fileName = dataArray[0];
                        String fileUnit = dataArray[1];
                        String filesize = dataArray[2];
                        String bufferSizeS = dataArray[3];
                        Arrays.fill(buffer, 0, buffer.length, (byte) 0);

                        long filesizeee111;
                        try {
                            filesizeee111 = Long.parseLong(filesize);
                        } catch (NumberFormatException e) {
                            LOG.addLog(currentDate(),"Invalid format", e.getMessage());
                            break;
                        }
                        double filesizeee = conferteFileSize(filesizeee111,fileUnit);

                        String confirmationMessage;
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                                            "/" + fileName);
                            long fullBytes = 0;
                            byte[] bufferData = new byte[Integer.parseInt(bufferSizeS)];
                            while ((bytes = inputStream.read(bufferData)) > 0) {
                                fos.write(bufferData, 0, bytes);
                                fullBytes+=bytes;
                                int precent = (int) (fullBytes * 100.0) / (int) filesizeee111;
                                runOnUiThread(() -> textView_percent.setText("Download: " + precent + " %"));
                                progressBar.setProgress(precent);
                                if(precent == 100) {
                                    Arrays.fill(bufferData, 0, bufferData.length, (byte) 0);
                                    break;
                                }
                            }
                            runOnUiThread(() -> Toast.makeText(MainActivity_BT.this, "Downloaded", Toast.LENGTH_SHORT).show());
                            fos.flush();
                            LOG.addLog(new Date(System.currentTimeMillis()),"The file has been downloaded and saved");
                            confirmationMessage= "Confirmed";
                        } catch (IOException e) {
                            LOG.addLog(new Date(System.currentTimeMillis()),"Error saving file:", e.getMessage());
                            confirmationMessage= "NoneConfirmed";
                            outputStream.write(confirmationMessage.getBytes());
                            outputStream.flush();
                            break;
                        } finally {
                            try {
                                if (fos != null) {
                                    fos.close();
                                }
                            } catch (IOException e) {
                                LOG.addLog(new Date(System.currentTimeMillis()),"Error closing output stream:", e.getMessage());
                            }
                        }
                        outputStream.write(confirmationMessage.getBytes());
                        outputStream.flush();
                        runOnUiThread(() -> textView_inf.setText("The name of the received file: "+
                                fileName + "\nFile size: " +decimalFormat.format(filesizeee) + " " + fileUnit));
                    }
                } catch (IOException e) {
                    LOG.addLog(currentDate(),"Error closing output stream:", e.getMessage());
                }
                if(!socketServer.isConnected()) {
                    try {
                        runOnUiThread(() -> textView_connected.setText("Disconnected"));
                        runOnUiThread(() -> Toast.makeText(MainActivity_BT.this, "Disconnected", Toast.LENGTH_SHORT).show());
                        inputStream.close();
                        outputStream.close();
                        socketServer.close();
                        break;
                    } catch (IOException ex) {
                        LOG.addLog(currentDate(),"Error closing output stream:", ex.getMessage());
                    }
                }
                if (closeTread)
                    break;
            }
        }
        private double conferteFileSize(long filesizeee111, String fileUnit)
        {
            double filesizeee = (double) filesizeee111;
            switch (fileUnit) {
                case "MB":
                    filesizeee /= 1024; //to KB
                    filesizeee /= 1024; //to MB
                    break;
                case "KB":
                    filesizeee /= 1024; //to KB
                    break;
            }
            return filesizeee;
        }
    }

    //endregion Connect as a Server

    private void sendData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent,1);
    }

    //Reactions to permission response received openFile
    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            fileToSend = data;
            dataSendFromClient = true;
        }
    }


    @SuppressLint("MissingPermission")
    private void closeBtConnection()
    {
        closeTread = true;
        if(receiver.isOrderedBroadcast())
            unregisterReceiver(receiver);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        if(socketClient != null)
            if(socketClient.isConnected()) {
                try {
                    socketClient.close();
                } catch (IOException e) {
                    LOG.addLog(currentDate(),"Error closing output stream:", e.getMessage());
                }
            }
        if(socketServer != null)
            if(socketServer.isConnected()) {
                try {
                    socketServer.close();
                } catch (IOException e) {
                    LOG.addLog(currentDate(),"Error closing output stream:", e.getMessage());
                }
            }
    }

    private Date currentDate()
    {
        return new Date(System.currentTimeMillis());
    }

    //The method that is run when the application is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBtConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem showLog = menu.findItem(R.id.show_log);
        showLog.setTitle("Show Log");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_log) {
            Intent intent = new Intent(this, MainActivity_Log.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
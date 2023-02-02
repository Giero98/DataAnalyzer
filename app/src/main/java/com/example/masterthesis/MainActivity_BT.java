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
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
    private static final String TAG_BT = "BT_ACTIVITY";
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Button button_back, button_detect, button_foundDevice;
    TextView text;
    ListView listView;

    @SuppressLint({"SetTextI18n", "MissingPermission"}) //Used to bypass validation re-verification
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bt);

        button_back = findViewById(R.id.button4);
        button_detect = findViewById(R.id.button5);
        button_foundDevice = findViewById(R.id.button6);
        text = findViewById(R.id.textView3);
        listView = findViewById(R.id.ListView);

        text.setText("Good Job!\n" +
                "You are connected by Bluetooth.\n");
        button_back.setText("Disconnect");
        button_detect.setText("Start detected");
        button_foundDevice.setText("Found device");

        //The invoked thread listening for the connection attempt
        AcceptBtThread threadAccept = new AcceptBtThread();
        threadAccept.start();

        //Button to detection by other devices
        button_detect.setOnClickListener(v -> discoverableBt());

        //Button to find device
        button_foundDevice.setOnClickListener((v -> foundDeviceBt()));

        //Button to disconnect
        button_back.setOnClickListener(v -> {
            bluetoothAdapter.cancelDiscovery();
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
            ConnectBtThread thread = new ConnectBtThread(device);
            thread.start();
        });
    }

    //region BT detection

    //Calling intent enable discoverability
    private void discoverableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        ActivityDiscoverableBt.launch(intent);
    }

    //Reactions to permission response received discoverableBt
    ActivityResultLauncher<Intent> ActivityDiscoverableBt = registerForActivityResult(
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

    private class ConnectBtThread extends Thread {
        private BluetoothSocket socketBt;
        private final BluetoothDevice device;

        //ConnectBtThread class constructor
        @SuppressLint("MissingPermission")
        public ConnectBtThread(BluetoothDevice newDevice) {
            device = newDevice;
            try {
                socketBt = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG_BT, "Socket's create() method failed", e);
            }
        }

        //A method that is run when the start() method is called on an object representing a thread
        @SuppressLint({"MissingPermission", "SetTextI18n"})
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                socketBt.connect();
            } catch (IOException connectException) {
                // Unable to connect, close the socket and return.
                closeSocket();
                return;
            }
            // The connection attempt succeeded.
            //runOnUiThread() Used to run code on the main UI thread.
            runOnUiThread(() -> text.setText("Connected as a client with "+device.getName()));
            try {
                OutputStream outputStream = socketBt.getOutputStream();
                outputStream.write(bluetoothAdapter.getName().getBytes());
                //flush() is used to push out all written bytes
                outputStream.flush();
                outputStream.close();
                closeSocket();
            } catch (IOException e) {
                //Forwarding information about the exception to the place where it will be handled
                throw new RuntimeException(e);
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void closeSocket() {
            try {
                socketBt.close();
            } catch (IOException e) {
                Log.e(TAG_BT, "Could not close the client socket", e);
            }
        }
    }

    //endregion Connect as a Client

    //region Connect as a Server

    private class AcceptBtThread extends Thread {
        private BluetoothServerSocket serverSocket;

        //AcceptBtThread class constructor
        @SuppressLint("MissingPermission")
        public AcceptBtThread() {
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG_BT, "Socket's listen() method failed", e);
            }
        }

        //A method that is run when the start() method is called on an object representing a thread
        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG_BT, "Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    // A connection was accepted. Perform work associated with the connection in a separate thread.
                    ConnectedServerThread thread = new ConnectedServerThread(socket);
                    thread.start();
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }
    }

    //endregion Connect as a Server

    //region Send Data as a Server

    private class ConnectedServerThread extends Thread {
        private final BluetoothSocket socket;
        private InputStream streamIn;
        private OutputStream streamOut;

        //ConnectedServerThread class constructor
        public ConnectedServerThread(BluetoothSocket newSocket) {
            socket = newSocket;
            // Get the input and output streams
            try {
                streamIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG_BT, "Error occurred when creating input stream", e);
            }
            try {
                streamOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG_BT, "Error occurred when creating output stream", e);
            }
        }

        //A method that is run when the start() method is called on an object representing a thread
        @SuppressLint("SetTextI18n")
        public void run() {
            // buffer store for the stream
            byte[] buffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the streamIn until an exception occurs.
            while (true) {
                try {
                    // Read from the streamIn.
                    numBytes = streamIn.read(buffer);
                    // Send the obtained bytes to the UI activity.
                    String incomingMessage = new String(buffer, 0, numBytes);
                    //runOnUiThread() Used to run code on the main UI thread.
                    runOnUiThread(() -> text.setText("Connected as a server with "+incomingMessage));

                } catch (IOException e) {
                    Log.e(TAG_BT, "Input stream was disconnected", e);
                    break;
                }
            }
            closeSocket();
        }
        // Closes the client socket and causes the thread to finish.
        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG_BT, "Could not close the client socket", e);
            }
        }
    }

    //endregion Send Data as a Server

    //The method that is run when the application is closed
    @SuppressLint("MissingPermission") //Used to bypass validation re-verification
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        bluetoothAdapter.cancelDiscovery();
    }
}

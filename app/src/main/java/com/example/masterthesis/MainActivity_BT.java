package com.example.masterthesis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/*
  View of the application after successful connection via Bluetooth
 */
public class MainActivity_BT extends AppCompatActivity {
    //Variable containing the list of devices found
    private final ArrayList<String> discoveredDevices = new ArrayList<>();
    //Adapter connecting arrays with ListView
    private ArrayAdapter<String> listAdapter;
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

        /*
          Button to detection by other devices
         */
        button_detect.setOnClickListener(v -> discoverableBt());

        /*
          Button to find device
         */
        button_foundDevice.setOnClickListener((v -> foundDeviceBt()));

        /*
          Button to disconnect
         */
        button_back.setOnClickListener(v -> {
            bluetoothAdapter.cancelDiscovery();
            Toast.makeText(MainActivity_BT.this, "Disconnect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity_BT.this, MainActivity.class);
            startActivity(intent);
        });
    }

    //Checks if the API version is >=23
    private boolean checkAPI() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
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

    //region searching for BT devices

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
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(device.getName() + "\n" + device.getAddress());
                listAdapter.notifyDataSetChanged();
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.remove(device.getName() + "\n" + device.getAddress());
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    //endregion searching for BT devices

    @SuppressLint("MissingPermission") //Used to bypass validation re-verification
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        bluetoothAdapter.cancelDiscovery();
    }
}
package com.example.masterthesis;

import android.bluetooth.BluetoothAdapter;

import java.text.DecimalFormat;
import java.util.UUID;

public interface Constants {

    //Responses to given user actions
    int     REQUEST_BT_CONNECT = 0,
            REQUEST_BT_SCAN = 1,
            REQUEST_BT_ADVERTISE = 2,
            REQUEST_BT_ACCESS_FINE_LOCATION = 3,
            REQUEST_BT_SEND_DATA_FILE = 4;

    //Buffer size
    int     confirmBufferBytes = 100,
            getBufferFirstInfOfFile = 1024;

    //Time to search for Bluetooth devices
    int timeSearch = 12000; //ms

    //A unique UUID that will be used as a common identifier for both devices in Bluetooth
    //generated thanks to the website https://www.uuidgenerator.net/
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Variable used as the name of the Bluetooth server
    String NAME = "MASTER_THESIS";

    //The formula by which the decimal variable will be displayed with an accuracy of two decimal places
    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    //local device Bluetooth adapter
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
}

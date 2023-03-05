package com.example.masterthesis;

import android.bluetooth.BluetoothAdapter;

import com.example.masterthesis.bluetooh.ConnectBtClientThread;

import java.text.DecimalFormat;
import java.util.UUID;

public interface Constants {

    int     REQUEST_BT_CONNECT = 0,
            REQUEST_BT_SCAN = 1,
            REQUEST_BT_ADVERTISE = 2,
            REQUEST_BT_ACCESS_FINE_LOCATION = 3,
            REQUEST_BT_SEND_DATA_FILE = 4;

    int     confirmBufferBytes = 100,
            getBufferFirstInfOfFile = 4096,
            timeSearch = 12000; //ms

    String  NAME = "MASTER_THESIS",
            uploadTimeUnit = "[s]",
            qualitySignalUnit = "[%]",
            uploadSpeedUnit = "[" + ConnectBtClientThread.getFileSizeUnit() + "/s]";

    //A unique UUID that will be used as a common identifier for both devices in Bluetooth
    //generated thanks to the website https://www.uuidgenerator.net/
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
}

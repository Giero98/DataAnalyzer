package com.example.masterthesis;

import android.bluetooth.BluetoothAdapter;
import android.graphics.DashPathEffect;

import com.example.masterthesis.file.FileInformation;

import java.io.File;
import java.text.DecimalFormat;
import java.util.UUID;

public interface Constants {

    int     REQUEST_BT_CONNECT = 0,
            REQUEST_BT_SCAN = 1,
            REQUEST_BT_ADVERTISE = 2,
            REQUEST_ACCESS_FINE_LOCATION = 3,
            REQUEST_BT_SEND_DATA_FILE = 4,
            REQUEST_CHANGE_WIFI_STATE = 5,
            REQUEST_ACCESS_WIFI_STATE = 6,
            REQUEST_INTERNET = 7,
            REQUEST_NEARBY_WIFI_DEVICES = 8,
            REQUEST_ACCESS_NETWORK_STATE = 9,
            REQUEST_CHANGE_NETWORK_STATE = 10;

    int     minimumNumberOfUploadFiles = 1,
            maximumNumberOfUploadFiles = 100,
            maximumQualitySignal = 100,
            confirmBufferBytes = 100,
            getBufferFirstInfOfFile = 4096,
            size1Kb = 1024,
            timeSearch = 12000, //ms
            delayReadingSignal = 500, //ms
            graphAnimationDuration = 1000, //s
            maximumNumberOfColumnsOnTheScreen = 4,
            rangePossiblePortsToConnect = 6000,
            smallestPortToConnect = 1024;
    float   axisValueSize = 16f,
            columnWidth = 0.7f,
            distanceBetweenXAxisData = 1f,
            distanceBetweenYAxisData = 0.001f,
            minimumYAxisValue = 0f;
    DashPathEffect girdLineStyle = new DashPathEffect(new float[]{10f, 5f}, 0f);

    String  NAME = "MASTER_THESIS",
            titleLog = "Show LOG",
            back = "Back",
            connectionBt = "BT",
            connectionWiFi = "WIFI",
            uploadTimeUnit = "[s]",
            qualitySignalUnit = "[%]",
            fileSizeUnitBytes = "Bytes",
            fileSizeUnitKB = "KB",
            fileSizeUnitMB = "MB",
            titleDialogToSaveData = "Enter the name of the measurement data file",
            titleDialogToSelectDevice = "Select a device";

    //A unique UUID that will be used as a common identifier for both devices in Bluetooth
    //generated thanks to the website https://www.uuidgenerator.net/
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
}

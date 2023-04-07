package com.bg.masterthesis;

import android.bluetooth.BluetoothAdapter;
import android.graphics.DashPathEffect;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface Constants {

    int     REQUEST_BT_CONNECT = 0,
            REQUEST_BT_SCAN = 1,
            REQUEST_BT_ADVERTISE = 2,
            REQUEST_BT_SEND_DATA_FILE = 3,
            REQUEST_ACCESS_FINE_LOCATION = 4,
            REQUEST_CHANGE_WIFI_STATE = 5,
            REQUEST_ACCESS_WIFI_STATE = 6,
            REQUEST_INTERNET = 7,
            REQUEST_NEARBY_WIFI_DEVICES = 8,
            REQUEST_ACCESS_NETWORK_STATE = 9,
            REQUEST_CHANGE_NETWORK_STATE = 10;

    List<Integer>   requestBtCodes = Arrays.asList(REQUEST_BT_CONNECT, REQUEST_BT_SCAN, REQUEST_BT_ADVERTISE, REQUEST_BT_SEND_DATA_FILE),
                    requestWifiCodes = Arrays.asList(REQUEST_CHANGE_WIFI_STATE, REQUEST_ACCESS_WIFI_STATE, REQUEST_INTERNET,
                            REQUEST_NEARBY_WIFI_DEVICES, REQUEST_ACCESS_NETWORK_STATE, REQUEST_CHANGE_NETWORK_STATE);

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

    int[] bufferSizes = {
            size1Kb * 4,
            size1Kb * 8,
            size1Kb * 16,
            size1Kb * 32,
            size1Kb * 64,
            size1Kb * 128,
            size1Kb * 256,
            0
            };

    float   axisValueSize = 16f,
            columnWidth = 0.7f,
            distanceBetweenXAxisData = 1f,
            distanceBetweenYAxisData = 0.001f,
            minimumYAxisValue = 0f;

    String  NAME = "MASTER_THESIS",
            titleLogActivity = "LOG",
            titleBtActivity = "Bluetooth",
            titleWifiActivity = "Wi-Fi",
            wifiDirect = "Wi-Fi Direct",
            connectionBt = "BT",
            connectionWiFi = "WIFI",
            uploadTimeUnit = "[s]",
            qualitySignalUnit = "[%]",
            fileSizeUnitBytes = "Bytes",
            fileSizeUnitKB = "KB",
            fileSizeUnitMB = "MB";

    //A unique UUID that will be used as a common identifier for both devices in Bluetooth
    //generated thanks to the website https://www.uuidgenerator.net/
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    DashPathEffect girdLineStyle = new DashPathEffect(new float[]{10f, 5f}, 0f);
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
}

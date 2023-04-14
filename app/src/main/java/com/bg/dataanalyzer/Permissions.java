package com.bg.dataanalyzer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions extends MainActivity{
    Context context;

    public Permissions (Context context) {
        this.context = context;
    }
    public boolean checkAPI29() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    boolean checkAPI31() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    boolean checkAPI33() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    public boolean checkPermissionBt() {
        if (!checkBtConnect()) {
            getPermissionBtConnect();
            return false;
        }
        else if (!checkBtScan()) {
            getPermissionBtScan();
            return false;
        }
        else if (!checkBtAdvertise()) {
            getPermissionBtAdvertise();
            return false;
        }
        else return true;
    }

    public boolean checkPermissionWifi() {
        if(!checkAccessWiFiState()) {
            getPermissionAccessWiFiState();
            return false;
        }
        else if(!checkChangeWiFiState()) {
            getPermissionChangeWiFiState();
            return false;
        }
        else if(!checkInternet()) {
            getPermissionInternet();
            return false;
        }
        else if(!checkAccessNetworkState()) {
            getPermissionAccessNetworkState();
            return false;
        }
        else if(!checkChangeNetworkState()) {
            getPermissionChangeNetworkState();
            return false;
        }
        else if(!checkNearbyWiFiDevices()) {
            getPermissionNearbyWiFiDevices();
            return false;
        }
        else return true;
    }

    //region checkPermission

    boolean checkBtConnect() {
        if (checkAPI31()) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }
    boolean checkBtScan() {
        if (checkAPI31()) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }
    boolean checkBtAdvertise() {
        if (checkAPI31()) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }
    boolean checkAccessFineLocation() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    boolean checkChangeWiFiState() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    boolean checkAccessWiFiState() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    boolean checkInternet() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    boolean checkNearbyWiFiDevices() {
        if(checkAPI33()) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }

    boolean checkAccessNetworkState() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    boolean checkChangeNetworkState() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    //endregion

    //region getPermission

    void getPermissionBtConnect() {
        if (checkAPI31()) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, Constants.REQUEST_BT_CONNECT);
        }
    }
    void getPermissionBtScan() {
        if (checkAPI31()) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_SCAN}, Constants.REQUEST_BT_SCAN);
        }
    }
    void getPermissionBtAdvertise() {
        if (checkAPI31()) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, Constants.REQUEST_BT_ADVERTISE);
        }
    }
    public void getPermissionAccessFineLocation() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_ACCESS_FINE_LOCATION);
    }

    void getPermissionChangeWiFiState() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, Constants.REQUEST_CHANGE_WIFI_STATE);
    }

    void getPermissionAccessWiFiState() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, Constants.REQUEST_ACCESS_WIFI_STATE);
    }

    void getPermissionInternet() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.INTERNET}, Constants.REQUEST_INTERNET);
    }

    void getPermissionNearbyWiFiDevices() {
        if (checkAPI33()) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, Constants.REQUEST_NEARBY_WIFI_DEVICES);
        }
    }

    void getPermissionAccessNetworkState() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, Constants.REQUEST_ACCESS_NETWORK_STATE);
    }

    void getPermissionChangeNetworkState() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CHANGE_NETWORK_STATE}, Constants.REQUEST_CHANGE_NETWORK_STATE);
    }

    //endregion


}

package com.example.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import com.example.masterthesis.Constants;
import com.example.masterthesis.ui.DeclarationOfUIVar;
import com.example.masterthesis.Logs;

import java.io.IOException;

public class ClientBt extends Thread {
    @SuppressLint("StaticFieldLeak")
    static Context context;
    static BluetoothSocket socket;
    final BluetoothDevice device;
    static Logs.ListLog LOG = new Logs.ListLog();

    public ClientBt(Context context, BluetoothDevice device)
    {
        ClientBt.context = context;
        this.device = device;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        LOG.addLog("A client Bt has started");
        createSocket();
        Constants.bluetoothAdapter.cancelDiscovery();
        tryConnecting();

        LOG.addLog("The connection attempt succeeded");
        DeclarationOfUIVar.viewAfterSuccessConnectionOnClientBt();

        if(!socket.isConnected()) {
            closeSocketClient();
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show());
        }
    }

    @SuppressLint("MissingPermission")
    void createSocket()
    {
        try {
            socket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
        } catch (IOException e) {
            LOG.addLog("Socket's create() method failed", e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    void tryConnecting()
    {
        try {
            socket.connect();
        } catch (IOException e) {
            LOG.addLog("Unable to connect", e.getMessage());
            closeSocketClient();
        }
    }

    public static void closeSocketClient() {
        try {
            socket.close();
            LOG.addLog("Client socket closed");
        } catch (IOException e) {
            LOG.addLog("Could not close the client socket", e.getMessage());
        }
    }

    public static BluetoothSocket getSocket()
    {
        return socket;
    }
}

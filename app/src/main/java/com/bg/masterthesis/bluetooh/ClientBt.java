package com.bg.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import com.bg.masterthesis.Constants;
import com.bg.masterthesis.ui.DeclarationOfUIVar;
import com.bg.masterthesis.Logs;

import java.io.IOException;

public class ClientBt extends Thread {
    Context context;
    static BluetoothSocket socket;
    BluetoothDevice device;
    final Logs LOG = new Logs();
    DeclarationOfUIVar declarationUI;

    public ClientBt(Context context, BluetoothDevice device) {
        this.context = context;
        this.device = device;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        LOG.addLog("A client Bt has started");
        createSocket();
        Constants.bluetoothAdapter.cancelDiscovery();
        tryConnecting();

        LOG.addLog("The connection attempt succeeded");
        declarationUI = new DeclarationOfUIVar(context);
        declarationUI.viewAfterSuccessConnectionOnClientBt();

        if(!socket.isConnected()) {
            closeSocketClient();
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show());
        }
    }

    @SuppressLint("MissingPermission")
    void createSocket() {
        try {
            socket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
        }
        catch (IOException e) {
            LOG.addLog("Socket's create() method failed", e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    void tryConnecting() {
        try {
            socket.connect();
        }
        catch (IOException e) {
            LOG.addLog("Unable to connect", e.getMessage());
            closeSocketClient();
        }
    }

    void closeSocketClient() {
        try {
            socket.close();
            LOG.addLog("Client socket closed");
        }
        catch (IOException e) {
            LOG.addLog("Could not close the client socket", e.getMessage());
        }
    }

    public static BluetoothSocket getSocket() {
        return socket;
    }
}

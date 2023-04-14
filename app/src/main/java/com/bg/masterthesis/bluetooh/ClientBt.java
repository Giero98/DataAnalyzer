package com.bg.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import com.bg.masterthesis.Constants;
import com.bg.masterthesis.R;
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
        LOG.addLog(context.getString(R.string.bt_client_on));
        createSocket();
        Constants.bluetoothAdapter.cancelDiscovery();
        tryConnecting();

        LOG.addLog(context.getString(R.string.connect_bt_success));
        declarationUI = new DeclarationOfUIVar(context);
        declarationUI.viewAfterSuccessConnectionOnClientBt();

        if(!socket.isConnected()) {
            closeSocketClient();
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, context.getString(R.string.disconnect), Toast.LENGTH_SHORT).show());
        }
    }

    @SuppressLint("MissingPermission")
    void createSocket() {
        try {
            socket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
        }
        catch (IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.failed_create_socket_bt),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.failed_create_socket_bt), e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    void tryConnecting() {
        try {
            socket.connect();
        }
        catch (IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.unable_connect),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.unable_connect), e.getMessage());
            closeSocketClient();
        }
    }

    void closeSocketClient() {
        try {
            socket.close();
            LOG.addLog(context.getString(R.string.client_socket_close));
        }
        catch (IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.client_socket_close_error),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.client_socket_close_error), e.getMessage());
        }
    }

    public static BluetoothSocket getSocket() {
        return socket;
    }
}

package com.bg.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.bg.masterthesis.Constants;
import com.bg.masterthesis.R;
import com.bg.masterthesis.ui.DeclarationOfUIVar;
import com.bg.masterthesis.Logs;
import com.bg.masterthesis.file.SavingData;

import java.io.IOException;

public class ServerBt extends Thread {
    public static boolean running;
    final Logs LOG = new Logs();
    DeclarationOfUIVar declarationUI;
    Context context;
    static BluetoothSocket socket;
    BluetoothServerSocket serverSocket;

    public ServerBt(Context context) {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    public void run() {
        LOG.addLog(context.getString(R.string.bt_server_on));
        startServerSocket();
        waitingForConnection();
        if (socket != null) {
            LOG.addLog(context.getString(R.string.connect_bt_success));
            declarationUI = new DeclarationOfUIVar(context);
            declarationUI.viewAfterSuccessConnectionOnServerBt();
            savingData();
        }
        closeServerSocket();
        LOG.addLog(context.getString(R.string.bt_server_off));
    }

    @SuppressLint("MissingPermission")
    void startServerSocket() {
        try {
            serverSocket = Constants.bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME, Constants.MY_UUID);
        }
        catch (IOException e) {
            LOG.addLog(context.getString(R.string.socket_bt_failed), e.getMessage());
        }
    }

    void closeServerSocket() {
        try {
            serverSocket.close();
        }
        catch (IOException e) {
            LOG.addLog(context.getString(R.string.error_close_server_socket_bt), e.getMessage());
        }
    }

    void waitingForConnection() {
        try {
            socket = serverSocket.accept();
        }
        catch (IOException e) {
            LOG.addLog(context.getString(R.string.failed_socket_bt), e.getMessage());
        }
    }

    void savingData() {
        SavingData savingData = new SavingData(LOG, context, socket);
        while(running) {
            savingData.startSavingData();
        }
        declarationUI.updateViewWhenDisconnected();
        SavingData.closeStreams(LOG, context);
        closeSocket();
    }

    void closeSocket() {
        try {
            socket.close();
        }
        catch (IOException ex) {
            LOG.addLog(context.getString(R.string.error_close_socket_bt), ex.getMessage());
        }
    }

    public static BluetoothSocket getSocket() {
        return socket;
    }
}

package com.bg.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.bg.masterthesis.Constants;
import com.bg.masterthesis.ui.DeclarationOfUIVar;
import com.bg.masterthesis.Logs;
import com.bg.masterthesis.file.SavingData;

import java.io.IOException;

public class ServerBt extends Thread {
    public static boolean running;
    final Logs LOG = new Logs();
    DeclarationOfUIVar declarationUI;
    final Context context;
    static BluetoothSocket socket;
    BluetoothServerSocket serverSocket;

    public ServerBt(Context context)
    {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    public void run() {
        LOG.addLog("A server Bt has started");
        startServerSocket();
        waitingForConnection();
        if (socket != null) {
            LOG.addLog("The connection by Bt attempt succeeded");
            declarationUI = new DeclarationOfUIVar(context);
            declarationUI.viewAfterSuccessConnectionOnServerBt();
            savingData();
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOG.addLog("Error closing output stream on Bt connection:", e.getMessage());
            }
            LOG.addLog("The server Bt has ended");
        }
    }

    @SuppressLint("MissingPermission")
    void startServerSocket()
    {
        try {
            serverSocket = Constants.bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME, Constants.MY_UUID);
        } catch (IOException e) {
            LOG.addLog("Socket's Bt listen() method failed", e.getMessage());
        }
    }
    void waitingForConnection()
    {
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            LOG.addLog("Socket's Bt accept() method failed", e.getMessage());
        }
    }

    void savingData()
    {
        SavingData savingData = new SavingData(LOG, context, socket);
        while(running) {
            savingData.startSavingData();
            if(!socket.isConnected()) {
                LOG.addLog("running stop");
                running = false;
            }
        }
        declarationUI.updateViewWhenDisconnected();
        LOG.addLog("updateView");
        SavingData.closeStreams(LOG);
        LOG.addLog("closeStreams");
        try {
            socket.close();
        } catch (IOException ex) {
            LOG.addLog("Error closing socket's Bt", ex.getMessage());
        }
    }

    public static BluetoothSocket getSocket() {return socket;}
}

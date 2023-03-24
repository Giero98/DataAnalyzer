package com.example.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.example.masterthesis.Constants;
import com.example.masterthesis.ui.DeclarationOfUIVar;
import com.example.masterthesis.Logs;
import com.example.masterthesis.file.SavingData;

import java.io.IOException;

public class ServerBt extends Thread {
    public static boolean running;
    final Logs.ListLog LOG = new Logs.ListLog();
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
            DeclarationOfUIVar.viewAfterSuccessConnectionOnServerBt();
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
        while(running) {
            new SavingData(LOG, context, socket);
            if(!socket.isConnected())
                try {
                    DeclarationOfUIVar.updateViewWhenDisconnected();
                    running = false;
                    SavingData.closeStream();
                    socket.close();
                } catch (IOException ex) {
                    LOG.addLog("Error closing input stream and socket's Bt", ex.getMessage());
                }
        }
    }

    public static BluetoothSocket getSocket() {return socket;}
}

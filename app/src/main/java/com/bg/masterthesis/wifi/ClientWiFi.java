package com.bg.masterthesis.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;

import com.bg.masterthesis.ui.DeclarationOfUIVar;
import com.bg.masterthesis.Logs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class ClientWiFi extends Thread {
    static Socket socket = new Socket();
    String serverAddress;
    int port;
    final Logs.ListLog LOG = new Logs.ListLog();
    Context context;

    public ClientWiFi(Context context, WifiP2pInfo wifiDirectInfo, String portNumber) {
        this.context = context;
        this.serverAddress = wifiDirectInfo.groupOwnerAddress.getHostAddress();
        port = Integer.parseInt(portNumber);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(serverAddress,port),5000);

            DeclarationOfUIVar declarationUI = new DeclarationOfUIVar(context);
            declarationUI.updateViewWhenStartClientWifi();
        } catch(IOException e) {
            LOG.addLog("Client socket creation error with host", e.getMessage());
        }
    }

    public static Socket getSocket()
    {
        return socket;
    }
}
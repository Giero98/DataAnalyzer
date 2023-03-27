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
    static Logs.ListLog LOG = new Logs.ListLog();

    public ClientWiFi(WifiP2pInfo wifiDirectInfo, String portNumber) {
        this.serverAddress = wifiDirectInfo.groupOwnerAddress.getHostAddress();
        port = Integer.parseInt(portNumber);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(serverAddress,port),5000);

            DeclarationOfUIVar.updateViewWhenStartClientWifi();
        } catch(IOException e) {
            LOG.addLog("Client socket creation error with host", e.getMessage());
        }
    }

    public static Socket getSocket()
    {
        return socket;
    }
}
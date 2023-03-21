package com.example.masterthesis.wifi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.widget.TextView;

import com.example.masterthesis.Logs;
import com.example.masterthesis.R;
import com.example.masterthesis.SendReceive;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientWiFi extends Thread{
    static Socket socket = new Socket();
    String serverAddress, serverName;
    int port;
    Logs.ListLog LOG;
    Context WiFi;

    public ClientWiFi(Logs.ListLog LOG, Context WiFi, WifiP2pInfo wifiDirectInfo, String portNumber) {
        this.LOG = LOG;
        this.WiFi = WiFi;
        this.serverAddress = wifiDirectInfo.groupOwnerAddress.getHostAddress();
        Thread getServerName = new Thread(() -> this.serverName = wifiDirectInfo.groupOwnerAddress.getHostName());
        getServerName.start();
        try {
            getServerName.join();
        } catch (InterruptedException e) {
            LOG.addLog("Server name retrieval error", e.getMessage());
        }
        port = Integer.parseInt(portNumber);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(serverAddress,port),5000);

            TextView textView_connected = ((Activity) WiFi).findViewById(R.id.textView_connected);
            ((Activity) WiFi).runOnUiThread(() ->
                    textView_connected.setText("Connected as a client with " + serverName));

            SendReceive send = new SendReceive(LOG,WiFi,socket);
            send.start();

        } catch(IOException e) {
            LOG.addLog("Client socket creation error with host", e.getMessage());
        }
    }

    public static Socket getSocket()
    {
        return socket;
    }
}
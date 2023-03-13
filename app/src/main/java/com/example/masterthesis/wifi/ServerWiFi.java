package com.example.masterthesis.wifi;

import static com.example.masterthesis.wifi.WiFi.wifiDirectChannel;
import static com.example.masterthesis.wifi.WiFi.wifiDirectManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.widget.TextView;

import com.example.masterthesis.Logs;
import com.example.masterthesis.R;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerWiFi extends Thread{
    static Socket socket;
    static ServerSocket serverSocket;
    final Logs.ListLog LOG;
    String deviceName;
    static int port;
    Context WiFi;

    public ServerWiFi(Logs.ListLog LOG, Context WiFi, String deviceName){
        this.LOG = LOG;
        this.WiFi = WiFi;
        this.deviceName = deviceName;
    }


    @Override
    public void run(){
        generatePort();
    }

    void generatePort()
    {
        int i=0;
        do {
            i++;
            port = (int) (Math.random() * 60000 + 1024);
            LOG.addLog("Port number "+i+" = "+port);
        }while(!createServerSocket());
    }

    boolean createServerSocket() {
        try (ServerSocket serverSocketTest = new ServerSocket(port)) {
            serverSocket = serverSocketTest;
            startRegistration();
            startConnect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void startRegistration() {
        Map record = new HashMap();
        record.put("PORT", String.valueOf(port));

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("Port", "WiFiDirect", record);

        wifiDirectManager.addLocalService(wifiDirectChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                LOG.addLog("Added network service providing port");
            }
            @Override
            public void onFailure(int arg0) {
                LOG.addLog("Failed added network service providing port", String.valueOf(arg0));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    void startConnect()
    {
        do {
            try {
                socket = serverSocket.accept();

                TextView textView_connected = ((Activity) WiFi).findViewById(R.id.textView_connected);
                ((Activity) WiFi).runOnUiThread(() ->
                        textView_connected.setText("Connected as a server with " + deviceName));
            } catch (IOException e) {
                LOG.addLog("Client connection error", e.getMessage());
            }
        }while(socket==null);
    }

    public static int getPort()
    {
        return port;
    }

    public static Socket getSocket()
    {
        return socket;
    }

    public static ServerSocket getServerSocket()
    {
        return serverSocket;
    }
}

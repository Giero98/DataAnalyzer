package com.example.masterthesis.wifi;

import static com.example.masterthesis.wifi.WiFi.wifiDirectChannel;
import static com.example.masterthesis.wifi.WiFi.wifiDirectManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

import com.example.masterthesis.Constants;
import com.example.masterthesis.ui.DeclarationOfUIVar;
import com.example.masterthesis.Logs;
import com.example.masterthesis.file.SavingData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerWiFi extends Thread{
    static Socket socket;
    static ServerSocket serverSocket;
    static Logs.ListLog LOG = new Logs.ListLog();
    int port;
    Context context;

    public ServerWiFi(Context context){
        this.context = context;
    }

    @Override
    public void run(){
        LOG.addLog("A server Wifi has started");
        generatePort();
    }

    void generatePort()
    {
        int i=0;
        do {
            i++;
            port = (int) (Math.random() * Constants.rangePossiblePortsToConnect +
                    Constants.smallestPortToConnect);
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
                DeclarationOfUIVar.updateViewWhenStartServerWifi();
                savingData();
            } catch (IOException e) {
                LOG.addLog("Connection by Wifi Direct error", e.getMessage());
            }
        }while(socket==null);
    }

    void savingData() {
        while (socket!=null)
        {
            new SavingData(LOG, context, socket);
        }
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

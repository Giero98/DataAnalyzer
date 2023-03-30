package com.bg.masterthesis.wifi;

import static com.bg.masterthesis.wifi.WiFi.wifiDirectChannel;
import static com.bg.masterthesis.wifi.WiFi.wifiDirectManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

import com.bg.masterthesis.Constants;
import com.bg.masterthesis.ui.DeclarationOfUIVar;
import com.bg.masterthesis.Logs;
import com.bg.masterthesis.file.SavingData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ServerWiFi extends Thread{
    static Socket socket;
    static ServerSocket serverSocket;
    final Logs LOG = new Logs();
    int port;
    Context context;

    public ServerWiFi(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        LOG.addLog("A server Wifi has started");
        generatePort();
    }

    void generatePort() {
        int i=0;
        do {
            i++;
            port = (int) (Math.random() * Constants.rangePossiblePortsToConnect +
                    Constants.smallestPortToConnect);
            LOG.addLog("Port number "+i+" = "+port);
        } while(!createServerSocket());
    }

    boolean createServerSocket() {
        try (ServerSocket serverSocketTest = new ServerSocket(port)) {
            serverSocket = serverSocketTest;
            startRegistration();
            startConnect();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public void startRegistration() {
        @SuppressWarnings("rawtypes")
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
    void startConnect() {
        do {
            try {
                socket = serverSocket.accept();

                DeclarationOfUIVar declarationUI = new DeclarationOfUIVar(context);
                declarationUI.updateViewWhenStartServerWifi();
                savingData();
            }
            catch (IOException e) {
                LOG.addLog("Connection by Wifi Direct error", e.getMessage());
            }
        } while(socket==null);
    }

    void savingData() {
        SavingData savingData = new SavingData(LOG, context, socket);
        while (socket!=null) {
            savingData.startSavingData();
        }
    }

    public static Socket getSocket() {
        return socket;
    }

    public static ServerSocket getServerSocket() {
        return serverSocket;
    }
}

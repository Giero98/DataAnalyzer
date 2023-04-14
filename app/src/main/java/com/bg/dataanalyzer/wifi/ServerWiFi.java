package com.bg.dataanalyzer.wifi;

import static com.bg.dataanalyzer.wifi.WiFi.wifiDirectChannel;
import static com.bg.dataanalyzer.wifi.WiFi.wifiDirectManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.widget.Toast;

import com.bg.dataanalyzer.Constants;
import com.bg.dataanalyzer.R;
import com.bg.dataanalyzer.ui.DeclarationOfUIVar;
import com.bg.dataanalyzer.Logs;
import com.bg.dataanalyzer.file.SavingData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

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
        LOG.addLog(context.getString(R.string.server_wifi_on));
        generatePort();
    }

    void generatePort() {
        int i=0;
        do {
            i++;
            port = (int) (Math.random() * Constants.rangePossiblePortsToConnect +
                    Constants.smallestPortToConnect);
            LOG.addLog(context.getString(R.string.port_number) +" "+i+" = "+ port);
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

    @SuppressLint("MissingPermission")
    public void startRegistration() {
        HashMap<String,String> record = new HashMap<>();
        record.put(context.getString(R.string.port), String.valueOf(port));

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(context.getString(R.string.port), Constants.wifiDirect, record);

        wifiDirectManager.addLocalService(wifiDirectChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                LOG.addLog(context.getString(R.string.adding_port));
            }
            @Override
            public void onFailure(int arg0) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context,context.getString(R.string.adding_port_error),Toast.LENGTH_SHORT).show());
                LOG.addLog(context.getString(R.string.adding_port_error), String.valueOf(arg0));
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
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context,context.getString(R.string.connect_by_wifi_direct_error),Toast.LENGTH_SHORT).show());
                LOG.addLog(context.getString(R.string.connect_by_wifi_direct_error), e.getMessage());
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

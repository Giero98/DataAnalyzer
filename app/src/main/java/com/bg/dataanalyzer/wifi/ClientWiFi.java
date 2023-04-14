package com.bg.dataanalyzer.wifi;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.widget.Toast;

import com.bg.dataanalyzer.Constants;
import com.bg.dataanalyzer.R;
import com.bg.dataanalyzer.ui.DeclarationOfUIVar;
import com.bg.dataanalyzer.Logs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class ClientWiFi extends Thread {
    static Socket socket = new Socket();
    String serverAddress;
    int port;
    final Logs LOG = new Logs();
    Context context;

    public ClientWiFi(Context context, WifiP2pInfo wifiDirectInfo, String portNumber) {
        this.context = context;
        this.serverAddress = wifiDirectInfo.groupOwnerAddress.getHostAddress();
        port = Integer.parseInt(portNumber);
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(serverAddress,port), Constants.connectionTimeout);

            DeclarationOfUIVar declarationUI = new DeclarationOfUIVar(context);
            declarationUI.updateViewWhenStartClientWifi();
        }
        catch(IOException e) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,context.getString(R.string.socket_client_close_error),Toast.LENGTH_SHORT).show());
            LOG.addLog(context.getString(R.string.socket_client_close_error), e.getMessage());
        }
    }

    public static Socket getSocket() {
        return socket;
    }
}
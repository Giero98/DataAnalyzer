package com.example.masterthesis.wifi;

import com.example.masterthesis.Logs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientWiFi extends Thread{

    Socket socket = new Socket();
    String deviceAddress;
    final Logs.ListLog LOG;

    public ClientWiFi(Logs.ListLog LOG, String deviceAddress){
        this.LOG = LOG;
        this.deviceAddress = deviceAddress;
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(deviceAddress,8888),500);
        } catch(IOException e) {
            LOG.addLog("Client socket creation error with host", e.getMessage());
        }
    }
}

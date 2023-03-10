package com.example.masterthesis.wifi;

import com.example.masterthesis.Logs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWiFi extends Thread{
    Socket socket;
    ServerSocket serverSocket;
    final Logs.ListLog LOG;

    public ServerWiFi(Logs.ListLog LOG){
        this.LOG = LOG;
    }

    @Override
    public void run(){
        try{
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();
        } catch (IOException e)
        {
            LOG.addLog("Client connection error", e.getMessage());
        }
    }
}

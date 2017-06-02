package com.example.simpleclient;

import android.app.Application;
import android.widget.Toast;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by acer on 2017/6/1.
 */

public class ApplicationUtil extends Application {
    public static final String ADDRESS = "192.168.1.217";
    public static final int PORT = 10001;

    private Socket socket = null;

    private DataOutputStream dos = null;
    private DataInputStream dis = null;

    public void init() throws IOException, Exception {
        if (socket == null) { // only create socket once
            try {
                socket = new Socket(ADDRESS, PORT);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void finish() throws IOException, Exception {
        if (socket != null) {
        //    socket.shutdownInput();
        //    socket.shutdownOutput();
            try {
                dis.close();
                dos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            socket.close();
            dis = null;
            dos = null;
            socket = null;
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public void setDos(DataOutputStream dos) {
        this.dos = dos;
    }

    public DataInputStream getDis() {
        return dis;
    }

    public void setDis(DataInputStream dis) {
        this.dis = dis;
    }
}

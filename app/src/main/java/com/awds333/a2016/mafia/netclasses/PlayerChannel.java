package com.awds333.a2016.mafia.netclasses;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PlayerChannel {
    private Thread thread;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream reader;
    private int id;
    private int port;
    private InetAddress address;
    private String password;
    private boolean lock;

    public String getPassword() {
        return password;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void unlock(){
        lock = false;
    }

    public boolean isLock(){
        return lock;
    }

    public PlayerChannel(Thread thread, Socket socket, int id, int port, String pass) {
        this.thread = thread;
        this.socket = socket;
        this.id = id;
        this.port = port;
        lock = true;
        password = pass;
        address=socket.getInetAddress();

        try {
            reader = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            sendByteMessage(message.getBytes("UTF-8"));
        } catch (IOException e) {
        }
    }

    public void sendByteMessage(byte[] bytes) throws IOException {
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public String getMessage() throws IOException {
        byte data[] =getByteMessage();
        String message = new String(data,"UTF-8");
        return message;
    }

    public byte[] getByteMessage() throws IOException {
        int length = reader.readInt();
        byte[] data = new byte[length];
        reader.readFully(data);
        return  data;
    }

    public void close() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }
}

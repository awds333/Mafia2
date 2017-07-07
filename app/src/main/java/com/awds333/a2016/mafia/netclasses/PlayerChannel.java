package com.awds333.a2016.mafia.netclasses;

import android.support.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerChannel {
    private Thread thread,mesThread;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream reader;
    private int id;
    private int port;
    private String password;
    private boolean lock, active;
    private ArrayList<byte[]> messageQueue;
    private Timer timer;
    private TimerTask timerTask;

    public String getPassword() {
        return password;
    }

    public void unlock(){
        lock = false;
    }

    public boolean isLock(){
        return lock;
    }

    public PlayerChannel(Thread thread, final Socket socket, int id, int port, String pass) {
        this.thread = thread;
        this.socket = socket;
        this.id = id;
        this.port = port;
        lock = true;
        active = true;
        password = pass;
        messageQueue = new ArrayList<byte[]>();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                close();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask,3000);
        try {
            reader = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active) {
                    byte[] message = addReadeMessage(null);
                    if (message != null) {
                        try {
                            out.writeInt(message.length);
                            out.write(message);
                        } catch (IOException e) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e1) {
                                e.printStackTrace();
                            }
                        }
                    } else try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mesThread.start();
    }

    public void sendMessage(String message) {
        try {
            sendByteMessage(message.getBytes("UTF-8"));
        } catch (IOException e) {
        }
    }

    public void sendByteMessage(byte[] bytes) throws IOException {
        addReadeMessage(bytes);
    }

    private synchronized byte[] addReadeMessage(@Nullable byte[] bytes){
        if(bytes!=null){
            messageQueue.add(bytes);
            return null;
        } else {
            if(messageQueue.size()>0){
                byte[] retBytes = messageQueue.get(0);
                messageQueue.remove(0);
                return retBytes;
            }
            return null;
        }
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
        if(data.length<50){
            String message = new String(data,"UTF-8");
            if(message.equals("ping")){
                timer.cancel();
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        close();
                    }
                };
                timer.schedule(timerTask,3000);
                return getByteMessage();
            }
        }
        return  data;
    }

    public void close() {
        active = false;
        timer.cancel();
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

package com.awds333.a2016.mafia.netclasses;

import android.os.Handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpPinger {
    private Thread[] threads;
    private int myIpc;
    private String ip;
    private Handler mhandler;
    private int ping;
    private int count;

    public IpPinger(int treadCount, int[] myIp,Handler handler, int pingTyme) {
        myIpc = myIp[3];
        ip = myIp[0] + "." + myIp[1] + "." + myIp[2] + ".";
        this.mhandler = handler;
        ping = pingTyme;
        count = 1;
        threads = new Thread[treadCount];
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (count == myIpc)
                    count++;
                int lcount = count;
                count++;
                if (lcount <= 255) {
                    try {
                        InetAddress address = InetAddress.getByName(ip + lcount);
                        if (address.isReachable(ping)) {
                            mhandler.sendEmptyMessage(lcount);
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mhandler.sendEmptyMessage(0);
                    run();
                }
            }
        };
        for (Thread startThread : threads) {
            startThread = new Thread(runnable);
            startThread.start();
        }
    }
}

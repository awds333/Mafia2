package com.awds333.a2016.mafia.netclasses;

import android.os.Handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpPinger {
    private Thread[] threads;
    private String ip;
    private Handler mhandler;
    private int ping, count, myIpc, pingRepits;
    private boolean run;

    public IpPinger(int treadCount, int[] myIp, Handler handler, int pingTyme, int pingRepit) {
        myIpc = myIp[3];
        ip = myIp[0] + "." + myIp[1] + "." + myIp[2] + ".";
        this.mhandler = handler;
        pingRepits = pingRepit;
        ping = pingTyme;
        count = 1;
        run = true;
        threads = new Thread[treadCount];
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (run) {

                    if (count == myIpc)
                        count++;
                    int lcount = count;
                    count++;
                    if (lcount <= 255) {
                        try {
                            InetAddress address = InetAddress.getByName(ip + lcount);
                            for (int i = 0; i < pingRepits; i++)
                                if (address.isReachable(ping)) {
                                    if (run) mhandler.sendEmptyMessage(lcount);
                                    break;
                                }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mhandler.sendEmptyMessage(0);
                        run();
                    } else run = false;
                }
            }
        };
        for (Thread startThread : threads) {
            startThread = new Thread(runnable);
            startThread.start();
        }
    }

    public void stop() {
        if (run) {
            run = false;
        }
    }
}

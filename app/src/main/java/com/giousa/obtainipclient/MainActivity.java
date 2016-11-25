package com.giousa.obtainipclient;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private Button mObtainIP,mStopIP;
    private TextView mIpCount,mHostIP;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isPause = false;
    private static int delay = 0;  //延迟0s
    private static int period = 3000;  //重复执行2s
    private static final int HOST_IP = 110;
    private int mSendCount=0;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case HOST_IP:
                    Log.d(TAG,"HOST_IP="+HOST_IP);
                    achieveHostIP();
                    mSendCount++;
                    mIpCount.setText("请求IP次数："+mSendCount);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mObtainIP = (Button) findViewById(R.id.btn_obtainip);
        mStopIP = (Button) findViewById(R.id.btn_stop);
        mIpCount = (TextView) findViewById(R.id.tv_count);
        mHostIP = (TextView) findViewById(R.id.tv_ip);
        mObtainIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
                mSendCount = 0;
            }
        });

        mStopIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
                mSendCount = 0;
                mIpCount.setText("发送IP次数："+mSendCount);
                mHostIP.setText("获取IP：");
            }
        });
    }


    private void achieveHostIP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"achieve host ip");
                int port = 9999;
                DatagramSocket ds = null;
                DatagramPacket dp = null;
                byte[] buf = new byte[1024];
                StringBuffer sbuf = new StringBuffer();
                try {
                    ds = new DatagramSocket(port);
                    dp = new DatagramPacket(buf, buf.length);
                    Log.d(TAG,"监听广播端口打开：");
                    ds.receive(dp);
                    ds.close();
                    int i;
                    for(i=0;i<1024;i++){
                        if(buf[i] == 0){
                            break;
                        }
                        sbuf.append((char) buf[i]);
                    }

                    if(sbuf != null){
                        String mConfigServerIP = sbuf.toString();
                        Log.d(TAG,"收到广播: "+mConfigServerIP);
                        mHostIP.setText("获取IP："+mConfigServerIP);
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG,"timer start");
                    sendMessage(HOST_IP);
                    do {
                        try {
                            Log.i(TAG, "sleep(5000)...");
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);

                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, delay, period);

    }

    private void stopTimer(){

        Log.d(TAG,"timer end");

        if(mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }

        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }

    public void sendMessage(int id){
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
    }
}

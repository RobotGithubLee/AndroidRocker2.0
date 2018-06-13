package com.kongqw.kqwrockerdemo.socket;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.kongqw.rockerlibrary.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;



/**
 * Created by Lee on 2018/1/2.
 */

public class client {
    String TAG="client";
    private ConnectInterface myConnectInterface;
    private static  Socket socket = null;
    private static OutputStream writer=null;

    private static BufferedReader reader = null;
    private static Handler myHandler;
    private String line;
    private static String IPAdr="192.168.3.84";//"10.14.0.77";
    private static int PORT=8088;

    public static boolean isConnected = false;


    private static final int MSG_DATA_CHANGE = 0x11;
    private static final int MSG_CONNECT_FAIL = 0x20;
    private static final int MSG_CONNECT_SUCCED = 0x21;


    public client(Handler mHandler){
        myHandler=mHandler;
    }

   public void setIP(String IP){
       IPAdr=IP;
   }
    /* 连接按钮处理函数：建立Socket连接 */
    public void connect() {

        if (false == isConnected) {
            new Thread() {
                public void run() {
                    try {
                        socket = new Socket(IPAdr, PORT);
                        writer = socket.getOutputStream();
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        isConnected = true;
                        Log.i(TAG, "connect success!!!");


                        Message msg = myHandler.obtainMessage();
                        msg.what=MSG_CONNECT_SUCCED;
                        myHandler.sendMessage(msg);
                        msg.obj = "connect success!!!";



                        char[] buf = new char[1024];
                        int i;
                        while ((i = reader.read(buf, 0, 512)) != -1) {
                            line = new String(buf, 0, i);
                            Message msgbuf = myHandler.obtainMessage();
                            msgbuf.what = MSG_DATA_CHANGE;
                            msgbuf.obj = line;
                            myHandler.sendMessage(msgbuf);
                            Log.i(TAG, "send to handler buf="+buf);
                        }
                    } catch (UnknownHostException e) {
                        sendHandle("Socket连接失败");
                        e.printStackTrace();
                        isConnected = false;
                    } catch (IOException e) {
                       // sendHandle("Socket连接失败");
                        e.printStackTrace();
                        isConnected = false;
                    }
                }
            }.start();

        }else {
            Message msg = myHandler.obtainMessage();
            msg.what=MSG_CONNECT_SUCCED;
            myHandler.sendMessage(msg);
            msg.obj = "Already connect !!!";
            Log.i(TAG, "Already connect !!!");
        }
    }

    /* 发送按钮处理函数：向输出流写数据 */
    public void send(byte[] buffer) {
        Logger.i(TAG, "sendData=" + buffer.length);
        Logger.i(TAG, " writer=" + writer+"   isConnected="+isConnected);
        if(writer!=null && isConnected) {
            try {
            /* 向输出流写数据 */
                writer.write(buffer);
                //writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                sendHandle("Socket连接失败,请重新登录！！！");
            }
        }else {
            sendHandle("Socket连接失败,请重新登录！！！");
        }
    }



    public void onDestroy() {
        isConnected = false;
        try {
            if(null != socket){
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
                Log.i(TAG, "Socket连接已断开");
            }
        } catch (IOException e) {
            Log.d(TAG,e.getMessage());
        }
    }




    private void sendHandle(String srtData){
        Message msg = myHandler.obtainMessage();
        msg.what=MSG_CONNECT_FAIL;
        msg.obj = srtData;
        myHandler.sendMessage(msg);

    }

    public interface ConnectInterface {
       public void ConnectedFalse();

       public void ConnectedSucceed();
    }

}



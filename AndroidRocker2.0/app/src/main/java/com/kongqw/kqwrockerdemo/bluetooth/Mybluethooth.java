package com.kongqw.kqwrockerdemo.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.kongqw.kqwrockerdemo.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Lee on 2017/12/29.
 */

public class Mybluethooth {
    public ConnectThread connectThread;
    public static ConnectedThread connectedThread;
    private static boolean booleanConnect = false;
    private Context mContext;

    private Handler myHandler;


    private static final int MSG_DATA_CHANGE = 0x11;
    private static final int MSG_DATA_TIME = 0x22;
    private static final int MSG_ITEM_CLICK = 0x33;

   public Mybluethooth(Context mContext,Handler mHandler){
       this.mContext=mContext;
       myHandler=mHandler;
   }

   public void initConnectThread(BluetoothDevice device){
       connectThread =new ConnectThread(device);
       connectThread.start();
   }

    public class ConnectThread extends Thread{
        private BluetoothSocket mmsocket;
        private BluetoothDevice mmdevice;


        public ConnectThread(BluetoothDevice device){
            mmdevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = mmdevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

            } catch (IOException e) {
                //Toast.makeText(MainActivity.C, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            mmsocket = tmp;
        }
        public void run(){

            try {
                mmsocket.connect();
                Log.e("","Connected");
            } catch (IOException e) {
                Log.e("",e.getMessage());
                 //Toast.makeText(mainActivity.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                try {
                    Log.e("","trying fallback...");

                    mmsocket =(BluetoothSocket) mmdevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmdevice,1);
                    mmsocket.connect();
                    Log.e("","Connected");

                   // Toast.makeText(mContext, "准备开启蓝牙", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e2) {
                    Log.e("", "Couldn't establish Bluetooth connection!");
                    //连接失败
                    return;
                }
            }


            booleanConnect = true;
            //新建一个数据交换线程
            connectedThread = new ConnectedThread(mmsocket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                mmsocket.close();
            } catch (IOException e) {

            }
        }
    }

    public class ConnectedThread extends Thread{
        private BluetoothSocket mmsocket;
        private InputStream inStream;
        private OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket){

            mmsocket = socket;
            try {
                //获得输入输出流
                inStream = mmsocket.getInputStream();
                outStream = mmsocket.getOutputStream();
            } catch (IOException e) {

            }
        }


        public void run(){
           // myHandler=mainActivity.getHandler();
            byte[] buff = new byte[1024];
            int len = 0;
            //读数据需不断监听，写不需要
            while(true){
                try {
                    len = inStream.read(buff,0,512);
                   //把读取到的数据发送给UI进行显示
                    String strBuffer = new String(buff);

                    Message toMain =  myHandler.obtainMessage();
                    toMain.what= MSG_DATA_CHANGE;
                    toMain.obj = strBuffer;
                    toMain.arg1=len;
                    myHandler.sendMessage(toMain);
                } catch (IOException e) {

                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outStream.write(buffer);
            } catch (IOException e) {

            }
        }

        public void cancel() {
            try {
                mmsocket.close();
            } catch (IOException e) {

            }
        }
    }




    public void write(byte[] buffer) {
        if (booleanConnect) {
            connectedThread.write(buffer);
        }
    }
}

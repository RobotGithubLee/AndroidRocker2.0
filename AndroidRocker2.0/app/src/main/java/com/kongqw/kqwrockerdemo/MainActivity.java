package com.kongqw.kqwrockerdemo;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kongqw.rockerlibrary.Logger;
import com.kongqw.rockerlibrary.view.RockerView;
import com.kongqw.kqwrockerdemo.bluetooth.Mybluethooth;
import com.kongqw.kqwrockerdemo.socket.client;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    String TAG="MainActivity";
    Mybluethooth mybluethooth;
    private client myclient;
    private Vibrator mVibrator;
    final Timer timer = new Timer();
    private Context mContext;
    private Switch bluetoothSwitch;
    private Switch autoSwitch;
    private BluetoothAdapter bluetoothAdapter;
    private Button bluetoothSend;
    private TextView recvData;
    private TextView mLogRight;
    private boolean isSendData=false;
    private boolean isSendCmd=false;
    private boolean isSocketConnect=true;
    private short Radius=0;
    private byte Angle=0;
    private byte Cmd=0;



    //private String strMacAddress="00:22:33:76:93:1F";
    private String strMacAddress="00:04:4B:8D:49:66";


    private static final int MSG_DATA_CHANGE = 0x11;

    private static final int MSG_CONNECT_FAIL = 0x20;
    private static final int MSG_CONNECT_SUCCED = 0x21;
    private static final int MSG_CONNECT_SENDBUF = 0x01;


    private Handler  mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case MSG_DATA_CHANGE:
                    String rcv=(String)msg.obj;
                    //Toast.makeText(getApplicationContext(), rcv,Toast.LENGTH_SHORT).show();
                    break;
                case MSG_CONNECT_FAIL:
                    Toast.makeText(getApplicationContext(),(String)msg.obj,Toast.LENGTH_SHORT).show();
                    myclient.connect();
                    break;
                case MSG_CONNECT_SUCCED:
                    Toast.makeText(getApplicationContext(),(String)msg.obj,Toast.LENGTH_SHORT).show();
                    break;
                case MSG_CONNECT_SENDBUF:
                    Toast.makeText(getApplicationContext(),(String)msg.obj,Toast.LENGTH_SHORT).show();

                    break;

            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        timer.schedule(new MyTask(), 0, 200);
        mContext=this;

        //获取手机震动服务
        mVibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        mybluethooth=new Mybluethooth(mContext,mHandler);
        myclient=new client(mHandler);
        //myclient.connect();


        bluetoothSwitch = (Switch) findViewById(R.id.swtch);
        autoSwitch= (Switch) findViewById(R.id.swtch_auto);
        bluetoothSend = (Button) findViewById(R.id.send);
        recvData = (TextView) findViewById(R.id.recvdata);
        mLogRight=(TextView) findViewById(R.id.recvdata);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙设备
        if (bluetoothAdapter!=null && bluetoothAdapter.isEnabled()) {
            bluetoothSwitch.setChecked(false);
        }

        RockerView rockerViewRight = (RockerView) findViewById(R.id.rockerView_right);
        if (rockerViewRight != null) {
            rockerViewRight.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                @Override
                public void onStart() {
                   // mLogRight.setText(null);
                    isSendData=true;
                    mVibrator.vibrate(new long[]{100,100,100,0},-1);
                }

                @Override
                public void angle(double angle,double radian) {
                    mLogRight.setText("摇动角度 : angle=" + angle +" Angle="+Angle+ "半径：radian=" +radian+" Radius=" + Radius);
                   // angle=Math.toRadians(angle);
                    double absAngle=Math.abs(angle);
                    if (radian> 50) {
                        radian=radian-50;
                        if (absAngle <= 15 ) {
                            Angle=(byte)(-radian/10);
                            Radius = (short) 0;
                        }else if(absAngle>=165){
                            Angle=(byte)(radian/10);
                            Radius = (short) (0);
                        }else if(absAngle>=75 &&absAngle<105){
                            Angle=(byte)0;
                            Radius = (short) (radian*absAngle/angle );
                        }else{
                            if(angle>0&&angle<90) {
                                Angle = (byte) (-radian / 10 * Math.cos(Math.toRadians((angle-15)*1.5)));
                                Radius = (short) (radian * Math.sin(Math.toRadians((angle-15)*1.5)));
                            }else if(angle<0&&angle>-90){
                                Angle = (byte) (-radian / 10 * Math.cos(Math.toRadians((angle+15)*1.5)));
                                Radius = (short) (radian * Math.sin(Math.toRadians((angle+15)*1.5)));
                            }else if(angle>90){
                                Angle = (byte) (-radian / 10 * Math.cos(Math.toRadians((angle-105)*1.5+90)));
                                Radius = (short) (radian * Math.sin(Math.toRadians((angle-105)*1.5+90)));
                            }else if(angle<-90){
                                Angle = (byte) (-radian / 10 * Math.cos(Math.toRadians((angle+105)*1.5-90)));
                                Radius = (short) (radian * Math.sin(Math.toRadians((angle+105)*1.5-90)));
                            }
                            //Log.i(TAG, "Math.toRadians(angle)="+Math.toRadians(angle)+"Math.sin(Math.toRadians(angle))="+Math.sin(Math.toRadians(angle)));
                        }
                    } else {
                        Angle = (byte) 0;
                        Radius = (short) 0;
                    }
                }

                @Override
                public void onFinish() {
                    mLogRight.setText(null);
                    Angle= (byte)0;
                    Radius=(short)0;
                  //  isSendData=false;
                }
            });
        }


        // 添加监听
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSendData=false;
                isSendCmd=true;
                if (isChecked){
                    Cmd=1;
                }else {
                    Cmd=0;
                    try {
                        Thread.currentThread().sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isSendData=true;
                }

            }
        });



        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if(bluetoothAdapter!=null) {
                        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启，则开启蓝牙
                            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            //startActivity(enableIntent);
                            Toast.makeText(MainActivity.this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
                            bluetoothSwitch.setChecked(false);
                        }else {
                            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strMacAddress);
                            mybluethooth.initConnectThread(device);
                            isSocketConnect=false;
                        }
                    }else {
                        //表明此手机不支持蓝牙
                        Toast.makeText(MainActivity.this, "未发现蓝牙设备", Toast.LENGTH_SHORT).show();
                    }
                    myclient.onDestroy();
                } else {
                   // bluetoothAdapter.disable();

                    if(mybluethooth.connectThread!=null){
                        mybluethooth.connectThread.cancel();
                        //Toast.makeText(MainActivity.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                    }
                   // myclient.connect();
                    isSocketConnect=true;
                }
            }
        });


        bluetoothSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strSendData = new String("123");
                mybluethooth.write(ConvertData.hexStringToBytes("0123456789ABCDEF"));
                myclient.send(ConvertData.hexStringToBytes("0123456789ABCDEF"));
            }
        });
    }



    private void sendBuf(byte Seq, byte Cmd, byte State, byte[] Data,int Len)
    {
        byte [] sendData=new byte[Len+6];
        sendData[0]=(byte)0xAA;
        sendData[1]=Seq;
        sendData[2]=Cmd;
        sendData[3]=State;
        sendData[4]=(byte)Len;

        for(int i=0;i<Len;i++){
            sendData[5+i]=Data[i];
        }

        byte X = 0x0;
        if (Len > 0)
        {
            X = Data[0];
            for (int i = 1; i < Len; i++)
            {
                X ^= Data[i];
            }
        }

        sendData[Len+5]=X;           //异或校验位
        Logger.i(TAG, "isSocketConnect=" + isSocketConnect);


        if (isSocketConnect)
        {
            myclient.send(sendData);
        }else {
            Logger.i(TAG, "mybluethooth sendData=" + sendData);
            mybluethooth.write(sendData);
        }
    }

    @Override
    protected void onResume() {
        myclient=new client(mHandler);
        super.onResume();

    }


    @Override
    protected void onDestroy() {
        isSendCmd=false;
        timer.cancel();
        myclient.onDestroy();
        super.onDestroy();
    }

    class MyTask extends TimerTask {

        @Override
        public void run() {
            if(isSendData) {
                byte[] buf=new byte[4];
                //int mTime=(int)(System.currentTimeMillis()/1000);

                ByteUtil.putShort(buf,Angle,0);
                ByteUtil.putShort(buf,Radius,2);

                sendBuf((byte)0x1,(byte)0x60,(byte)0x00,buf,4);

                Log.i(TAG, "send to handler");
            } else if(isSendCmd)
            {
                byte[] buff=new byte[1];
                buff[0]=Cmd;
                sendBuf((byte)0x2,(byte)0x01,(byte)0x00,buff,1);
                Log.i(TAG, "send to handler Cmd"+buff[0]);
                isSendCmd=false;
            }
        }
    }



}

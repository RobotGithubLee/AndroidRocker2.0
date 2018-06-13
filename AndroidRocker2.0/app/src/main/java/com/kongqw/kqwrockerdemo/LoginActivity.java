package com.kongqw.kqwrockerdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kongqw.kqwrockerdemo.socket.client;

public class LoginActivity extends AppCompatActivity implements client.ConnectInterface {

    String TAG="LoginActivity";
    private EditText ip_login;
    private EditText id_login;
    private EditText password_login;
    private ImageView avatar_login;
    private CheckBox rememberpassword_login;
    private CheckBox auto_login;
    private Button button_login;
    ProgressDialog progressDialog;
    private SharedPreferences sp;

    private static boolean isAutoLogin=false;

    private String ipAdr="192.168.4.1";//"10.14.0.77";
    private String idvalue;
    private String passwordvalue;

    private Handler mHandler;
    private client myclient;


    private static final int MSG_DATA_CHANGE = 0x11;
    private static final int MSG_CONNECT_FAIL = 0x20;
    private static final int MSG_CONNECT_SUCCED = 0x21;
    private static final int PASSWORD_MIWEN = 0x81;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case MSG_DATA_CHANGE:
                        String rcv = TAG + (String) msg.obj;
                        Toast.makeText(getApplicationContext(), rcv, Toast.LENGTH_SHORT).show();
                    case MSG_CONNECT_FAIL:
                        Toast.makeText(getApplicationContext(), TAG + (String) msg.obj, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        ip_login.setVisibility(View.VISIBLE);
                        break;
                    case MSG_CONNECT_SUCCED:
                        Toast.makeText(getApplicationContext(), TAG + (String) msg.obj, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        ip_login.setVisibility(View.GONE);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        if (!isAutoLogin) {
                            finish();
                        }
                        break;
                }
            }
        };



        sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        //找到相应的布局及控件

        setContentView(R.layout.activity_login);
        id_login=(EditText) findViewById(R.id.login_id);
        ip_login=(EditText) findViewById(R.id.login_ip);
        password_login=(EditText) findViewById(R.id.login_password);
        avatar_login=(ImageView) findViewById(R.id.login_avatar);
        rememberpassword_login=(CheckBox) findViewById(R.id.login_rememberpassword);
        auto_login=(CheckBox) findViewById(R.id.login_autologin);
        button_login=(Button) findViewById(R.id.login_button);



        myclient=new client(mHandler);

        ip_login.setText(sp.getString("IPADDRESS",""));
        ipAdr=sp.getString("IPADDRESS","");
        if (sp.getBoolean("ischeck",false)) {
            rememberpassword_login.setChecked(true);
            id_login.setText(sp.getString("PHONEEDIT", ""));
            password_login.setText(sp.getString("PASSWORD", ""));
            //密文密码
            password_login.setInputType(PASSWORD_MIWEN);
            if (sp.getBoolean("auto_ischeck", false)) {
                auto_login.setChecked(true);

                if (ipAdr != null){
                    myclient.setIP(ipAdr);
                    myclient.connect();
                    isAutoLogin=true;
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("正在连接"+ipAdr);
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                }
            }
        }

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ip_login.getPaint().setFlags(0);
                ipAdr=ip_login.getText().toString();

                id_login.getPaint().setFlags(0);
                idvalue=id_login.getText().toString();

                password_login.getPaint().setFlags(0);
                passwordvalue=password_login.getText().toString();

                SharedPreferences.Editor editor=sp.edit();
                editor.putString("IPADDRESS",ipAdr);

                if (idvalue.equals("admin")&&passwordvalue.equals("123456")){
                    if (rememberpassword_login.isChecked()){
                        editor.putString("PHONEEDIT",idvalue);
                        editor.putString("PASSWORD",passwordvalue);
                        editor.commit();
                    }
                    myclient.setIP(ipAdr);
                    myclient.connect();
                    isAutoLogin=false;
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("正在连接"+ipAdr);
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                }else{
                    Toast.makeText(LoginActivity.this, "用户名或密码错误，请重新登录", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rememberpassword_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (rememberpassword_login.isChecked()){
                    System.out.println("记住密码已选中");
                    sp.edit().putBoolean("ischeck",true).commit();
                }
                else {
                    System.out.println("记住密码没有选中");
                    sp.edit().putBoolean("ischeck",false).commit();
                }
            }
        });

        auto_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (auto_login.isChecked()){
                    System.out.println("自动登录已选中");
                    sp.edit().putBoolean("auto_ischeck",true).commit();
                }else {
                    System.out.println("自动登录没有选中");
                    sp.edit().putBoolean("auto_ischeck",false).commit();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        myclient=new client(mHandler);
        //myclient.onDestroy();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void ConnectedSucceed(){
        Toast.makeText(LoginActivity.this, "Socket连接成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void ConnectedFalse(){
        ip_login.setVisibility(View.VISIBLE);
        Toast.makeText(LoginActivity.this, "Socket连接失败", Toast.LENGTH_SHORT).show();
    }
}
package com.example.simpleclient;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.security.auth.login.LoginException;

import com.example.simpleclient.ApplicationUtil;
import com.example.simpleclient.LoggedInActivity;

public class MainActivity extends AppCompatActivity {

    private String mUsername;
    private String mPassword;
    private String mEnPassword; // password md5
    private byte[] mSendBuf;
    private byte[] mRecvBuf;

    private Button mLogInButton;
    private Button mSignUpButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private ApplicationUtil appUtil1;
    private static String mLogInRes = "LogInResult";

    @Override
    public void onDestroy() {
        super.onDestroy();
        ApplicationUtil appUtil1 = (ApplicationUtil) MainActivity.this.getApplication();
        try {
            appUtil1.finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsernameEditText = (EditText) findViewById(R.id.username_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);

        appUtil1 = (ApplicationUtil) MainActivity.this.getApplication();
        mLogInButton = (Button) findViewById(R.id.login_button);
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsername = mUsernameEditText.getText().toString();
                mPassword = mPasswordEditText.getText().toString();
                if ("".equals(mUsername) || "".equals(mPassword)) {
                    Toast.makeText(MainActivity.this, "用户名/密码不能为空！", Toast.LENGTH_SHORT).show();
                } else {
                    mEnPassword = TokenEnCrypt.MD5(mPassword);
                    new Thread(logInTask).start();
                    // Toast.makeText(MainActivity.this, mEnPassword, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSignUpButton = (Button) findViewById(R.id.signup_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start SignUp Activity
                Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(i);
                mUsernameEditText.setText("");
                mPasswordEditText.setText("");
            }
        });
    }

    public boolean LoginCheck(String username, String password) {
        Socket socket;
        DataOutputStream dos;
        DataInputStream dis;

        try {
            appUtil1.init();
            socket = appUtil1.getSocket();
         /*   while(socket == null){
                ;socket = appUtil1.getSocket();
            }*/
            dos = appUtil1.getDos();
            dis = appUtil1.getDis();
            if (dos == null) {
                Log.d("dos", "dos null");
            }
            if (dis == null) {
                Log.d("dis", "dis null");
            }
            if(socket == null){
                Log.d("socket", "socket null");
            }
            if (socket.isConnected() == true) {
                //     Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            } else {
                //    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
            mSendBuf = new byte[MyProtocol.LOGIN_LEN];
            mSendBuf[0] = MyProtocol.CS_H;
            mSendBuf[1] = MyProtocol.LOGIN_L;
            mSendBuf[2] = MyProtocol.LOGIN_LEN;
            mSendBuf[3] = MyProtocol.LOGIN_DATA_LEN;
            int i;
            for (i = 0; i < username.length(); i++) {
                mSendBuf[4 + i] = username.getBytes()[i];
            }
            for (; i < MyProtocol.USERNAME_LEN; i++) {
                mSendBuf[4 + i] = (byte) 0;
            }
            for (i = 0; i < password.length(); i++) {
                mSendBuf[4 + MyProtocol.USERNAME_LEN + i] = password.getBytes()[i];
            }
            for (; i < MyProtocol.PASSWORD_LEN; i++) {
                mSendBuf[4 + MyProtocol.USERNAME_LEN + i] = (byte) 0;
            }
            dos.write(mSendBuf, 0, MyProtocol.LOGIN_LEN);
            //     Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            mRecvBuf = new byte[MyProtocol.RESPONSE_LEN];
            int len = 0;
            int total = 0;
            while (total < MyProtocol.RESPONSE_LEN) {
                len = dis.read(mRecvBuf, total, MyProtocol.RESPONSE_LEN - total);
                if (len == -1) {
                    break;
                }
                total += len;
            }
            //  Toast.makeText(MainActivity.this, "recv ok", Toast.LENGTH_SHORT).show();
            if (mRecvBuf[0] != MyProtocol.SC_H || mRecvBuf[1] != MyProtocol.LOGIN_L) {
                //    Toast.makeText(MainActivity.this, "网络协议错误 - 非[服务端][登录]协议", Toast.LENGTH_SHORT).show();
                return false;
            }
            int err = MyProtocol.bytesToInt(mRecvBuf, 4);
            if (err == MyProtocol.ERR_ALREADY) {
                return false;
            } else if (err == MyProtocol.ERR_WRONG) {
                return false;
            } else if (err == MyProtocol.ERR_NONE) {
                return true;
            } else {
                return true; // that is fd
            }
        } catch (IOException ioex) {
            // Toast.makeText(MainActivity.this, "IO Exception", Toast.LENGTH_SHORT).show();
            ioex.printStackTrace();
        } catch (Exception ex) {
            //  Toast.makeText(MainActivity.this, "Other Exception", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
        return false;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            boolean res = data.getBoolean(mLogInRes);
            if (res == true) {
                Toast.makeText(MainActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                Intent i = LoggedInActivity.newIntent(MainActivity.this, mUsername, mEnPassword);
                startActivity(i);
                mUsernameEditText.setText("");
                mPasswordEditText.setText("");
            } else {
                Toast.makeText(MainActivity.this, "登录失败！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    Runnable logInTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (LoginCheck(mUsername, mEnPassword) == true) {
                // if logging in successful, then go into LoggedInActivity
                data.putBoolean(mLogInRes, true);
            } else {
                data.putBoolean(mLogInRes, false);
            }
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
}
package com.example.simpleclient;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SignUpActivity extends AppCompatActivity {

    private String mUsername;
    private String mPassword;
    private String mPasswordDup;
    private String mEnPassword; // password md5

    private byte[] mSendBuf;
    private byte[] mRecvBuf;

    private static String PASSWORD_DIFFERENT = "两次密码不一致！";
    private static String ACCOUNT_LIMIT =
            "Username: ([0-9]|[A-Z]|[a-z]|_){6, 12}\n"
                    + "Password: ([0-9]|[A-Z]|[a-z]|[!@#$%^&*_]){6, 16}";
    private static String mSignUpRes = "SignUpResult";

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordDupEditText;

    private Button mSignUpButton;
    private Button mSignUpCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUsernameEditText = (EditText) findViewById(R.id.signup_username_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.signup_password_edit_text);
        mPasswordDupEditText = (EditText) findViewById((R.id.signup_password_dup_edit_text));

        mSignUpButton = (Button) findViewById(R.id.signup_real_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mUsername = mUsernameEditText.getText().toString();
            mPassword = mPasswordEditText.getText().toString();
            mPasswordDup = mPasswordDupEditText.getText().toString();

            if ("".equals(mUsername) || "".equals(mPassword) || "".equals(mPasswordDup)) {
                Toast.makeText(SignUpActivity.this, "用户名/密码不能为空！", Toast.LENGTH_SHORT).show();
            } else if (mPassword.equals(mPasswordDup) == false) {
                Toast.makeText(SignUpActivity.this, PASSWORD_DIFFERENT, Toast.LENGTH_SHORT).show();
            } else {
                if (MyAccount.IsAccountValid(mUsername, mPassword) == false) {
                    Toast.makeText(SignUpActivity.this, ACCOUNT_LIMIT, Toast.LENGTH_SHORT).show();
                } else {
                    mEnPassword = TokenEnCrypt.MD5(mPassword);
                    new Thread(signUpTask).start();
                }
            }
            }
        });

        mSignUpCancelButton = (Button) findViewById(R.id.signup_cancel_button);
        mSignUpCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpActivity.this.finish();
            }
        });
    }

    public boolean SignUpCheck(String username, String password) {
        Socket socket;
        DataOutputStream dos;
        DataInputStream dis;
        ApplicationUtil appUtil1 = (ApplicationUtil) SignUpActivity.this.getApplication();
        try {
            appUtil1.init();
            socket = appUtil1.getSocket();
            dos = appUtil1.getDos();
            dis = appUtil1.getDis();
            if (dos == null) {
                Log.d("dos", "dos null");
            }
            if (dis == null) {
                Log.d("dis", "dis null");
            }
            if (socket == null) {
                Log.d("socket", "socket null");
            }
            mSendBuf = new byte[MyProtocol.SIGNUP_LEN];
            mSendBuf[0] = MyProtocol.CS_H;
            mSendBuf[1] = MyProtocol.SIGNUP_L;
            mSendBuf[2] = MyProtocol.SIGNUP_LEN;
            mSendBuf[3] = MyProtocol.SIGNUP_DATA_LEN;
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
            dos.write(mSendBuf, 0, MyProtocol.SIGNUP_LEN);
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
            if (mRecvBuf[0] != MyProtocol.SC_H || mRecvBuf[1] != MyProtocol.SIGNUP_L) {
                //    Toast.makeText(MainActivity.this, "网络协议错误 - 非[服务端][登录]协议", Toast.LENGTH_SHORT).show();
                return false;
            }
            int err = MyProtocol.bytesToInt(mRecvBuf, 4);
            if (err == MyProtocol.ERR_ALREADY) {
                Log.d("ERR", "Already");
                return false;
            } else if (err == MyProtocol.ERR_WRONG) {
                Log.d("ERR", "Wrong");
                return false;
            } else if (err == MyProtocol.ERR_NONE) {
                Log.d("ERR", "None");
                return false;
            } else if (err == MyProtocol.ERR_DUP) {
                Log.d("ERR", "Dup");
                return false;
            } else {
                Log.d("ERR", "successful" + String.valueOf(err));
                return true; // means fd received, which really means success
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            boolean res = data.getBoolean(mSignUpRes);
            if (res == true) {
                Toast.makeText(SignUpActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                Intent i = LoggedInActivity.newIntent(SignUpActivity.this, mUsername, mEnPassword);
                startActivity(i);
                SignUpActivity.this.finish();
            } else {
                Toast.makeText(SignUpActivity.this, "注册失败！", Toast.LENGTH_SHORT).show();
            }
        }
    };
    Runnable signUpTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (SignUpCheck(mUsername, mEnPassword) == true) {
                // if logging in successful, then go into LoggedInActivity
                data.putBoolean(mSignUpRes, true);
            } else {
                data.putBoolean(mSignUpRes, false);
            }
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
}

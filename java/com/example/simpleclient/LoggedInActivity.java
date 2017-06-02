package com.example.simpleclient;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoggedInActivity extends AppCompatActivity {

    private TextView mPromptTextView;
    private Button mScanButton;
    private Button mLogOutButton;

    private static final String EXTRA_USERNAME = "com.example.simpleclient.username";
    private static final String EXTRA_PASSWORD = "com.example.simpleclient.password";

    private String mUsername;
    private String mEnPassword;
    private String mToken;
    private String mEnToken; // from qr-code
    private byte[] mSendBuf;
    private byte[] mRecvBuf;
    private static String mLogOutRes = "LogOutResult";
    private static String mQRIdenRes = "QRIdenResult";
    private static String ALPHA = "46CdeFGhIJK1mn0pQR5tuVwxYz";
    // fetch username and password from parent activity
    public static Intent newIntent(Context packageContext, String username, String enPassword) {
        Intent i = new Intent(packageContext, LoggedInActivity.class);
        i.putExtra(EXTRA_USERNAME, username);
        i.putExtra(EXTRA_PASSWORD, enPassword);
        return i;
    }

    public String TokenDecrypt(String enToken) {
        // remain to complete
        int i;
        int tmp;
        StringBuffer sbu = new StringBuffer();
        for(i = 0; i < enToken.length(); i++){
            tmp = ALPHA.indexOf(enToken.getBytes()[i]) + 26;
            if(tmp < 46){
                tmp += 26;
            }
            sbu.append((char)tmp);
        }
        return sbu.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        mPromptTextView = (TextView) findViewById(R.id.login_prompt_text_view);
        mPromptTextView.setText("Welcome, " + getIntent().getStringExtra(EXTRA_USERNAME));

        mScanButton = (Button) findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // scan qr-code
                startActivityForResult(new Intent(LoggedInActivity.this, CaptureActivity.class), 0);
            }
        });

        mLogOutButton = (Button) findViewById(R.id.logout_button);
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(logOutTask).start();
            }
        });
    }

    // to receive the result of qr-code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            mEnToken = bundle.getString("result");
            //    Toast.makeText(LoggedInActivity.this, mEnToken, Toast.LENGTH_SHORT).show();

        }
        if (resultCode == RESULT_CANCELED) {
            mEnToken = "";
            Toast.makeText(LoggedInActivity.this, "Error occurs", Toast.LENGTH_SHORT).show();
        }
        if (mEnToken.equals("") == false) { // scanned successfully
            Toast.makeText(LoggedInActivity.this, mEnToken, Toast.LENGTH_SHORT).show();
            mToken = TokenDecrypt(mEnToken);
            if (mToken.equals("") == true) {
                Toast.makeText(LoggedInActivity.this, "Token decrypting error", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoggedInActivity.this, mToken, Toast.LENGTH_SHORT).show();
                new Thread(qrIdenTask).start();
            }
        } else {
            Toast.makeText(LoggedInActivity.this, "Scan Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean LogOut() {
        Socket socket;
        DataOutputStream dos;
        DataInputStream dis;
        ApplicationUtil appUtil1 = (ApplicationUtil) LoggedInActivity.this.getApplication();
        try {
            appUtil1.init();
            socket = appUtil1.getSocket();
         /*   while(socket == null){
                ;socket = appUtil1.getSocket();
            }*/
            dos = appUtil1.getDos();
            dis = appUtil1.getDis();
            if (socket.isConnected() == true) {
                //     Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            } else {
                //    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
            mSendBuf = new byte[MyProtocol.LOGOUT_LEN];
            mSendBuf[0] = MyProtocol.CS_H;
            mSendBuf[1] = MyProtocol.LOGOUT_L;
            mSendBuf[2] = MyProtocol.LOGOUT_LEN;
            mSendBuf[3] = MyProtocol.LOGOUT_DATA_LEN;
            dos.write(mSendBuf, 0, MyProtocol.LOGOUT_LEN);
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
            if (mRecvBuf[0] != MyProtocol.SC_H || mRecvBuf[1] != MyProtocol.LOGOUT_L) {
                //    Toast.makeText(MainActivity.this, "网络协议错误 - 非[服务端][登录]协议", Toast.LENGTH_SHORT).show();
                return false;
            }
            int err = MyProtocol.bytesToInt(mRecvBuf, 4);
            appUtil1.finish();
            if (err == MyProtocol.ERR_ALREADY) {
                Log.d("ERR", "Logged out already");
                return false;
            } else if (err == MyProtocol.ERR_WRONG) {
                Log.d("ERR", "Logged out wrong");
                return false;
            } else if (err == MyProtocol.ERR_NONE) {
                Log.d("ERR", "Logged out none");
                return true;
            } else {
                Log.d("ERR", "logged out others");
                return false; // that is error now
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

    Handler handlerLogOut = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            boolean res = data.getBoolean(mLogOutRes);
            if (res == true) {
                Toast.makeText(LoggedInActivity.this, "注销成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoggedInActivity.this, "注销失败！", Toast.LENGTH_SHORT).show();
            }
            LoggedInActivity.this.finish();
        }
    };
    Runnable logOutTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (LogOut() == true) {
                // if logging in successful, then go into LoggedInActivity
                data.putBoolean(mLogOutRes, true);
            } else {
                data.putBoolean(mLogOutRes, false);
            }
            msg.setData(data);
            handlerLogOut.sendMessage(msg);
        }
    };

    public boolean QRIden(String token) {
        Socket socket;
        DataOutputStream dos;
        DataInputStream dis;
        ApplicationUtil appUtil1 = (ApplicationUtil) LoggedInActivity.this.getApplication();
        try {
            appUtil1.init();
            socket = appUtil1.getSocket();
            dos = appUtil1.getDos();
            dis = appUtil1.getDis();
            if (socket.isConnected() == true) {
                //     Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            } else {
                //    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
            mSendBuf = new byte[MyProtocol.QRIDEN_LEN];
            mSendBuf[0] = MyProtocol.CS_H;
            mSendBuf[1] = MyProtocol.QRIDEN_L;
            mSendBuf[2] = MyProtocol.QRIDEN_LEN;
            mSendBuf[3] = MyProtocol.QRIDEN_DATA_LEN;
            int i;
            for (i = 0; i < token.length(); i++) {
                mSendBuf[4 + i] = token.getBytes()[i];
            }
            for(; i < MyProtocol.QRIDEN_DATA_LEN; i++){
                mSendBuf[4 + i] = (byte)0;
            }
            dos.write(mSendBuf, 0, MyProtocol.QRIDEN_LEN);
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
            if (mRecvBuf[0] != MyProtocol.SC_H || mRecvBuf[1] != MyProtocol.QRIDEN_L) {
                //    Toast.makeText(MainActivity.this, "网络协议错误 - 非[服务端][登录]协议", Toast.LENGTH_SHORT).show();
                return false;
            }
            int err = MyProtocol.bytesToInt(mRecvBuf, 4);
            if (err == MyProtocol.ERR_ALREADY) {
                Log.d("ERR", "qr-code already");
                return false;
            } else if (err == MyProtocol.ERR_WRONG) {
                Log.d("ERR", "qr-code wrong");
                return false;
            } else if (err == MyProtocol.ERR_NONE) {
                Log.d("ERR", "qr-code none");
                return true;
            } else {
                Log.d("ERR", "qr-code others");
                return false; // that is error now
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

    Handler handlerQRIden = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            boolean res = data.getBoolean(mQRIdenRes);
            if (res == true) {
                Toast.makeText(LoggedInActivity.this, "扫码登录成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoggedInActivity.this, "扫码登录失败！", Toast.LENGTH_SHORT).show();
            }
        }
    };
    Runnable qrIdenTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            if (QRIden(mToken) == true) {
                // if logging in successful, then go into LoggedInActivity
                data.putBoolean(mQRIdenRes, true);
            } else {
                data.putBoolean(mQRIdenRes, false);
            }
            msg.setData(data);
            handlerQRIden.sendMessage(msg);
        }
    };
}
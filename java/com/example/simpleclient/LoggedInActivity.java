package com.example.simpleclient;

import android.content.Context;
import android.content.Intent;
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

    // fetch username and password from parent activity
    public static Intent newIntent(Context packageContext, String username, String enPassword){
        Intent i = new Intent(packageContext, LoggedInActivity.class);
        i.putExtra(EXTRA_USERNAME, username);
        i.putExtra(EXTRA_PASSWORD, enPassword);
        return i;
    }
    public static String TokenDecrypt(String enToken) {
        // remain to complete
        String res = "";
        return res;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        mPromptTextView = (TextView)findViewById(R.id.login_prompt_text_view);

        mScanButton = (Button)findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // scan qr-code
                startActivityForResult(new Intent(LoggedInActivity.this, CaptureActivity.class), 0);
                // decrypt and send the token to server
                if(mEnToken != ""){ // scanned successfully
                    mToken = TokenDecrypt(mEnToken);
                    if(mToken == ""){
                        Toast.makeText(LoggedInActivity.this, "Token decrypting error", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Socket socket;
                        DataOutputStream dos;
                        DataInputStream dis;
                        ApplicationUtil appUtil1 = (ApplicationUtil)LoggedInActivity.this.getApplication();
                        try{
                            appUtil1.init();
                            socket = appUtil1.getSocket();
                            dos = appUtil1.getDos();
                            dis = appUtil1.getDis();
                        }
                        catch(IOException ioex){
                            ioex.printStackTrace();
                        }
                        catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        mLogOutButton = (Button)findViewById(R.id.logout_button);
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Socket socket;
                DataOutputStream dos;
                DataInputStream dis;
                ApplicationUtil appUtil1 = (ApplicationUtil)LoggedInActivity.this.getApplication();
                try{
                    appUtil1.init();
                    socket = appUtil1.getSocket();
                    dos = appUtil1.getDos();
                    dis = appUtil1.getDis();
                }
                catch(IOException ioex){
                    ioex.printStackTrace();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                // after sending a LOGOUT_L packet to server, finish this activity
                LoggedInActivity.this.finish();
            }
        });
    }

    // to receive the result of qr-code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            mEnToken = bundle.getString("result");
            Toast.makeText(LoggedInActivity.this, mEnToken, Toast.LENGTH_SHORT).show();

        }
        if(resultCode == RESULT_CANCELED){
            mEnToken = "";
            Toast.makeText(LoggedInActivity.this, "Error occurs", Toast.LENGTH_SHORT).show();
        }
    }
}
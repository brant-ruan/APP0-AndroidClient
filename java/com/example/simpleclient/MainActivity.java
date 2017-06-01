package com.example.simpleclient;

import android.app.Application;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsernameEditText = (EditText)findViewById(R.id.username_edit_text);
        mPasswordEditText = (EditText)findViewById(R.id.password_edit_text);

        mLogInButton = (Button)findViewById(R.id.login_button);
        mLogInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mUsername = mUsernameEditText.getText().toString();
                mPassword = mPasswordEditText.getText().toString();

                if(LoginCheck(mUsername, mPassword) == true){
                    // if logging in successful, then go into LoggedInActivity
                    Intent i = LoggedInActivity.newIntent(MainActivity.this, mUsername, mEnPassword);
                    startActivity(i);
                    MainActivity.this.finish();
                }
                Socket socket;
                DataOutputStream dos;
                DataInputStream dis;
                ApplicationUtil appUtil1 = (ApplicationUtil)MainActivity.this.getApplication();
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
        });

        mSignUpButton = (Button)findViewById(R.id.signup_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start SignUp Activity
                Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(i);
                MainActivity.this.finish();
            }
        });
    }

    public boolean LoginCheck(String username, String password){

        return true;
    }
}
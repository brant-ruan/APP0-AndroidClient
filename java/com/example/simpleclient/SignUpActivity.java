package com.example.simpleclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private static String PASSWORD_DIFFERENT = "Twice passwords not the same!";
    private static String ACCOUNT_LIMIT =
            "Username: ([0-9]|[A-Z]|[a-z]|_){6, 12}\n"
            + "Password: ([0-9]|[A-Z]|[a-z]|[!@#$%^&*_]){6, 16}";

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordDupEditText;

    private Button mSignUpButton;
    private Button mSignUpCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUsernameEditText = (EditText)findViewById(R.id.signup_username_edit_text);
        mPasswordEditText = (EditText)findViewById(R.id.signup_password_edit_text);
        mPasswordDupEditText = (EditText)findViewById((R.id.signup_password_dup_edit_text));

        mSignUpButton = (Button)findViewById(R.id.signup_real_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsername = mUsernameEditText.getText().toString();
                mPassword = mPasswordEditText.getText().toString();
                mPasswordDup = mPasswordDupEditText.getText().toString();

                if(mPassword != mPasswordDup){
                    Toast.makeText(SignUpActivity.this, PASSWORD_DIFFERENT, Toast.LENGTH_SHORT).show();
                }
                else{
                    if(MyAccount.IsAccountValid(mUsername, mPassword) == false){
                        Toast.makeText(SignUpActivity.this, ACCOUNT_LIMIT, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mEnPassword = TokenEnCrypt.MD5(mPassword);

                        Socket socket;
                        DataOutputStream dos;
                        DataInputStream dis;
                        ApplicationUtil appUtil1 = (ApplicationUtil)SignUpActivity.this.getApplication();
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

        mSignUpCancelButton = (Button)findViewById(R.id.signup_cancel_button);
        mSignUpCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpActivity.this.finish();
            }
        });
    }

}

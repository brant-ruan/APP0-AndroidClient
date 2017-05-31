package com.example.simpleclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xys.libzxing.zxing.activity.CaptureActivity;

public class MainActivity extends AppCompatActivity {

    private String mUsername;
    private String mPassword;
    private String mToken;

    private Button mLogInButton;
    private Button mSignUpButton;
    private Button mScanButton;

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

            }
        });

        mSignUpButton = (Button)findViewById(R.id.signup_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // scan qr-code
        mScanButton = (Button)findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 0);
            }
        });
    }
    // to receive the result of qr-code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            mToken = bundle.getString("result");
            Toast.makeText(MainActivity.this, mToken, Toast.LENGTH_SHORT).show();

        }
        if(resultCode == RESULT_CANCELED){
            mToken = "";
            Toast.makeText(MainActivity.this, "Error occurs", Toast.LENGTH_SHORT).show();
        }
    }
}

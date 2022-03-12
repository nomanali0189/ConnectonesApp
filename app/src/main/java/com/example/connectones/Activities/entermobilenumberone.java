package com.example.connectones.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.connectones.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class entermobilenumberone extends AppCompatActivity {

    EditText enternumber;
    Button getotpbutton;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entermobilenumberone);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null){
            Intent intent = new Intent(entermobilenumberone.this,dashboard.class);
            startActivity(intent);
            finish();
        }
        getSupportActionBar().hide();

        enternumber= findViewById(R.id.nameBox);
        getotpbutton=findViewById(R.id.continueBtn);

        final ProgressBar progressBar=findViewById(R.id.progressbar_sending_otp);

        getotpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!enternumber.getText().toString().trim().isEmpty()){
                    if((enternumber.getText().toString().trim()).length() == 10){

                        progressBar.setVisibility(View.VISIBLE);
                        getotpbutton.setVisibility(View.INVISIBLE);

//                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                                "+91" + enternumber.getText().toString(),
//                                60,
//                                TimeUnit.SECONDS,
//                                entermobilenumberone.this,
                        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber("+92" + enternumber.getText().toString())
                                .setTimeout(60L,TimeUnit.SECONDS)
                                .setActivity(entermobilenumberone.this)
                                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        progressBar.setVisibility(View.GONE);
                                        getotpbutton.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        progressBar.setVisibility(View.GONE);
                                        getotpbutton.setVisibility(View.VISIBLE);
                                        Toast.makeText(entermobilenumberone.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String backendotp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        progressBar.setVisibility(View.GONE);
                                        getotpbutton.setVisibility(View.VISIBLE);

                                        Intent intent =new Intent(getApplicationContext(),verifyenterotptwo.class);
                                        intent.putExtra("mobile", enternumber.getText().toString());
                                        intent.putExtra("backendotp",backendotp);
                                        startActivity(intent);
                                    }
                                }).build();
                            PhoneAuthProvider.verifyPhoneNumber(options);
//                        Intent intent =new Intent(getApplicationContext(),verifyenterotptwo.class);
//                        intent.putExtra("mobile", enternumber.getText().toString());
//                        startActivity(intent);
                    }else{
                        Toast.makeText(entermobilenumberone.this,"Please enter correct number",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(entermobilenumberone.this,"Enter Mobile number",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
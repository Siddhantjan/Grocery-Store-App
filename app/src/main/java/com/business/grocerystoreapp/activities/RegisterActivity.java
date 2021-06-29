package com.business.grocerystoreapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.business.grocerystoreapp.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText inputMobile = findViewById(R.id.inputMobile);
        Button buttonContinue = findViewById(R.id.btn_getOTP);
        final ProgressBar progressBar = findViewById(R.id.progress_bar);


        buttonContinue.setOnClickListener(v -> {
            if (inputMobile.getText().toString().trim().isEmpty() ) {
                Toast.makeText(RegisterActivity.this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (inputMobile.length() != 10){
                Toast.makeText(RegisterActivity.this, "Enter a Valid Mobile Number", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                progressBar.setVisibility(View.VISIBLE);
                buttonContinue.setVisibility(View.INVISIBLE);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91"+inputMobile.getText().toString(),
                        60,
                        TimeUnit.SECONDS,
                        RegisterActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                progressBar.setVisibility(View.GONE);
                                buttonContinue.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                progressBar.setVisibility(View.GONE);
                                buttonContinue.setVisibility(View.VISIBLE);
                                Toast.makeText(RegisterActivity.this, "[Error]:"+e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                progressBar.setVisibility(View.GONE);
                                buttonContinue.setVisibility(View.VISIBLE);
                                Intent otpIntent = new Intent(getApplicationContext(), VerificationActivity.class);
                                otpIntent.putExtra("mobile", inputMobile.getText().toString());
                                otpIntent.putExtra("verificationId",verificationId);
                                startActivity(otpIntent);
                            }
                        }
                );
            }
        });
    }
}
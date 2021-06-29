package com.business.grocerystoreapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.business.grocerystoreapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {
    private static final long SPLASH_TIME = 2000;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userInfoRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            firebaseDatabase = FirebaseDatabase.getInstance();
            userInfoRef = firebaseDatabase.getReference("Users");
            if (user != null) {
                Log.d("User", "onCreate: exists");
                checkUserFromFirebase();
            } else {
                Log.d("User", "onCreate: not exists");
                gotoRegisterScreen();
            }
        },SPLASH_TIME);


    }


    private void gotoHomeScreen() {
        Intent mainIntent;
        mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    //Internal Checking Fun
    private void checkUserFromFirebase() {
        userInfoRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d("User", "onDataChange: User Present");
                            gotoHomeScreen();
                        } else {
                            Log.d("User", "onDataChange: not registerd");
                            Toast.makeText(SplashScreenActivity.this, "Your Details Not Found Please Register yourself", Toast.LENGTH_SHORT).show();
                            Intent registerIntent = new Intent(getApplicationContext(), UserRegistrationActivity.class);
                            registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(registerIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreenActivity.this, "[Error]" + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void gotoRegisterScreen() {
        Log.d("User", "gotoRegisterScreen: login screen");
        Intent authIntent = new Intent(SplashScreenActivity.this,RegisterActivity.class);
        startActivity(authIntent);
        finish();
    }

}
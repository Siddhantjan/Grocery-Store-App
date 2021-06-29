package com.business.grocerystoreapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.business.grocerystoreapp.R;
import com.business.grocerystoreapp.adapters.AdapterShop;
import com.business.grocerystoreapp.modal.ModalShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private TextView nameTV,phone, emailTv;
    private ImageButton logoutBtn, editProfileBtn;
    private CircleImageView profile_img;
    private TextView  tabShopTv,tabOrdersTv;
    private RelativeLayout shopRl,orderRl;

    private RecyclerView shopRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModalShop> shopList;
    private AdapterShop adapterShop;

    @Override
    protected void onStart() {
        super.onStart();
        makeMeOnline();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        nameTV = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.s_LogoutBtn);
        editProfileBtn = findViewById(R.id.editProBtn);
        profile_img = findViewById(R.id.profile_img);
        phone = findViewById(R.id.phnTv);
        emailTv = findViewById(R.id.emailTv);
        tabShopTv = findViewById(R.id.tabShopTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        shopRl = findViewById(R.id.shopRl);
        orderRl = findViewById(R.id.orderRl);

        shopRv = findViewById(R.id.shopRv);
        showShopUi();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersUI();
            }
        });
        tabShopTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopUi();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  open edit profile  activity
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
            }
        });


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();

            }
        });

    }

    private void showShopUi() {
        shopRl.setVisibility(View.VISIBLE);
        orderRl.setVisibility(View.GONE);

        tabShopTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui and  hide product ui
        shopRl.setVisibility(View.GONE);
        orderRl.setVisibility(View.VISIBLE);
        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabShopTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void makeMeOffline() {
        progressDialog.setMessage("Logging Out....");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //save to DB
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseAuth.signOut();
                        checkUser();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void makeMeOnline() {
        progressDialog.setMessage("Logging Out....");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        //save to DB
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkUser();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        } else {
            LoadMyInfo();
        }
    }

    private void LoadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String phoneNumber = "" + ds.child("phoneNumber").getValue();
                            String email = "" + ds.child("email").getValue();
                            String profileImg = "" + ds.child("profileImage").getValue();
                            String city  = ""+ds.child("city").getValue();

                            nameTV.setText(name);
                            phone.setText(phoneNumber);
                            emailTv.setText(email);
                            try {
                                Glide.with(getApplicationContext()).load(profileImg).into(profile_img);
                            } catch (Exception e) {
                                profile_img.setImageResource(R.drawable.ic_person_grey);
                                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            loadShops(city);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShops(String city) {
        //init List
        shopList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Sellers");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before listing
                        shopList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModalShop modalShop = ds.getValue(ModalShop.class);
                            //show only user city shops
                            String shopCity = ""+ds.child("city").getValue();
                            if (shopCity.equals(city)){
                                shopList.add(modalShop);
                            }
                            //if  you want to display all shop then skip that  if part add  directly in list (Update feature ---Siddhant Jain)
                        }
                        //setup adapter
                        adapterShop = new AdapterShop(MainActivity.this,shopList);
                        ///setAdapter
                        shopRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
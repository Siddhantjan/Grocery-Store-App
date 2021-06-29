package com.business.grocerystoreapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.business.grocerystoreapp.Constants;
import com.business.grocerystoreapp.R;
import com.business.grocerystoreapp.adapters.AdapterProductUser;
import com.business.grocerystoreapp.modal.ModalProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare Ui views
    private ImageView shopIv;
    private TextView shopNameTxt, shopPhoneTxt, shopMailTxt, openClosedTxt;
    private TextView deliveryFeeTxt, shopAddressTxt, filteredProductsTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn;
    private EditText searchProductEt;
    private RecyclerView productRv;
    private String shopUid;
    private String myLatitude, myLongitude;
    private String shopLatitude, shopLongitude;
    private String shopName, shopEmail, shopPhone, shopAddress, shopDeliveryFee;
    private ArrayList<ModalProduct> productList;
    private AdapterProductUser adapterProductUser;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference mShopRef, mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init
        shopIv = findViewById(R.id.shopImg);
        shopNameTxt = findViewById(R.id.shopNameTxt);
        shopPhoneTxt = findViewById(R.id.shopPhoneTxt);
        shopMailTxt = findViewById(R.id.shopMailTxt);
        openClosedTxt = findViewById(R.id.openClosedTxt);
        deliveryFeeTxt = findViewById(R.id.deliveryFeeTxt);
        shopAddressTxt = findViewById(R.id.shopAddressTxt);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        productRv = findViewById(R.id.productRv);

        shopUid = getIntent().getStringExtra("shopUid");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        //reference of sellers
        mShopRef = FirebaseDatabase.getInstance().getReference("Sellers");
        // reference of users
        mUserRef = FirebaseDatabase.getInstance().getReference("Users");

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();


        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get Selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")) {
                                    loadShopProducts();
                                } else {
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });


    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        mUserRef.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String phoneNumber = "" + ds.child("phoneNumber").getValue();
                            String email = "" + ds.child("email").getValue();
                            String profileImg = "" + ds.child("profileImage").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ShopDetailsActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadShopDetails() {
        mShopRef.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                String name = "" + ds.child("name").getValue();
                shopName = "" + ds.child("shopName").getValue();
                shopPhone = "" + ds.child("phoneNumber").getValue();
                shopDeliveryFee = "" + ds.child("deliveryFee").getValue();
                shopEmail = "" + ds.child("email").getValue();
                shopAddress = "" + ds.child("address").getValue();
                String online = "" + ds.child("online").getValue();
                shopLatitude = "" + ds.child("latitude").getValue();
                shopLongitude = "" + ds.child("longitude").getValue();
                String profileImage = "" + ds.child("profileImage").getValue();
                String shopOpen = "" + ds.child("shopOpen").getValue();


                //setData
                shopNameTxt.setText(shopName);
                shopPhoneTxt.setText(shopPhone);
                shopMailTxt.setText(shopEmail);
                shopAddressTxt.setText(shopAddress);
                deliveryFeeTxt.setText("Delivery fee : ₹" + shopDeliveryFee);
                if (shopOpen.equals("true")) {
                    openClosedTxt.setText("Open");
                } else {
                    openClosedTxt.setText("Closed");
                }
                try {
                    Glide.with(getApplicationContext()).load(profileImage).into(shopIv);
                } catch (Exception e) {
                    shopIv.setImageResource(R.drawable.ic_store_gray);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadShopProducts() {
        productList = new ArrayList<>();
        //get all products
        mShopRef.child(shopUid).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            productList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModalProduct model = ds.getValue(ModalProduct.class);
                                productList.add(model);
                            }
                            adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productList);
                            productRv.setAdapter(adapterProductUser);
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {

        productList = new ArrayList<>();
        //get all products
        mShopRef.child(shopUid).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            productList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String productCategory = "" + ds.child("productCategory").getValue();
                                if (selected.equals(productCategory)) {
                                    ModalProduct model = ds.getValue(ModalProduct.class);
                                    productList.add(model);
                                }
                            }
                            adapterProductUser = new AdapterProductUser(getApplicationContext(), productList);
                            productRv.setAdapter(adapterProductUser);
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}
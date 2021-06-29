package com.business.grocerystoreapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.business.grocerystoreapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserRegistrationActivity extends AppCompatActivity implements LocationListener {
    private ImageButton BackBtn;
    private ImageButton GpsBtn;
    private CircleImageView sellerImg;
    private Button sRegButton;
    private EditText sellerName;
    private EditText sellerPhone;
    private EditText sCountry;
    private EditText sState;
    private EditText sCity;
    private EditText sAddress;
    private EditText sMail;
    private String fullName, shopName, phoneNumber, deliveryFee, country, state, city, email, address;

    // permission constants
    private static final int LOCATION_REQUEST_CODE = 966;
    private static final int CAMERA_REQUEST_CODE = 967;
    private static final int STORAGE_REQUEST_CODE = 968;

    private static final int IMAGE_PICK_GALLERY_CODE = 965;
    private static final int IMAGE_PICK_CAMERA_CODE = 964;


    //permission arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked uri
    private Uri s_image_uri;

    private LocationManager locationManager;

    private double latitude, longitude;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userInfoRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        BackBtn = findViewById(R.id.s_backBtn);
        GpsBtn = findViewById(R.id.s_gpsBtn);
        sellerImg = findViewById(R.id.s_profile_image);
        sellerName = findViewById(R.id.sName);
        sellerPhone = findViewById(R.id.sMobile);
        sCountry = findViewById(R.id.countryEt);
        sCity = findViewById(R.id.cityEt);
        sState = findViewById(R.id.stateEt);
        sAddress = findViewById(R.id.sAddressLi);
        sMail = findViewById(R.id.s_login_mail);
        sRegButton = findViewById(R.id.registerBtn);

        //init permissions arrays
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        init();
        sellerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image
                showImagePickDialog();
            }
        });
        GpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Live Location
                if (checkLocationPermissions()) {
                    //already Allowed
                    detectLocation();
                } else {
                    //not Allowed, request for  permission
                    requestLocationPermission();
                }
            }
        });

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserRegistrationActivity.this, "You Can't Go Back...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        userInfoRef = firebaseDatabase.getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            checkUserFromFirebase();
        }
    }

    private void inputData() {
        //input data
        fullName = sellerName.getText().toString();
        phoneNumber = sellerPhone.getText().toString();
        country = sCountry.getText().toString();
        state = sState.getText().toString();
        city = sCity.getText().toString();
        address = sAddress.getText().toString();
        email = sMail.getText().toString();

        //Validate data
        if (fullName.isEmpty()) {
            sellerName.setError("Name is Required");
            return;
        } else if (phoneNumber.isEmpty()) {
            sellerPhone.setError("Phone Number is Required");
            return;
        }else if (country.isEmpty()) {
            sCountry.setError("fill Country Name");
            return;
        } else if (state.isEmpty()) {
            sState.setError("State Name is Empty");
            return;
        } else if (address.isEmpty()) {
            sAddress.setError("Address is Required");
            return;
        } else if (city.isEmpty()) {
            sCity.setError("City Name is Empty");
            return;
        } else if (email.isEmpty()) {
            sMail.setError("Email is Required");
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            sMail.setError("Invalid Mail Address");
            return;
        } else {
            saveInfoInFirebase();
            Toast.makeText(this, "all OKk. Please Wait.....", Toast.LENGTH_SHORT).show();
        }

    }
    private void saveInfoInFirebase() {
        progressDialog.setMessage("saving account info ...");

        String timestamp = "" + System.currentTimeMillis();

        if (s_image_uri == null) {
            //save info without image

            //setup data to save
            //save info without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + fullName);
            hashMap.put("phoneNumber", "" + phoneNumber);
            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("accountType", "User");
            hashMap.put("online", "true");
            hashMap.put("profileImage", "");

            //save to DB
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //dbUpdated
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Having issue
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                    });

        }
        else {
            // save info with image

            // name and path of image
            String filePathName = "profile_images/"+""+firebaseAuth.getUid();
            //upload Image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(s_image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // get the Url of UploadTask
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful())
                            {
                                //save info with image
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("email", "" + email);
                                hashMap.put("name", "" + fullName);
                                hashMap.put("phoneNumber", "" + phoneNumber);
                                hashMap.put("country", "" + country);
                                hashMap.put("state", "" + state);
                                hashMap.put("city", "" + city);
                                hashMap.put("address", "" + address);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("accountType", "User");
                                hashMap.put("online", "true");
                                hashMap.put("profileImage", ""+downloadImageUri);
                                //save to DB
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // data created
                                                progressDialog.dismiss();
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // not created
                                                progressDialog.dismiss();
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkUserFromFirebase() {
        userInfoRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String full_name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            String MobileNumber = Objects.requireNonNull(snapshot.child("phoneNumber").getValue()).toString();
                            String Email = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                            String Country = Objects.requireNonNull(snapshot.child("country").getValue()).toString();
                            String State = Objects.requireNonNull(snapshot.child("state").getValue()).toString();
                            String City = Objects.requireNonNull(snapshot.child("city").getValue()).toString();
                            String CompleteAddress = Objects.requireNonNull(snapshot.child("address").getValue()).toString();
                            String imageURI = Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();

                            sellerName.setText(full_name);
                            sellerPhone.setText(MobileNumber);
                            sMail.setText(Email);
                            sCountry.setText(Country);
                            sState.setText(State);
                            sCity.setText(City);
                            sAddress.setText(CompleteAddress);
                            if (imageURI.equals(null)) {
                                sellerImg.setImageResource(R.drawable.ic_person_grey);
                            } else {
                                Glide.with(getApplicationContext()).load(imageURI).into(sellerImg);

                            }

                        } else {

                            if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null
                                    && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                                sellerPhone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                            sRegButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    inputData();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showImagePickDialog() {
        // options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //Camera Clicked
                            if (checkCameraPermission()) {
                                // cameraPermission allowed
                                pickFromCamera();
                            } else {
                                // cameraPermission not allowed, request
                                requestCameraPermission();
                            }
                        } else {
                            //Gallery Clicked
                            if (checkStoragePermission()) {
                                // Storage Permission allowed
                                pickFromGallery();
                            } else {
                                // Storage Permission not allowed, request
                                requestStoragePermission();

                            }

                        }
                    }
                }).show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        s_image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, s_image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {
        Toast.makeText(getApplicationContext(), "please wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private boolean checkLocationPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                locationPermissions, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    private void findAddress() {
        // find country state , city house no near landmark
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //Complete Address
            String City = addresses.get(0).getLocality();
            String State = addresses.get(0).getAdminArea();
            String Country = addresses.get(0).getCountryName();

            //setAddresses
            sCountry.setText(Country);
            sCity.setText(City);
            sState.setText(State);
            sAddress.setText(address);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // gps location disabled
        Toast.makeText(this, "Please turn on Location...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permission allowed
                        detectLocation();
                    } else {
                        //permission Denied
                        Toast.makeText(getApplicationContext(), "Location permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission Denied
                        Toast.makeText(getApplicationContext(), "Camera permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission Denied
                        Toast.makeText(getApplicationContext(), "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image
                s_image_uri = data.getData();
                //set to imageView
                sellerImg.setImageURI(s_image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageView
                sellerImg.setImageURI(s_image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
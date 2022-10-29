package com.karanja.Register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karanja.R;
import com.karanja.utils.SharePreference;
import com.karanja.views.admin.AdminHomeActivity;
import com.karanja.views.HomeActivity;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    final String TAG = "LOGIN";
    private EditText username;
    private EditText password;
    private EditText email;
    private Button login;
    private Switch isAdminSwitch;
    private TextView registerNowBtn;
    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Parking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);

        registerNowBtn = findViewById(R.id.registerNowBtn);
        isAdminSwitch = findViewById(R.id.admin_switch_login);
        email = findViewById(R.id.email_edt);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(view -> {
            final String usernameTxt = username.getText().toString();
            final String passwordTxt = password.getText().toString();
            final String emailTxt = email.getText().toString();
            if (usernameTxt.isEmpty() || passwordTxt.isEmpty()) {
                Toast.makeText(LoginActivity.this, "enter username and pasword", Toast.LENGTH_SHORT).show();

            } else {
                if (isAdminSwitch.isChecked()) {
                    signIn(emailTxt, passwordTxt);
                } else {
                    databaseReference.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //check idNo if it exist
                            if (dataSnapshot.hasChild(usernameTxt)) {
                                SharePreference.getINSTANCE(getApplicationContext()).setUser(usernameTxt);
                                final String getpassword = dataSnapshot.child(usernameTxt).child("password").getValue(String.class);
                                if (getpassword.equals(passwordTxt)) {
                                    Toast.makeText(LoginActivity.this, "login success", Toast.LENGTH_SHORT).show();
                                    SharePreference.getINSTANCE(getApplicationContext()).setPhoneNumber(dataSnapshot.child(usernameTxt).child("phoneNumber").getValue(String.class));
                                    //login to user activity
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "wrong password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "invalid id", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
        });


        registerNowBtn.setOnClickListener(v -> {
            //open register
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

        });

    }

    private void signIn(String txt_email, String txt_password) {
        mAuth.signInWithEmailAndPassword(txt_email, txt_password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        Toast.makeText(LoginActivity.this, "Log in success.", Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addNaivasParkingSpace() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        ParkingSpace parkingSpace = new ParkingSpace();
//        parkingSpace.setName("Naivas");
//        parkingSpace.setAddress("Gakere Road, Nyeri Town, Central");
//        parkingSpace.setPhone("+254202400498");
//        parkingSpace.setFee(50);
//        parkingSpace.setStatus(5);
//        List<SlotDetails> slotDetails = new ArrayList<>();
//        SlotDetails slotDetails1 = new SlotDetails();
//        slotDetails1.setSlot(1);
//        slotDetails1.setOccupant(null);
//        slotDetails1.setCheckIn(null);
//        slotDetails1.setCheckOut(null);
//        SlotDetails slotDetails2 = new SlotDetails();
//        slotDetails2.setSlot(2);
//        slotDetails2.setOccupant(null);
//        slotDetails2.setCheckIn(null);
//        slotDetails2.setCheckOut(null);
//        SlotDetails slotDetails3 = new SlotDetails();
//        slotDetails3.setSlot(3);
//        slotDetails3.setOccupant(null);
//        slotDetails3.setCheckIn(null);
//        slotDetails3.setCheckOut(null);
//        SlotDetails slotDetails4 = new SlotDetails();
//        slotDetails4.setSlot(4);
//        slotDetails4.setOccupant(null);
//        slotDetails4.setCheckIn(null);
//        slotDetails4.setCheckOut(null);
//        SlotDetails slotDetails5 = new SlotDetails();
//        slotDetails5.setSlot(5);
//        slotDetails5.setOccupant(null);
//        slotDetails5.setCheckIn(null);
//        slotDetails5.setCheckOut(null);
//        slotDetails.add(slotDetails1);
//        slotDetails.add(slotDetails2);
//        slotDetails.add(slotDetails3);
//        slotDetails.add(slotDetails4);
//        slotDetails.add(slotDetails5);
//        parkingSpace.setSlots(slotDetails);
//        db.collection("parkingspaces")
//                .document("Naivas")
//                .set(parkingSpace)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "DocumentSnapshot successfully written!");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error writing document", e);
//                    }
//                });
    }
}
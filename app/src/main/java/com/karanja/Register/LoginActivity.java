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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.R;
import com.karanja.utils.SharePreference;
import com.karanja.views.HomeActivity;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    final String TAG = "LOGIN";
    private EditText username;
    private EditText password;
    private Button login;
    Switch active;
    private TextView registerNowBtn;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        // username=findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        // active=findViewById(R.id.active);
        //TextView btn = findViewById(R.id.registerNowBtn);
        registerNowBtn = findViewById(R.id.registerNowBtn);
        databaseReference = FirebaseDatabase.getInstance().getReference();
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
        login.setOnClickListener(view -> {

            final String usernameTxt = username.getText().toString();


            final String passwordTxt = password.getText().toString();
            if (usernameTxt.isEmpty() || passwordTxt.isEmpty()) {
                Toast.makeText(LoginActivity.this, "enter username and pasword", Toast.LENGTH_SHORT).show();

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

                      /* if (dataSnapshot.child(input1).exists()) {
                           if (dataSnapshot.child(input1).child("password").getValue(String.class).equals(input2)) {
                              if (active.isChecked()) {
                                   if (dataSnapshot.child(input1).child("as").getValue(String.class).equals("admin")) {
                                       preferences.setDataLogin(MainActivity.this, true);
                                       preferences.setDataAs(MainActivity.this, "admin");
                                       startActivity(new Intent(MainActivity.this, AdminActivity.class));
                                   } else if (dataSnapshot.child(input1).child("as").getValue(String.class).equals("user")){
                                       preferences.setDataLogin(MainActivity.this, true);
                                       preferences.setDataAs(MainActivity.this, "user");
                                       startActivity(new Intent(MainActivity.this, UserActivity.class));
                                   }
                               }
                           else {
                                   if (dataSnapshot.child(input1).child("as").getValue(String.class).equals("admin")) {
                                       preferences.setDataLogin(MainActivity.this, false);
                                       startActivity(new Intent(MainActivity.this, AdminActivity.class));

                                   } else if (dataSnapshot.child(input1).child("as").getValue(String.class).equals("user")){
                                       preferences.setDataLogin(MainActivity.this, false);
                                       startActivity(new Intent(MainActivity.this, UserActivity.class));
                                   }
                               }

                           } else {
                               Toast.makeText(MainActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                           }
                       }
                       else {
                           Toast.makeText(MainActivity.this, "Not registered!", Toast.LENGTH_SHORT).show();
                       }

                        */
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {


                    }
                });
            }


        });


        registerNowBtn.setOnClickListener(v -> {
            //open register
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

        });

    }
    /*@Override
    protected void onStart() {
        super.onStart();
        if (preferences.getDataLogin(this)) {
            if (preferences.getDataAs(this).equals("admin")) {
               //startActivity(new Intent(this, AdminActivity.class));


                  Intent intent2 = new Intent( MainActivity.this, AdminActivity.class);
                startActivity(intent2);


                finish();
            } else {
               // startActivity(new Intent(this, UserActivity.class));
                 Intent intent2 = new Intent( MainActivity.this, UserActivity.class);
                  startActivity(intent2);
                finish();

            }
        }
    }
*/
}
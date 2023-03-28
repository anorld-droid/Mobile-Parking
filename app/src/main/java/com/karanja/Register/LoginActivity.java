package com.karanja.Register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.User;
import com.karanja.R;
import com.karanja.utils.SharePreference;
import com.karanja.views.HomeActivity;
import com.karanja.views.admin.AdminHomeActivity;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    final String TAG = "LOGIN";
    DatabaseReference databaseReference;
    private EditText password;
    private EditText email;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch isAdminSwitch;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Parking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        TextView registerNowBtn = findViewById(R.id.registerNowBtn);
        isAdminSwitch = findViewById(R.id.admin_switch_login);
        email = findViewById(R.id.email_edt);

        mDatabase = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(view -> {
            final String passwordTxt = password.getText().toString();
            final String emailTxt = email.getText().toString();
            if (passwordTxt.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter your email and password", Toast.LENGTH_SHORT).show();
            } else {
                signIn(emailTxt, passwordTxt);
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

                        Intent intent;
                        if (isAdminSwitch.isChecked()) {
                            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                            getUserCredentials("User", Objects.requireNonNull(task.getResult().getUser()).getUid());
                        } else {
                            intent = new Intent(LoginActivity.this, HomeActivity.class);
                            getUserCredentials("User", Objects.requireNonNull(task.getResult().getUser()).getUid());
                        }
                        startActivity(intent);
                        finish();
                        Toast.makeText(LoginActivity.this, "Log in success.", Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserCredentials(String userType, String userID) {
        mDatabase.collection(userType).document(userID).get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    User user = documentSnapshot.toObject(User.class);
                    assert user != null;
                    SharePreference.getINSTANCE(getApplicationContext()).setUser(user.getFirstName() + " " + user.getLastName());
                    SharePreference.getINSTANCE(getApplicationContext()).setPhoneNumber(user.getPhone());
                    SharePreference.getINSTANCE(getApplicationContext()).setUserType(userType);
                });
    }
}
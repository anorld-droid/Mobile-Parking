package com.karanja.Register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.User;
import com.karanja.R;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    private static boolean verifyPhoneNumber(String phone) {
        if (phone.equals("")) {
            return false;
        }
        return phone.length() == 10 && phone.startsWith("0");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText fullName = findViewById(R.id.fullname);
        final EditText email = findViewById(R.id.email);
        final EditText phoneNo = findViewById(R.id.phone_number);
        final EditText password = findViewById(R.id.password);
        @SuppressLint("UseSwitchCompatOrMaterialCode") final Switch isAdminSwitch = findViewById(R.id.admin_switch_reg);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        final Button registerBtn = findViewById(R.id.registerBtn);
        final TextView loginNowBtn = findViewById(R.id.loginNow);

        registerBtn.setOnClickListener(v -> {
            //get data from edittext  into strings var
            final String name = fullName.getText().toString();
            final String phoneNumber = phoneNo.getText().toString();
            final String passwordTxt = password.getText().toString();
            final String emailTxt = email.getText().toString();
            //check all fields are filled
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(emailTxt) || TextUtils.isEmpty(passwordTxt) || !verifyPhoneNumber(phoneNumber)) {
                Toast.makeText(RegisterActivity.this, "Fill the missing fields", Toast.LENGTH_SHORT).show();
            } else if (!validateEmail(emailTxt)) {
                Toast.makeText(RegisterActivity.this, "Invalid email.", Toast.LENGTH_SHORT).show();
            } else {
                signUpUser(isAdminSwitch.isChecked(), emailTxt, passwordTxt, name, phoneNumber);
            }
        });

        loginNowBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

    }

    public void signUpUser(Boolean isAdminSwitchChecked, String txt_email, String txt_password, String txt_name, String txt_contact_no) {
        try {
            String[] names = txt_name.split(" ");
            String fName = names[0];
            String lName = names[1];
            mAuth.createUserWithEmailAndPassword(txt_email, txt_password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser mAuthCurrentUser = mAuth.getCurrentUser();
                            User user = new User();
                            user.setEmail(txt_email);
                            user.setFirstName(fName);
                            user.setLastName(lName);
                            user.setPhone(txt_contact_no);
                            assert mAuthCurrentUser != null;
                            if (isAdminSwitchChecked) {
                                user.setRole("Admin");
                                addUserToDB("Admin", Objects.requireNonNull(task.getResult().getUser()).getUid(), user);
                            } else {
                                user.setRole("User");
                                addUserToDB("User", Objects.requireNonNull(task.getResult().getUser()).getUid(), user);
                            }
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception exception) {
            Toast.makeText(this, "Try again, make sure you fill your full names separated by space \" \"", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserToDB(String userType, String userID, User user) {
        mDatabase.collection(userType).document(userID).set(user)
                .addOnSuccessListener(documentReference -> Log.d("TAG", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));

    }

    private boolean validateEmail(String email) {
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        //Compile regular expression to get the pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
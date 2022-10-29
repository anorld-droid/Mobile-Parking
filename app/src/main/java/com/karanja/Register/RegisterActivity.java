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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.User;
import com.karanja.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    private final String TAG = "REG/ACT";
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText fullname = findViewById(R.id.fullname);
        final EditText username = findViewById(R.id.username);
        final EditText email = findViewById(R.id.email);
        final EditText phoneNo = findViewById(R.id.phone_number);
        final EditText password = findViewById(R.id.password);
        final EditText conPassword = findViewById(R.id.conPassword);
        final Switch isAdminSwitch = findViewById(R.id.admin_switch_reg);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        final Button registerBtn = findViewById(R.id.registerBtn);
        final TextView loginNowBtn = findViewById(R.id.loginNow);

        registerBtn.setOnClickListener(v -> {

            //get data from edittext  into strings var
            final String fullnameTxt = fullname.getText().toString();
            final String usernameTxt = username.getText().toString();
            final String phoneNoTxt = phoneNo.getText().toString();
            final String passwordTxt = password.getText().toString();
            final String conpasswordTxt = conPassword.getText().toString();
            final String emailTxt = email.getText().toString();
            //check all fields are filled
            if (fullnameTxt.isEmpty() || usernameTxt.isEmpty() || phoneNoTxt.isEmpty() || passwordTxt.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Fill the missing fields", Toast.LENGTH_SHORT).show();
            }
            //password match
            else if (!passwordTxt.equals(conpasswordTxt)) {
                Toast.makeText(RegisterActivity.this, "Password Mismatch", Toast.LENGTH_SHORT).show();
            } else if (!validateEmail(emailTxt)) {
                Toast.makeText(RegisterActivity.this, "Invalid email.", Toast.LENGTH_SHORT).show();
            } else {
                if (isAdminSwitch.isChecked()) {
                    signUpUser(emailTxt, passwordTxt, fullnameTxt, phoneNoTxt);
                } else {
                    //send data to database
                    databaseReference.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //check if id is registerd before
                            if (snapshot.hasChild(usernameTxt)) {
                                Toast.makeText(RegisterActivity.this, "username already registered", Toast.LENGTH_SHORT).show();

                            } else {
                                databaseReference.child("user").child(usernameTxt).child("fullname").setValue(fullnameTxt);

                                // databaseReference.child("user").child(idNoTxt).child("email").setValue(emailTxt);
                                databaseReference.child("user").child(usernameTxt).child("password").setValue(passwordTxt);
                                databaseReference.child("user").child(usernameTxt).child("phoneNumber").setValue(phoneNoTxt);

                                Toast.makeText(RegisterActivity.this, "Registered Succesfullly", Toast.LENGTH_SHORT).show();
                                finish();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });

        loginNowBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

    }

    public void signUpUser(String txt_email, String txt_password, String txt_name, String txt_contact_no) {
        try {
            String[] names = txt_name.split(" ");
            String fName = names[0];
            String lName = names[1];
            mAuth.createUserWithEmailAndPassword(txt_email, txt_password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser mAuthCurrentUser = mAuth.getCurrentUser();
                            User user = new User();
                            user.setEmail(txt_email);
                            user.setFirstName(fName);
                            user.setLastName(lName);
                            user.setPhone(txt_contact_no);
                            assert mAuthCurrentUser != null;
                            addUserToDB(user);
                            Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed. Password too short.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception exception) {
            Toast.makeText(this, "Try again, make sure you fill your full names separated by space \" \"", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserToDB(User user) {
        mDatabase.collection("admins").add(user)
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
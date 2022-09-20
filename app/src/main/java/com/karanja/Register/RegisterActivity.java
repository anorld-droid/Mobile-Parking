package com.karanja.Register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karanja.R;
import com.karanja.views.BasicUtils;
import com.karanja.views.HomeActivity;


public class RegisterActivity extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText fullname =findViewById(R.id.fullname);
        final EditText username =findViewById(R.id.username);
        final EditText idNo =findViewById(R.id.idNo);
        final EditText password =findViewById(R.id.password);
        final EditText conPassword =findViewById(R.id.conPassword);

        final Button registerBtn =findViewById(R.id.registerBtn);
        final TextView loginNowBtn =findViewById(R.id.loginNow);

        registerBtn.setOnClickListener(v -> {

            //get data from edittext  into strings var
            final String fullnameTxt = fullname.getText().toString();
            final String usernameTxt =username.getText().toString();
            final String idNoTxt = idNo.getText().toString();
            final String passwordTxt = password.getText().toString();
            final String conpasswordTxt = conPassword.getText().toString();
            //check all fields are filled
            if (fullnameTxt.isEmpty() || usernameTxt.isEmpty() || idNoTxt.isEmpty() || passwordTxt.isEmpty()){
                Toast.makeText(RegisterActivity.this, "Fill the missing fields", Toast.LENGTH_SHORT).show();
            }
            //password match
            else if (!passwordTxt.equals(conpasswordTxt)){
                Toast.makeText(RegisterActivity.this,"Password Mismatch", Toast.LENGTH_SHORT).show();
            }

            else {
                //send data to database
                databaseReference.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //check if id is registerd before
                        if (snapshot.hasChild(usernameTxt)){
                            Toast.makeText(RegisterActivity.this,"username already registered",Toast.LENGTH_SHORT).show();

                        }
                        else {
                            databaseReference.child("user").child(usernameTxt).child("fullname").setValue(fullnameTxt);

                            // databaseReference.child("user").child(idNoTxt).child("email").setValue(emailTxt);
                            databaseReference.child("user").child(usernameTxt).child("password").setValue(passwordTxt);
                            databaseReference.child("user").child(usernameTxt).child("username").setValue(idNoTxt);

                            Toast.makeText(RegisterActivity.this,"Registered Succesfullly",Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }});

        loginNowBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));


            finish();
        });

    }
}
package com.altor.android.altor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.altor.android.altor.utils.EncryptionTechnology;
import com.altor.android.altor.utils.MyHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Map;


public class Login extends AppCompatActivity implements View.OnClickListener {
    private Button btnRegister,btnLogin;
    private TextView txtForgotPassword;
    private EditText txtusername,txtpassword;
    private MyHandler myHandler;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private String weight,age,sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(getString(R.string.dbName));

        btnLogin = (Button) findViewById(R.id.btnSignIn);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtForgotPassword = (TextView) findViewById(R.id.txtforgotPass);
        txtusername = (EditText) findViewById(R.id.txtUsername);
        txtpassword = (EditText) findViewById(R.id.txtpassword);

        myHandler = new MyHandler(this);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        txtForgotPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view == btnLogin)
            login();
        else if(view == btnRegister)
            registerNewUser();
        else if(view == txtForgotPassword)
            forgotPassword();
    }

    private void forgotPassword() {
        txtForgotPassword.setTextColor(Color.parseColor("#f64c73"));
    }
    private void registerNewUser() {
        startActivity(new Intent(this,Registration.class));
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builer = new AlertDialog.Builder(this);
        builer.setMessage("Do you want to exit the app?");
        builer.setTitle("Quit App");
        builer.setCancelable(false);
        builer.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(0);
            }
        });
        builer.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builer.show();
    }
    private void login() {
        myHandler.obtainMessage(1,"Logging In...").sendToTarget();

        String email = txtusername.getText().toString();
        String password = EncryptionTechnology.Encrypt(txtpassword.getText().toString());
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        myHandler.obtainMessage(2,"Logging In...").sendToTarget();
                        if (!task.isSuccessful()) {
                           txtpassword.setError("Invalid username/Password!");
                        } else {
                            fetchUserDetailsFromDB();
                        }
                    }
                });
    }

    private void loginSuccessful() {
        myHandler.obtainMessage(0, new String[]{auth.getCurrentUser().getUid(),age,weight,sex}).sendToTarget();
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    private void fetchUserDetailsFromDB() {
        mDatabase.child("users").child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals("age"))
                    age = (String) dataSnapshot.getValue();
                if(dataSnapshot.getKey().equals("weight")){
                    weight  = (String) dataSnapshot.getValue();
                    loginSuccessful();
                }
                if(dataSnapshot.getKey().equals("sex"))
                    sex = (String) dataSnapshot.getValue();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

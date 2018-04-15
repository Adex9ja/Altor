package com.altor.android.altor;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.altor.android.altor.utils.EncryptionTechnology;
import com.altor.android.altor.utils.MyHandler;
import com.altor.android.altor.utils.PrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity implements View.OnClickListener {
    private EditText tfname,tusername,tpassword,tage,tphoneno,temail,toccup,tweight;
    private Spinner tmaritals,tsext;
    private Button btnRegister;
    private DatabaseReference mDatabase;
    private PrefManager mypref;
    private MyHandler myhandler;
    private FirebaseAuth auth;
    private  String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();


        tfname = (EditText) findViewById(R.id.txtfullname);
        tusername = (EditText) findViewById(R.id.txtUsername);
        tpassword = (EditText) findViewById(R.id.txtpassword);
        tage = (EditText) findViewById(R.id.txtAge);
        tmaritals = (Spinner) findViewById(R.id.cbMaritalStatus);
        tmaritals.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.maritalStatus)));
        tsext = (Spinner) findViewById(R.id.cbSex);
        tsext.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sex)));
        tphoneno = (EditText) findViewById(R.id.txtPhoneNo);
        temail = (EditText) findViewById(R.id.txtEmail);
        toccup = (EditText) findViewById(R.id.txtoccup);
        tweight = (EditText) findViewById(R.id.txtweight);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        mypref = new PrefManager(this);
        myhandler = new MyHandler(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnRegister){
            if(AllFieldEntered())
                RegisterUser();
            else
                Toast.makeText(getApplicationContext(),"Fill all field!",Toast.LENGTH_SHORT).show();
        }

    }

    private boolean AllFieldEntered() {
        boolean mflag = false;
        if(!tusername.getText().toString().equals("") && !tpassword.getText().toString().equals("") && !tfname.getText().toString().equals("") && !temail.getText().toString().equals("") && !tphoneno.getText().toString().equals(""))
            if(!tusername.getText().toString().equals("admin") || !tpassword.getText().toString().equals("1234"))
                mflag = true;
        return mflag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this,Login.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void RegisterUser() {
        email = temail.getText().toString();
        password = EncryptionTechnology.Encrypt(tpassword.getText().toString());
        myhandler.obtainMessage(1,"Please wait...").sendToTarget();
        mDatabase = FirebaseDatabase.getInstance().getReference(getString(R.string.dbName));
        CreateNewEmailAndPassword();
    }
    private void CreateNewEmailAndPassword() {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Registration.this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if(task.getException() instanceof FirebaseAuthUserCollisionException)
                                Toast.makeText(Registration.this, "This email ID already used by someone else", Toast.LENGTH_SHORT).show();
                            else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                                Toast.makeText(Registration.this, "Invaid email format", Toast.LENGTH_SHORT).show();
                            } else {
                            authenticateUser();
                        }
                    }

                });

    }
    private void authenticateUser() {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(Registration.this, "Authentication failed" + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                           } else {
                            saveNewUserToDB();
                        }

                    }
                });
    }
    private void saveNewUserToDB() {
        String encUsername = EncryptionTechnology.Encrypt(tusername.getText().toString());
        String encPassword = EncryptionTechnology.Encrypt(tpassword.getText().toString());
        User user = new User(tfname.getText().toString(),encUsername,encPassword,
                tage.getText().toString(),tphoneno.getText().toString(),temail.getText().toString(),
                toccup.getText().toString(),tmaritals.getSelectedItem().toString(),tsext.getSelectedItem().toString(),
                tweight.getText().toString());
        String userId = auth.getCurrentUser().getUid();
        myhandler.obtainMessage(2,null).sendToTarget();
        RegistrationSuccessful();

       /** mDatabase.child("users_entity").child(userId).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                myhandler.obtainMessage(2,null).sendToTarget();
                if (databaseError != null) {
                   RegistrationFailed();
                } else {
                   RegistrationSuccessful();
                }
            }


        });*/
    }
    private void RegistrationFailed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Failed");
        builder.setCancelable(false);
        builder.setMessage("Registration Failed, Username might have been used by another user");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private void RegistrationSuccessful() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Successful");
        builder.setCancelable(false);
        builder.setMessage("Registration Successful! Do you want to proceed to Home page?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                proceedToLogin();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                clearAllFieds();
            }
        });
        builder.show();

    }
    private void proceedToLogin() {
        String username = tusername.getText().toString();
        mypref.saveLoginCookie(new String[]{username,tage.getText().toString(),tweight.getText().toString(),tsext.getSelectedItemPosition() == 1 ? "Male" : "Female"});
        Intent intent = new Intent(Registration.this,MainActivity.class);
        intent.putExtra(getString(R.string.username),username);
        startActivity(intent);
        finish();
    }
    private void clearAllFieds() {
        tfname.setText("");
        tusername.setText("");
        tpassword.setText("");
        tage.setText("");
        tphoneno.setText("");
        temail.setText("");
        toccup.setText("");
        tmaritals.setSelection(0);
        tsext.setSelection(0);
        tweight.setText("");
    }
    public static class User {

        public String fname,username,password,age,phoneno,email,occup,weight;
        public String maritals,sex;

        public User(String fname,String username,String password,String age,String phoneno,
                    String email,String occup,String maritals,String sex,String weight) {
            this.fname = fname;
            this.username = username;
            this.password = password;
            this.age = age;
            this.phoneno = phoneno;
            this.email = email;
            this.occup = occup;
            this.maritals = maritals;
            this.sex = sex;
            this.weight = weight;
        }

    }
}

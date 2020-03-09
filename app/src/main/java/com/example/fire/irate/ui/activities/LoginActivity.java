package com.example.fire.irate.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.android.logindemo.Main.ImagesActivity;
import com.fire.fire.postandcommenttutorial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
//variables
    private EditText Name;
    private EditText Password;
    private TextView Info;
    private Button Login;
    private int counter = 5;
    private TextView userRegistration;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private TextView forgotPassword;
    protected FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //xml layout
        setContentView(R.layout.login);

        //assigning variables to objects
        Name = (EditText) findViewById(R.id.etName);
        Password = (EditText) findViewById(R.id.etPassword);
        Info = (TextView) findViewById(R.id.tvInfo);
        Login = (Button) findViewById(R.id.btnLogin);
        userRegistration = (TextView) findViewById(R.id.tvRegister);
        forgotPassword = (TextView) findViewById(R.id.tvForgotPassword);

        //default showing in text view
        Info.setText("No of attempts remaining: 5");

        //firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //pop up progress box
        progressDialog = new ProgressDialog(this);

        //firebase finding user capabilty
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //if user not null go to images activity
        if(user != null){

           // User userr = new User();
           // userr.setEmail(Name.getText().toString());
            //userr.setUser(Name.getText().toString());
            //userr.setUid(firebaseAuth.getCurrentUser().getUid());


            finish();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));



        }
    //validating user
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(Name.getText().toString(), Password.getText().toString());
            }
        });
    //button to regeister activity
        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    //button to pass word reset
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, UpdatePassword.class));
            }
        });
    }

    private void validate(String userName, String userPassword) {
    //simple dialog box
        progressDialog.setMessage("Welcome To iRate");
        progressDialog.show();
    //seeing if users
        firebaseAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();

                   // Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                   // startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    checkIfEmailVerified();
                }else{
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    counter--;
                    Info.setText("No of attempts remaining: " + counter);
                    progressDialog.dismiss();
                    if(counter == 0){
                        Login.setEnabled(false);
                    }
                }
            }
        });


    }
    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        }
        else
        {

            Toast.makeText(LoginActivity.this, "Please verify the email ", Toast.LENGTH_SHORT).show();
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());


            //restart this activity

        }
    }
/*
    private void checkEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        Boolean emailflag = firebaseUser.isEmailVerified();

        startActivity(new Intent(LoginActivity.this, ImagesActivity.class));

     if(emailflag){
           finish();


          startActivity(new Intent(LoginActivity.this, ImagesActivity.class));
      }else{
          Toast.makeText(this, "Verify your email", Toast.LENGTH_SHORT).show();
           firebaseAuth.signOut();
        }
    }
    */

}

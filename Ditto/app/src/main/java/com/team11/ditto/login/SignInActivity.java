package com.team11.ditto.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team11.ditto.MainActivity;
import com.team11.ditto.R;

/**
 * Activity to login a User so they can access the application
 * @author Reham Albakouni
 */
public class SignInActivity extends AppCompatActivity {
    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    final String TAG = "Sample";

    Context context = this;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Instructions for creating Activity
     * -Image background
     * -input fields to sign in the user
     * -button to transfer to sign up Activity
     * -button to submit login details
     *
     * @param savedInstanceState current app state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.login_email_field);
        passwordField = findViewById(R.id.login_password_field);
        loginButton = findViewById(R.id.login_button);

        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = db.collection("User");

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();

                Log.d(TAG, "SIGNING IN");

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "loginUserWithEmail:success");
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Log.d(TAG, "loginUserWithEmail:failure");
                            }
                        }
                    });
            }
        });
    }
}

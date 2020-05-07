package com.example.ugotthis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Users");
    private FirebaseAuth mAuth;
    EditText password, email;
    Button register;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        password = findViewById(R.id.txtNewPassword);
        email = findViewById(R.id.txtNewEmail);
        register = findViewById(R.id.btnNewRegister);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newEmail = email.getText().toString().trim();
                String newPassword = password.getText().toString().trim();
                if(newEmail.isEmpty())
                {
                    email.setError("Enter Email");
                    return;
                }
                if(newPassword.isEmpty())
                {
                    password.setError("Enter Password");
                    return;
                }
                if(newPassword.length() < 6)
                {
                    password.setError("Must be 6 chars in length");
                    return;
                }
                if(!newEmail.contains("@") || !newEmail.contains(".com"))
                {
                    email.setError("enter valid email address");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(newEmail, newPassword)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {

                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(MainActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                                    CollectionReference notebookRef = FirebaseFirestore.getInstance()
                                            .collection("Users");
                                    notebookRef.add(new Users(newEmail, mAuth.getUid()));
                                }



                                else
                                {

                                    Toast.makeText(MainActivity.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            startActivity(intent);

        }
    }
}

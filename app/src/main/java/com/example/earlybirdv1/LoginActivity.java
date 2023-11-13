package com.example.earlybirdv1;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    // Database helper
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.login_username_et);
        passwordEditText = findViewById(R.id.login_password_et);
        loginButton = findViewById(R.id.login_login_btn);

        // Initialize the database helper
        dbHelper = new MyDatabaseHelper(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String enteredUsername = usernameEditText.getText().toString();
                String enteredPassword = passwordEditText.getText().toString();

                // Check if any of the fields are empty
                if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Use the database helper to check user credentials
                    dbHelper.checkUserCredentials(enteredUsername, enteredPassword, new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(Task<Boolean> task) {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult()) {
                                // Credentials are correct, redirect to HomePageActivity and pass the userId
                                Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                                intent.putExtra("userId", dbHelper.getCurrentUserId()); // Pass the userId
                                startActivity(intent);

                                // Finish the LoginActivity to prevent going back to it with the back button
                                finish();
                            } else {
                                // Credentials are incorrect, display a Toast message
                                Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}

package com.example.goeat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goeat.auth.Auth;
import com.example.goeat.auth.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailTV, passwordTV;
    private Button regBtn, toLoginBtn;
    private Auth mAuth;

    private EditText usernameTV;
    private RadioGroup genderRG;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = Auth.getInstance();
        initializeUI();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    private void registerNewUser() {
        String email = emailTV.getText().toString();
        String password = passwordTV.getText().toString();
        String username = usernameTV.getText().toString();
        int selectedId = genderRG.getCheckedRadioButtonId();
        String gender = (selectedId == R.id.female) ? "Female" : "Male";

        if (!Validator.isValidEmail(email)) {
            emailTV.setError("Email không hợp lệ");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            passwordTV.setError(Validator.PASSWORD_RULE);
            return;
        }
        if (!Validator.isValidUsername(username)) {
            usernameTV.setError(Validator.USERNAME_RULE);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        User user = new User(username, email, gender);

        mAuth.createUserWithEmailAndPassword(user, password)
                .addOnCompleteListener(new OnCompleteListener<User>() {
                    @Override
                    public void onComplete(@NonNull Task<User> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d("Login", "onComplete: " + task.getResult());
                            Toast.makeText(getApplicationContext(), "Chào mừng " + mAuth.getCurrentUser().getUsername(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(SignUpActivity.this, TabActivity.class);
                            startActivity(intent);
                            SignUpActivity.this.finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Đăng kí thất bại", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("State:", "onStop");
        finish();
    }

    private void initializeUI() {
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        regBtn = findViewById(R.id.register);
        toLoginBtn = findViewById(R.id.toLogin);
        usernameTV = findViewById(R.id.username);
        genderRG = findViewById(R.id.sex);
        progressBar = findViewById(R.id.progressBar);
    }
}
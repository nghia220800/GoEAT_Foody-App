package com.example.goeat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.goeat.auth.Auth;
import com.example.goeat.auth.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    private EditText emailTV, passwordTV;
    private Button loginBtn, toSignUpBtn;
    private ProgressBar progressBar;
    private Button forgotBtn;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private Auth mAuth;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("State:", "onCreate");
        setContentView(R.layout.activity_login);

        initializeUI();

        mAuth = Auth.getInstance();
        checkAuthState();

        checkPermissions();
    }

    private void setLoading(boolean state){
        if (isLoading != state) {
            isLoading = state;
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
    private void checkAuthState() {
        setLoading(true);
        mAuth.loadCurrentUser().addOnCompleteListener(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                LoginActivity.this.setLoading(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, TabActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }

    private void loginUserAccount() {
        String email = emailTV.getText().toString();
        String password = passwordTV.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Bạn đang bỏ trống email này", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Bạn đang bỏ trống mật khẩu này", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<User>() {
                    @Override
                    public void onComplete(@NonNull Task<User> task) {
                        LoginActivity.this.setLoading(false);
                        if (task.isSuccessful()) {
                            Log.d("Login", "onComplete: " + task.getResult());
                            //Toast.makeText(getApplicationContext(), "Chào mừng "+mAuth.getCurrentUser().getUsername(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(LoginActivity.this, TabActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void sendPasswordResetEmail() {
        String email = emailTV.getText().toString();
        if (!Validator.isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Vui lòng nhập email...", Toast.LENGTH_LONG).show();
            return;
        }
        setLoading(true);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setLoading(false);
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Chúng tôi đã gửi đến email của bạn một hướng dẫn, vui lòng thực hiện đễ thay đổi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initializeUI() {
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        toSignUpBtn = findViewById(R.id.toSignUp);
        loginBtn = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);
        forgotBtn = findViewById(R.id.forgotPassword);
        setupUI();
    }

    private void setupUI(){
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });
        toSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPasswordResetEmail();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("State:", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("State:", "onPause");
    }

    void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Log.d("State:", "onRequest");
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();
            // Initial
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            // Fill with results
            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);
            // Check for WRITE_EXTERNAL_STORAGE
            boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (!storage) {
                // Permission Denied
            } // else: permission was granted, yay!
        }
    }
}
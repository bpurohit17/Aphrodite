package com.bpurohit.aphrodite.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bpurohit.aphrodite.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private String email, password;
//    boolean showContent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

/*
        splashScreen.setKeepOnScreenCondition(() -> true );
        startSomeNextActivity();
        finish();

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (showContent) {
                    content.getViewTreeObserver().removeOnDrawListener(this);
                }
                showContentAfterSomeTime();
                return false;
            }

            private void startSomeNextActivity() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showContent = true;
                    }
                }, 2000);
            }

        });
*/

        binding.signupRedirectText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        binding.txtForgetPassword.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgetActivity.class)));
        binding.btnLogin.setOnClickListener(view -> {
            if (areFieldReady()) {
                login();
            }
        });
    }

    private boolean areFieldReady() {

        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();

        boolean flag = false;
        View requestView = null;

        if (email.isEmpty()) {
            binding.edtEmail.setError("Field is required");
            flag = true;
            requestView = binding.edtEmail;
        } else if (password.isEmpty()) {
            binding.edtPassword.setError("Field is required");
            flag = true;
            requestView = binding.edtPassword;
        } else if (password.length() < 8) {
            binding.edtPassword.setError("Minimum 8 characters");
            flag = true;
            requestView = binding.edtPassword;
        }

        if (flag) {
            requestView.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    private void login() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();

        }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());

    }
}
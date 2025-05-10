package com.devmats.easyshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    Button btnSignUp;
    TextView txtLogin;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        edtUsername = findViewById(R.id.edt_username);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_user_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnSignUp = findViewById(R.id.btn_signup);
        txtLogin = findViewById(R.id.txt_login);
        // In your activity's onCreate or where you need database access
        DBHelper db = DBHelper.getInstance(this);

        btnSignUp.setOnClickListener(v -> {
            String username = edtUsername.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                boolean inserted = db.insertUser(username, email, password);
                if (inserted) {
                    Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        });
    }
}

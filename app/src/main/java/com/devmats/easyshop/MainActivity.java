package com.devmats.easyshop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "EasyShopPrefs";
    private static final String KEY_LAST_LOGIN = "lastLoginTime";

    TextView txtSignup;
    EditText edtUserEmail, edtPassword;
    CheckBox chkKeepLoggedIn;
    Button btnLogin;

    DBHelper dbHelper;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // In your activity's onCreate or where you need database access
        dbHelper = DBHelper.getInstance(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // If user logged in within last 24h, skip login
        long lastLogin = prefs.getLong(KEY_LAST_LOGIN, 0);
        if (System.currentTimeMillis() - lastLogin < 24 * 60 * 60 * 1000L) {
            startActivity(new Intent(this, HomePageActivity.class));
            finish();
            return;
        }

        // Bind views
        txtSignup = findViewById(R.id.txt_signup);
        edtUserEmail = findViewById(R.id.edt_user_email);
        edtPassword = findViewById(R.id.edt_user_password);
        chkKeepLoggedIn = findViewById(R.id.chk_keep_logged_in);
        btnLogin = findViewById(R.id.btn_login);

        txtSignup.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String userEmail = edtUserEmail.getText().toString().trim();
            String userPassword = edtPassword.getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isValidUser = dbHelper.checkUser(userEmail, userPassword);
            if (isValidUser) {
                // If checkbox checked, save login time
                if (chkKeepLoggedIn.isChecked()) {
                    prefs.edit()
                            .putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
                            .apply();
                }
                // Go to home
                startActivity(new Intent(MainActivity.this, HomePageActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

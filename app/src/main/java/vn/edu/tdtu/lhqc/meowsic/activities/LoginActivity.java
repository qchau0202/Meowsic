package vn.edu.tdtu.lhqc.meowsic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import vn.edu.tdtu.lhqc.meowsic.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtLoginEmail, edtLoginPassword;
    private Button btnLogin;
    private TextView tvNavigateToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_login);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvNavigateToRegister = findViewById(R.id.tvNavigateToRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredEmail = edtLoginEmail.getText().toString().trim();
                String enteredPassword = edtLoginPassword.getText().toString().trim();

                // Validation
                if (enteredEmail.isEmpty() || enteredPassword.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!isValidEmail(enteredEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                String savedEmail = sharedPreferences.getString("email", null);
                String savedPassword = sharedPreferences.getString("password", null);

                if (savedEmail == null || savedPassword == null) {
                    Toast.makeText(LoginActivity.this, "No account found. Please register first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (enteredEmail.equals(savedEmail) && enteredPassword.equals(savedPassword)){
                    // Set login state
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();
                    
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvNavigateToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
package vn.edu.tdtu.lhqc.meowsic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import vn.edu.tdtu.lhqc.meowsic.R;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtRegisterName, edtRegisterEmail, edtRegisterPassword;
    private Button btnRegister;
    private TextView tvNavigateToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtRegisterName = findViewById(R.id.edtRegisterName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvNavigateToLogin = findViewById(R.id.tvNavigateToLogin);

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String name = edtRegisterName.getText().toString().trim();
                String email = edtRegisterEmail.getText().toString().trim();
                String password = edtRegisterPassword.getText().toString().trim();

                // Validation
                if(name.isEmpty() || email.isEmpty() || password.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if(name.length() < 2) {
                    Toast.makeText(RegisterActivity.this, "Name must be at least 2 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if(!isValidEmail(email)) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if(password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if user already exists
                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String existingEmail = sharedPreferences.getString("email", null);
                
                if(existingEmail != null && existingEmail.equals(email)) {
                    Toast.makeText(RegisterActivity.this, "An account with this email already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save user data
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", name);
                editor.putString("email", email);
                editor.putString("password", password); // Note: In production, hash this password
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Toast.makeText(RegisterActivity.this, "Registration successful! Please login", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        tvNavigateToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
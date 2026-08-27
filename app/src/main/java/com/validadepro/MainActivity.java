package br.com.validadepro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText companyEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private Button loginButton;
    private Button googleButton;
    private LinearLayout loginContainer;
    private LinearLayout dashboardContainer;
    private TextView companyLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        companyEdit = findViewById(R.id.companyEdit);
        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleButton);
        loginContainer = findViewById(R.id.loginContainer);
        dashboardContainer = findViewById(R.id.dashboardContainer);
        companyLabel = findViewById(R.id.companyLabel);

        companyEdit.setText("Padaria Lobo");
        usernameEdit.setText("lobo");
        passwordEdit.setText("lobopt260");

        loginButton.setOnClickListener(v -> attemptLogin());
        googleButton.setOnClickListener(v ->
                Toast.makeText(this, "Login Google pronto para Firebase/Google Sign-In", Toast.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        String company = companyEdit.getText() == null ? "" : companyEdit.getText().toString().trim();
        String username = usernameEdit.getText() == null ? "" : usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText() == null ? "" : passwordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(company) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Preencha empresa, usuário e senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean valid =
                (username.equalsIgnoreCase("lobo") && password.equals("lobopt260")) ||
                (username.equalsIgnoreCase("beta") && password.equals("pt260")) ||
                (username.equalsIgnoreCase("padaria") && password.equals("lobo2026"));

        if (!valid) {
            Toast.makeText(this, "Credenciais inválidas.", Toast.LENGTH_SHORT).show();
            return;
        }

        companyLabel.setText(company.isEmpty() ? "Padaria Lobo" : company);
        loginContainer.setVisibility(View.GONE);
        dashboardContainer.setVisibility(View.VISIBLE);
    }
}

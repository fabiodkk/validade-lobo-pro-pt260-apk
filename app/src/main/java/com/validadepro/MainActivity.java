package br.com.validadepro;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.InputType;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.widget.ScrollView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private SharedPreferences historyPreferences;

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
        historyPreferences = getSharedPreferences("lobo_pro_history", MODE_PRIVATE);

        companyEdit.setText("Padaria Lobo");
        usernameEdit.setText("lobo");
        passwordEdit.setText("lobopt260");

        loginButton.setOnClickListener(v -> attemptLogin());
        googleButton.setOnClickListener(v ->
                Toast.makeText(this, "Login Google pronto para Firebase/Google Sign-In", Toast.LENGTH_SHORT).show());
        findViewById(R.id.labelButton).setOnClickListener(v -> showLabelDialog());
        findViewById(R.id.historyButton).setOnClickListener(v -> showHistoryDialog());
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

    private void showLabelDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, 0, padding, 0);

        EditText productEdit = new EditText(this);
        productEdit.setHint("Produto");
        form.addView(productEdit);

        EditText validityEdit = new EditText(this);
        validityEdit.setHint("Validade (ex.: 7 dias)");
        form.addView(validityEdit);

        EditText copiesEdit = new EditText(this);
        copiesEdit.setHint("Quantidade de etiquetas");
        copiesEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        copiesEdit.setText("1");
        form.addView(copiesEdit);

        new AlertDialog.Builder(this)
                .setTitle("Gerar etiqueta")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar no histórico", (dialog, which) -> {
                    String product = productEdit.getText().toString().trim();
                    String validity = validityEdit.getText().toString().trim();
                    int copies = parseCopies(copiesEdit.getText().toString());

                    if (TextUtils.isEmpty(product) || TextUtils.isEmpty(validity)) {
                        Toast.makeText(this, "Informe produto e validade.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    savePrint(product, validity, copies);
                    Toast.makeText(this, "Etiqueta salva no histórico do Lobo Pro.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private int parseCopies(String value) {
        try {
            return Math.max(1, Math.min(999, Integer.parseInt(value.trim())));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private void savePrint(String product, String validity, int copies) {
        JSONArray history = readHistory();
        JSONArray updatedHistory = new JSONArray();
        JSONObject print = new JSONObject();
        try {
            print.put("product", product);
            print.put("validity", validity);
            print.put("copies", copies);
            print.put("printed_at", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
            updatedHistory.put(print);
            for (int index = 0; index < history.length(); index++) {
                updatedHistory.put(history.getJSONObject(index));
            }
            historyPreferences.edit().putString("prints", updatedHistory.toString()).apply();
        } catch (Exception exception) {
            Toast.makeText(this, "Não foi possível salvar a etiqueta.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray readHistory() {
        try {
            return new JSONArray(historyPreferences.getString("prints", "[]"));
        } catch (Exception exception) {
            return new JSONArray();
        }
    }

    private void showHistoryDialog() {
        JSONArray history = readHistory();
        if (history.length() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Histórico do Lobo")
                    .setMessage("Nenhuma etiqueta foi gerada ainda.")
                    .setPositiveButton("Fechar", null)
                    .show();
            return;
        }

        StringBuilder content = new StringBuilder();
        try {
            for (int index = 0; index < history.length(); index++) {
                JSONObject print = history.getJSONObject(index);
                content.append(index + 1)
                        .append(". ")
                        .append(print.optString("product"))
                        .append(" - ")
                        .append(print.optString("validity"))
                        .append("\n")
                        .append("   ")
                        .append(print.optInt("copies", 1))
                        .append(" etiqueta(s) em ")
                        .append(print.optString("printed_at"))
                        .append("\n\n");
            }
        } catch (Exception exception) {
            content.append("Não foi possível ler o histórico.");
        }

        TextView historyText = new TextView(this);
        historyText.setText(content.toString());
        historyText.setTextColor(getColor(R.color.text_primary));
        historyText.setTextSize(16);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        historyText.setPadding(padding, 0, padding, 0);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(historyText);
        new AlertDialog.Builder(this)
                .setTitle("Histórico do Lobo")
                .setView(scrollView)
                .setPositiveButton("Fechar", null)
                .show();
    }
}

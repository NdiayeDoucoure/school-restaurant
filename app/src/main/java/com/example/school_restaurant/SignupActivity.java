package com.example.school_restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText edtFullName = findViewById(R.id.edtFullName);
        EditText edtMatricule = findViewById(R.id.edtMatricule);
        EditText edtPassword = findViewById(R.id.edtPassword);
        Spinner spinnerRole = findViewById(R.id.spinnerRole);
        Button btnSignup = findViewById(R.id.btnSignup);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnSignup.setOnClickListener(view -> {
            String fullName = edtFullName.getText().toString().trim();
            String matricule = edtMatricule.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (fullName.isEmpty() || matricule.isEmpty() || password.isEmpty() || role.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            } else {
                performSignup(fullName, matricule, password, role);
            }
        });
    }

    // Méthode pour effectuer l'inscription via l'API
    private void performSignup(String fullName, String matricule, String password, String role) {
        // Création de l'instance Retrofit pour appeler l'API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000") // URL de votre backend (sur un émulateur Android, utilisez 10.0.2.2 pour localhost)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        new Retrofit.Builder().baseUrl("http://10.0.2.2:5000");

        // Construction du JSON pour envoyer les données
        JSONObject json = new JSONObject();
        try {
            json.put("fullName", fullName);
            json.put("matricule", matricule);
            json.put("password", password);
            json.put("role", role);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Création du corps de la requête
        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        // Appel Retrofit
        retrofit.create(ApiService.class).signup(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Si l'inscription est réussie, affichage d'un message et redirection vers la page de connexion
                    Toast.makeText(SignupActivity.this, "Inscription réussie.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                } else {
                    // En cas d'échec
                    Toast.makeText(SignupActivity.this, "Erreur lors de l'inscription.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // En cas de problème réseau
                Toast.makeText(SignupActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Interface Retrofit pour l'appel API, sans fichier séparé
    public interface ApiService {
        @POST("/api/signup")
        Call<ResponseBody> signup(@Body RequestBody body);
    }
}


package com.example.school_restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText edtMatricule = findViewById(R.id.edtMatricule);
        EditText edtPassword = findViewById(R.id.edtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(view -> {
            String matricule = edtMatricule.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (matricule.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(matricule, password);
            }
        });
    }

    // Méthode pour effectuer la connexion via l'API
    private void performLogin(String matricule, String password) {
        // Création de l'instance Retrofit pour appeler l'API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000") // URL de votre backend (sur un émulateur Android, utilisez 10.0.2.2 pour localhost)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Création de la requête JSON pour l'authentification
        JSONObject json = new JSONObject();
        try {
            json.put("matricule", matricule);
            json.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Création du corps de la requête
        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        // Appel Retrofit
        retrofit.create(ApiService.class).login(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("matricule", matricule);
                    editor.putFloat("balance", 10000);
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Identifiants incorrects.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Interface Retrofit pour l'appel API
    public interface ApiService {
        @POST("/api/login")
        Call<ResponseBody> login(@Body RequestBody body);
    }
}

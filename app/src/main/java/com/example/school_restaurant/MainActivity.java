package com.example.school_restaurant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
    private TextView tvFullName, tvMatricule, tvBalance;
    private ImageView qrCodeImage;
    private Button btnLogout, btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFullName = findViewById(R.id.tvFullName);
        tvMatricule = findViewById(R.id.tvMatricule);
        tvBalance = findViewById(R.id.tvBalance);
        qrCodeImage = findViewById(R.id.qrCodeImage);
        btnLogout = findViewById(R.id.btnLogout);
        btnScan = findViewById(R.id.btnScan);

        // Récupérer le matricule de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String matricule = sharedPreferences.getString("matricule", "Matricule");

        getUserInfo(matricule);

        // Bouton Déconnexion
        btnLogout.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Bouton Scan QR Code
        btnScan.setOnClickListener(view -> {
            String qrData = "{\"matricule\":\"" + matricule + "\",\"deductionAmount\":50}";  // Exemple de déduction de 50 FCFA
            deductBalanceFromQRCode(qrData);
        });
    }

    // Méthode pour obtenir les informations de l'utilisateur via l'API
    private void getUserInfo(String matricule) {
        // Création de l'instance Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000") // URL de votre backend (pour un émulateur Android, utilisez 10.0.2.2 pour localhost)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Création de la requête JSON
        JSONObject json = new JSONObject();
        try {
            json.put("matricule", matricule);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Création du corps de la requête
        RequestBody body = RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json"));
        System.out.println("Requête envoyée : " + json.toString());
        // Appel API Retrofit
        retrofit.create(ApiService.class).getUserInfo(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // Traitement de la réponse et extraction des données
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String fullName = jsonResponse.getString("fullName");
                        float balance = (float) jsonResponse.getDouble("balance");

                        // Mise à jour de l'UI avec les informations de l'utilisateur
                        tvFullName.setText("Nom complet : " + fullName);
                        tvMatricule.setText("Matricule : " + matricule);
                        tvBalance.setText("Solde actuel : " + balance + " FCFA");

                        // Génération du code QR avec le matricule et un montant de déduction exemple (par ex. 50 FCFA)
                        JSONObject qrData = new JSONObject();
                        try {
                            qrData.put("matricule", matricule);
                            qrData.put("deductionAmount", 1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Génération du QR code
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        qrCodeImage.setImageBitmap(
                                barcodeEncoder.encodeBitmap(qrData.toString(), BarcodeFormat.QR_CODE, 400, 400)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Erreur de traitement des données.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Erreur de connexion, réessayez.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Gestion des erreurs de connexion
                Toast.makeText(MainActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode pour déduire le solde de l'étudiant après l'opération de déduction
    private void deductBalanceFromQRCode(String qrData) {
        try {
            JSONObject json = new JSONObject(qrData);
            String matricule = json.getString("matricule");
            int deductionAmount = json.getInt("deductionAmount");

            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3NjQ3OGNhZGY0ODFlOTJiZjIyZmU2NiIsInJvbGUiOiJQZXJzb25uZWwiLCJpYXQiOjE3MzcwNDc3NjYsImV4cCI6MTczNzA1MTM2Nn0.zyi8ikhhRWpMiuUlX3gmNVUv_j2Vjya5DzZagcZm4Gk";

            // Appel à l'API pour effectuer la déduction
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:5000") // URL de votre backend
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Créer la requête JSON pour la déduction
            JSONObject deductionRequest = new JSONObject();
            deductionRequest.put("matricule", matricule);
            deductionRequest.put("deductionAmount", deductionAmount);

            // Création du corps de la requête
            RequestBody body = RequestBody.create(deductionRequest.toString(), okhttp3.MediaType.parse("application/json"));

            // Créer l'en-tête Authorization
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token);

            // Faire l'appel à l'API avec l'en-tête Authorization
            retrofit.create(ApiService.class).deductBalance(headers, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // Mise à jour automatique du solde après déduction
                        updateBalance(matricule);
                        Toast.makeText(MainActivity.this, "Déduction réussie !", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            // Lecture du corps de la réponse d'erreur
                            String errorMessage = response.errorBody().string();
                            Toast.makeText(MainActivity.this, "Erreur de déduction : " + errorMessage, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Erreur de déduction, réessayez.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // Gestion des erreurs
                    Toast.makeText(MainActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Erreur dans le QR code.", Toast.LENGTH_SHORT).show();
        }
    }


    // Méthode pour mettre à jour le solde après la déduction
    private void updateBalance(String matricule) {
        // Appel à l'API pour obtenir le solde mis à jour
        getUserInfo(matricule);
    }

    // Interface Retrofit pour l'appel API
    public interface ApiService {
        @POST("/api/userInfo")
        Call<ResponseBody> getUserInfo(@Body RequestBody body);

        @POST("/api/deduct")
        Call<ResponseBody> deductBalance(@HeaderMap Map<String, String> headers, @Body RequestBody body);
    }
}

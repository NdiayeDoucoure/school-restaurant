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

import org.json.JSONObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
    private TextView tvFullName, tvMatricule, tvBalance;
    private ImageView qrCodeImage;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFullName = findViewById(R.id.tvFullName);
        tvMatricule = findViewById(R.id.tvMatricule);
        tvBalance = findViewById(R.id.tvBalance);
        qrCodeImage = findViewById(R.id.qrCodeImage);
        btnLogout = findViewById(R.id.btnLogout);

        // Récupérer le matricule de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String matricule = sharedPreferences.getString("matricule", "Matricule");

        // Appel à l'API pour récupérer les informations utilisateur
        getUserInfo(matricule);

        // Bouton Déconnexion
        btnLogout.setOnClickListener(view -> {
            // Suppression de la session utilisateur
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Redirection vers l'écran de connexion
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
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

                        // Génération du code QR avec le matricule
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        qrCodeImage.setImageBitmap(
                                barcodeEncoder.encodeBitmap(matricule, BarcodeFormat.QR_CODE, 400, 400)
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

    // Interface Retrofit pour l'appel API
    public interface ApiService {
        @POST("/api/userInfo")
        Call<ResponseBody> getUserInfo(@Body RequestBody body);
    }
}

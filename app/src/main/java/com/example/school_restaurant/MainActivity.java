package com.example.school_restaurant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
    private TextView tvFullName, tvMatricule, tvBalance, tvHistory;
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

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String matricule = sharedPreferences.getString("matricule", "Matricule");

        getUserInfo(matricule);

        btnLogout.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Méthode pour récupérer les informations utilisateur via l'API
    private void getUserInfo(String matricule) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JSONObject json = new JSONObject();
        try {
            json.put("matricule", matricule);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json"));

        retrofit.create(ApiService.class).getUserInfo(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String fullName = jsonResponse.getString("fullName");
                        float balance = (float) jsonResponse.getDouble("balance");

                        tvFullName.setText("Nom complet : " + fullName);
                        tvMatricule.setText("Matricule : " + matricule);
                        tvBalance.setText("Solde actuel : " + balance + " FCFA");

                        JSONArray history = jsonResponse.getJSONArray("mealHistory");
                        LinearLayout historyLayout = findViewById(R.id.historyLayout);
                        historyLayout.removeAllViews();

                        for (int i = 0; i < history.length(); i++) {
                            JSONObject transaction = history.getJSONObject(i);
                            String date = transaction.getString("date");
                            float price = (float) transaction.getDouble("price");

                            LinearLayout transactionLayout = new LinearLayout(MainActivity.this);
                            transactionLayout.setOrientation(LinearLayout.HORIZONTAL);
                            transactionLayout.setWeightSum(3);
                            transactionLayout.setBackgroundColor(getResources().getColor(R.color.white));

                            TextView mealName = new TextView(MainActivity.this);
                            mealName.setText(transaction.getString("mealName"));
                            mealName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                            TextView priceView = new TextView(MainActivity.this);
                            priceView.setText("-" + price + " FCFA");
                            priceView.setTextColor(getResources().getColor(R.color.red));
                            priceView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            Date transactionDateObj = null;
                            try {
                                transactionDateObj = sdf.parse(date);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (transactionDateObj != null) {
                                String formattedDate = new SimpleDateFormat("dd MMM yyyy HH:mm").format(transactionDateObj);
                                TextView transactionDateView = new TextView(MainActivity.this);
                                transactionDateView.setText(formattedDate);
                                transactionDateView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                                transactionLayout.addView(transactionDateView);
                            }

                            transactionLayout.addView(mealName);
                            transactionLayout.addView(priceView);
                            historyLayout.addView(transactionLayout);
                        }

                        // Générer le QR code
                        if (matricule != null && !matricule.isEmpty()) {
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            qrCodeImage.setImageBitmap(
                                    barcodeEncoder.encodeBitmap(matricule, BarcodeFormat.QR_CODE, 400, 400)
                            );
                        } else {
                            Toast.makeText(MainActivity.this, "Matricule invalide", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Erreur de traitement des données : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Erreur de connexion, réessayez. Code : " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
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
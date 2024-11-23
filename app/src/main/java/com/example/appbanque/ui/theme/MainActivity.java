package com.example.appbanque.ui.theme;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanque.R;
import com.example.appbanque.adapters.CompteAdapter;
import com.example.appbanque.api.CompteAPI;
import com.example.appbanque.api.RetrofitClient;
import com.example.appbanque.models.Compte;
import com.example.appbanque.models.TypeCompte;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button btnAdd;
    private Spinner spinnerFormat;
    private CompteAdapter adapter;
    private List<Compte> compteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btn_add);
        spinnerFormat = findViewById(R.id.spinner_format);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialisez l'adaptateur avec une liste vide
        adapter = new CompteAdapter(compteList);
        recyclerView.setAdapter(adapter);

        // Charger les comptes au démarrage en JSON
        loadComptes("json");

        // Écouteur pour le Spinner pour changer de format
        spinnerFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String format = parent.getItemAtPosition(position).toString().toLowerCase();
                loadComptes(format);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadComptes("json");
            }
        });

        // Gestionnaire pour le bouton "Ajouter un compte"
        btnAdd.setOnClickListener(v -> showAddCompteDialog());
    }

    /**
     * Charger les comptes depuis le backend.
     */
    private void loadComptes(String format) {
        CompteAPI api = RetrofitClient.getInstance(format).create(CompteAPI.class);
        api.getComptes().enqueue(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    compteList.clear();
                    compteList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d("MainActivity", "Comptes reçus (" + format + "): " + response.body());
                } else {
                    Toast.makeText(MainActivity.this, "Erreur lors du chargement des comptes.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MainActivity", "Erreur lors du chargement des comptes : " + t.getMessage());
            }
        });
    }

    /**
     * Afficher un dialog pour ajouter un nouveau compte.
     */
    private void showAddCompteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un nouveau compte");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);
        builder.setView(dialogView);

        EditText etSolde = dialogView.findViewById(R.id.et_solde);
        EditText etType = dialogView.findViewById(R.id.et_type);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String soldeStr = etSolde.getText().toString().trim();
            String typeStr = etType.getText().toString().toUpperCase().trim();

            if (TextUtils.isEmpty(soldeStr) || TextUtils.isEmpty(typeStr)) {
                Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double solde = Double.parseDouble(soldeStr);
                TypeCompte type;

                // Vérifiez si le type est valide (COURANT ou EPARGNE)
                try {
                    type = TypeCompte.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(this, "Type invalide. Utilisez COURANT ou EPARGNE.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Créez un nouveau compte
                Compte nouveauCompte = new Compte();
                nouveauCompte.setSolde(solde);
                nouveauCompte.setType(type);
                nouveauCompte.setDateCreation(new java.util.Date());

                String selectedFormat = spinnerFormat.getSelectedItem().toString().toLowerCase();

                // Ajoutez le compte via l'API Retrofit
                CompteAPI api = RetrofitClient.getInstance(selectedFormat).create(CompteAPI.class);
                api.createCompte(nouveauCompte).enqueue(new Callback<Compte>() {
                    @Override
                    public void onResponse(Call<Compte> call, Response<Compte> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(MainActivity.this, "Compte ajouté avec succès!", Toast.LENGTH_SHORT).show();
                            loadComptes(selectedFormat); // Recharge les comptes
                        } else {
                            Toast.makeText(MainActivity.this, "Erreur lors de l'ajout du compte.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Compte> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Veuillez entrer un solde valide.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}

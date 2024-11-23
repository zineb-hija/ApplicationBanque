package com.example.appbanque.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanque.R;
import com.example.appbanque.api.CompteAPI;
import com.example.appbanque.api.RetrofitClient;
import com.example.appbanque.models.Compte;
import com.example.appbanque.models.TypeCompte;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.CompteViewHolder> {

    private final List<Compte> compteList;

    public CompteAdapter(List<Compte> compteList) {
        this.compteList = compteList;
    }

    @NonNull
    @Override
    public CompteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compte, parent, false);
        return new CompteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompteViewHolder holder, int position) {
        Compte compte = compteList.get(position);

        // Remplir les données dans chaque item
        holder.tvId.setText("ID: " + compte.getId());
        holder.tvBalance.setText("Balance: " + compte.getSolde());
        holder.tvType.setText("Type: " + compte.getType().name()); // Conversion en String
        holder.tvDate.setText("Created: " + compte.getDateCreation());

        // Bouton Modifier
        holder.btnEdit.setOnClickListener(v -> showEditDialog(holder.itemView, compte, position));

        // Bouton Supprimer
        holder.btnDelete.setOnClickListener(v -> deleteCompte(holder.itemView, compte, position));
    }

    @Override
    public int getItemCount() {
        return compteList.size();
    }

    public static class CompteViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvBalance, tvType, tvDate;
        Button btnEdit, btnDelete; // Utilisation de Button au lieu d'ImageButton

        public CompteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_id);
            tvBalance = itemView.findViewById(R.id.tv_balance);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private void showEditDialog(View view, Compte compte, int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
        builder.setTitle("Modifier le compte");

        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_add_compte, null);
        builder.setView(dialogView);

        EditText etSolde = dialogView.findViewById(R.id.et_solde);
        EditText etType = dialogView.findViewById(R.id.et_type);

        // Pré-remplir les champs
        etSolde.setText(String.valueOf(compte.getSolde()));
        etType.setText(compte.getType().name());

        builder.setPositiveButton("Modifier", (dialog, which) -> {
            String newSoldeStr = etSolde.getText().toString();
            String newTypeStr = etType.getText().toString().toUpperCase();

            if (newSoldeStr.isEmpty() || newTypeStr.isEmpty()) {
                Toast.makeText(view.getContext(), "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newSolde = Double.parseDouble(newSoldeStr);

                // Valider le type
                try {
                    TypeCompte newType = TypeCompte.valueOf(newTypeStr); // Valider et convertir

                    // Mettre à jour les informations du compte
                    compte.setSolde(newSolde);
                    compte.setType(newType);

                    CompteAPI api = RetrofitClient.getInstance("json").create(CompteAPI.class);
                    api.updateCompte(compte.getId(), compte).enqueue(new Callback<Compte>() {
                        @Override
                        public void onResponse(Call<Compte> call, Response<Compte> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                compteList.set(position, response.body());
                                notifyItemChanged(position);
                                Toast.makeText(view.getContext(), "Compte modifié avec succès.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(view.getContext(), "Erreur lors de la modification.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Compte> call, Throwable t) {
                            Toast.makeText(view.getContext(), "Erreur: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IllegalArgumentException e) {
                    Toast.makeText(view.getContext(), "Type invalide. Utilisez COURANT ou EPARGNE.", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(view.getContext(), "Solde invalide.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteCompte(View view, Compte compte, int position) {
        CompteAPI api = RetrofitClient.getInstance("json").create(CompteAPI.class);
        api.deleteCompte(compte.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    compteList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(view.getContext(), "Compte supprimé avec succès.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(view.getContext(), "Erreur lors de la suppression.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(view.getContext(), "Erreur: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

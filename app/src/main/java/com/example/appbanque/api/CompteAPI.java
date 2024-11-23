package com.example.appbanque.api;

import com.example.appbanque.models.Compte;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CompteAPI {
    @GET("comptes")
    Call<List<Compte>> getComptes();

    @POST("comptes")
    Call<Compte> createCompte(@Body Compte compte);

    @PUT("comptes/{id}")
    Call<Compte> updateCompte(@Path("id") Long id, @Body Compte compte);

    @DELETE("comptes/{id}")
    Call<Void> deleteCompte(@Path("id") Long id);
}

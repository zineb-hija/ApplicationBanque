package com.example.appbanque.api;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8082/banque/";
    private static Retrofit retrofit;
    private static Retrofit retrofitJson;
    private static Retrofit retrofitXml;

    public static Retrofit getInstance(String format) {
        if ("xml".equalsIgnoreCase(format)) {
            if (retrofitXml == null) {
                retrofitXml = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(SimpleXmlConverterFactory.createNonStrict()) // Utilisez createNonStrict()
                        .build();
            }
            return retrofitXml;
        } else {
            if (retrofitJson == null) {
                retrofitJson = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofitJson;
        }
    }
}

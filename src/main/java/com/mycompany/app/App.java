package com.mycompany.app;

import com.mycompany.services.HttpService;
import io.vavr.control.Try;
import okhttp3.OkHttpClient;
import com.google.gson.GsonBuilder;
import com.mycompany.repository.DatabaseClient;
import com.mycompany.financial_api.FinancialAPIClient;
import com.mycompany.services.DatasetService;

import java.time.Instant;

public class App {

    public void init() {
        Try
                .of(() -> new HttpService(
                        new DatasetService(
                                new FinancialAPIClient(
                                "https://larlonboubrsjpexqzzolyfl.z1.web.core.windows.net",
                                new OkHttpClient()),
                                new DatabaseClient()),
                        new GsonBuilder()
                                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                                .create(),
                        "application/json"))
                .get()
                .runService();
    }

    public static void main(String[] args) {
        new App().init();
    }
}

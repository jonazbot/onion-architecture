package com.mycompany.financial_api;

import com.google.gson.Gson;
import com.mycompany.app.ValidationException;
import com.mycompany.model.Company;
import io.vavr.control.Try;
import lombok.Value;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Value
public class FinancialAPIClient {
    String api;
    OkHttpClient client;

    public List<String> symbols() throws IOException, ValidationException {
        return validateSymbols(Try
                .of(() -> client
                        .newCall(new Request
                                .Builder()
                                .url(api + "/all.json")
                                .build())
                        .execute())
                .get());
    }

    private List<String> validateSymbols(Response response) throws IOException, ValidationException {
        if (response.body() == null) {
            throw new ValidationException("body is `null`"); }
        return List.of(new Gson().fromJson(response.body().string(), String[].class));
    }

    public Optional<Company> financialData(String symbol) throws IOException {
        return validateFinancialData(Try
                .of(() -> client
                        .newCall(new Request
                                .Builder()
                                .url(api + "/" + symbol.toUpperCase() + ".json")
                                .build())
                        .execute())
                .get());
    }

    private Optional<Company> validateFinancialData(Response response) throws IOException {
        if (response.body() == null || response.code() == 404)
            return Optional.empty();
        return Optional.ofNullable(new Gson().fromJson(response.body().string(), Company.class));
    }
}

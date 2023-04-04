package com.mycompany.services;

import com.google.gson.Gson;
import com.mycompany.app.ValidationException;
import com.mycompany.model.Company;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static spark.Spark.get;
import static spark.Spark.post;

import java.util.stream.Collectors;

@Value
public class HttpService {
    DatasetService datasetService;
    Gson gson;
    String json;
    private static final Logger logger = LoggerFactory.getLogger(HttpService.class);

    public void runService() {
        get("/ping", (req, res) -> {
            logger.atInfo().log("GET /ping");
            return "pong";  });
        get("/datasets/:id", (request, response) -> datasetService
                .getDataset(Integer.parseInt(request.params("id")))
                .map(src -> {
                    response.type(json);
                    return gson.toJson(src); })
                .orElseGet(() -> {
                    response.status(404);
                    return "dataset with id " + Integer.parseInt(request.params("id")) + " does not exist"; }));
        get("/datasets", (request, response) -> {
            response.type(json);
            return gson.toJson(datasetService.getDatasetIds()); });
        post("/datasets", (request, response) -> {
            try {
                logger.atInfo().log("POST /datasets");
                response.header("Location", "/datasets/" + datasetService.newDataset(request.body()));
                response.status(201);
                return ""; }
            catch (ValidationException ex) {
                logger.atError().log("error: ", ex);
                response.status(400);
                return ex.getMessage(); }});
        get("/recommendation", (request, response) -> {
            logger.atInfo().log("GET /recommendation");
            return datasetService.getRecommendation(request.queryParams("dataset"))
                    .map(companyList -> companyList
                            .stream().map(Company::getSymbol)
                            .collect(Collectors.toList()))
                    .map(companyList -> {
                        response.type(json);
                        return gson.toJson(companyList); })
                    .orElseGet(() -> "invalid dataset id: " + request.queryParams("dataset")); });
    }
}

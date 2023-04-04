package com.mycompany.services;

import com.mycompany.model.NewDataset;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.app.ValidationException;
import com.mycompany.repository.DatabaseClient;
import com.mycompany.model.Dataset;
import com.mycompany.model.Company;
import com.mycompany.financial_api.FinancialAPIClient;
import io.vavr.control.Try;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingDouble;

@Value
public class DatasetService {
    FinancialAPIClient financialAPIClient;
    DatabaseClient databaseClient;
    Logger logger = LoggerFactory.getLogger(DatasetService.class);

    public DatasetService(FinancialAPIClient financialAPIClient, DatabaseClient databaseClient) {
        this.financialAPIClient = financialAPIClient;
        this.databaseClient = databaseClient;
    }

    public int newDataset(String body) throws ValidationException, IOException {
        var newDatasetSymbols = parsePostDatasetRequest(body).getSymbols();
        var symbols = newDatasetSymbols
                .stream().anyMatch(symbol -> symbol.equals(":all")) ?
                        financialAPIClient.symbols()
                        : newDatasetSymbols;
        var companies = symbols
                .stream().map(this::findCompany)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (companies.size() == symbols.size())
            return databaseClient.saveDataset(companies);
        throw new IOException("could not get data for all requested companies");
    }

    private Optional<Company> findCompany(String symbol) {
        return Try
                .of(() -> financialAPIClient.financialData(symbol))
                .getOrElseThrow(ex -> {
                    logger.atError().log("communication error with financial api: ", ex);
                    throw new RuntimeException(ex); });
    }

    private NewDataset parsePostDatasetRequest(String body) throws ValidationException {
        try {
            var newDatasetBody = new Gson().fromJson(body, NewDataset.class);
            if (newDatasetBody == null) {
                throw new ValidationException("Body of request is missing. To create a new dataset, I need a list of symbols or the special value `:all` in the `symbols` field.");
            }
            if (newDatasetBody.getSymbols().isEmpty()) {
                throw new ValidationException("`symbols` is empty");
            }
            return newDatasetBody;
        } catch (JsonSyntaxException e) {
            throw new ValidationException("not valid", e);
        }
    }

    public Optional<Dataset> getDataset(int id) {
        return databaseClient.getDataset(id);
    }

    public List<Integer> getDatasetIds() {
        return databaseClient.getDatasetIds();
    }

    public Optional<List<Company>> getRecommendation(String id) {
        return parseDatasetId(id)
                .flatMap(databaseClient::getDataset)
                .map(this::top5Companies);
    }

    private List<Company> top5Companies(Dataset dataset) {
        return dataset
                .getCompanies().stream()
                .sorted(comparingDouble(DatasetService::priceToEarnings))
                .limit(5)
                .collect(Collectors.toList());
    }

    private static double priceToEarnings(Company company) {
        return company.getPrice() / company.getEarningsPerShare();
    }

    private Optional<Integer> parseDatasetId(String id) {
        return Try.of(() -> Optional.of(Integer.parseInt(id))).getOrElse(Optional::empty);
    }
}

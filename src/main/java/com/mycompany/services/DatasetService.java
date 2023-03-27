package com.mycompany.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.database.DatabaseClient;
import com.mycompany.database.Dataset;
import com.mycompany.financial_api.Company;
import com.mycompany.financial_api.FinancialAPIClient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetService {
  private final FinancialAPIClient financialAPIClient;
  private final Logger logger = LoggerFactory.getLogger(DatasetService.class);
  private final DatabaseClient databaseClient = new DatabaseClient();
  private static final Gson gson = new Gson();

  public DatasetService() throws Exception {
    financialAPIClient = new FinancialAPIClient();
  }

  public int newDataset(String body) throws ValidationException {
    var newDataset = parsePostDatasetRequest(body);
    List<String> symbols;
    if (newDataset.getSymbols().stream().anyMatch(symbol -> symbol.equals(":all"))) {
      symbols = financialAPIClient.symbols();
    } else {
      symbols = newDataset.getSymbols();
    }
    var companies =
        symbols.stream()
            .map(
                symbol -> {
                  Optional<Company> opt = financialAPIClient.financialData(symbol);
                  if (opt.isEmpty()) {
                    logger.atWarn().log("could not get financial data for {}", symbol);
                  }
                  return opt;
                })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    return databaseClient.saveDataset(companies);
  }

  private NewDataset parsePostDatasetRequest(String body) throws ValidationException {
    try {
      var newDatasetBody = gson.fromJson(body, NewDataset.class);
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

  public static class ValidationException extends Exception {
    public ValidationException(String message, Throwable cause) {
      super(message, cause);
    }

    public ValidationException(String message) {
      super(message);
    }
  }
}

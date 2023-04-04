package com.mycompany.repository;

import com.mycompany.model.Dataset;
import com.mycompany.model.Company;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.Collections.emptyList;


public class DatabaseClient {
    private final Jdbi jdbi;

    public DatabaseClient() {
        this.jdbi = Jdbi.create("jdbc:sqlite:database.db");
        this.jdbi.useTransaction(transaction -> {
            transaction
                    .execute("create table if not exists dataset(timestamp)");
            transaction
                    .execute("create table if not exists company (dataset_id references dataset(ROWID), symbol, companyName, marketCap, sector, industry, beta, price, lastAnnualDividend, volume, exchange, exchangeShortName, country, isEtf, isActivelyTrading, earningsPerShare, bookValuePerShare, salesPerShare)"); });
    }

    public int saveDataset(List<Company> companies) {
        return jdbi.inTransaction(transaction -> {
            var now = Instant.now();
            var id = transaction
                    .createUpdate("insert into dataset values(:timestamp)")
                    .bind("timestamp", Timestamp.from(now))
                    .executeAndReturnGeneratedKeys()
                    .map((rs, ctx) -> new Dataset(rs.getInt("last_insert_rowid()"), now, emptyList())).one().getId();
            var batch = transaction
                    .prepareBatch("insert into company values (:dataset_id, :symbol, :companyName, :marketCap, :sector, :industry, :beta, :price, :lastAnnualDividend, :volume, :exchange, :exchangeShortName, :country, :isEtf, :isActivelyTrading, :earningsPerShare, :bookValuePerShare, :salesPerShare)");
            companies.forEach(company -> batch
                    .bind("dataset_id", id)
                    .bindBean(company)
                    .add());
//            int[] counts = batch.execute();
            return id; });
    }

    public Optional<Dataset> getDataset(int id) {
        return jdbi.withHandle(handle -> handle
                .createQuery("select * from dataset join company on company.dataset_id = dataset.rowid where dataset.rowid = :id")
                .bind("id", id)
                .registerRowMapper(BeanMapper.factory(Dataset.class))
                .registerRowMapper(BeanMapper.factory(Company.class))
                .reduceRows(Optional.empty(), (optionalDataset, rowView) -> Optional.of(new Dataset(
                        id,
                        rowView
                                .getColumn("timestamp", Timestamp.class)
                                .toInstant(),
                        Stream
                                .concat(optionalDataset
                                        .map(Dataset::getCompanies)
                                        .orElse(emptyList()).stream(), Stream.of(rowView.getRow(Company.class)))
                                .collect(Collectors.toList())))));
    }

    public List<Integer> getDatasetIds() {
        return jdbi.withHandle(handle -> handle
                .createQuery("select rowid from dataset")
                .mapTo(Integer.class)
                .list());
    }
}

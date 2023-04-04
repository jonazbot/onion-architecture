package com.mycompany.model;

import lombok.Value;

@Value
public class Company {
    String symbol;
    String companyName;
    long marketCap;
    String sector;
    String industry;
    double beta;
    double price;
    double lastAnnualDividend;
    long volume;
    String exchange;
    String exchangeShortName;
    String country;
    boolean isEtf;
    boolean isActivelyTrading;
    double earningsPerShare;
    double bookValuePerShare;
    double salesPerShare;

    @Override
    public String toString() {
        return "Company{"
                + "symbol='" + symbol + '\''
                + ", companyName='" + companyName + '\''
                + ", marketCap=" + marketCap
                + ", sector='" + sector + '\''
                + ", industry='" + industry + '\''
                + ", beta=" + beta
                + ", price=" + price
                + ", lastAnnualDividend=" + lastAnnualDividend
                + ", volume=" + volume
                + ", exchange='" + exchange + '\''
                + ", exchangeShortName='" + exchangeShortName + '\''
                + ", country='" + country + '\''
                + ", isEtf=" + isEtf
                + ", isActivelyTrading=" + isActivelyTrading
                + ", earningsPerShare=" + earningsPerShare
                + ", bookValuePerShare=" + bookValuePerShare
                + ", salesPerShare=" + salesPerShare
                + '}';
    }
}

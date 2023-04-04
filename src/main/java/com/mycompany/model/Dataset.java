package com.mycompany.model;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class Dataset {
    int id;
    Instant timestamp;
    List<Company> companies;
}

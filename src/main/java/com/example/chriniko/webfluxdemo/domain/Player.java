package com.example.chriniko.webfluxdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor

@Table(value = "players")
public class Player {

    @PrimaryKey
    private UUID id;

    private String firstname;
    private String initials;
    private String surname;

    private Instant creationTime;
}

package com.example.chriniko.webfluxdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor

@Table(value = "lotto_tickets")
public class LottoTicket {

    @PrimaryKey
    private UUID ticketId;

    private List<Board> boards;
    private Instant creationTime;
}

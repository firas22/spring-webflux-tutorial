package com.example.chriniko.webfluxdemo.repository;

import com.example.chriniko.webfluxdemo.domain.LottoTicket;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import java.util.UUID;

public interface LottoTicketRepository extends ReactiveCassandraRepository<LottoTicket, UUID> {
}

package com.example.chriniko.webfluxdemo.repository;

import com.example.chriniko.webfluxdemo.domain.Player;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import java.util.UUID;

public interface PlayerRepository extends ReactiveCassandraRepository<Player, UUID> {
}

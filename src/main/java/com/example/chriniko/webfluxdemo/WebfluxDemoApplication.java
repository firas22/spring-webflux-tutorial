package com.example.chriniko.webfluxdemo;

import com.example.chriniko.webfluxdemo.domain.Board;
import com.example.chriniko.webfluxdemo.domain.LottoTicket;
import com.example.chriniko.webfluxdemo.domain.Player;
import com.example.chriniko.webfluxdemo.repository.LottoTicketRepository;
import com.example.chriniko.webfluxdemo.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.CassandraTemplate;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
@EnableAutoConfiguration
public class WebfluxDemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxDemoApplication.class, args);
    }

    @Autowired
    private LottoTicketRepository lottoTicketRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Override
    public void run(String... args) {
        initializeTickets();

        // populate db
        List<Player> players = IntStream.rangeClosed(1, 10)
                .boxed()
                .map(idx ->
                        Player.builder()
                                .id(UUID.randomUUID())
                                .firstname("firstname" + idx)
                                .initials("initials" + idx)
                                .surname("surname" + idx)
                                .creationTime(Instant.now())
                                .build()
                )
                .collect(Collectors.toList());

        playerRepository.saveAll(players).subscribe();


        // print db data
        Flux
                .create(fluxSink -> {
                    try {

                        List<Player> result = cassandraTemplate.select("select * from dbg_keyspace.players", Player.class);
                        result.forEach(fluxSink::next);

                        fluxSink.complete();

                    } catch (Exception error) {
                        fluxSink.error(error);
                    }
                })
                .subscribe(
                        result -> System.out.println("WebfluxDemoApplication#run ---> onNext: " + result),
                        error -> System.err.println("WebfluxDemoApplication#run ---> onError: " + error),
                        () -> System.out.println("WebfluxDemoApplication#run ---> onCompleted")
                );

    }

    private void initializeTickets() {
        // populate db
        List<LottoTicket> lottoTickets = IntStream.range(0, 9)
                .boxed()
                .map(idx -> LottoTicket
                        .builder()
                        .ticketId(UUID.randomUUID())
                        .boards(Arrays.asList(
                                Board.builder().numbers(Arrays.asList(
                                        generateNumber(50),
                                        generateNumber(50),
                                        generateNumber(50),
                                        generateNumber(50),
                                        generateNumber(50))).build(),
                                Board.builder().numbers(Arrays.asList(
                                        generateNumber(20),
                                        generateNumber(20))).build()

                        ))
                        .creationTime(Instant.now())
                        .build())
                .collect(Collectors.toList());

        lottoTicketRepository.saveAll(lottoTickets)
                .subscribe(
                        result -> System.out.println("WebfluxDemoApplication#run ---> onNext: " + result),
                        error -> System.err.println("WebfluxDemoApplication#run ---> onError: " + error),
                        () -> System.out.println("WebfluxDemoApplication#run ---> onCompleted")
                );


        // print db data
        List<LottoTicket> result = cassandraTemplate.select("select * from dbg_keyspace.lotto_tickets", LottoTicket.class);
        result.forEach(System.out::println);
    }

    private int generateNumber(int bound) {
        return ThreadLocalRandom.current().nextInt(bound) + 1;
    }

}

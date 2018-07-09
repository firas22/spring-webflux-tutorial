package com.example.chriniko.webfluxdemo.resource;

import com.example.chriniko.webfluxdemo.domain.Board;
import com.example.chriniko.webfluxdemo.domain.LottoTicket;
import com.example.chriniko.webfluxdemo.dto.CreateLottoTicket;
import com.example.chriniko.webfluxdemo.dto.UpdateLottoTicket;
import com.example.chriniko.webfluxdemo.repository.LottoTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

// IMPORTANT NOTE: this resource does not apply business rules - validation to the incoming request.

@RestController
@RequestMapping("/tickets")
public class LottoResource {

    private final LottoTicketRepository lottoTicketRepository;
    private final CassandraTemplate cassandraTemplate;

    @Autowired
    public LottoResource(LottoTicketRepository lottoTicketRepository, CassandraTemplate cassandraTemplate) {
        this.lottoTicketRepository = lottoTicketRepository;
        this.cassandraTemplate = cassandraTemplate;
    }

    // getAll
    // Note: 1st way
    @RequestMapping(method = RequestMethod.GET, value = "getall", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<LottoTicket> getAll() {
        return lottoTicketRepository
                .findAll()
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }

    // getAll
    // Note: 2nd way
    @RequestMapping(method = RequestMethod.GET, value = "getall2", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<LottoTicket> getAll2() {

        return Flux
                .<LottoTicket>create(fluxSink -> {
                    try {
                        System.out.println("LottoTicketRepository#getAll2 ---> ThreadName: " + Thread.currentThread().getName());
                        final List<LottoTicket> tickets = cassandraTemplate.select("select * from lotto_tickets", LottoTicket.class);
                        tickets.forEach(fluxSink::next);

                        fluxSink.complete();
                    } catch (Exception error) {
                        fluxSink.error(error);
                    }
                })
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }

    //get by id
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Mono<LottoTicket> get(@PathVariable(value = "id") String ticketId) {
        return Mono
                .just(ticketId)
                .map(UUID::fromString)
                .flatMap(lottoTicketRepository::findById)
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }

    //delete by id
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Mono<LottoTicket> delete(@PathVariable("id") String ticketId) {
        return Mono
                .just(ticketId)
                .map(UUID::fromString)
                .flatMap((Function<UUID, Mono<LottoTicket>>) lottoTicketRepository::findById)
                .flatMap(publisher -> {
                    lottoTicketRepository.deleteById(publisher.getTicketId()).subscribe();
                    return Mono.just(publisher);
                })
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }

    //update by id
    @RequestMapping(
            method = RequestMethod.PATCH,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public @ResponseBody
    Mono<LottoTicket> update(@RequestBody UpdateLottoTicket updateLottoTicket) {
        return Mono
                .just(updateLottoTicket)
                .zipWith(lottoTicketRepository.findById(updateLottoTicket.getTicketId()))
                .map(tupple2 -> {

                    final UpdateLottoTicket ltu = tupple2.getT1();
                    final LottoTicket lottoTicket = tupple2.getT2();

                    lottoTicket.setBoards(
                            Arrays.asList(
                                    Board.builder().numbers(ltu.getBoards().get(0).getNumbers()).build(),
                                    Board.builder().numbers(ltu.getBoards().get(0).getNumbers()).build()
                            )
                    );

                    return lottoTicket;
                })
                .flatMap(lottoTicketRepository::save)
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }

    //create
    /*
        Note:

        {
          "boards": [
            {
              "numbers": [
                1,
                1,
                1,
                1,
                1
              ]
            },
            {
              "numbers": [
                1,
                1
              ]
            }
          ]
        }
     */
    @RequestMapping(
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public @ResponseBody
    Mono<LottoTicket> create(@RequestBody CreateLottoTicket createLottoTicket) {

        return Mono
                .just(createLottoTicket)
                .map(_Create_lottoTicket -> {
                    final List<CreateLottoTicket.LottoTicketBoard> boards = _Create_lottoTicket.getBoards();

                    return LottoTicket
                            .builder()
                            .ticketId(UUID.randomUUID())
                            .creationTime(Instant.now())
                            .boards(Arrays.asList(
                                    Board.builder().numbers(boards.get(0).getNumbers()).build(),
                                    Board.builder().numbers(boards.get(1).getNumbers()).build()
                            ))
                            .build();
                })
                .flatMap(lottoTicketRepository::save)
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }

}

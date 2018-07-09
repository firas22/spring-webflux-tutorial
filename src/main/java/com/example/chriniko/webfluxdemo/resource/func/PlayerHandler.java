package com.example.chriniko.webfluxdemo.resource.func;

import com.example.chriniko.webfluxdemo.domain.Player;
import com.example.chriniko.webfluxdemo.dto.CreatePlayer;
import com.example.chriniko.webfluxdemo.dto.UpdatePlayer;
import com.example.chriniko.webfluxdemo.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

@Component
public class PlayerHandler {

    @Autowired
    private PlayerRepository playerRepository;

    Mono<ServerResponse> getPlayers() {

        return ServerResponse
                .ok()
                .body(fromPublisher(playerRepository.findAll(), Player.class))
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);

    }

    Mono<ServerResponse> getPlayer(ServerRequest serverRequest) {

        return Mono.just(serverRequest.pathVariable("playerId"))
                .map(UUID::fromString)
                .flatMap(playerId -> playerRepository.findById(playerId))
                .flatMap(player -> ServerResponse.ok().body(fromPublisher(Mono.just(player), Player.class)))
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);

    }

    Mono<ServerResponse> deletePlayer(ServerRequest serverRequest) {

        return Mono.just(serverRequest.pathVariable("playerId"))
                .map(UUID::fromString)
                .flatMap(playerId -> {
                    Mono<Player> playerPublisher = playerRepository.findById(playerId);
                    playerRepository.deleteById(playerId).subscribe();
                    return playerPublisher;
                })
                .flatMap(player -> ServerResponse.ok().body(fromPublisher(Mono.just(player), Player.class)))
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }

    Mono<ServerResponse> createPlayer(ServerRequest serverRequest) {

        return serverRequest
                .bodyToMono(CreatePlayer.class)
                .map(createPlayer -> Player.builder()
                        .id(UUID.randomUUID())
                        .firstname(createPlayer.getFirstname())
                        .initials(createPlayer.getInitials())
                        .surname(createPlayer.getSurname())
                        .creationTime(Instant.now())
                        .build()
                )
                .flatMap(player -> playerRepository.insert(player))
                .map(player -> Tuples.of(player, URI.create("http://localhost:8080/players/" + player.getId()), Player.class))
                .flatMap(tuple2 -> ServerResponse
                        .created(tuple2.getT2())
                        .body(fromPublisher(Mono.just(tuple2.getT1()), tuple2.getT3()))
                )
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);

    }

    Mono<ServerResponse> updatePlayer(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(UpdatePlayer.class)
                .zipWith(Mono.just(UUID.fromString(serverRequest.pathVariable("playerId"))))
                .flatMap(t2 -> {
                    return playerRepository.findById(t2.getT2())
                            .flatMap(retrievedRecord -> {

                                final UpdatePlayer updatePlayer = t2.getT1();

                                Optional.ofNullable(updatePlayer.getFirstname())
                                        .ifPresent(retrievedRecord::setFirstname);

                                Optional.ofNullable(updatePlayer.getInitials())
                                        .ifPresent(retrievedRecord::setInitials);

                                Optional.ofNullable(updatePlayer.getSurname())
                                        .ifPresent(retrievedRecord::setSurname);

                                return playerRepository.save(retrievedRecord);
                            });
                })
                .flatMap(updatedRecord -> ServerResponse
                        .ok()
                        .body(fromPublisher(Mono.just(updatedRecord), Player.class))
                )
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .retry(3);
    }
}

package com.example.chriniko.webfluxdemo.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@RestController
@RequestMapping("/events")
public class EventResource {

    /*
        Note: curl http://localhost:8080/events
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> getEvents() {

        final Flux<Long> intervalPublisher = Flux.interval(
                Duration.ofSeconds(1),
                Schedulers.fromExecutor(Executors.newSingleThreadScheduledExecutor())
        );

        final Flux<Event> eventsPublisher = Flux.fromStream(Stream.generate(() -> new Event(
                "name-" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE),
                Instant.now()))
        );

        return Flux.zip(intervalPublisher, eventsPublisher).map(Tuple2::getT2);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public class Event {

        private String name;
        private Instant creationTime;
    }

}

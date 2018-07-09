package com.example.chriniko.webfluxdemo.resource.func;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PlayerRoute {

    @Bean
    public RouterFunction<ServerResponse> playerRouter(final PlayerHandler playerHandler) {

        return
                route(
                        GET("/players"), serverRequest -> playerHandler.getPlayers()
                ).andRoute(
                        POST("/players"), playerHandler::createPlayer
                ).andRoute(
                        GET("/players/{playerId}"), playerHandler::getPlayer
                ).andRoute(
                        DELETE("/players/{playerId}"), playerHandler::deletePlayer
                ).andRoute(
                        PATCH("/players/{playerId}"), playerHandler::updatePlayer
                );

    }

}

package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Copyright eConsult Health Ltd 2022
 * Author : Jim Renwick
 * Date: 01/02/2022
 */
@Singleton
class RedisService {

    @Inject
    ObjectMapper objectMapper

    @Inject
    StatefulRedisConnection<String, String> connection

    /**
     * publishes the event to the redis Pub/Sub channel.
     * @param event
     */
    void publish(Event event) {
        RedisCommands<String, String> commands = connection.sync()
        String json = objectMapper.writeValueAsString(event)
        commands.publish(RedisListener.REDIS_CHANNEL, json)
    }
}


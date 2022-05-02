package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import jakarta.inject.Inject
import jakarta.inject.Singleton

import javax.annotation.PostConstruct

/**
 * Copyright eConsult Health Ltd 2022
 * Author : Jim Renwick
 * Date: 01/02/2022
 */
@Singleton
class RedisService {

    final static String REDIS_CHANNEL = "my-redis-channel"
    final static String REDIS_USERS_KEY = "users"

    @Inject
    ObjectMapper objectMapper

    @Inject
    StatefulRedisConnection<String, String> connection

    RedisCommands<String, String> commands

    @PostConstruct
    def init() {
        commands = connection.sync()
    }

    /**
     * publishes the event to the redis Pub/Sub channel.
     * @param event
     */
    void publish(Event event) {
        String json = objectMapper.writeValueAsString(event)
        commands.publish(REDIS_CHANNEL, json)
        // user session events will need to be replayed to a new user when they log in - therefore
        // we save the data to redis so it be sent over in the websocket's @onOpen method.
        if (event.type == EventType.USER_SESSION_EVENT) {
            if (event.eventAction == EventAction.VIEWING_CARD) {
                commands.set(assembleKey(event.sourceId, EventAction.VIEWING_CARD), event.targetId)
            }
        }
    }

    void addUser(String userId) {
        commands.sadd(REDIS_USERS_KEY, userId)
    }

    boolean isActiveUser(String userId) {
        commands.sismember(REDIS_USERS_KEY, userId)
    }

    void removeUser(String userId) {
        commands.srem(REDIS_USERS_KEY, userId)
    }

    Map<String, String> getAssignedCards() {
        Set<String> users = commands.smembers(REDIS_USERS_KEY)
        // TODO this fetch isn't efficient because of the redis structure
        Map<String, String> assignedCards = [:]
        users.each{
            String cardId = commands.get(assembleKey(it, EventAction.VIEWING_CARD))
            if (cardId) {
                assignedCards.put(it, cardId)
            }
        }
        return assignedCards
    }

    void clearUserData(String userId) {
        Set<String> keysToRemove = []
        EventAction.enumConstants.each{keysToRemove.add(assembleKey(userId, it))}
        commands.del(convertToVarArgs(keysToRemove))
    }

    static String assembleKey(String inputOne, Enum<?> inputTwo) {
        String.join(":", inputOne, inputTwo.name())
    }

    static String[] convertToVarArgs(Collection<String> input) {
        return input.toArray(new String[] { })
    }
}


package com.example

import groovy.util.logging.Slf4j
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.reactivestreams.Publisher

/**
 * Copyright eConsult Health Ltd 2022
 * Author : Jim Renwick
 * Date: 01/02/2022
 */

@ServerWebSocket("/ws/{practiceId}/{username}")
@Singleton
@Slf4j
class Websocket {

    @Inject
    RedisService redisService

    @OnOpen
    void onOpen(String practiceId, String username, WebSocketSession session) {
        log.info("onOpen")
        redisService.addUser(username)
        /*
            An event could be created here to signify the user is logged in e.g.

            session.send(new Event(eventId: "12345", eventName: "initialExampleEvent"))

            and any event data relevant to the user could be pulled from the redis cache and similarly sent
            to the user's session.
         */
    }

    @OnMessage
    void onMessage(String practiceId, String username, String message, WebSocketSession session) {
        log.info("onMessage")
        log.info(message)
    }

    /**
     * Note that this gets called automatically on the browser tab being closed
     * - here the redis data for the user closing the connection is deleted so it will not be replayed
     * to any new users starting sessions
     * @param session
     */
    @OnClose
    void onClose(String username, WebSocketSession session) {
        log.info("onClose")
        redisService.clearUserData(username)
        redisService.removeUser(username)
        // publishing an onClose event for the user so that other frontend sessions can update appropriately
        redisService.publish(new Event(eventId: UUID.randomUUID().toString(), sourceId: username,
                type: EventType.USER_SESSION_EVENT,  eventAction: EventAction.USER_LOGOUT))
    }
}

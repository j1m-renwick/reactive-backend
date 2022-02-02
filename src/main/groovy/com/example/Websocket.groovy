package com.example

import groovy.util.logging.Slf4j
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import io.reactivex.rxjava3.core.Flowable
import jakarta.inject.Singleton
import org.reactivestreams.Publisher

/**
 * Copyright eConsult Health Ltd 2022
 * Author : Jim Renwick
 * Date: 01/02/2022
 */

@ServerWebSocket("/ws/{practiceId}")
@Singleton
@Slf4j
class Websocket {

    @OnOpen
    Publisher<Event> onOpen(String practiceId, WebSocketSession session) {
        log.info("onOpen")
        return session.send(new Event(id: "12345", eventName: "initialExampleEvent"))
    }

    @OnMessage
    void onMessage(String practiceId, String message, WebSocketSession session) {
        log.info("onMessage")
        log.info(message)
    }

    /**
     * Note that this gets called automatically on the browser tab being closed - this could
     * be useful in sending events to let other users know to clear this user's data
     * @param session
     */
    @OnClose
    void onClose(WebSocketSession session) {
        log.info("onClose")
    }
}
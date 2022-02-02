package com.example

import groovy.util.logging.Slf4j
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Inject
import jakarta.inject.Singleton

import javax.annotation.PostConstruct
import java.util.function.Predicate

/**
 * Copyright eConsult Health Ltd 2022
 * Author : Jim Renwick
 * Date: 02/02/2022
 *
 * Listens to a redis Pub/Sub channel and broadcasts the event to all relevant websockets.
 */
@Slf4j
@Singleton
class RedisListener extends RedisPubSubAdapter<String, String> {

    final static String REDIS_CHANNEL = "my-redis-channel"

    @Inject
    StatefulRedisPubSubConnection<String, String> connection

    @Inject
    WebSocketBroadcaster broadcaster

    @PostConstruct
    def init() {
        connection.addListener(this)
        connection.sync().subscribe(REDIS_CHANNEL)
    }

    private Predicate<WebSocketSession> practiceIdMatches(String topic) {
        return { topic.equalsIgnoreCase(it.getUriVariables().get("practiceId", String.class, null))}
    }

    @Override
    void message(String channel, String message) {
        log.info("Got {} on channel {}",  message, channel)
        /**
         *
         * Broadcast the received redis event to the interested websockets - the broadcast() method accepts a predicate
         * so events can be braodcast on a per-user/ per-practice permissions basis, as long as the parameter
         * is supplied in the webflow path.
         *
         * e.g. so to satisfy user assignment we _could_ do something like:
         * - broadcast to everyone in user X's practice (except for user X) that User X is viewing card with id 123.
         * - braodcast to everyone in user X's practice that the unread card queue count needs to be fetched again.
         *
         * this example only sends the event message out to websockets that have the required practice id.
         */
        broadcaster.broadcast(message, practiceIdMatches("myPracticeId")).subscribe()
        /**
         * If we wanted to send a stream of broadcasts out for whatever reason, here's how we could do it
         * (example stream of 1-5 broadcasted at 1 second intervals)
         */
//        Flowable.just(1,2,3,4,5)
//                .map(i -> new Item(i))
//                .zipWith(Flowable.interval(1000, TimeUnit.MILLISECONDS), (item, interval) -> item)
//                .map{
//                    broadcaster.broadcast(it, practiceIdMatches("myPracticeId")).subscribe()
//                }.subscribe()
    }
}

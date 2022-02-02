package com.example

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.websocket.WebSocketBroadcaster
import jakarta.inject.Inject

import static io.micronaut.http.HttpResponse.ok

@Controller('/example')
@Slf4j
class ExampleController {

    @Inject
    WebSocketBroadcaster broadcaster

    @Inject
    RedisService redisService

    /**
     * An example endpoint that is called by the frontend - e.g. retrieving a consult, or
     * whatever endpoint we want to generate a realtime event from,
     * like a user looking at a specific consult.
     *
     * - 
     *
     * @return
     */
    @Get(uri = "/some_endpoint")
    HttpResponse someEndpoint() {
        /**
         * ...some logic here - e.g. updating the DB, pulling data, whatever
         */
        /**
         *
         * use redis to share the event between all workflow instances via a pub/sub channel - this is then
         * picked up in the RedisListener class of each instance and broadcast to
         * that instance's websocket connections.
         */
        redisService.publish(new Event(id: UUID.randomUUID().toString(), eventName: "some-event"))
        return ok()
    }

}

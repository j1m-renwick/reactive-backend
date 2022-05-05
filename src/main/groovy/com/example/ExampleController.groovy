package com.example

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.websocket.WebSocketBroadcaster
import jakarta.inject.Inject

import static io.micronaut.http.HttpResponse.ok

@Controller('/poc')
@Slf4j
class ExampleController {

    @Inject
    WebSocketBroadcaster broadcaster

    @Inject
    RedisService redisService

    /**
     * Although this is an dedicated endpoint to submit events, in reality the publish invocation would
     * likely happen as a result of a standard endpoint being invoked - e.g. saving a consult, retrieving a consult, or
     * whatever endpoint we want to generate a realtime event from.
     *
     * @return
     */
    @Post(uri = "/submit_update_event")
    HttpResponse submitEvent(EndpointRequest request) {
        /*
         *
         * use redis to share the event between all workflow instances via a pub/sub channel - this is then
         * picked up in the RedisListener class of each instance and broadcast to
         * that instance's websocket connections.
         */
        // (NB in reality the user would be set based on the authentication information passed in the request cookie)
        redisService.publish(new Event(eventId: UUID.randomUUID().toString(), sourceId: request.username, data: request.data,
                type: EventType.USER_SESSION_EVENT,  targetId: request.cardId, eventAction: EventAction.UPDATING_CARD))
        return ok()
    }

    /**
     * If some of the data represented by an event are ephemeral (e.g. what users are looking at a consult),
     * we will probably need to be able to replay them to a user who is logging in since those events took place.
     *
     * This API mirrors that functionality only in concept - the implementation here isn't good.
     *
     * @return
     */
    @Get(uri = "/replay_event_data/{userId}")
    HttpResponse replayEventData(String userId) {
        return ok(redisService.getLastEventsForUser(userId))
    }

}

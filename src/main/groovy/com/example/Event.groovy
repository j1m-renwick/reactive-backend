package com.example

/**
 * Copyright eConsult Health Ltd 2022
 * Author : Jim Renwick
 * Date: 01/02/2022
 *
 * The Event that is sent through the Redis Pub/Sub channel.
 */
class Event {

    String eventId
    // e.g. the user that caused the event
    String sourceId
    // e.g. the card that the user clicked
    String targetId
    EventAction eventAction
    EventType type

}

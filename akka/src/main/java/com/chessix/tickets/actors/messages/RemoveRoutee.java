package com.chessix.tickets.actors.messages;

import org.apache.commons.lang3.builder.ToStringBuilder;

import akka.actor.ActorRef;

/**
 * Remove given routee
 * 
 * @author Mark Wigmans
 * 
 */
public class RemoveRoutee {

    private final ActorRef routee;

    public RemoveRoutee(final ActorRef routee) {
        super();
        this.routee = routee;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public ActorRef getRoutee() {
        return routee;
    }
}

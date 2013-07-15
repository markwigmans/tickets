package com.chessix.tickets.actors.messages;

/**
 * No tickets left
 * 
 * @author Mark Wigmans
 * 
 */
public final class SoldOut extends AbstractTicketMessage {

    public SoldOut(final String event) {
        super(event, 0);
    }

}

package com.chessix.tickets.actors.messages;

/**
 * Request the number of tickets from the office.
 * 
 * @author Mark Wigmans
 * 
 */
public final class TicketsRequest extends AbstractTicketMessage {

    public TicketsRequest(final String event, final int tickets) {
        super(event, tickets);
    }
}

package com.chessix.tickets.actors.messages;

/**
 * Return the number of tickets to the office.
 * 
 * @author Mark Wigmans
 * 
 */
public final class ReturnTickets extends AbstractTicketMessage {

    public ReturnTickets(final String event, final int tickets) {
        super(event, tickets);
    }
}

package com.chessix.tickets.actors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

import com.chessix.tickets.actors.messages.PrintTickets;
import com.chessix.tickets.model.Ticket;

/**
 * Printer which print tickets
 * 
 * @author Mark Wigmans
 * 
 */
public class Printer extends UntypedActor {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object msg) throws Exception {
        if (msg instanceof PrintTickets.Request) {
            final PrintTickets.Request message = (PrintTickets.Request) msg;
            final List<Ticket> tickets = new ArrayList<>(message.getTickets());
            for (int i = 0; i < message.getTickets(); i++) {
                tickets.add(new Ticket(message.getEvent(), message.getOffset() + i));
            }
            getSender().tell(new PrintTickets.Response(message.getEvent(), tickets), getSelf());
        } else {
            unhandled(msg);
        }
    }
}

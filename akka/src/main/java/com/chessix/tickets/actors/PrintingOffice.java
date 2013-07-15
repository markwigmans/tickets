package com.chessix.tickets.actors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.Props;
import akka.actor.UntypedActor;

import com.chessix.tickets.actors.messages.ReturnTickets;
import com.chessix.tickets.actors.messages.SoldOut;
import com.chessix.tickets.actors.messages.TicketsRequest;

/**
 * Office responsible for providing the tickets to Ticket agents.
 * 
 * @author Mark Wigmans
 * 
 */
public class PrintingOffice extends UntypedActor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Which event this print office prints tickets for
     */
    private final String event;
    /**
     * Current number of available tickets.
     */
    private int availableTickets;

    /**
     * Create Props for an actor of this type.
     */
    public static Props mkProps(final String event, final int availableTickets) {
        return Props.create(PrintingOffice.class, event, availableTickets);
    }

    /**
     * 
     * @param event
     * @param availableTickets
     */
    public PrintingOffice(final String event, final int availableTickets) {
        super();
        this.event = event;
        this.availableTickets = availableTickets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object msg) throws Exception {
        logger.debug("onReceive({})", msg);
        try {
            if (msg instanceof TicketsRequest) {
                final TicketsRequest message = (TicketsRequest) msg;
                if (availableTickets == 0) {
                    logger.debug("sold out");
                    getSender().tell(new SoldOut(event), getSelf());
                } else if (message.getTickets() <= availableTickets) {
                    logger.debug("return tickets: {}", message.getTickets());
                    getSender().tell(new ReturnTickets(event, message.getTickets()), getSelf());
                    availableTickets -= message.getTickets();
                } else {
                    // return all available tickets because we have less than
                    // the number of requested tickets.
                    logger.debug("return all available tickets: {}", availableTickets);
                    getSender().tell(new ReturnTickets(event, availableTickets), getSelf());
                    availableTickets = 0;
                }
            } else if (msg instanceof ReturnTickets) {
                final ReturnTickets message = (ReturnTickets) msg;
                availableTickets += message.getTickets();
            } else {
                unhandled(msg);
            }
        } catch (Exception e) {
            getSender().tell(new akka.actor.Status.Failure(e), getSelf());
            throw e;
        }
    }

    /**
     * Return number of available tickets for testing purposes.
     */
    int getAvailableTickets() {
        return availableTickets;
    }
}

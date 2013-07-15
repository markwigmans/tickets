package com.chessix.tickets.actors;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.chessix.tickets.actors.messages.AbstractTicketMessage;
import com.chessix.tickets.actors.messages.RemoveRoutee;
import com.chessix.tickets.actors.messages.ReturnTickets;
import com.chessix.tickets.actors.messages.SoldOut;
import com.chessix.tickets.actors.messages.TicketsRequest;

/**
 * Do the actual selling of a ticket
 * 
 * @author Mark Wigmans
 * 
 */
public class TicketAgent extends UntypedActor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Timeout timeout = new Timeout(Duration.create(500, TimeUnit.MILLISECONDS));
    private final ActorRef printingOffice;
    private final String event;
    private int ticketsOrderValue;
    private final int ticketOrderTreshhold;

    private int availableTickets = 0;

    /**
     * Create Props for an actor of this type.
     */
    public static Props mkProps(final String event, final ActorRef printingOffice, final int ticketsOrderValue,
            final int ticketOrderTreshhold) {
        return Props.create(TicketAgent.class, event, printingOffice, ticketsOrderValue, ticketOrderTreshhold);
    }

    /**
     * 
     */
    public TicketAgent(final String event, final ActorRef printingOffice, final int ticketsOrderValue,
            final int ticketOrderTreshhold) {
        super();
        this.event = event;
        this.printingOffice = printingOffice;
        this.ticketsOrderValue = ticketsOrderValue;
        this.ticketOrderTreshhold = ticketOrderTreshhold;
    }

    @Override
    public void onReceive(final Object msg) throws Exception {
        logger.debug("this:[{}] onReceive({})", this, msg);
        if (msg instanceof TicketsRequest) {
            final TicketsRequest message = (TicketsRequest) msg;
            if (message.getTickets() <= availableTickets) {
                getSender().tell(new ReturnTickets(event, message.getTickets()), getSelf());
                availableTickets -= message.getTickets();

                // test if we have to request more tickets already
                if (availableTickets <= ticketOrderTreshhold) {
                    printingOffice.tell(new TicketsRequest(event, ticketsOrderValue), getSelf());
                }
            } else {
                // not enough tickets, so request more
                final int nr = availableTickets + ticketsOrderValue >= message.getTickets() ? ticketsOrderValue : message
                        .getTickets();
                logger.debug("request more tickets: {}", nr);
                final Future<Object> future = Patterns.ask(printingOffice, new TicketsRequest(event, nr), timeout);
                final AbstractTicketMessage result = (AbstractTicketMessage) Await.result(future, timeout.duration());
                availableTickets += result.getTickets();
                if (message.getTickets() <= availableTickets) {
                    // request was successful, so return the tickets
                    getSender().tell(new ReturnTickets(event, message.getTickets()), getSelf());
                    availableTickets -= message.getTickets();
                } else {
                    logger.debug("sold out");
                    // ask to remove this routee and let the router handle the
                    // request again
                    getContext().parent().tell(new RemoveRoutee(getSelf()), getSelf());
                    getContext().parent().forward(msg, getContext());
                }
            }
        } else if (msg instanceof ReturnTickets) {
            final ReturnTickets message = (ReturnTickets) msg;
            availableTickets += message.getTickets();
        } else if (msg instanceof SoldOut) {
            logger.debug("this:[{}] SoldOut()", this);
        } else {
            unhandled(msg);
        }
    }

    @Override
    public void preStart() {
        // already order tickets to prevent a user waiting for tickets
        printingOffice.tell(new TicketsRequest(event, ticketsOrderValue), getSelf());
    }

    @Override
    public void postStop() {
        // return the tickets we still have
        if (availableTickets > 0) {
            printingOffice.tell(new ReturnTickets(event, availableTickets), ActorRef.noSender());
        }
    }

    /**
     * Return number of available tickets for testing purposes.
     */
    int getAvailableTickets() {
        return availableTickets;
    }
}

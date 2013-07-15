package com.chessix.tickets.actors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.chessix.tickets.actors.messages.RemoveRoutee;
import com.chessix.tickets.actors.messages.SoldOut;
import com.chessix.tickets.actors.messages.TicketsRequest;

/**
 * endpoint for the RESTful / WS service for all ticket related questions.
 * 
 * @author Mark Wigmans
 * 
 */
public class BoxOffice extends UntypedActor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String event;
    private final int nrOfRoutees;
    private final ActorRef printingOffice;
    private final List<ActorRef> routees = new ArrayList<>();
    private int roundRobinCounter;

    private final int ticketsOrderValue = 50;
    private final int ticketOrderTreshhold = 5;

    /**
     * Create Props for an actor of this type.
     */
    public static Props mkProps(final String event, final ActorRef printingOffice, final int nrOfRoutees) {
        return Props.create(BoxOffice.class, event, printingOffice, nrOfRoutees);
    }

    /**
     * 
     */
    public BoxOffice(final String event, final ActorRef printingOffice, final int nrOfRoutees) {
        super();
        this.event = event;
        this.printingOffice = printingOffice;
        this.nrOfRoutees = nrOfRoutees;
        roundRobinCounter = 0;
    }

    @Override
    public void preStart() {
        for (int i = 1; i <= nrOfRoutees; i++) {
            routees.add(getContext().actorOf(TicketAgent.mkProps(event, printingOffice, ticketsOrderValue, ticketOrderTreshhold),
                    "ticketAgent-" + i));
        }
    }

    @Override
    public void onReceive(final Object msg) throws Exception {
        logger.debug("this:[{}] onReceive({})", this, msg);
        if (msg instanceof TicketsRequest) {
            if (routees.isEmpty()) {
                logger.debug("sold out, tell: {}", getSender());
                getSender().tell(new SoldOut(event), getSelf());
            } else {
                final ActorRef routee = routees.get(roundRobinCounter);
                routee.forward(msg, getContext());
                incRoundRobinCounter();
            }
        } else if (msg instanceof RemoveRoutee) {
            final RemoveRoutee message = (RemoveRoutee) msg;
            final ActorRef routee = message.getRoutee();
            // remove given routee
            routees.remove(routee);
            updateRoundRobinCounter();
            routee.tell(PoisonPill.getInstance(), ActorRef.noSender());
        } else {
            unhandled(msg);
        }
    }

    void incRoundRobinCounter() {
        roundRobinCounter++;
        updateRoundRobinCounter();
    }

    void updateRoundRobinCounter() {
        logger.debug("updateRoundRobinCounter: {}", routees.size());
        if (roundRobinCounter >= routees.size()) {
            roundRobinCounter = 0;
        }
    }
}

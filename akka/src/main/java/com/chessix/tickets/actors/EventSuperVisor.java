package com.chessix.tickets.actors;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

/**
 * 
 * @author Mark Wigmans
 * 
 */
@Component
@Scope("prototype")
public class EventSuperVisor extends UntypedActor {

    static class CreateMessage {
        final String event;
        final int nrOfTickets;
        final int nrOfRoutees;

        public CreateMessage(final String event, final int nrOfTickets, final int nrOfRoutees) {
            super();
            this.event = event;
            this.nrOfTickets = nrOfTickets;
            this.nrOfRoutees = nrOfRoutees;
        }
    }

    @Override
    public void onReceive(final Object msg) throws Exception {
        if (msg instanceof CreateMessage) {
            final CreateMessage message = (CreateMessage) msg;
            getSender().tell(createActors(message.event, message.nrOfTickets, message.nrOfRoutees), getSelf());
        }
    }

    ActorRef createActors(final String event, final int nrOfTickets, final int nrOfRoutees) {
        final ActorRef printingOffice = getContext().actorOf(PrintingOffice.mkProps(event, nrOfTickets), "printingOffice");
        final ActorRef boxOffice = getContext().actorOf(BoxOffice.mkProps(event, printingOffice, nrOfRoutees), "boxOffice");

        return boxOffice;
    }

    public static ActorRef create(final ActorSystem system, final String event, final int nrOfTickets) throws Exception {
        // TODO make br of routees better
        final int nrOfRoutees = 4;
        final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        final ActorRef eventSuperVisor = system.actorOf(Props.create(EventSuperVisor.class), event);
        final Future<Object> future = Patterns.ask(eventSuperVisor, new CreateMessage(event, nrOfTickets, nrOfRoutees), timeout);
        return (ActorRef) Await.result(future, timeout.duration());
    }
}

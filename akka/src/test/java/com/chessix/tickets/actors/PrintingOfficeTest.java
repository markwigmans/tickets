package com.chessix.tickets.actors;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;

import com.chessix.tickets.actors.messages.ReturnTickets;
import com.chessix.tickets.actors.messages.SoldOut;
import com.chessix.tickets.actors.messages.TicketsRequest;

/**
 * 
 * @author Mark Wigmans
 * 
 */
public class PrintingOfficeTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Test the 'Return tickets' message behavior directly
     */
    @Test
    public void testBehaviorReturnTickets() throws Exception {
        final String event = "test-event";

        final TestActorRef<PrintingOffice> ref = TestActorRef.create(system, PrintingOffice.mkProps(event, 0));
        final PrintingOffice actor = ref.underlyingActor();
        final int nrOfTickets = 20;

        ref.receive(new ReturnTickets(event, nrOfTickets));
        assertThat(actor.getAvailableTickets()).isEqualTo(nrOfTickets);

        ref.receive(new ReturnTickets(event, nrOfTickets));
        assertThat(actor.getAvailableTickets()).isEqualTo(nrOfTickets * 2);
    }

    @Test
    public void testBehaviorRequestTickets() throws Exception {
        final String event = "test-event";
        final int nrOfTickets = 20;
        final int nrOfBatches = 5;

        final TestActorRef<PrintingOffice> ref = TestActorRef.create(system,
                PrintingOffice.mkProps(event, nrOfBatches * nrOfTickets));
        final PrintingOffice actor = ref.underlyingActor();

        // start with asking some tickets
        final Future<Object> future1 = akka.pattern.Patterns.ask(ref, new TicketsRequest(event, nrOfTickets), 3000);
        assertThat(future1.isCompleted());
        assertThat(Await.result(future1, Duration.Zero())).isEqualTo(new ReturnTickets(event, nrOfTickets));
        assertThat(actor.getAvailableTickets()).isEqualTo((nrOfBatches - 1) * nrOfTickets);

        // ask all available tickets
        final Future<Object> future2 = akka.pattern.Patterns.ask(ref, new TicketsRequest(event, nrOfBatches * nrOfTickets), 3000);
        assertThat(future2.isCompleted());
        assertThat(Await.result(future2, Duration.Zero())).isEqualTo(new ReturnTickets(event, (nrOfBatches - 1) * nrOfTickets));
        assertThat(actor.getAvailableTickets()).isEqualTo(0);

        // check behavior if no tickets are left
        final Future<Object> future3 = akka.pattern.Patterns.ask(ref, new TicketsRequest(event, 1), 3000);
        assertThat(future3.isCompleted());
        assertThat(Await.result(future3, Duration.Zero())).isEqualTo(new SoldOut(event));
    }
}

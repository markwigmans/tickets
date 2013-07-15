package com.chessix.tickets.actors;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;

import com.chessix.tickets.actors.messages.RemoveRoutee;
import com.chessix.tickets.actors.messages.ReturnTickets;
import com.chessix.tickets.actors.messages.TicketsRequest;

public class TicketAgentTest extends TestKit {

    static ActorSystem _system = ActorSystem.create();

    public TicketAgentTest() {
        super(_system);
    }

    /**
     * Test the PreStart message behavior directly
     */
    @Test
    public void testBehaviorPreStart() throws Exception {
        final String event = "test-event";
        final TestActorRef<PrintingOffice> printingOfficeRef = TestActorRef.create(_system, PrintingOffice.mkProps(event, 10));
        final PrintingOffice printingOfficeActor = printingOfficeRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(10);

        final TestActorRef<TicketAgent> ticketAgentRef = TestActorRef.create(_system,
                TicketAgent.mkProps(event, printingOfficeRef, 4, 1));
        final TicketAgent ticketAgentActor = ticketAgentRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(6);
        assertThat(ticketAgentActor.getAvailableTickets()).isEqualTo(4);
    }

    /**
     * Test the Request tickets behavior directly
     */
    @Test
    public void testBehaviorRequestTickets() throws Exception {
        final String event = "test-event";

        final TestActorRef<PrintingOffice> printingOfficeRef = TestActorRef.create(_system, PrintingOffice.mkProps(event, 10));
        final PrintingOffice printingOfficeActor = printingOfficeRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(10);

        final TestActorRef<TicketAgent> ticketAgentRef = TestActorRef.create(_system,
                TicketAgent.mkProps(event, printingOfficeRef, 4, 1));
        final TicketAgent ticketAgentActor = ticketAgentRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(6);
        assertThat(ticketAgentActor.getAvailableTickets()).isEqualTo(4);

        // request tickets
        final Future<Object> future1 = akka.pattern.Patterns.ask(ticketAgentRef, new TicketsRequest(event, 2), 1000);
        assertThat(future1.isCompleted());
        assertThat(Await.result(future1, Duration.Zero())).isEqualTo(new ReturnTickets(event, 2));
        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(6);
        assertThat(ticketAgentActor.getAvailableTickets()).isEqualTo(2);
    }

    /**
     * Test the Request tickets behavior with 'request more' from PrintingOffice
     * directly
     */
    @Test
    public void testBehaviorRequestTicketsRequestMore() throws Exception {
        final String event = "test-event";

        final TestActorRef<PrintingOffice> printingOfficeRef = TestActorRef.create(_system, PrintingOffice.mkProps(event, 10));
        final PrintingOffice printingOfficeActor = printingOfficeRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(10);

        final TestActorRef<TicketAgent> ticketAgentRef = TestActorRef.create(_system,
                TicketAgent.mkProps(event, printingOfficeRef, 2, 0));
        final TicketAgent ticketAgentActor = ticketAgentRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(8);
        assertThat(ticketAgentActor.getAvailableTickets()).isEqualTo(2);

        // request tickets
        final Future<Object> future1 = akka.pattern.Patterns.ask(ticketAgentRef, new TicketsRequest(event, 3), 1000);
        assertThat(future1.isCompleted());
        assertThat(Await.result(future1, Duration.Zero())).isEqualTo(new ReturnTickets(event, 3));
        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(6);
        assertThat(ticketAgentActor.getAvailableTickets()).isEqualTo(1);
    }

    /**
     * Test the Request tickets behavior with 'request more' from PrintingOffice
     * directly
     */
    @Test
    public void testBehaviorRequestTicketsSoldOut() throws Exception {
        final String event = "test-event";

        final TestActorRef<PrintingOffice> printingOfficeRef = TestActorRef.create(_system, PrintingOffice.mkProps(event, 10));
        final PrintingOffice printingOfficeActor = printingOfficeRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(10);

        final TestProbe probe = new TestProbe(_system);
        final TestActorRef<TicketAgent> ticketAgentRef = new TestActorRef<TicketAgent>(_system, TicketAgent.mkProps(event,
                printingOfficeRef, 2, 0), probe.ref(), "ticketAgent-A");
        final TicketAgent ticketAgentActor = ticketAgentRef.underlyingActor();

        assertThat(printingOfficeActor.getAvailableTickets()).isEqualTo(8);
        assertThat(ticketAgentActor.getAvailableTickets()).isEqualTo(2);

        // request tickets
        final Future<Object> future1 = akka.pattern.Patterns.ask(ticketAgentRef, new TicketsRequest(event, 3), 1000);
        assertThat(future1.isCompleted());
        assertThat(Await.result(future1, Duration.Zero())).isEqualTo(new ReturnTickets(event, 3));

        final Future<Object> future2 = akka.pattern.Patterns.ask(ticketAgentRef, new TicketsRequest(event, 8), 1000);
        assertThat(future2.isCompleted());
        probe.expectMsgClass(RemoveRoutee.class);
    }
}

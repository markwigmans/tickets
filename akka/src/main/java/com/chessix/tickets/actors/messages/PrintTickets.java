package com.chessix.tickets.actors.messages;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.chessix.tickets.model.Ticket;

/**
 * Request the number of tickets from the office.
 * 
 * @author Mark Wigmans
 * 
 */
public final class PrintTickets {

    public static final class Request {
        private final String event;
        private final int tickets;
        private final int offset;

        public Request(String event, int tickets, int offset) {
            super();
            this.event = event;
            this.tickets = tickets;
            this.offset = offset;
        }

        public String getEvent() {
            return event;
        }

        public int getTickets() {
            return tickets;
        }

        public int getOffset() {
            return offset;
        }
    }

    public static class Response {
        private final String event;
        private final List<Ticket> tickets;

        public Response(String event, List<Ticket> tickets) {
            super();
            this.event = event;
            this.tickets = tickets;
        }

        public String getEvent() {
            return event;
        }

        public List<Ticket> getTickets() {
            return tickets;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

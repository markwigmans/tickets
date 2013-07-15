package com.chessix.tickets.actors.messages;

import org.apache.commons.lang3.builder.*;

/**
 * Return the number of tickets to the office.
 * 
 * @author Mark Wigmans
 * 
 */
public abstract class AbstractTicketMessage {

    private final String event;
    private final int tickets;

    public AbstractTicketMessage(final String event, final int tickets) {
        super();
        this.event = event;
        this.tickets = tickets;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AbstractTicketMessage) {
            if (this == obj) {
                return true;
            }
            final AbstractTicketMessage otherObject = (AbstractTicketMessage) obj;

            return new EqualsBuilder().append(this.getClass(), otherObject.getClass()).append(this.event, otherObject.event)
                    .append(this.tickets, otherObject.tickets).isEquals();
        } else {
            return false;
        }
    }

    public String getEvent() {
        return event;
    }

    public int getTickets() {
        return tickets;
    }
}

package com.chessix.tickets.sp.model;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chessix.tickets.messages.AbstractTicketMessage;
import com.chessix.tickets.messages.ReturnTickets;
import com.chessix.tickets.messages.SoldOut;
import com.chessix.tickets.utils.EventUtils;

/**
 * 
 * 
 * @author Mark Wigmans
 * 
 */
public class BoxOffice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String event;
    private final AtomicInteger availableTickets;

    /**
	 * 
	 */
    public BoxOffice(final String event, final int availableTickets) {
        super();
        this.event = event;
        this.availableTickets = new AtomicInteger(availableTickets);
    }

    /**
     * Buy the given number of tickets.
     */
    public AbstractTicketMessage buy(final int tickets) {
        logger.debug("event: '{}' : buy({})", event, tickets);
        EventUtils.busy();
        final int newValue = availableTickets.addAndGet(-tickets);
        if (newValue < 0) {
            logger.debug("event: '{}' : sold out", event);
            availableTickets.addAndGet(tickets);
            return new SoldOut(event);
        } else {
            return new ReturnTickets(event, tickets);
        }
    }

}

package com.chessix.tickets.sp.controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chessix.tickets.messages.AbstractTicketMessage;
import com.chessix.tickets.messages.EventCreated;
import com.chessix.tickets.sp.model.BoxOffice;
import com.chessix.tickets.utils.EventUtils;

/**
 * 
 * @author Mark Wigmans
 * 
 */
@Controller
public class TicketController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Remember which event manager to use for a given event.
	 */
	private final ConcurrentMap<String, BoxOffice> eventManagers = new ConcurrentHashMap<>();

	/**
	 * Create given event
	 * 
	 * @param event
	 *            name of event
	 * @param tickets
	 *            number of available tickets
	 */
	@RequestMapping(value = "/event/{event}/{tickets}", method = RequestMethod.POST)
	public @ResponseBody
	EventCreated createEvent(@PathVariable final String event,
			@PathVariable final int tickets) {
		logger.info("createEvent() : {}", event);
		final String key = EventUtils.getEventName(event);

		final EventCreated result;
		if (!eventManagers.containsKey(key)) {
			final BoxOffice manager = new BoxOffice(event, tickets);
			logger.info("manager created: {}", manager);
			eventManagers.putIfAbsent(key, manager);
			result = new EventCreated(key);
		} else {
			result = new EventCreated(key, false, "Already created");
		}
		return result;
	}

	/**
	 * Request 1 ticket to buy
	 */
	@RequestMapping(value = "/ticket/{event}", method = RequestMethod.POST)
	@ResponseBody
	public AbstractTicketMessage buySingle(@PathVariable final String event) {
		return buyMultiple(event, 1);
	}

	/**
	 * Request {@code tickets} ticket(s) to buy
	 */
	@RequestMapping(value = "/ticket/{event}/{tickets}", method = RequestMethod.POST)
	@ResponseBody
	public AbstractTicketMessage buyMultiple(@PathVariable final String event,
			@PathVariable final int tickets) {
		logger.debug("buy({},{})", event, tickets);
		final BoxOffice manager = eventManagers.get(EventUtils
				.getEventName(event));
		if (manager != null) {
			return manager.buy(tickets);
		}
		throw new IllegalArgumentException(String.format(
				"Unknown event: '%s'", event));
	}

}

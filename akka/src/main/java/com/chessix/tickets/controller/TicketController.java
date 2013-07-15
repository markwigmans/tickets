package com.chessix.tickets.controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.chessix.tickets.actors.EventSuperVisor;
import com.chessix.tickets.actors.messages.EventCreated;
import com.chessix.tickets.actors.messages.TicketsRequest;

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
    private final ConcurrentMap<String, ActorRef> eventManagers = new ConcurrentHashMap<>();
    private final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));

    private final ActorSystem actorSystem;

    @Autowired
    public TicketController(final ActorSystem actorSystem) {
        super();
        this.actorSystem = actorSystem;
    }

    /**
     * Create given event
     * 
     * @param event
     *            name of event
     * @param nr
     *            number of available tickets
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/event/{id}/{nr}", method = RequestMethod.POST)
    public @ResponseBody
    EventCreated createEvent(@PathVariable final String id, @PathVariable final int nr) throws Exception {
        logger.info("createEvent() : {}", id);
        final String key = getEventName(id);

        EventCreated result = new EventCreated(key, false, "Already created");
        if (!eventManagers.containsKey(key)) {
            try {
                final ActorRef manager = EventSuperVisor.create(actorSystem, key, nr);
                logger.info("manager created: " + manager);
                eventManagers.putIfAbsent(key, manager);
                result = new EventCreated(key);
            } catch (akka.actor.InvalidActorNameException e) {
                // it could happen that multiple client want to create an event
                // in parallel.
                logger.info("createEvent() : {}", e.toString());
            }
        }
        return result;
    }

    /**
     * Request 1 ticket to buy
     */
    @RequestMapping(value = "/ticket/{event}", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<Object> buySingle(@PathVariable final String event) {
        return buyMultiple(event, 1);
    }

    /**
     * Request {@code nr} ticket(s) to buy
     */
    @RequestMapping(value = "/ticket/{event}/{nr}", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<Object> buyMultiple(@PathVariable final String event, @PathVariable final int nr) {
        logger.debug("buy({},{})", event, nr);
        final ActorRef manager = eventManagers.get(getEventName(event));
        final DeferredResult<Object> deferredResult = new DeferredResult<Object>(timeout.duration().toMillis());
        if (manager != null) {
            final ExecutionContext ec = actorSystem.dispatcher();
            final Future<Object> future = Patterns.ask(manager, new TicketsRequest(event, nr), timeout);
            future.onSuccess(new OnSuccess<Object>() {
                public void onSuccess(final Object result) {
                    logger.debug("result: {}", result);
                    deferredResult.setResult(result);
                }
            }, ec);
            future.onFailure(new OnFailure() {
                public void onFailure(final Throwable arg) throws Throwable {
                    logger.error("onFailure", arg);
                    deferredResult.setErrorResult(arg);
                }
            }, ec);
        } else {
            // event not found
            deferredResult.setErrorResult(new IllegalArgumentException(String.format("event: '%s' not found", event)));
        }
        return deferredResult;
    }

    String getEventName(final String event) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(event));
    }
}

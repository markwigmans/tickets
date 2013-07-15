package com.chessix.tickets.actors;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.DeadLetter;
import akka.actor.UntypedActor;

public class DeadLetterActor extends UntypedActor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void onReceive(Object message) {
        if (message instanceof DeadLetter) {
            logger.error(ObjectUtils.toString(message));
        }
    }
}

package com.chessix.tickets.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import akka.actor.Actor;
import akka.actor.UntypedActorFactory;

@Component
public class ActorBuilder implements ApplicationContextAware, UntypedActorFactory {

    private static final long serialVersionUID = -9196378493335936906L;
    
    private ApplicationContext applicationContext;
    private String actorBeanId;

    public ActorBuilder() {
        this(null);
    }
    
    public ActorBuilder(final String actorBeanId) {
        super();
        this.actorBeanId = actorBeanId;
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public final Actor create() throws Exception {
        return (Actor) applicationContext.getBean(actorBeanId);
    }
}

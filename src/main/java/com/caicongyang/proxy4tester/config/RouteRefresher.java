package com.caicongyang.proxy4tester.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RouteRefresher implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(RouteRefresher.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    public void refreshRoutes() {
        logger.info("Refreshing routes");
        publisher.publishEvent(new RefreshRoutesEvent(this));
        logger.info("Routes refreshed");
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}

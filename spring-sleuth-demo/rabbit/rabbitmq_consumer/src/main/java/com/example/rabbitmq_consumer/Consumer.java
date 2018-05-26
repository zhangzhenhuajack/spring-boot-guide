package com.example.rabbitmq_consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Consumer {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private RestTemplate restTemplate = new RestTemplate();
    @RabbitListener(queues = "${jsa.rabbitmq.queue}")
    public void recievedMessage(String msg) {
        logger.info("receive amqp message:{}", msg);
        msg = msg + " rest";
        logger.info("sender rest message:{}", msg);
        restTemplate.postForEntity("http://127.0.0.1:8011/mq/receive/world", msg, String.class);
    }
}

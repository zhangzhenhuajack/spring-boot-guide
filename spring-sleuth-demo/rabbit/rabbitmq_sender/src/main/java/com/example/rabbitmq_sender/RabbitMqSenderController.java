package com.example.rabbitmq_sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("mq")
public class RabbitMqSenderController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Producer producer;

    @GetMapping("sender/hello")
    public String sendMessage() {
        String message = "helloworld jsm";
        logger.info("send jms message:{}", message);
        producer.produceMsg(message);
        return "hello";
    }

    @PostMapping("receive/world")
    public String receiveResetMessage(String message) {
        logger.info("receive reset message:{}", message);
        return "world";
    }
}

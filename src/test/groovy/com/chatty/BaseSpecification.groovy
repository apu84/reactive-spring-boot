package com.chatty

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@ContextConfiguration()
@AutoConfigureDataMongo
@ComponentScan(["com.chatty"])
class BaseSpecification extends Specification {
    @Autowired
    WebTestClient webTestClient;
}

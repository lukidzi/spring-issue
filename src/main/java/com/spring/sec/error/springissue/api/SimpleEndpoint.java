package com.spring.sec.error.springissue.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SimpleEndpoint {

    @GetMapping(value = "response", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SimpleResponse> simpleResponse() {
        return Mono.just(new SimpleResponse("Some message"));
    }

}

package com.dineswift.Api_Auth.Service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

        @Bean
        @LoadBalanced
        public WebClient.Builder loadBalancedWebClientBuilder() {
            return WebClient.builder();
        }

        @Bean
        public WebClient webClient(@LoadBalanced WebClient.Builder loadBalancedBuilder) {
            return loadBalancedBuilder
                    .filter(this::logRequest)
                    .filter(this::handleErrors)
                    .build();
        }

        private Mono<ClientResponse> logRequest(ClientRequest request, ExchangeFunction next) {
            log.info("Making request to URL: {}", request.url());
            return next.exchange(request);
        }

        private Mono<ClientResponse> handleErrors(ClientRequest request, ExchangeFunction next) {
            return next.exchange(request)
                    .flatMap(response -> {
                        if (response.statusCode().isError()) {
                            return response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Remote service call failed with status: {} and body: {}",
                                                response.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException(errorBody));
                                    })
                                    .switchIfEmpty(Mono.error(new RuntimeException(
                                            "Service call failed with status: " + response.statusCode()
                                    )))
                                    .then(Mono.error(new RuntimeException(
                                            "Service call failed with status: " + response.statusCode()
                                    )));
                        }
                        return Mono.just(response);
                    });
        }

}


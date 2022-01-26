package com.example.rsocketserver;

import io.rsocket.SocketAcceptor;
import io.rsocket.metadata.WellKnownMimeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.UUID;

public class RSocketClient {
    static final Logger log = LogManager.getLogger(RSocketClient.class);

    private static final String CLIENT = "Client";
    private static final String REQUEST = "Request";
    private static final String FIRE_AND_FORGET = "Fire-And-Forget";
    private static final String STREAM = "Stream";
    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final MimeType SIMPLE_AUTH = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());
    private static Disposable disposable;

    private RSocketRequester rsocketRequester;
    private RSocketRequester.Builder rsocketRequesterBuilder;
    private RSocketStrategies rsocketStrategies;

    //    @Autowired
    public void abc() {
        this.rsocketStrategies = RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2JsonEncoder()))
                .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                .decoders(decoders -> decoders.add(new Jackson2JsonDecoder()))
                .build();

        this.rsocketRequesterBuilder = RSocketRequester.builder()
                .rsocketStrategies(rsocketStrategies);
    }

    public void login(String username, String password) {
        abc();
        log.info("Connecting using client ID: {} and username: {}", CLIENT_ID, username);
        SocketAcceptor responder = RSocketMessageHandler.responder(rsocketStrategies, new ClientHandler());
        this.rsocketRequester = rsocketRequesterBuilder
                .setupRoute("shell-client")
                .setupData(CLIENT_ID)
//                .setupMetadata(user, SIMPLE_AUTH)
                .rsocketStrategies(rsocketStrategies)
                .rsocketConnector(connector -> connector.acceptor(responder))
                .connectTcp("localhost", 9898)
                .block();

        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(error -> log.warn("Connection CLOSED"))
                .doFinally(consumer -> log.info("Client DISCONNECTED"))
                .subscribe();
    }

    @PreDestroy
//    @ShellMethod("Logout and close your connection")
    public void logout() {
        if (userIsLoggedIn()) {
            this.s();
            this.rsocketRequester.rsocket().dispose();
            log.info("Logged out.");
        }
    }

    private boolean userIsLoggedIn() {
        if (null == this.rsocketRequester || this.rsocketRequester.rsocket().isDisposed()) {
            log.info("No connection. Did you login?");
            return false;
        }
        return true;
    }

    //    @ShellMethod("Send one request. One response will be printed.")
    public void requestResponse() throws InterruptedException {
        if (userIsLoggedIn()) {
            log.info("\nSending one request. Waiting for one response...");
            Message message = this.rsocketRequester
                    .route("request-response")
                    .data(new Message(CLIENT, REQUEST))
                    .retrieveMono(Message.class)
                    .block();
            log.info("\nResponse was: {}", message);
        }
    }

    //    @ShellMethod("Send one request. No response will be returned.")
    @Test
    public void fireAndForget() throws InterruptedException {
//        this.login("aa", "bb");
        rsocketRequester = getRSocketRequester();
            log.info("\nFire-And-Forget. Sending one request. Expect no response (check server console log)...");
            rsocketRequester
                    .route("fire-and-forget")
                    .data("aaa")
                    .send()
                    .block();
            Thread.currentThread().join(10*1000);
//        }
    }

    //    @ShellMethod("Send one request. Many responses (stream) will be printed.")
    @Test
    public void stream() throws InterruptedException {
        rsocketRequester = getRSocketRequester();
            log.info("\n\n**** Request-Stream\n**** Send one request.\n**** Log responses.\n**** Type 's' to stop.");
            disposable = rsocketRequester
                    .route("stream")
                    .data("new Message(CLIENT, STREAM)")
                    .retrieveFlux(String.class)
                    .subscribe(message -> log.info("Response: {} \n(Type 's' to stop.)", message));
        Thread.currentThread().join(10*1000);
    }

    //    @ShellMethod("Stream some settings to the server. Stream of responses will be printed.")
    public void channel() {
        if (userIsLoggedIn()) {
            log.info("\n\n***** Channel (bi-directional streams)\n***** Asking for a stream of messages.\n***** Type 's' to stop.\n\n");

            Mono<Duration> setting1 = Mono.just(Duration.ofSeconds(1));
            Mono<Duration> setting2 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(5));
            Mono<Duration> setting3 = Mono.just(Duration.ofSeconds(5)).delayElement(Duration.ofSeconds(15));

            Flux<Duration> settings = Flux.concat(setting1, setting2, setting3)
                    .doOnNext(d -> log.info("\nSending setting for a {}-second interval.\n", d.getSeconds()));

            disposable = this.rsocketRequester
                    .route("channel")
                    .data(settings)
                    .retrieveFlux(Message.class)
                    .subscribe(message -> log.info("Received: {} \n(Type 's' to stop.)", message));
        }
    }

    //    @ShellMethod("Stops Streams or Channels.")
    public void s() {
        if (userIsLoggedIn() && null != disposable) {
            log.info("Stopping the current stream.");
            disposable.dispose();
            log.info("Stream stopped.");
        }
    }

    public RSocketRequester getRSocketRequester() {
        RSocketStrategies strategies = RSocketStrategies.builder()
//                .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
//                .encoders(encoders -> encoders.add(new Jackson2JsonEncoder()))
//                .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
//                .decoders(decoders -> decoders.add(new Jackson2JsonDecoder()))
                .build();
        RSocketRequester.Builder builder = RSocketRequester.builder();

        return builder
                .rsocketConnector(
                        rSocketConnector ->
                                rSocketConnector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2)))
                )
//                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .tcp("localhost", 9898);
    }
}
class ClientHandler {

    @MessageMapping("client-status")
    public Flux<String> statusUpdate(String status) {
        return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }
}
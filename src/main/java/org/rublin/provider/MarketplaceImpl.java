package org.rublin.provider;

import lombok.extern.slf4j.Slf4j;
import org.rublin.dto.OptimalOrderDto;
import org.rublin.dto.PairDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component("marketplace")
public class MarketplaceImpl implements Marketplace {

    private static final long TIMEOUT_SECONDS = 5;
    @Autowired
    @Qualifier("btc")
    private Marketplace btcTradeMarketplace;

    @Autowired
    @Qualifier("ctyptopia")
    private Marketplace cryptopiaMarketplace;

    @Autowired
    @Qualifier("livecoin")
    private Marketplace livecoinMarketplace;

    @Override
    public List<OptimalOrderDto> tradesByPair(PairDto pair) {
        List<Marketplace> marketplaces = Arrays.asList(cryptopiaMarketplace, livecoinMarketplace, btcTradeMarketplace);
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<CompletableFuture<List<OptimalOrderDto>>> futures = marketplaces.stream().map(
                m -> CompletableFuture.supplyAsync(() ->
                m.tradesByPair(pair), executorService)
                .applyToEither(timeoutAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS), Function.identity())
                .exceptionally(error -> {
                    log.warn("Failed getOneSegmentDetails: " + error);
                    return Collections.emptyList();
                }))
                .collect(Collectors.toList());

        List<OptimalOrderDto> collect = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        executorService.shutdown();
        return collect;
    }

    private  <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
        ScheduledThreadPoolExecutor delayer = new ScheduledThreadPoolExecutor(1);

        CompletableFuture<T> result = new CompletableFuture<>();
        delayer.schedule(() -> result.completeExceptionally(new TimeoutException("Timeout after " + timeout)), timeout, unit);
        return result;
    }
}

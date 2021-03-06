package org.rublin.provider.btctrade;

import lombok.extern.slf4j.Slf4j;
import org.rublin.Currency;
import org.rublin.TradePlatform;
import org.rublin.dto.OptimalOrderDto;
import org.rublin.dto.PairDto;
import org.rublin.provider.Marketplace;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component("btc")
public class BtcTradeMarketplace implements Marketplace {

    public static final String BTC_TRADE_URL = "https://btc-trade.com.ua/api/trades/";

    @Override
    public List<OptimalOrderDto> tradesByPair(PairDto pair) {
        List<OptimalOrderDto> result = new ArrayList<>();
        RestTemplate template = new RestTemplate();
        Currency buy = pair.getBuyCurrency();
        Currency sell = pair.getSellCurrency();

        String url = BTC_TRADE_URL;
        if (pair.isBought()) {
            url = url.concat("sell/").concat(buy.name()).concat("_").concat(sell.name());
        } else {
            url = url.concat("buy/").concat(sell.name()).concat("_").concat(buy.name());
        }
        TradesBuyPair btcTradeResult = null;
        int count = 0;
        while (Objects.isNull(btcTradeResult) && count < 3
                ) {
            try {
                log.info("Send {} req", url);
                btcTradeResult = template.getForObject(url, TradesBuyPair.class);
            } catch (Throwable e) {
                log.warn("{} error", e.getMessage());
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        if (Objects.isNull(btcTradeResult)) {
            return result;
        }

        if (btcTradeResult != null) {
            log.info("{} returns {} orders", TradePlatform.BTC_TRADE, btcTradeResult.getTrades().size());
            List<OptimalOrderDto> orders = btcTradeResult.getTrades().stream()
                    .map(trade -> OptimalOrderDto.builder()
                            .marketplace(TradePlatform.BTC_TRADE.name())
                            .amountToSale(pair.isBought() ? trade.getCurrencyBase() : trade.getCurrencyTrade())
                            .amountToBuy(pair.isBought() ? trade.getCurrencyTrade(): trade.getCurrencyBase())
                            .rate(trade.getPrice())
                            .build())
                    .collect(toList());
            result.addAll(orders);
        }
        return result;
    }
}

package org.rublin.controller.v1;

import org.rublin.Currency;
import org.rublin.dto.OptimalOrdersResult;
import org.rublin.dto.PairDto;
import org.rublin.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping(value = OrderRestController.URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderRestController extends AbstractController {

    static final String URL = URL_PREFIX + "order";

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/sell", method = RequestMethod.GET)
    public OptimalOrdersResult findOptimalSellOrders(@RequestParam(value = "currency") String currency,
                                                     @RequestParam(value = "amountSell") BigDecimal amount) {
        PairDto pair = PairDto.builder()
                .sellCurrency(Currency.KRB)
                .buyCurrency(Currency.valueOf(currency.toUpperCase()))
                .build();
        return orderService.findOptimalOrders(pair, amount);
    }

    public void findOptimalBuyOrders(String currency, BigDecimal amount) {

    }
}

package guru.sfg.beer.order.service.services.listener;

import com.comon.brewery.model.event.ValidateOrderRequest;
import com.comon.brewery.model.event.ValidateOrderResult;
import guru.sfg.beer.order.service.config.JmsConfigConvert;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BeerOrderValidationResponseService {

     private final BeerOrderManager beerOrderManager;
    @JmsListener(destination = JmsConfigConvert.VALIDATE_ORDER_QUEUE)
    public void listen(ValidateOrderResult validateOrderResult)
    {
        final UUID orderId = validateOrderResult.getOrderId();
        beerOrderManager.processValidationResult(orderId,validateOrderResult.isValid());
    }
}

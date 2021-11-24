package guru.sfg.beer.order.service.statemachine.Action;

import com.comon.brewery.model.BeerOrderDto;
import com.comon.brewery.model.event.ValidateOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfigConvert;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    public  final BeerOrderRepository beerOrderRepository;
    public  final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));
        BeerOrder order = beerOrderOptional.get();


            BeerOrderDto beerOrderDto = beerOrderMapper.beerOrderToDto(order);
            ValidateOrderRequest validateOrderRequest = ValidateOrderRequest.builder()
                    .beerOrderDto(beerOrderDto)
                    .build();
            jmsTemplate.convertAndSend(JmsConfigConvert.VALIDATE_ORDER_QUEUE, validateOrderRequest);
           log.error("Order Not Found. Id: " + beerOrderId);

        log.debug("Sent Validation request to queue for order id " + beerOrderId);
    }
}
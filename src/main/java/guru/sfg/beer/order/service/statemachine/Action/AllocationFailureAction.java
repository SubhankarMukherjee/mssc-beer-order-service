package guru.sfg.beer.order.service.statemachine.Action;

import com.comon.brewery.model.event.AllocationFailureEvent;
import guru.sfg.beer.order.service.config.JmsConfigConvert;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final JmsTemplate jmsTemplate;
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String beerOrderId = (String) stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        log.error("Compensating Transaction Allocation failed"+ beerOrderId);
        //Sending JMS message

        jmsTemplate.convertAndSend(JmsConfigConvert.ALLOCATE_ORDER_FAILURE_QUEUE, AllocationFailureEvent
                .builder().orderId(UUID.fromString(beerOrderId)).build());
    }
}

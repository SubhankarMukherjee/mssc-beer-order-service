package guru.sfg.beer.order.service.services.beer;

import com.comon.brewery.model.BeerDTO;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<BeerDTO> getBeerById(UUID id);
    Optional <BeerDTO> getBeerByUPC(String UPC);
}

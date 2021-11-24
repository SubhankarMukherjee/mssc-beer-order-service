package guru.sfg.beer.order.service.services.beer;

import com.comon.brewery.model.BeerDTO;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.Optional;
import java.util.UUID;

@Service
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
public class BeerServiceImpl implements BeerService {


    public static final String BEER_PATH_V1 = "/api/v1/beer/";
    public static final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";

    private final RestTemplate restTemplate;
    private String beerServiceHost;

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }
    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        BeerDTO object = restTemplate.getForObject(beerServiceHost + BEER_PATH_V1 + id.toString(), BeerDTO.class);
        return Optional.of(object);
    }

    @Override
    public Optional<BeerDTO> getBeerByUPC(String UPC) {

        String url=beerServiceHost + BEER_UPC_PATH_V1 + UPC;
        System.out.println("URL is :"+ url);
        BeerDTO object = restTemplate.getForObject(url, BeerDTO.class);
        return Optional.of(object);

    }
}

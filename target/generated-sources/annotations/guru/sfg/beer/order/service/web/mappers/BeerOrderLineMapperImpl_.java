package guru.sfg.beer.order.service.web.mappers;

import com.comon.brewery.model.BeerOrderLineDto;
import com.comon.brewery.model.BeerOrderLineDto.BeerOrderLineDtoBuilder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderLine.BeerOrderLineBuilder;
import javax.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-11-25T02:31:39+0530",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 1.8.0_291 (Oracle Corporation)"
)
@Component
@Qualifier("delegate")
public class BeerOrderLineMapperImpl_ implements BeerOrderLineMapper {

    @Autowired
    private DateMapper dateMapper;

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        if ( line == null ) {
            return null;
        }

        BeerOrderLineDtoBuilder beerOrderLineDto = BeerOrderLineDto.builder();

        beerOrderLineDto.upc( line.getUpc() );
        beerOrderLineDto.beerId( line.getBeerId() );
        beerOrderLineDto.orderQuantity( line.getOrderQuantity() );
        beerOrderLineDto.id( line.getId() );
        if ( line.getVersion() != null ) {
            beerOrderLineDto.version( line.getVersion().intValue() );
        }
        beerOrderLineDto.createdDate( dateMapper.asOffsetDateTime( line.getCreatedDate() ) );
        beerOrderLineDto.lastModifiedDate( dateMapper.asOffsetDateTime( line.getLastModifiedDate() ) );

        return beerOrderLineDto.build();
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        if ( dto == null ) {
            return null;
        }

        BeerOrderLineBuilder beerOrderLine = BeerOrderLine.builder();

        beerOrderLine.id( dto.getId() );
        if ( dto.getVersion() != null ) {
            beerOrderLine.version( dto.getVersion().longValue() );
        }
        beerOrderLine.createdDate( dateMapper.asTimestamp( dto.getCreatedDate() ) );
        beerOrderLine.lastModifiedDate( dateMapper.asTimestamp( dto.getLastModifiedDate() ) );
        beerOrderLine.beerId( dto.getBeerId() );
        beerOrderLine.upc( dto.getUpc() );
        beerOrderLine.orderQuantity( dto.getOrderQuantity() );
        beerOrderLine.quantityAllocated( dto.getQuantityAllocated() );

        return beerOrderLine.build();
    }
}

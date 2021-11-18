package guru.sfg.beer.order.service.web.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
@Data  // data will give getter and setters and euuals and hashcode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeerDTO  implements Serializable{



        private static final long serialVersionUID = 5485517880898835202L;
        @Null   // dont want cliemt to set
        private UUID id;
        @Null
        private Integer version;
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ", shape=JsonFormat.Shape.STRING)
        @Null
        private OffsetDateTime createdDate;
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ", shape=JsonFormat.Shape.STRING)
        @Null
        private OffsetDateTime lastModifiedDate;
        @NotBlank
        private String beerName;
        @NotNull
        private String beerStyle;

        @NotNull
        private String upc;
        private Integer quantityOnHand;
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        @Positive
        @NotNull
        private BigDecimal price;
    }



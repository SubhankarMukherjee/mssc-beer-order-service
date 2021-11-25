package com.comon.brewery.model.event;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocationFailureEvent {

    private UUID orderId;
}

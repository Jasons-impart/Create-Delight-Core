package io.github.jasonsimpart.createdelightcore.content.order;

import java.util.List;

public record OrderEntryCandidates(OrderEntry entry, List<OrderCandidate> candidates) {
    public OrderEntryCandidates {
        candidates = List.copyOf(candidates);
    }
}

package io.github.bayramsevim.notification_service.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")

public class ProcessedEvent {
    @Id
    private UUID eventId;

    Instant processedAt;

    public ProcessedEvent(UUID eventId, Instant processedAt) {
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    protected ProcessedEvent() {
    }


}

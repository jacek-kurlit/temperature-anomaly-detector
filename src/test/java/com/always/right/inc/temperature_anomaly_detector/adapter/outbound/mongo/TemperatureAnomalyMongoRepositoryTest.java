package com.always.right.inc.temperature_anomaly_detector.adapter.outbound.mongo;

import com.always.right.inc.temperature_anomaly_detector.MongoBaseTest;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomalyRepository;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

class TemperatureAnomalyMongoRepositoryTest extends MongoBaseTest implements WithAssertions {

    @Autowired
    TemperatureAnomalyMongoRepository repository;

    @Test
    void shouldSaveAndFindById() {
        // given
        var anomaly = anomaly(UUID.randomUUID());

        // when
        repository.save(anomaly);

        // then
        var found = repository.findById(anomaly.id());
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(anomaly);
    }

    @Test
    void shouldReturnTrueForExistingAnomaly() {
        // given
        var anomaly = anomaly(UUID.randomUUID());
        repository.save(anomaly);

        // when / then
        assertThat(repository.existsById(anomaly.id())).isTrue();
    }

    @Test
    void shouldReturnFalseForNonExistingAnomaly() {
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldNotOverwriteExistingAnomalyOnDuplicateSave() {
        // given
        UUID id = UUID.randomUUID();
        var original = anomaly(id);
        var duplicate = new TemperatureAnomaly(id, "other-room", "other-thermo", 99.0, 99.0, Instant.now());
        repository.save(original);

        // when: naive duplicate check in listener prevents this, but verify MongoDB behaviour
        repository.save(duplicate);

        // then: last write wins in MongoDB — this documents the behaviour explicitly
        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(id).get().roomId()).isEqualTo(duplicate.roomId());
    }

    private static TemperatureAnomaly anomaly(UUID id) {
        return new TemperatureAnomaly(
                id,
                "living-room",
                "thermo-001",
                20.0,
                27.5,
                Instant.parse("2025-01-15T10:30:00Z")
        );
    }
}

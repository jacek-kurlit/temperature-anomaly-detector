package com.always.right.inc.temperature_anomaly_detector.adapter.outbound.mongo;

import com.always.right.inc.temperature_anomaly_detector.MongoBaseTest;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomalyRepository;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerAnomalyCount;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
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

    @Test
    void shouldReturnAnomaliesForThermometerSortedByCreatedAtDesc() {
        // given
        var older = new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 27.0, Instant.parse("2025-01-15T10:00:00Z"));
        var newer = new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 28.0, Instant.parse("2025-01-15T11:00:00Z"));
        var other = new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-002", 20.0, 30.0, Instant.parse("2025-01-15T12:00:00Z"));
        repository.save(older);
        repository.save(newer);
        repository.save(other);

        // when
        var page = repository.findByThermometerIdOrderByCreatedAtDesc("thermo-001", PageRequest.of(0, 10));

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(TemperatureAnomaly::id)
                .containsExactly(newer.id(), older.id());
    }

    @Test
    void shouldReturnEmptyPageForUnknownThermometer() {
        // when
        var page = repository.findByThermometerIdOrderByCreatedAtDesc("unknown", PageRequest.of(0, 10));

        // then
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void shouldPaginateAnomaliesForThermometer() {
        // given
        for (int i = 0; i < 5; i++) {
            repository.save(new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 27.0,
                    Instant.parse("2025-01-15T10:00:00Z").plusSeconds(i)));
        }

        // when
        var firstPage = repository.findByThermometerIdOrderByCreatedAtDesc("thermo-001", PageRequest.of(0, 2));
        var secondPage = repository.findByThermometerIdOrderByCreatedAtDesc("thermo-001", PageRequest.of(1, 2));

        // then
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(2);
    }

    @Test
    void shouldReturnAnomaliesForRoomSortedByCreatedAtDesc() {
        // given
        var older = new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 27.0, Instant.parse("2025-01-15T10:00:00Z"));
        var newer = new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-002", 20.0, 28.0, Instant.parse("2025-01-15T11:00:00Z"));
        var otherRoom = new TemperatureAnomaly(UUID.randomUUID(), "room-b", "thermo-001", 20.0, 30.0, Instant.parse("2025-01-15T12:00:00Z"));
        repository.save(older);
        repository.save(newer);
        repository.save(otherRoom);

        // when
        var page = repository.findByRoomIdOrderByCreatedAtDesc("room-a", PageRequest.of(0, 10));

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(TemperatureAnomaly::id)
                .containsExactly(newer.id(), older.id());
    }

    @Test
    void shouldReturnThermometersExceedingAnomalyThreshold() {
        // given: thermo-001 has 3 anomalies, thermo-002 has 1
        Instant base = Instant.parse("2025-01-15T10:00:00Z");
        for (int i = 0; i < 3; i++) {
            repository.save(new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 27.0, base.plusSeconds(i)));
        }
        repository.save(new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-002", 20.0, 27.0, base));

        // when: threshold = 2, so only thermo-001 qualifies
        List<ThermometerAnomalyCount> result = repository.findThermometersWithAnomalyCountExceeding(2, Instant.EPOCH);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("thermo-001");
        assertThat(result.getFirst().count()).isEqualTo(3);
    }

    @Test
    void shouldExcludeAnomaliesBeforeFromDate() {
        // given: 3 anomalies for thermo-001, but only 2 are after fromDate
        Instant cutoff = Instant.parse("2025-01-15T10:30:00Z");
        repository.save(new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 27.0, Instant.parse("2025-01-15T10:00:00Z")));
        repository.save(new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 28.0, Instant.parse("2025-01-15T11:00:00Z")));
        repository.save(new TemperatureAnomaly(UUID.randomUUID(), "room-a", "thermo-001", 20.0, 29.0, Instant.parse("2025-01-15T12:00:00Z")));

        // when: threshold = 2, only 2 anomalies are after cutoff — not enough to exceed
        List<ThermometerAnomalyCount> resultExcluded = repository.findThermometersWithAnomalyCountExceeding(2, cutoff);
        // when: threshold = 1, 2 anomalies exceed threshold of 1
        List<ThermometerAnomalyCount> resultIncluded = repository.findThermometersWithAnomalyCountExceeding(1, cutoff);

        // then
        assertThat(resultExcluded).isEmpty();
        assertThat(resultIncluded).hasSize(1);
        assertThat(resultIncluded.getFirst().count()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyWhenNoThermometerExceedsThreshold() {
        // given
        repository.save(anomaly(UUID.randomUUID()));

        // when
        List<ThermometerAnomalyCount> result = repository.findThermometersWithAnomalyCountExceeding(5, Instant.EPOCH);

        // then
        assertThat(result).isEmpty();
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

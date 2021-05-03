package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.database.MeasurementDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MeasurementDayRepository extends JpaRepository<MeasurementDay, Integer> {

    @Query("SELECT md.measurementDayDate FROM MeasurementDay md")
    List<LocalDate> findMeasurementDates();

    @Query("select COUNT(md) FROM MeasurementDay md WHERE md.measurementDayDate = :dataDay")
    Integer findExistingMeasureDate(@Param("dataDay") LocalDate dataDay);
}

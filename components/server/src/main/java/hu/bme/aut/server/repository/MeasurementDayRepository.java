package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.database.MeasurementDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MeasurementDayRepository extends JpaRepository<MeasurementDay, Integer> {

    //@Query(value = "SELECT measurement_day_date FROM MeasurementDay", nativeQuery = true)
    @Query("SELECT md.measurementDayDate FROM MeasurementDay md")
    List<LocalDate> findMeasurementDates();
}

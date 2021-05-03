package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.database.MeasurementDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for measurement day database
 */
@Repository
public interface MeasurementDayRepository extends JpaRepository<MeasurementDay, Integer> {

    /**
     * Get all distinct measurement days
     *
     * @return  all distinct measurement days in LocalDate format
     */
    @Query("SELECT md.measurementDayDate FROM MeasurementDay md")
    List<LocalDate> findMeasurementDates();

    /**
     * Get the measurements on given day
     *
     * @param dataDay   given day (int LocalDate format)
     * @return          sum of measurement on given day
     */
    @Query("select COUNT(md) FROM MeasurementDay md WHERE md.measurementDayDate = :dataDay")
    Integer findExistingMeasureDate(@Param("dataDay") LocalDate dataDay);
}

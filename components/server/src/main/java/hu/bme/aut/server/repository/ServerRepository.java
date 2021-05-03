package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.database.LightData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.SqlResultSetMapping;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<LightData, Integer>, ServerRepositoryCustom {

    @Query("SELECT ld FROM LightData ld WHERE DATEDIFF(day, ld.measureDate, :dataDate) = 0")
    List<LightData> findLightDataMeasuredOnDay(@Param("dataDate") LocalDateTime dataDate);

    // https://stackoverflow.com/questions/14844186/selecting-a-distinct-date-by-day-only-from-datetime-yyyy-mm-dd-hhmmss-in-mys#14844269
    @Query(value = "SELECT DISTINCT YEAR(measure_date), MONTH(measure_date), DAY(measure_date) FROM LightData)", nativeQuery = true)
    List<String> findMeasurementDates();
}

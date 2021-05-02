package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.LightData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<LightData, Integer>, ServerRepositoryCustom {

    @Query("SELECT ld FROM LightData ld WHERE DATEDIFF(day, ld.measureDate, :dataDate) = 0")
    List<LightData> findLightDataMeasuredOnDay(@Param("dataDate") LocalDateTime dataDate);

    // https://stackoverflow.com/questions/14844186/selecting-a-distinct-date-by-day-only-from-datetime-yyyy-mm-dd-hhmmss-in-mys#14844269
    @Query("SELECT DISTINCT CONCAT(YEAR(ld.measureDate), ' ', MONTH(ld.measureDate), ' ', DAY(ld.measureDate)) FROM LightData ld") // GROUP BY ld.id, YEAR(ld.measureDate), MONTH(ld.measureDate), DAY(ld.measureDate)")
    //@Query("SELECT DATE(ld.measureDate) FROM LightData ld")
    List<Integer> findMeasurementDates();
}

package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.database.LightData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<LightData, Integer> {

    @Query("SELECT ld FROM LightData ld WHERE DATEDIFF(day, ld.measureDate, :dataDate) = 0")
    List<LightData> findLightDataMeasuredOnDay(@Param("dataDate") LocalDateTime dataDate);
}

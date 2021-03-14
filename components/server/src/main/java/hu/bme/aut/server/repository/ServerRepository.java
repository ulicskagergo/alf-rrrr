package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.LightData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends JpaRepository<LightData, Integer> {

}

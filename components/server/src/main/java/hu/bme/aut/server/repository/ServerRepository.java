package hu.bme.aut.server.repository;

import hu.bme.aut.server.domain.Server;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class ServerRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(Server server) {
        em.persist(server);
    }
}

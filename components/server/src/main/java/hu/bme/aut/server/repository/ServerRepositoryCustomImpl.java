package hu.bme.aut.server.repository;

public class ServerRepositoryCustomImpl implements ServerRepositoryCustom {
    /*@Autowired
    private EntityManager em;

    @Override
    public List<LocalDateTime> findMeasurementDates() {
        String queryString = "SELECT ld.measureDate FROM LightData ld GROUP BY ld.measureDate";
        TypedQuery<LocalDateTime> query = em.createQuery(queryString, LocalDateTime.class);
        LightData lightData = new LightData();
        query.setParameter("date", new Date());
        return query.getResultList();
    }*/
}

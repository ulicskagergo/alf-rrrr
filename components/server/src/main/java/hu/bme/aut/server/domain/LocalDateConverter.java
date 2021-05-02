/*package hu.bme.aut.server.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.time.LocalTime;
import java.util.Optional;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDateTime, Date> {

    @Override
    public Date convertToDatabaseColumn(LocalDateTime locDate) {
        return (locDate == null ? null : Date.valueOf(locDate.toLocalDate()));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Date sqlDate) {
        return (sqlDate == null ? null : LocalDateTime.of(sqlDate.toLocalDate(), LocalTime.now()));
    }
}*/

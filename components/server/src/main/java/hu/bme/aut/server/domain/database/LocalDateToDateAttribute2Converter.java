package hu.bme.aut.server.domain.database;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.sql.Date;

/**
 * Converter for SQL DATE format and Java LocalDate formats
 */
@Converter(autoApply = true)
public class LocalDateToDateAttribute2Converter implements AttributeConverter<Date, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    @Override
    public Date convertToEntityAttribute(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }
}

CREATE TABLE LightData
(
    id           INT       NOT NULL,
    measure_date TIMESTAMP NOT NULL,
    is_on        BOOLEAN   NOT NULL,
    threshold    INT       NOT NULL,
    actual_value INT       NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE MeasurementDay
(
    id                   INT  NOT NULL,
    measurement_day_date DATE NOT NULL UNIQUE,
    PRIMARY KEY (id)
);
CREATE TABLE LightData (
    id INT NOT NULL,
    measure_date TIMESTAMP NOT NULL,
    is_on BOOLEAN NOT NULL,
    threshold INT NOT NULL,
    PRIMARY KEY(id)
);
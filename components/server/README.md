## Server component

### Build and run

Install Maven:
```bash
apt install maven
```

Compile the project download all dependencies and generate the target folder:
```bash
mvn compile
```

Run the project:
```bash
mvn exec:java -Dexec.mainClass="hu.bme.aut.server.ServerApplication"
```

To customize the log level of the project use the following flag (it is INFO by default):
```
-Dlogging.level.hu.aut.server=DEBUG
```

Optional: if you want to build and install into your local Maven repository (first delete the target folder):
```bash
mvn clean install
```

Optional: if you want to package the project:
```bash
mvn package
```

When started you can access the server on localhost:8080/data/{id}.

To test POST with ```curl```:
```bash
 curl -X POST http://localhost:8080/data -H 'cache-control: no-cache' -H 'content-type: application/json' -d '{ "threshold":<0-999>, "isOn":<true/false> }'
```

```bash
 curl -X GET http://localhost:8080/dates
```

```bash
 curl -X GET http://localhost:8080/data/2021-05-03T00:00:00.000000
```

```bash
 curl -X POST http://localhost:8080/settings -H 'cache-control: no-cache' -H 'content-type: application/json' -d '{ "sensitivity": 0, "from": "11:00", "to":"13:00" }'
```

```bash
 curl -X GET http://localhost:8080/settings
```

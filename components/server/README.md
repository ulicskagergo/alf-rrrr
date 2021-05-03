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
mvn exec:java -Dexec.mainClass="hu.bme.aut.server.ServerDemoApplication"
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
 curl -X GET http://localhost:8080/data
```

```bash
 curl -X GET http://localhost:8080/dates
```

```bash
 curl -X GET http://localhost:8080/data/2021-05-30T15:13:48.934496
```

```bash
 curl -X POST http://localhost:8080/settings -H 'cache-control: no-cache' -H 'content-type: application/json' -d '{ "sensitivity": 100, "from": "09:00", "to":"19:00" }'
```

```bash
 curl -X GET http://localhost:8080/settings
```
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
# Camunda 7 – Mensch ärgere dich nicht (Solo, 1 Figur)

## Warum der Fix?

Dein Fehler kam daher, dass ich im ersten ZIP ein *nicht existierendes* BOM importiert habe.
Diese Version nutzt stattdessen die offiziellen Starter-Artefakte direkt mit Version `7.24.0`.

## Voraussetzungen

- Java 17+
- Maven 3.9+

## Start

```bash
mvn -U clean spring-boot:run
```

Wenn Maven einmal „absent“ gecached hat, hilft auch:

- `mvn -U ...` oder
- den betroffenen Ordner unter `~/.m2/repository/org/camunda/bpm/springboot` löschen.

## UI

- Spiel-UI: http://localhost:8080/
- Camunda Webapps: http://localhost:8080/camunda/app/welcome/default/#!/login
    - Login: `demo` / `demo`

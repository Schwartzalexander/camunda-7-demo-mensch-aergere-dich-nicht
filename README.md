# Camunda 7 – Mensch ärgere dich nicht (Solo, 1 Figur)

Eine kleine Camunda-7-Demo, die den Spielablauf von _Mensch ärgere dich nicht_ mit nur einer Figur automatisiert. Der Prozess wird in BPMN modelliert und über External-Task-Worker (im selben Spring Boot-Prozess) ausgeführt. Die UI visualisiert den aktuellen Spielstand.

## Voraussetzungen

- Java 17+
- Maven 3.9+

## Start

```bash
mvn -U clean spring-boot:run
```

Das Spiel wartet auf einen Klick auf **Würfeln** (oder einen entsprechenden REST-Request), bevor die External-Tasks verarbeitet werden.

## Nutzung

1. Öffne die Spiel-UI unter http://localhost:8080/.
2. Klicke auf **Neues Spiel starten**, um eine neue Prozessinstanz anzulegen.
3. Mit **Würfeln** (oder einem `POST /api/roll?processInstanceId=...`) wird genau ein Würfelvorgang ausgelöst; nur die zugehörigen External Tasks werden abgearbeitet.
4. Der Spielstand (Position, Würfe, Pasch-Infos) wird im UI angezeigt. Sobald die Figur das Ziel erreicht, stoppt die Instanz.

## Weitere Tools

- Camunda Webapps: http://localhost:8080/camunda/app/welcome/default/#!/login  
  Login: `demo` / `demo`
- H2-Konsole: http://localhost:8080/h2-console  
  JDBC-URL: `jdbc:h2:mem:camunda`

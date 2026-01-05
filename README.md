# Camunda 7 – Mensch ärgere dich nicht (Solo, 1 Figur)

Eine kleine Camunda-7-Demo, die den Spielablauf von _Mensch ärgere dich nicht_ mit nur einer Figur automatisiert. Der Prozess wird in BPMN modelliert und über External-Task-Worker (im selben Spring Boot-Prozess) ausgeführt. Die UI visualisiert den aktuellen Spielstand.

## Voraussetzungen

- Java 17+
- Maven 3.9+

## Start

```bash
mvn -U clean spring-boot:run
```

Die External-Task-Worker starten mit 3 Sekunden Verzögerung, damit der eingebettete REST-Endpunkt des Camunda-Engines sicher verfügbar ist.

## Nutzung

1. Öffne die Spiel-UI unter http://localhost:8080/.
2. Klicke auf **Neues Spiel starten**, um eine neue Prozessinstanz anzulegen.
3. Mit **Würfeln** kannst du den Status manuell aktualisieren; der Prozess würfelt und bewegt die Figur automatisch über die External-Task-Worker.
4. Der Spielstand (Position, Würfe, Pasch-Infos) wird im UI angezeigt. Sobald die Figur das Ziel erreicht, stoppt die Instanz.

## Weitere Tools

- Camunda Webapps: http://localhost:8080/camunda/app/welcome/default/#!/login  
  Login: `demo` / `demo`
- H2-Konsole: http://localhost:8080/h2-console  
  JDBC-URL: `jdbc:h2:mem:camunda`

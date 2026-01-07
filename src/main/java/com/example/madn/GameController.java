package com.example.madn;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class GameController {

  private final GameService gameService;
  private final UserTaskRollService userTaskRollService;

  public GameController(GameService gameService, UserTaskRollService userTaskRollService) {
    this.gameService = gameService;
    this.userTaskRollService = userTaskRollService;
  }

  @GetMapping("/")
  public String index(Model model) {
    var pidOpt = gameService.findAnyRunningInstanceId();
    model.addAttribute("processInstanceId", pidOpt.orElse(""));
    model.addAttribute("maxPlayers", GameService.MAX_PLAYERS);
    return "index";
  }

  @PostMapping("/api/start")
  @ResponseBody
  public Map<String, Object> start(@RequestParam(defaultValue = "2") int players) {
    String pid = gameService.startNewGame(players);
    return Map.of("processInstanceId", pid);
  }

  @GetMapping("/api/state")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> state(@RequestParam String processInstanceId) {
    return gameService.getState(processInstanceId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/api/roll")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> roll(@RequestParam String processInstanceId) {
    var stateOpt = gameService.getState(processInstanceId);
    if (stateOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    boolean rolled = userTaskRollService.rollOnce(processInstanceId);
    if (!rolled) {
      return ResponseEntity.status(409).body(Map.of(
          "message", "Kein würfelbarer User Task gefunden – der Prozess wartet vermutlich bereits."
      ));
    }

    return gameService.getState(processInstanceId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/api/choose")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> choose(@RequestParam String processInstanceId, @RequestParam int pieceId) {
    var stateOpt = gameService.getState(processInstanceId);
    if (stateOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    boolean chosen = userTaskRollService.choosePiece(processInstanceId, pieceId);
    if (!chosen) {
      return ResponseEntity.status(409).body(Map.of(
              "message", "Kein Auswahl-Task gefunden – der Prozess wartet vermutlich noch aufs Würfeln."
      ));
    }

    return gameService.getState(processInstanceId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
  }
}

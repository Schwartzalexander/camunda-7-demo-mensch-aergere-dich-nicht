package com.example.madn;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class GameController {

  private final GameService gameService;
  private final ManualExternalTaskWorker manualExternalTaskWorker;

  public GameController(GameService gameService, ManualExternalTaskWorker manualExternalTaskWorker) {
    this.gameService = gameService;
    this.manualExternalTaskWorker = manualExternalTaskWorker;
  }

  @GetMapping("/")
  public String index(Model model) {
    var pidOpt = gameService.findAnyRunningInstanceId();
    model.addAttribute("processInstanceId", pidOpt.orElse(""));
    return "index";
  }

  @PostMapping("/api/start")
  @ResponseBody
  public Map<String, Object> start() {
    String pid = gameService.startNewGame();
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

    boolean rolled = manualExternalTaskWorker.rollOnce(processInstanceId);
    if (!rolled) {
      return ResponseEntity.status(409).body(Map.of(
          "message", "Kein würfelbarer External Task gefunden – der Prozess wartet vermutlich bereits."
      ));
    }

    return gameService.getState(processInstanceId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}

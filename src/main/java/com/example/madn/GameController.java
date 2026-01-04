package com.example.madn;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class GameController {

  private final GameService gameService;

  public GameController(GameService gameService) {
    this.gameService = gameService;
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
    return ResponseEntity.ok(gameService.getState(processInstanceId));
  }
}

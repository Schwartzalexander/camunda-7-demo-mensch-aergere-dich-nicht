package com.example.madn.logic;

import com.example.madn.GameService;
import com.example.madn.model.MoveResult;
import com.example.madn.model.PiecePosition;
import com.example.madn.model.PieceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BoardLogic {

    private static final Logger log = LoggerFactory.getLogger(BoardLogic.class);

    private BoardLogic() {
    }

    public static PiecePosition toPiecePosition(Map<String, Object> map) {
        if (map == null) {
            return new PiecePosition(PieceStatus.HOME, 0);
        }
        String status = Objects.toString(map.getOrDefault("status", "HOME"));
        int index = toInt(map.get("index"), 0);
        return new PiecePosition(PieceStatus.valueOf(status), index);
    }

    public static Map<String, Object> toMap(PiecePosition pos) {
        return Map.of(
                "status", pos.getStatus().name(),
                "index", pos.getIndex()
        );
    }

    public static int toInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return defaultValue;
    }

    public static MoveResult movePiece(Map<String, Object> pieceMap, int dice) {
        PiecePosition position = toPiecePosition(pieceMap);
        MoveResult result = new MoveResult();

        if (position.getStatus() == PieceStatus.GOAL) {
            result.setMoveWasPossible(false);
            result.setMovedPosition(position);
            return result;
        }

        if (position.getStatus() == PieceStatus.HOME) {
            if (dice == 6) {
                PiecePosition next = new PiecePosition(PieceStatus.BOARD, 0);
                result.setMoveWasPossible(true);
                result.setMovedPosition(next);
            } else {
                result.setMoveWasPossible(false);
                result.setMovedPosition(position);
            }
            return result;
        }

        int targetSteps = position.getIndex() + dice;
        if (targetSteps < GameService.BOARD_SIZE) {
            PiecePosition next = new PiecePosition(PieceStatus.BOARD, targetSteps);
            result.setMoveWasPossible(true);
            result.setMovedPosition(next);
            return result;
        }

        int goalIndex = targetSteps - GameService.BOARD_SIZE;
        if (goalIndex < GameService.GOAL_LENGTH) {
            PiecePosition next = new PiecePosition(PieceStatus.GOAL, goalIndex);
            result.setMoveWasPossible(true);
            result.setMovedPosition(next);
            return result;
        }

        result.setMoveWasPossible(false);
        result.setMovedPosition(position);
        return result;
    }

    public static Integer absoluteBoardPosition(int startIndex, PiecePosition pos) {
        if (pos.getStatus() != PieceStatus.BOARD) {
            return null;
        }
        return (startIndex + pos.getIndex()) % GameService.BOARD_SIZE;
    }

    public static void applyKick(Map<String, Object> boardState, List<Map<String, Object>> players, String currentPlayerId, int chosenPieceId, MoveResult moveResult) {
        moveResult.setKicked(false);
        Integer movedAbsolute = findAbsolute(players, currentPlayerId, moveResult.getMovedPosition());
        if (movedAbsolute == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : boardState.entrySet()) {
            String playerId = entry.getKey();
            if (playerId.equals(currentPlayerId)) {
                continue;
            }
            List<Map<String, Object>> pieces = (List<Map<String, Object>>) entry.getValue();
            for (int i = 0; i < pieces.size(); i++) {
                Map<String, Object> p = pieces.get(i);
                PiecePosition pos = toPiecePosition(p);
                Integer abs = findAbsolute(players, playerId, pos);
                if (abs != null && abs == movedAbsolute) {
                    // kick
                    pieces.set(i, toMap(new PiecePosition(PieceStatus.HOME, 0)));
                    moveResult.setKicked(true);
                    moveResult.setKickedPlayerId(playerId);
                    moveResult.setKickedPieceId(i);
                    log.info("Kick: {}:{} kicks {}:{} at {}", currentPlayerId, chosenPieceId, playerId, i, abs);
                    return;
                }
            }
        }
    }

    private static Integer findAbsolute(List<Map<String, Object>> players, String playerId, PiecePosition position) {
        Integer startIndex = players.stream()
                .filter(p -> Objects.equals(p.get("id"), playerId))
                .map(p -> toInt(p.get("startIndex"), 0))
                .findFirst()
                .orElse(0);
        return absoluteBoardPosition(startIndex, position);
    }
}

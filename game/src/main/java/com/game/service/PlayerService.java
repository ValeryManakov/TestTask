package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PlayerService {
    List<Player> getAllPlayers();
    Player createPlayer(Player playerToBeCreated);
    Player getPlayer(Long id);
    void deletePlayer(Long id);
}

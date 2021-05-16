package com.game.service;

import com.game.entity.*;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    private PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getAllPlayers() {
        System.out.println("In getAllPlayers");
        return playerRepository.findAll();
    }

    @Override
    public Player createPlayer(Player playerToBeCreated) {
        System.out.println("In createPlayer, playerName: " + playerToBeCreated.getName());
        return playerRepository.save(playerToBeCreated);
    }

    @Override
    public Player getPlayer(Long id) {
        System.out.println("In getPlayer, playerId: " + id);
        return playerRepository.findById(id).get();
    }

    @Override
    public void deletePlayer(Long id) {
        System.out.println("In deletePlayer, playerId: " + id);
        playerRepository.deleteById(id);
    }
}

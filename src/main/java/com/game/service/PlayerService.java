package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.List;

public interface PlayerService {

    List<Player> getAllPlayers(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel);

    Player savePlayer(Player player);

    Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalAccessException;

    void deletePlayer(Player player);

    Player getPlayer(Long id);

    List<Player> sortPlayer(List<Player> players, PlayerOrder order);

    boolean isPlayerValid(Player player);

    boolean isIdValid(String id);

    Player setExpAndLvl(Player player);

    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);
}

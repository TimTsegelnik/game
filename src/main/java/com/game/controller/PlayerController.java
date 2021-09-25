package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
public class PlayerController {

    private PlayerService service;

    @Autowired
    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @GetMapping("/rest/players")
    public ResponseEntity<List<Player>> getAllPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false) PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        final List<Player> players = service.getAllPlayers(name, title, race, profession,
                after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        final List<Player> sortedPlayers = service.sortPlayer(players, order);

        return new ResponseEntity<>(service.getPage(sortedPlayers, pageNumber, pageSize), HttpStatus.OK);
    }

    @GetMapping("/rest/players/count")
    public ResponseEntity<Integer> getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {
        final Integer result = service.getAllPlayers(name, title, race, profession, after, before,
                banned, minExperience, maxExperience, minLevel, maxLevel).size();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/rest/players")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!service.isPlayerValid(player))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (player.getBanned() == null) player.setBanned(false);

        service.setExpAndLvl(player);
        final Player savedPlayer = service.savePlayer(player);
        return new ResponseEntity<>(savedPlayer, HttpStatus.OK);
    }

    @PostMapping("/rest/players/{id}")
    public ResponseEntity<Player> updatePlayer(
            @PathVariable(value = "id") String pathId,
            @RequestBody Player player) {

        if (!service.isIdValid(pathId.trim()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Long id = Long.parseLong(pathId);
        final Player oldPlayer = service.getPlayer(id);

        if (oldPlayer == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        final Player result;
        try {
            result = service.updatePlayer(oldPlayer,player);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @GetMapping("/rest/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable(value = "id") String pathId) {
        if (!service.isIdValid(pathId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Long id = Long.parseLong(pathId);
        final Player player = service.getPlayer(id);

        if (player == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @DeleteMapping("/rest/players/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable(value = "id") String pathId) {
        if (!service.isIdValid(pathId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Long id = Long.parseLong(pathId);
        final Player player = service.getPlayer(id);

        if (player == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        service.deletePlayer(player);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

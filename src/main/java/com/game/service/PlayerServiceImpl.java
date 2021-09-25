package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Service
public class PlayerServiceImpl implements PlayerService {

    private PlayerRepository playerRepository;

    public PlayerServiceImpl() {
    }

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getAllPlayers(
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
            Integer maxLevel) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Player> players = new ArrayList<>();
        playerRepository.findAll().forEach(player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;

            players.add(player);
        });
        return players;
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalAccessException {

        String name = newPlayer.getName();
        if (name != null) {
            if (isNameValid(name))
                oldPlayer.setName(name);
            else
                throw new IllegalAccessException();
        }

        String title = newPlayer.getTitle();
        if (title != null) {
            if (isTitleValid(title))
                oldPlayer.setTitle(title);
            else
                throw new IllegalAccessException();
        }

        if (newPlayer.getRace() != null)
            oldPlayer.setRace(newPlayer.getRace());

        if (newPlayer.getProfession() != null)
            oldPlayer.setProfession(newPlayer.getProfession());

        final Date newBirthday = newPlayer.getBirthday();
        if (newBirthday != null){
            if (isDateValid(newBirthday))
                oldPlayer.setBirthday(newBirthday);
            else
                throw new IllegalAccessException();
        }

        if (newPlayer.getBanned() != null)
            oldPlayer.setBanned(newPlayer.getBanned());

        final Integer exp = newPlayer.getExperience();
        if (exp != null){
            if (isExpValid(exp)){
                oldPlayer.setExperience(exp);
                final Integer lvl = currentLevel(exp);
                oldPlayer.setLevel(lvl);
                final Integer untilNextLvl = untilNextLevel(exp, lvl);
                oldPlayer.setUntilNextLevel(untilNextLvl);
            }else
                throw new IllegalAccessException();
        }
        playerRepository.save(oldPlayer);
        return oldPlayer;
    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public Player getPlayer(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public List<Player> sortPlayer(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((p1, p2) -> {
                switch (order) {
                    case ID:
                        return p1.getId().compareTo(p2.getId());
                    case NAME:
                        return p1.getName().compareTo(p2.getName());
                    case LEVEL:
                        return p1.getLevel().compareTo(p2.getLevel());
                    case BIRTHDAY:
                        return p1.getBirthday().compareTo(p2.getBirthday());
                    case EXPERIENCE:
                        return p1.getExperience().compareTo(p2.getExperience());
                    default:
                        return 0;
                }
            });
        }
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }

    @Override
    public boolean isPlayerValid(Player player) {
        return player != null && isNameValid(player.getName())
                && isTitleValid(player.getTitle())
                && isDateValid(player.getBirthday())
                && isExpValid(player.getExperience())
                && player.getRace() != null
                && player.getProfession() != null;
    }

    @Override
    public boolean isIdValid(String id) {
        if (id.isEmpty() || id == null)
            return false;
        return id.matches("^[1-9]\\d*");
    }

    @Override
    public Player setExpAndLvl(Player player) {
        Integer exp = player.getExperience();
        Integer lvl = currentLevel(exp);
        player.setLevel(lvl);
        player.setUntilNextLevel(untilNextLevel(exp, lvl));
        return player;
    }

    private boolean isNameValid(String name) {
        final int maxNameLength = 12;
        return name != null && !name.isEmpty() && name.length() <= maxNameLength;
    }

    private boolean isTitleValid(String title) {
        final int maxTitleLength = 30;
        return title != null && !title.isEmpty() && title.length() <= maxTitleLength;
    }

    private boolean isExpValid(Integer exp) {
        final int maxExp = 10_000_000;
        final int minExp = 0;
        return exp != null && exp.compareTo(maxExp) <= 0 && exp.compareTo(minExp) >= 0;
    }

    private boolean isDateValid(Date date) {
        final Date startDate = getDateForYear(2000);
        final Date endDate = getDateForYear(3000);
        return date != null && date.after(startDate) && date.before(endDate);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public Integer untilNextLevel(Integer exp, Integer lvl) {
        return 50 * (lvl + 1) * (lvl + 2) - exp;
    }

    public Integer currentLevel(Integer exp) {
        double result = (Math.sqrt(2500 + 200 * exp)- 50) / 100;
        return (int) result;
    }
}

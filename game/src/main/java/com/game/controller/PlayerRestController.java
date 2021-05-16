package com.game.controller;

import com.game.entity.*;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rest/players")
public class PlayerRestController {

    private PlayerService playerService;

    @Autowired
    public PlayerRestController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Player>> getPlayersList(@RequestParam(value = "name", required = false) String name,
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
                                                       @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        System.out.println("In GET /rest/players");

        PlayerPage playerPage = new PlayerPage();
        setPlayerPageParameters(playerPage, order, pageNumber, pageSize);

        PlayerSearchCriteria playerSearchCriteria = new PlayerSearchCriteria(name, title, race, profession,
                                                                             after, before, banned,
                                                                             minExperience, maxExperience,
                                                                             minLevel, maxLevel);

        List<Player> allPlayers = playerService.getAllPlayers();
        List<Player> filteredPlayers = getFilteredPlayers(allPlayers, playerSearchCriteria);
        sortPlayers(filteredPlayers, playerPage);

        Pageable pageable = PageRequest.of(playerPage.getPageNumber(),
                                        playerPage.getPageSize(),
                                        Sort.by(playerPage.getSortDirection(), playerPage.getSortBy()));

        Integer numberOfFirstPlayerOnPage = pageable.getPageNumber()*pageable.getPageSize();
        Integer numberOfLastPlayerOnPage = numberOfFirstPlayerOnPage + pageable.getPageSize();
        if (numberOfLastPlayerOnPage > filteredPlayers.size()) numberOfLastPlayerOnPage = filteredPlayers.size();

        return new ResponseEntity<List<Player>>(new PageImpl<>(filteredPlayers, pageable,
                                                filteredPlayers.size()).getContent().
                                                subList(numberOfFirstPlayerOnPage, numberOfLastPlayerOnPage),
                                                HttpStatus.OK);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ResponseEntity<Integer> getPlayersCount(@RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "title", required = false) String title,
                                                   @RequestParam(value = "race", required = false) Race race,
                                                   @RequestParam(value = "profession", required = false) Profession profession,
                                                   @RequestParam(value = "after", required = false) Long after,
                                                   @RequestParam(value = "before", required = false) Long before,
                                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        System.out.println("In GET /rest/players/count");

        PlayerSearchCriteria playerSearchCriteria = new PlayerSearchCriteria(name, title, race, profession,
                                                                             after, before, banned,
                                                                             minExperience, maxExperience,
                                                                             minLevel, maxLevel);
        List<Player> allPlayers = playerService.getAllPlayers();
        List<Player> filteredPlayers = getFilteredPlayers(allPlayers, playerSearchCriteria);
        Integer count = filteredPlayers.size();

        return new ResponseEntity<Integer>(count, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Player> createPlayer(@RequestBody Player playerToBeCreated) {
        System.out.println("In POST /rest/players");

        if (!playerIsReadyToBeCreated(playerToBeCreated)) return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);

        setLevelAndUntilNextLevelForPlayer(playerToBeCreated);

        if (playerToBeCreated.isBanned() == null) playerToBeCreated.setBanned(false);

        Player player = playerService.createPlayer(playerToBeCreated);

        return new ResponseEntity<Player>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Player> getPlayer(@PathVariable("id") String id) {
        System.out.println("In GET /rest/players/{id}");

        Long longId = null;
        if (idIsValid(id)) longId = Long.parseLong(id);
        else return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);

        Player player = null;
        try {
            player = playerService.getPlayer(longId);
        } catch (Exception e) { return new ResponseEntity<Player>(HttpStatus.NOT_FOUND); }
        if (player == null) return new ResponseEntity<Player>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<Player>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") String id,
                                               @RequestBody Player playerToBeUpdated) {
        System.out.println("In POST /rest/players/{id}");

        Long longId = null;
        if (idIsValid(id)) longId = Long.parseLong(id);
        else return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);

        Player playerToUpdate = null;
        try {
            playerToUpdate = playerService.getPlayer(longId);
        } catch (Exception e) { return new ResponseEntity<Player>(HttpStatus.NOT_FOUND); }
        if (playerToUpdate == null) return new ResponseEntity<Player>(HttpStatus.NOT_FOUND);

        setNewPropertiesForPlayer(playerToUpdate, playerToBeUpdated);
        setLevelAndUntilNextLevelForPlayer(playerToUpdate);

        Player player = playerService.createPlayer(playerToUpdate);

        return new ResponseEntity<Player>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") String id) {
        System.out.println("In DELETE /rest/players/{id}");

        Long longId = null;
        if (idIsValid(id)) longId = Long.parseLong(id);
        else return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);

        Player playerToDelete = null;
        try {
            playerToDelete = playerService.getPlayer(longId);
        } catch (Exception e) { return new ResponseEntity<Player>(HttpStatus.NOT_FOUND); }
        if (playerToDelete == null) return new ResponseEntity<Player>(HttpStatus.NOT_FOUND);

        playerService.deletePlayer(longId);

        return new ResponseEntity<Player>(HttpStatus.OK);
    }

    private void setPlayerPageParameters(PlayerPage playerPage,
                                         PlayerOrder order,
                                         Integer pageNumber,
                                         Integer pageSize) {
        PlayerOrder ultimateOrder = order == null ? PlayerOrder.ID : order;
        Integer ultimatePageNumber = pageNumber == null ? 0 : pageNumber;
        Integer ultimatePageSize = pageSize == null ? 3 : pageSize;

        playerPage.setSortBy(ultimateOrder.getFieldName());
        playerPage.setPageNumber(ultimatePageNumber);
        playerPage.setPageSize(ultimatePageSize);
    }

    private List<Player> getFilteredPlayers(List<Player> allPlayers,
                                            PlayerSearchCriteria playerSearchCriteria) {
        List<Player> filteredPlayers = new ArrayList<>();

        String name = playerSearchCriteria.getName();
        String title= playerSearchCriteria.getTitle();
        Race race = playerSearchCriteria.getRace();
        Profession profession = playerSearchCriteria.getProfession();
        Long after = playerSearchCriteria.getAfter();
        Long before = playerSearchCriteria.getBefore();
        Boolean banned = playerSearchCriteria.isBanned();
        Integer minExperience = playerSearchCriteria.getMinExperience();
        Integer maxExperience = playerSearchCriteria.getMaxExperience();
        Integer minLevel = playerSearchCriteria.getMinLevel();
        Integer maxLevel = playerSearchCriteria.getMaxLevel();

        for (Player player: allPlayers) {
            Boolean flag = true;

            if (name != null && !name.isEmpty() && !player.getName().contains(name)) flag = false;
            if (title != null && !title.isEmpty() && !player.getTitle().contains(title)) flag = false;
            if (race != null && !player.getRace().equals(race)) flag = false;
            if (profession != null && !player.getProfession().equals(profession)) flag = false;
            if (after != null && player.getBirthday().getTime() < after) flag = false;
            if (before != null && player.getBirthday().getTime() > before) flag = false;
            if (banned != null && !player.isBanned().equals(banned)) flag = false;
            if (minExperience != null && player.getExperience() < minExperience) flag = false;
            if (maxExperience != null && player.getExperience() > maxExperience) flag = false;
            if (minLevel != null && player.getLevel() < minLevel) flag = false;
            if (maxLevel != null && player.getLevel() > maxLevel) flag = false;

            if (flag) filteredPlayers.add(player);
        }

        return filteredPlayers;
    }

    private void sortPlayers(List<Player> filteredPlayers, PlayerPage playerPage) {
        String sortBy = playerPage.getSortBy();
        Collections.sort(filteredPlayers, new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                switch (sortBy) {
                    case "id":
                        return player1.getId() > player2.getId() ? 1 : -1;
                    case "name":
                        return player1.getName().compareTo(player2.getName());
                    case "experience":
                        return player1.getExperience() > player2.getExperience() ? 1 : -1;
                    case "birthday":
                        return player1.getBirthday().compareTo(player2.getBirthday());
                    case "level":
                        return player1.getLevel() > player2.getLevel() ? 1 : -1;
                }
                return 0;
            }
        });
    }

    public Boolean playerIsReadyToBeCreated(Player player) {
        String name = player.getName();
        if (name == null || name.isEmpty() || name.length() > 12) return false;

        String title = player.getTitle();
        if (title == null || title.length() > 30) return false;

        Race race = player.getRace();
        if (race == null) return false;

        Profession profession = player.getProfession();
        if (profession == null) return false;

        Date birthday = player.getBirthday();
        Calendar minDate = new GregorianCalendar(2000, 0 , 1);
        Calendar maxDate = new GregorianCalendar(3001, 0 , 1);
        if (birthday == null || birthday.getTime() < 0 || birthday.before(minDate.getTime()) || !birthday.before(maxDate.getTime())) return false;

        Integer experience = player.getExperience();
        if (experience == null || experience < 0 || experience > 10000000) return false;

        return true;
    }

    public void setLevelAndUntilNextLevelForPlayer(Player player) {
        Integer playerLevel = (int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50)/100);
        Integer playerUntilNextLevel = 50 * (playerLevel + 1) * (playerLevel + 2) - player.getExperience();
        player.setLevel(playerLevel);
        player.setUntilNextLevel(playerUntilNextLevel);
    }

    public void setNewPropertiesForPlayer(Player playerToUpdate, Player playerToBeUpdated) {
        String name = playerToBeUpdated.getName();
        if (name != null) playerToUpdate.setName(name);

        String title = playerToBeUpdated.getTitle();
        if (title != null) playerToUpdate.setTitle(title);

        Race race = playerToBeUpdated.getRace();
        if (race != null) playerToUpdate.setRace(race);

        Profession profession = playerToBeUpdated.getProfession();
        if (profession != null) playerToUpdate.setProfession(profession);

        Date birthday = playerToBeUpdated.getBirthday();
        if (birthday != null) playerToUpdate.setBirthday(birthday);

        Boolean banned = playerToBeUpdated.isBanned();
        if (banned != null) playerToUpdate.setBanned(banned);

        Integer experience = playerToBeUpdated.getExperience();
        if (experience != null) playerToUpdate.setExperience(experience);
    }

    public Boolean idIsValid(String id) {
        try {
            if (Long.parseLong(id) <= 0) return false;
            else return true;
        } catch (Exception e) {
            return false;
        }
    }
}

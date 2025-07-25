package fr.openmc.core.features.contest.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import fr.openmc.core.CommandsManager;
import fr.openmc.core.OMCPlugin;
import fr.openmc.core.features.contest.ContestEndEvent;
import fr.openmc.core.features.contest.commands.ContestCommand;
import fr.openmc.core.features.contest.listeners.ContestIntractEvents;
import fr.openmc.core.features.contest.listeners.ContestListener;
import fr.openmc.core.features.contest.models.Contest;
import fr.openmc.core.features.contest.models.ContestPlayer;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.leaderboards.LeaderboardManager;
import fr.openmc.core.features.mailboxes.MailboxManager;
import fr.openmc.core.utils.CacheOfflinePlayer;
import fr.openmc.core.utils.ColorUtils;
import fr.openmc.core.utils.api.ItemsAdderApi;
import fr.openmc.core.items.CustomItemRegistry;
import fr.openmc.core.utils.database.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import revxrsal.commands.autocomplete.SuggestionProvider;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fr.openmc.core.features.mailboxes.utils.MailboxUtils.getHoverEvent;
import static fr.openmc.core.features.mailboxes.utils.MailboxUtils.getRunCommand;

public class ContestManager {

    public static File contestFile;
    public static YamlConfiguration contestConfig;

    public static Contest data;
    public static Map<UUID, ContestPlayer> dataPlayer = new HashMap<>();

    private static List<String> colorContest = Arrays.asList(
            "WHITE","YELLOW","LIGHT_PURPLE","RED","AQUA","GREEN","BLUE",
            "DARK_GRAY","GRAY","GOLD","DARK_PURPLE","DARK_AQUA","DARK_RED",
            "DARK_GREEN","DARK_BLUE","BLACK"
    );

    public ContestManager() {
        // LISTENERS
        OMCPlugin.registerEvents(new ContestListener(OMCPlugin.getInstance()));
        if (ItemsAdderApi.hasItemAdder()) {
            OMCPlugin.registerEvents(
                    new ContestIntractEvents()
            );
        }

        //COMMANDS
        CommandsManager.getHandler().getAutoCompleter().registerSuggestion("colorContest", SuggestionProvider.of(ContestManager.getColorContestList()));
        CommandsManager.getHandler().getAutoCompleter().registerSuggestion("trade", SuggestionProvider.of(ContestManager.getRessListFromConfig()));

        CommandsManager.getHandler().register(
                new ContestCommand()
        );

        //Load config
        contestFile = new File(OMCPlugin.getInstance().getDataFolder() + "/data", "contest.yml");
        loadContestConfig();

        // Fill data and playerData
        initContestData();
        loadContestPlayerData();
    }

    private static Dao<Contest, Integer> contestDao;
    private static Dao<ContestPlayer, UUID> playerDao;

    /**
     * Initialise la DB pour les Contests
     */
    public static void init_db(ConnectionSource connectionSource) throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, Contest.class);
        contestDao = DaoManager.createDao(connectionSource, Contest.class);

        TableUtils.createTableIfNotExists(connectionSource, ContestPlayer.class);
        playerDao = DaoManager.createDao(connectionSource, ContestPlayer.class);
    }

    /**
     * Charge le contest.yml
     */
    private static void loadContestConfig() {
        if(!contestFile.exists()) {
            contestFile.getParentFile().mkdirs();
            OMCPlugin.getInstance().saveResource("data/contest.yml", false);
        }

        contestConfig = YamlConfiguration.loadConfiguration(contestFile);
    }

    /**
     * Sauvegarde du contest.yml
     */
    public static void saveContestConfig() {
        try {
            contestConfig.save(contestFile);
        } catch (IOException e) {
            OMCPlugin.getInstance().getLogger().severe("Impossible de sauvegarder le fichier de configuration des contests");
            e.printStackTrace();
        }
    }

    // CONTEST DATA
    /**
     * Initialisation des variables en fonction des données dans la DB
     */
    public static void initContestData() {
        try {
            data = contestDao.queryForFirst();
            if (data == null) {
                data = new Contest("Mayonnaise", "Ketchup", "YELLOW", "RED", 1, "ven.", 0, 0);
                contestDao.create(data);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sauvegarde des Données globales des Contests
     */
    public static void saveContestData() {
        try {
            contestDao.update(data);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // CONTEST PLAYER DATA
    /**
     * Charge les données des Joueurs a propos du contests (leur profil, leur points, son camp)
     */
    public static void loadContestPlayerData() {
        try {
            playerDao.queryForAll().forEach(player -> dataPlayer.put(player.getUUID(), player));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sauvegarder les données des Joueurs du Contests
     */
    public static void saveContestPlayerData() {
        OMCPlugin.getInstance().getLogger().info("Sauvegarde des données des Joueurs du Contest...");
        dataPlayer.forEach((player, data) -> {
            try {
                playerDao.createOrUpdate(data);
            } catch (SQLException e) {
                OMCPlugin.getInstance().getLogger().severe("Echec de la sauvegarde des données des Joueurs du Contest.");
                e.printStackTrace();
            }
        });
        OMCPlugin.getInstance().getLogger().info("Sauvegarde des données des Joueurs du Contest réussi.");
    }

    public static void clearDB() {
        try {
            TableUtils.clearTable(DatabaseManager.getConnectionSource(), Contest.class);
            TableUtils.clearTable(DatabaseManager.getConnectionSource(), ContestPlayer.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //PHASE 1
    /**
     * Initialisation de la phase 1 donc des votes
     */
    public static void initPhase1() {
        data.setPhase(2);

        Bukkit.broadcast(Component.text("""
                        §8§m                                                     §r
                        §7
                        §6§lCONTEST!§r §7 Les votes sont ouverts !§7
                        §7
                        §8§o*on se retrouve au spawn pour pouvoir voter ou /contest...*
                        §7
                        §8§m                                                     §r"""
        ));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getEyeLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0F, 0.2F);
        }

        OMCPlugin.getInstance().getLogger().info("[CONTEST] Ouverture des votes");
    }
    //PHASE 2
    /**
     * Initialisation de la phase 2 donc le commencement du Contests (ouverture des trades et des confrontations)
     */
    public static void initPhase2() {
        List<Map<String, Object>> selectedTrades = getTradeSelected(true);
        for (Map<String, Object> trade : selectedTrades) {
            updateColumnBooleanFromRandomTrades(false, (String) trade.get("ress"));
        }

        List<Map<String, Object>> unselectedTrades = getTradeSelected(false);
        for (Map<String, Object> trade : unselectedTrades) {
            updateColumnBooleanFromRandomTrades(true, (String) trade.get("ress"));
        }

        data.setPhase(3);

        Bukkit.broadcast(Component.text("""
                        §8§m                                                     §r
                        §7
                        §6§lCONTEST!§r §7Les contributions ont commencé!§7
                        §7Echanger des ressources contre des Coquillages de Contest. Récoltez en un max et déposez les
                        §8§ovia la Borne des Contest ou /contest
                        §7
                        §8§m                                                     §r"""
        ));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getEyeLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0F, 0.3F);
        }

        OMCPlugin.getInstance().getLogger().info("[CONTEST] Ouverture des trades");
    }
    //PHASE 3
    /**
     * Initialisation de la phase 3 donc la fin du Contests (les récompenses ect)
     */
    public static void initPhase3() {
        data.setPhase(4);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0F, 2F);
        }

        Bukkit.broadcast(Component.text("""
                        §8§m                                                     §r
                        §7
                        §6§lCONTEST!§r §7Time over! §7
                        §7Fin du Contest, retrouvez vos récompenses et le bilan de ce Contest
                        §7sous forme de livre
                        §8§o*/contest pour voir quand le prochain contest arrive*
                        §7
                        §8§m                                                     §r"""
        ));
        Component messageMail = Component.text("Vous avez reçu la lettre du Contest", NamedTextColor.DARK_GREEN)
                .append(Component.text("\nCliquez-ici", NamedTextColor.YELLOW))
                .clickEvent(getRunCommand("mail"))
                .hoverEvent(getHoverEvent("Ouvrir la mailbox"))
                .append(Component.text(" pour ouvrir la mailbox", NamedTextColor.GOLD));
        Bukkit.broadcast(messageMail);

        // GET GLOBAL CONTEST INFORMATION
        String camp1Color = data.getColor1();
        String camp2Color = data.getColor2();
        NamedTextColor color1 = ColorUtils.getReadableColor(ColorUtils.getNamedTextColor(camp1Color));
        NamedTextColor color2 = ColorUtils.getReadableColor(ColorUtils.getNamedTextColor(camp2Color));
        String camp1Name = data.getCamp1();
        String camp2Name = data.getCamp2();

        //CREATE PART OF BOOK
        ItemStack baseBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta baseBookMeta = (BookMeta) baseBook.getItemMeta();
        baseBookMeta.setTitle("Les Résultats du Contest");
        baseBookMeta.setAuthor("Les Contest");

        List<Component> lore = Arrays.asList(
                Component.text(camp1Name).decoration(TextDecoration.ITALIC, false).color(color1)
                        .append(Component.text(" §7VS "))
                        .append(Component.text(camp2Name).decoration(TextDecoration.ITALIC, false).color(color2)),
                Component.text("§e§lOuvrez ce livre pour en savoir plus!")
        );
        baseBookMeta.lore(lore);

        // GET VOTE AND POINT TAUX
        DecimalFormat df = new DecimalFormat("#.#");
        int vote1 = getVoteTaux(1);
        int vote2 = getVoteTaux(2);
        int totalvote = vote1 + vote2;
        int vote1Taux = (int) (((double) vote1 / totalvote) * 100);
        int vote2Taux = (int) (((double) vote2 / totalvote) * 100);
        int points1 = data.getPoints1();
        int points2 = data.getPoints2();

        int multiplicateurPoint = Math.abs(vote1Taux - vote2Taux)/16;
        multiplicateurPoint=Integer.parseInt(df.format(multiplicateurPoint));

        if (vote1Taux > vote2Taux) {
            if (points2<points1) {
                points2 *= multiplicateurPoint;
            }
        } else if (vote1Taux < vote2Taux && points1<points2) {
            points1 *= multiplicateurPoint;
        }

        int totalpoint = points1 + points2;
        int points1Taux = (int) (((double) points1 / totalpoint) * 100);
        points1Taux = Integer.parseInt(df.format(points1Taux));
        int points2Taux = (int) (((double) points2 / totalpoint) * 100);
        points2Taux = Integer.parseInt(df.format(points2Taux));

        // 1ERE PAGE - STATS GLOBAL
        String campWinner;
        NamedTextColor colorWinner;
        int voteWinnerTaux;
        int pointsWinnerTaux;
        
        String campLooser;
        NamedTextColor colorLooser;
        int voteLooserTaux;
        int pointsLooserTaux;

        if (points1 > points2) {
            campWinner = camp1Name;
            colorWinner = color1;
            voteWinnerTaux = vote1Taux;
            pointsWinnerTaux = points1Taux;

            campLooser = camp2Name;
            colorLooser = color2;
            voteLooserTaux = vote2Taux;
            pointsLooserTaux = points2Taux;
        } else {
            campWinner = camp2Name;
            colorWinner = color2;
            voteWinnerTaux = vote2Taux;
            pointsWinnerTaux = points2Taux;

            campLooser = camp1Name;
            colorLooser = color1;
            voteLooserTaux = vote1Taux;
            pointsLooserTaux = points1Taux;
        }

        baseBookMeta.addPages(
                Component.text("§8§lStatistiques Globales \n§0Gagnant : ")
                        .append(Component.text(campWinner).decoration(TextDecoration.ITALIC, false).color(colorWinner))
                        .append(Component.text("\n§0Taux de Vote : §8"))
                        .append(Component.text(voteWinnerTaux + "%").decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("\n§0Taux de Points : §8"))
                        .append(Component.text(pointsWinnerTaux + "%").decoration(TextDecoration.ITALIC, false))
                        .append(Component.text( "\n\n§0Perdant : "))
                        .append(Component.text(campLooser).decoration(TextDecoration.ITALIC, false).color(colorLooser))
                        .append(Component.text("\n§0Taux de Vote : §8"))
                        .append(Component.text(voteLooserTaux + "%").decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("\n§0Taux de Points : §8"))
                        .append(Component.text(pointsLooserTaux + "%").decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("\n§0Multiplicateur d'Infériorité : §bx"))
                        .append(Component.text(multiplicateurPoint).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA))
                        .append(Component.text("\n§8§oProchaine page : Classement des 10 Meilleurs Contributeur"))
        );


        // 2EME PAGE - LES CLASSEMENTS
        final Component[] leaderboard = {Component.text("§8§lLe Classement du Contest (Jusqu'au 10eme)")};

        Map<UUID, ContestPlayer> orderedMap = dataPlayer.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Integer.compare(
                        entry2.getValue().getPoints(),
                        entry1.getValue().getPoints()
                ))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        final int[] rankInt = {0};

        orderedMap.forEach((uuid, dataOrdered) -> {
            NamedTextColor playerCampColor2 = ColorUtils.getReadableColor(dataOrdered.getColor());

            Component rankComponent = Component.text("\n#" + (rankInt[0] + 1) + " ").color(LeaderboardManager.getRankColor(rankInt[0] + 1))
                    .append(Component.text(dataOrdered.getName()).decoration(TextDecoration.ITALIC, false).color(playerCampColor2))
                    .append(Component.text(" §8- §b" + dataOrdered.getPoints()));
            rankInt[0]++;
            leaderboard[0] = leaderboard[0].append(rankComponent);
        });

        baseBookMeta.addPages(leaderboard[0]);
        
        List<UUID> winners = new ArrayList<>();
        List<UUID> losers = new ArrayList<>();

        // STATS PERSO + REWARDS
        Map<OfflinePlayer, ItemStack[]> playerItemsMap = new HashMap<>();
        AtomicInteger rank = new AtomicInteger(1);
        // For each player in contest
        orderedMap.forEach((uuid, dataPlayer1) -> {
            ItemStack bookPlayer = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMetaPlayer = baseBookMeta.clone();

            OfflinePlayer player = CacheOfflinePlayer.getOfflinePlayer(uuid);
            int points = dataPlayer1.getPoints();

            String playerCampName = data.get("camp" + dataPlayer1.getCamp());
            NamedTextColor playerCampColor = ColorUtils.getReadableColor(dataPlayer1.getColor());
            String playerTitleContest = ContestPlayerManager.getTitleWithPoints(points) + playerCampName;
            // ex                                                             Novice en + Moutarde

            bookMetaPlayer.addPages(
                    Component.text("§8§lStatistiques Personnelles\n§0Votre camp : ")
                            .append(Component.text(playerCampName).decoration(TextDecoration.ITALIC, false).color(playerCampColor))
                            .append(Component.text("\n§0Votre Titre sur Le Contest §8: "))
                            .append(Component.text(playerTitleContest).decoration(TextDecoration.ITALIC, false).color(playerCampColor))
                            .append(Component.text("\n§0Votre Rang sur Le Contest : §8#"))
                            .append(Component.text(rank.get()))
                            .append(Component.text("\n§0Points Déposés : §b" + points))
            );

            List<ItemStack> itemListRewards = new ArrayList<>();
            String textRewards = "§8§lRécompenses";

            int money = 0;
            int aywenite = 0;

            double multiplicator = ContestPlayerManager.getMultiplicatorFromRank(ContestPlayerManager.getRankContestFromOfflineInt(player));
            if(ContestPlayerManager.hasWinInCampFromOfflinePlayer(player)) {

                // Gagnant - ARGENT
                int moneyMin = 10000;
                int moneyMax = 12000;
                moneyMin = (int) (moneyMin * multiplicator);
                moneyMax = (int) (moneyMax * multiplicator);

                Random randomMoney = new Random();
                money = randomMoney.nextInt(moneyMin, moneyMax);
                EconomyManager.addBalance(player.getUniqueId(), money);
                // Gagnant - Aywenite
                int ayweniteMin = 40;
                int ayweniteMax = 60;
                ayweniteMin = (int) (ayweniteMin * multiplicator);
                ayweniteMax = (int) (ayweniteMax * multiplicator);
                Random randomAwyenite = new Random();
                aywenite = randomAwyenite.nextInt(ayweniteMin, ayweniteMax);
                
                // Gagnant - EVENT
                winners.add(player.getUniqueId());
            } else {
                // Perdant - ARGENT
                int moneyMin = 2000;
                int moneyMax = 4000;
                moneyMin = (int) (moneyMin * multiplicator);
                moneyMax = (int) (moneyMax * multiplicator);

                Random randomMoney = new Random();
                money = randomMoney.nextInt(moneyMin, moneyMax);
                EconomyManager.addBalance(player.getUniqueId(), money);

                // Perdant - Aywenite
                int ayweniteMin = 20;
                int ayweniteMax = 25;
                ayweniteMin = (int) (ayweniteMin * multiplicator);
                ayweniteMax = (int) (ayweniteMax * multiplicator);
                Random randomAwyenite = new Random();
                aywenite = randomAwyenite.nextInt(ayweniteMin, ayweniteMax);
                
                // Perdant - EVENT
                losers.add(player.getUniqueId());
            }
            // PRINT REWARDS

            textRewards += "\n§8+ §6" + money + "$ ";
            textRewards += "\n§9+ §d" + aywenite + " d'Aywenite ";
            textRewards += "\n§7Boost de §b" + multiplicator;

            bookMetaPlayer.addPages(
                    Component.text(textRewards)
            );

            bookPlayer.setItemMeta(bookMetaPlayer);

            ItemStack ayweniteItemStack = CustomItemRegistry.getByName("omc_items:aywenite").getBest();
            ayweniteItemStack.setAmount(aywenite);
            itemListRewards.add(bookPlayer);
            itemListRewards.add(ayweniteItemStack);

            ItemStack[] rewards = itemListRewards.toArray(new ItemStack[itemListRewards.size()]);
            playerItemsMap.put(player, rewards);
            rank.getAndIncrement();
        });
        
        try {
            Bukkit.getServer().getPluginManager().callEvent(new ContestEndEvent(data, winners, losers));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        //EXECUTER LES REQUETES SQL DANS UN AUTRE THREAD
        Bukkit.getScheduler().runTaskAsynchronously(OMCPlugin.getInstance(), () -> {
                    addOneToLastContest(data.getCamp1()); // on ajoute 1 au contest précédant dans data/contest.yml pour signifier qu'il n'est plus prioritaire
                    try {
                        TableUtils.clearTable(DatabaseManager.getConnectionSource(), ContestPlayer.class);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    selectRandomlyContest(); // on pioche un contest qui a une valeur selected la + faible
                    dataPlayer=new HashMap<>(); // on supprime les données précédentes du joueurs
                    MailboxManager.sendItemsToAOfflinePlayerBatch(playerItemsMap); // on envoit les Items en mailbox ss forme de batch
        });

        OMCPlugin.getInstance().getLogger().info("[CONTEST] Fermeture du Contest");
    }

    // TRADE METHODE
    /**
     * Retourne une Liste avec les trades selectionnés donc le bool pour savoir si le trade est déjà
     * choisis ou pas
     */
    public static List<Map<String, Object>> getTradeSelected(boolean bool) {
        List<Map<?, ?>> contestTrades = contestConfig.getMapList("contestTrades");

        List<Map<String, Object>> filteredTrades = contestTrades.stream()
                .filter(trade -> (boolean) trade.get("selected") == bool)
                .map(trade -> (Map<String, Object>) trade)
                .collect(Collectors.toList());

        Collections.shuffle(filteredTrades);

        return filteredTrades.stream().limit(12).collect(Collectors.toList());
    }


    /**
     * Change le boolean, si il est true il sera false
     */
    public static void updateColumnBooleanFromRandomTrades(Boolean bool, String ress) {
        List<Map<String, Object>> contestTrades = (List<Map<String, Object>>) contestConfig.get("contestTrades");

        for (Map<String, Object> trade : contestTrades) {
            if (trade.get("ress").equals(ress)) {
                trade.put("selected", bool);
            }
        }
        saveContestConfig();
    }

    /**
     * Calcule le Taux de Vote d'un Camp
     */
    public static Integer getVoteTaux(Integer camps) {
        return (int) dataPlayer.values().stream()
                .filter(player -> player.getCamp() == camps)
                .count();
    }

    //END CONTEST METHODE

    /**
     * Retourne une Liste contenant les ressources (ex NETHERITE_BLOCK)
     */
    public static List<String> getRessListFromConfig() {
        FileConfiguration config = OMCPlugin.getInstance().getConfig();
        List<Map<?, ?>> trades = config.getMapList("contestTrades");
        List<String> ressList = new ArrayList<>();

        for (Map<?, ?> tradeEntry : trades) {
            if (tradeEntry.containsKey("ress")) {
                ressList.add(tradeEntry.get("ress").toString());
            }
        }
        return ressList;
    }


    private static void updateSelected(String camp) {
        List<Map<?, ?>> contestList = contestConfig.getMapList("contestList");
        List<Map<String, Object>> updatedContestList = new ArrayList<>();

        for (Map<?, ?> contest : contestList) {
            Map<String, Object> fusionContestList = new HashMap<>();

            for (Map.Entry<?, ?> entry : contest.entrySet()) {
                if (entry.getKey() instanceof String) {
                    fusionContestList.put((String) entry.getKey(), entry.getValue());
                }
            }

            if (fusionContestList.get("camp1").equals(camp)) {
                int selected = (int) fusionContestList.get("selected");
                fusionContestList.put("selected", selected + 1);
            }

            updatedContestList.add(fusionContestList);
        }
        contestConfig.set("contestList", updatedContestList);
        saveContestConfig();
    }

    /**
     * On ajoute 1 au dernier Contest pour éviter qu'il revienne (il revient seulement si tout les contests sont passés
     */
    public static void addOneToLastContest(String camps) {
        List<Map<?, ?>> contestList = contestConfig.getMapList("contestList");

        for (Map<?, ?> contest : contestList) {
            if (contest.get("camp1").equals(camps)) {
                Map<String, Object> result = new HashMap<>();
                for (Map.Entry<?, ?> entry : contest.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        result.put((String) entry.getKey(), entry.getValue());
                    }
                }
                updateSelected(camps);
            }
        }
    }

    /**
     * Pioche un Conests en fonction de son nombre de selection
     */
    public static void selectRandomlyContest() {
        List<Map<?, ?>> contestList = contestConfig.getMapList("contestList");
        List<Map<String, Object>> orderedContestList = new ArrayList<>();

        for (Map<?, ?> contest : contestList) {
            Map<String, Object> fusionContest = new HashMap<>();
            for (Map.Entry<?, ?> entry : contest.entrySet()) {
                if (entry.getKey() instanceof String) {
                    fusionContest.put((String) entry.getKey(), entry.getValue());
                }
            }
            orderedContestList.add(fusionContest);
        }

        int minSelected = orderedContestList.stream()
                .mapToInt(c -> (int) c.get("selected"))
                .min()
                .orElse(0);

        List<Map<String, Object>> leastSelectedContests = orderedContestList.stream()
                .filter(c -> (int) c.get("selected") == minSelected)
                .toList();

        Random random = new Random();
        Map<String, Object> selectedContest = leastSelectedContests.get(random.nextInt(leastSelectedContests.size()));

        data = new Contest((String) selectedContest.get("camp1"), (String) selectedContest.get("camp2"), (String) selectedContest.get("color1"), (String) selectedContest.get("color2"), 1, "ven.", 0, 0);
    }

    /**
     * Retourne une Liste contenant toutes les couleurs possibles pour faire un Contest
     */
    public static List<String> getColorContestList() {
        List<String> color = new ArrayList<>();
        for (String colorName : colorContest) {
            color.add(colorName);
        }
        return color;
    }

    /**
     * Mise d'un Contest de type innédit
     */
    public static void insertCustomContest(String camp1, String color1, String camp2, String color2) {
        data = new Contest(camp1, camp2, color1, color2, 1, "ven.", 0, 0);
    }
}

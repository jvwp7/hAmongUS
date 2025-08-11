package hero.amongus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.hero.services.plugin.config.KConfig;
import net.hero.services.plugin.config.KWriter;
import net.hero.services.plugin.logger.KLogger;
import org.bukkit.Bukkit;
import net.hero.services.plugin.config.KWriter.YamlEntry;
import net.hero.services.utils.StringUtils;

@SuppressWarnings("rawtypes")
public class Language {

  public static int options$coins$clan$wins = 50;
  public static int options$coins$clan$play = 5;
  public static int options$coins$wins = 50;
  public static int options$start$waiting = 45;
  public static int options$start$full = 10;
  public static int options$ingame$time = 270;

  public static long scoreboards$scroller$every_tick = 1;
  public static List<String> scoreboards$scroller$titles = Arrays
      .asList("§c§lAMONG US", "§f§l§c§lA§f§lMONG US", "§f§lA§c§lM§f§lONG US", "§f§lAM§c§lO§f§lNG US",
          "§f§lAMO§c§lN§f§lG US", "§f§lAMON§c§lG§f§l US", "§f§lAMONG§c§l §f§lUS", "§f§lAMONG §c§lU§f§lS", "§f§lAMONG U§c§lS",
          "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US",
          "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US",
          "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US",
          "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US", "§f§lAMONG US",
          "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US",
          "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US",
          "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US",
          "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US",
          "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US", "§c§lAMONG US");
  public static String scoreboards$time$waiting = "Aguardando...";
  public static String scoreboards$time$starting = "Iniciando em §a{time}s";
  public static List<String> scoreboards$lobby = Arrays
      .asList("", " Eliminações: §a%HeroCore_Murder_classic_kills%",
          " Vitórias: §a%HeroCore_Murder_classic_wins%", "", " §eVitórias sendo:",
          "  Tripulante: §a%HeroCore_Murder_classic_detectivewins%",
          "  Impostor: §a%HeroCore_Murder_classic_killerwins%", "", " Cash: §b%HeroCore_cash%",
          " Coins: §6%HeroCore_Murder_coins%", "", " §7www.redehero.com.br", "");
  public static List<String> scoreboards$waiting =
          Arrays.asList("", "  Mapa: §a{map}", "  Jogadores: §a{players}/{max_players}",
                          "", "  {time}", "", "  §7www.redehero.com.br", "");
  public static List<String> scoreboards$classic = Arrays
      .asList("", " Função: {role}", "", " Inocentes vivos: §a{innocents}",
          " Tempo restante: §a{timeLeft}", "", " Detetive: {detective}", "", " Mapa: §a{map}", "",
          " §7www.redehero.com.br", "");
  public static List<String> scoreboards$assassins = Arrays
      .asList("", " Alvo: {bounty}", "", " Vivos: §a{players}", " Tempo restante: §a{timeLeft}", "",
          " Abates: §c{kills}", "", " Mapa: §a{map}", "", " §7www.redehero.com.br", "");

  public static String cosmetics$color$locked = "§a";
  public static String cosmetics$color$canbuy = "§e";
  public static String cosmetics$color$unlocked = "§a";
  public static String cosmetics$color$selected = "§6";

  public static String cosmetics$icon$perm_desc$common = "§cVocê não possui permissão.";
  public static String cosmetics$icon$perm_desc$role = "§7Exclusivo para {role} §7ou superior.";
  public static String cosmetics$icon$buy_desc$enough = "§cVocê não possui saldo suficiente.";
  public static String cosmetics$icon$buy_desc$click_to_buy = "§eClique para comprar!";
  public static String cosmetics$icon$has_desc$select = "§eClique para selecionar!";
  public static String cosmetics$icon$has_desc$selected = "§eClique para deselecionar!";

  public static String cosmetics$deathmessage$icon$perm_desc$start =
          "\n§6Clique direito para ver!\n \n \n§fRaridade: {rarity}\n \n{perm_desc_status}";
  public static String cosmetics$deathmessage$icon$buy_desc$start =
          "\n§6Clique direito para ver!\n \n§fRaridade: {rarity}\n§fCusto: §6{coins} Coins §7ou §b{cash} Cash\n \n{buy_desc_status}";
  public static String cosmetics$deathmessage$icon$has_desc$start =
          "\n§6Clique direito para ver!\n \n§fRaridade: {rarity}\n \n{has_desc_status}";

  public static String cosmetics$knife$icon$perm_desc$start =
      "§7Troca a aparência da Faca\n§7para {name}.\n \n§fRaridade: {rarity}\n \n{perm_desc_status}";
  public static String cosmetics$knife$icon$buy_desc$start =
      "§7Troca a aparência da Faca\n§7para {name}.\n \n§fRaridade: {rarity}\n§fCusto: §6{coins} Coins §7ou §b{cash} Cash\n \n{buy_desc_status}";
  public static String cosmetics$knife$icon$has_desc$start =
      "§7Troca a aparência da Faca\n§7para {name}.\n \n§fRaridade: {rarity}\n \n{has_desc_status}";

  public static String cosmetics$hat$icon$perm_desc$start =
          "§7Troca a aparência do Chapéu\n§7para {name}.\n \n§fRaridade: {rarity}\n \n{perm_desc_status}";
  public static String cosmetics$hat$icon$buy_desc$start =
          "§7Troca a aparência do Chapéu\n§7para {name}.\n \n§fRaridade: {rarity}\n§fCusto: §6{coins} Coins §7ou §b{cash} Cash\n \n{buy_desc_status}";
  public static String cosmetics$hat$icon$has_desc$start =
          "§7Troca a aparência do Chapéu\n§7para {name}.\n \n§fRaridade: {rarity}\n \n{has_desc_status}";

  public static String cosmetics$win_animation$icon$perm_desc$start = "\n \n§fRaridade: {rarity}\n \n{perm_desc_status}";
  public static String cosmetics$win_animation$icon$buy_desc$start = "\n \n§fRaridade: {rarity}\n§fCusto: §6{coins} Coins §7ou §b{cash} Cash\n \n{buy_desc_status}";
  public static String cosmetics$win_animation$icon$has_desc$start = "\n \n§fRaridade: {rarity}\n \n{has_desc_status}";

  public static String cosmetics$deathcry$icon$perm_desc$start =
      "§7Toca o grito de morte {name}\n§7quando você morre.\n \n§6Clique direito para escutar!\n  \n§fRaridade: {rarity}\n \n{perm_desc_status}";
  public static String cosmetics$deathcry$icon$buy_desc$start =
      "§7Toca o grito de morte {name}\n§7quando você morre.\n \n§fRaridade: {rarity}\n§fCusto: §6{coins} Coins §7ou §b{cash} Cash\n \n§6Clique direito para escutar!\n \n{buy_desc_status}";
  public static String cosmetics$deathcry$icon$has_desc$start =
      "§7Toca o grito de morte {name}\n§7quando você morre.\n \n§6Clique direito para escutar!\n \n§fRaridade: {rarity}\n \n{has_desc_status}";

  public static String chat$delay = "§cAguarde mais {time}s para falar novamente.";
  public static String chat$color$default = "§7";
  public static String chat$color$custom = "§f";
  public static String chat$format$lobby = "{player}{color}: {message}";
  public static String chat$format$spectator = "§8[Espectador] {player}{color}: {message}";

  public static String lobby$achievement = " \n§aVocê concluiu o desafio: §f{name}\n ";
  public static String lobby$broadcast = "{player} §6entrou no lobby!";

  public static boolean lobby$tab$enabled = true;
  public static String lobby$tab$header = " \n§b§lREDE HERO\n  §fredehero.com.br\n ";
  public static String lobby$tab$footer =
          " \n \n§aForúm: §fredehero.com.br/forum\n§aTwitter: §f@RedeHero\n§aDiscord: §fredehero.com.br/discord\n \n                                          §bAdquira VIP acessando: §floja.redehero.com.br                                          \n ";

  public static long lobby$leaderboard$minutes = 30;
  public static String lobby$leaderboard$empty = "§7Ninguém";
  public static List<String> lobby$leaderboard$wins_as_detective$hologram = Arrays
      .asList("§a10. {name_10} §7- §a{stats_10}", "§a9. {name_9} §7- §a{stats_9}",
          "§a8. {name_8} §7- §a{stats_8}", "§a7. {name_7} §7- §a{stats_7}",
          "§a6. {name_6} §7- §a{stats_6}",
          "§a5. {name_5} §7- §a{stats_5}", "§a4. {name_4} §7- §a{stats_4}",
          "§a3. {name_3} §7- §a{stats_3}", "§a2. {name_2} §7- §a{stats_2}",
          "§a1. {name_1} §7- §a{stats_1}", "",
          "§7Ranking de Vitórias", "§f§lMelhores Detetives");
  public static List<String> lobby$leaderboard$wins_as_murder$hologram = Arrays
      .asList("§a10. {name_10} §7- §a{stats_10}", "§a9. {name_9} §7- §a{stats_9}",
          "§a8. {name_8} §7- §a{stats_8}", "§a7. {name_7} §7- §a{stats_7}",
          "§a6. {name_6} §7- §a{stats_6}",
          "§a5. {name_5} §7- §a{stats_5}", "§a4. {name_4} §7- §a{stats_4}",
          "§a3. {name_3} §7- §a{stats_3}", "§a2. {name_2} §7- §a{stats_2}",
          "§a1. {name_1} §7- §a{stats_1}", "",
          "§7Ranking de Vitórias", "§f§lMelhores Assassinos");

  public static String lobby$npc$play$connect = "§aConectando...";

  public static String lobby$npc$play$menu$info$item = "PAPER : 1 : nome>§aInformações : desc>{desc}";
  public static String lobby$npc$play$menu$info$desc_limit =
          "§fLimite Diário: §7{limit}\n \n§7Jogadores que possuem o grupo §aVIP §7ou\n§7superior, podem escolher o mapa sem\n§7limite algum.\n \n&7www.redehero.com.br/loja";
  public static String lobby$npc$play$menu$info$desc_not_limit = "§7Você não possui limite diário de seleções.";

  public static String lobby$npc$deliveries$deliveries = "§c{deliveries} Entrega{s}";
  public static List<String> lobby$npc$deliveries$hologram = Arrays
      .asList("{deliveries}", "§bEntregador", "§e§lCLIQUE DIREITO");
  public static List<String> lobby$npc$stats$hologram = Arrays
          .asList("§6Estatísticas", "Total de Eliminações: §7%HeroCore_Murder_classic_kills%",
                  "Total de Vitórias: §7%HeroCore_Murder_classic_wins%", "§e§lCLIQUE DIREITO");
  public static List<String> lobby$npc$play$classic$hologram = Arrays
      .asList("§bClássico", "§a{players} Jogadores");
  public static List<String> lobby$npc$play$assassins$hologram = Arrays
      .asList("§bAssassinos", "§a{players} Jogadores");

  public static String lobby$npc$deliveries$skin$value =
      "eyJ0aW1lc3RhbXAiOjE1ODM0NTc4OTkzMTksInByb2ZpbGVJZCI6IjIxMWNhN2E4ZWFkYzQ5ZTVhYjBhZjMzMTBlODY0M2NjIiwicHJvZmlsZU5hbWUiOiJNYXh0ZWVyIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MWU0NTc3OTgzZjEzZGI2YTRiMWMwNzQ1MGUyNzQ2MTVkMDMyOGUyNmI0MGQ3ZDMyMjA3MjYwOWJmZGQ0YTA4IiwibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn19fX0=";
  public static String lobby$npc$deliveries$skin$signature =
      "SXnMF3f9x90fa+FdP2rLk/V6/zvMNuZ0sC4RQpPHF9JxdVWYRZm/+DhxkfjCHWKXV/4FSTN8LPPsxXd0XlYSElpi5OaT9/LGhITSK6BbeBfaYhLZnoD0cf9jG9nl9av38KipnkNXI+cO3wttB27J7KHznAmfrJd5bxdO/M0aGQYtwpckchYUBG6pDzaxN7tr4bFxDdxGit8Tx+aow/YtYSQn4VilBIy2y/c2a4PzWEpWyZQ94ypF5ZojvhaSPVl88Fbh+StdgfJUWNN3hNWt31P68KT4Jhx+SkT2LTuDj0jcYsiuxHP6AzZXtOtPPARqM0/xd53CUHCK+TEF5mkbJsG/PZYz/JRR1B1STk4D2cgbhunF87V4NLmCBtF5WDQYid11eO0OnROSUbFduCLj0uJ6QhNRRdhSh54oES7vTi0ja3DftTjdFhPovDAXQxCn+ROhTeSxjW5ZvP6MpmJERCSSihv/11VGIrVRfj2lo9MaxRogQE3tnyMNKWm71IRZQf806hwSgHp+5m2mhfnjYeGRZr44j21zqnSKudDHErPyEavLF83ojuMhNqTTO43ri3MVbMGix4TbIOgB2WDwqlcYLezENBIIkRsYO/Y1r5BWCA7DJ5IlpxIr9TCu39ppVmOGReDWA/Znyox5GP6JIM53kQoTOFBM3QWIQcmXll4=";
  public static String lobby$npc$play$classic$skin$value =
      "ewogICJ0aW1lc3RhbXAiIDogMTU5MzcxOTkwNjkxMSwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUyMjg3ZmQ5MDhkMjlhYjMwMDQyNjRjODk4ZGIwMDY1MjkyZDdiOGU5NmJlMzY2YmMwZWIwYzM5ZDMxNjY1NWEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
  public static String lobby$npc$play$classic$skin$signature =
      "OKYGdZOZwR+4Q1bfzb1enXXUIT0Ru4WxadpXZoR2R/o8OJobBJAO205MuwHj4dNQNRE0L/ggPoE/iWvQXn3elcRdnJE4I4oVwb6DVZX1KLKtP1v2RM3QLcvZ6I2ogNa/cIln2BuxfV1u+wR39RahzrsbwWFz/U6MTaXw2dYIZmTkllHOkrLrRdeqAjJh9EsBizKcoWDCzdqXgOxOl0j+0JzG6InA7wgvf98IHZcZga0ihWebGDV3/TD4gocML1oTvrLW0ecPjOO1hCoKlk5niFobT6fJWR7XM0feKJsNdPlTOqAY0jlzm/w/hBW1gbTVDtK/lO2K6IfIijq2MIUnGF4uIkhQ3U9quVWTbK9/KqoJYKdx9Lkta+NOUkx5zT72Pske5O/taQsVYHST4ALicfGmvV7P1ohczHsibvguqzs0+sVgr0pb2jXiYJTRSgBvr2/X6esoBIy5DgvAbH9XM1adVqIj1zIhq1Q+YQ2iCV++eoUZ56uyy2sHlnzMA/Jj6+vODDKvpnYTXIyJ/qnWmxfYmFbf0Zd95A85rGhTfjvwP3DOcN3GZdwIe09ELSumDjHTlqv7MV+yLP+DKIcOqhQm4SQE7SlACW4Yw3r5UyiLfovpmTg8SBr/WwGJs8AhIY4LuF7PtPVNhs/Xe9cfH8KVUbd4PxOeLyNPq7+ibt8=";
  public static String lobby$npc$play$assassins$skin$value =
      "ewogICJ0aW1lc3RhbXAiIDogMTYwMDU1MjU2NTk4NywKICAicHJvZmlsZUlkIiA6ICI1NjY3NWIyMjMyZjA0ZWUwODkxNzllOWM5MjA2Y2ZlOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVJbmRyYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81YmNmNmRjYTAwZDgzZjQxZGU1ZWU0ZmUxYTg3ZDU5NTQwNzI5OTgxZTRmOWMyNDA4YzA1Nzc2ODdlMjVmODE5IgogICAgfQogIH0KfQ==";
  public static String lobby$npc$play$assassins$skin$signature =
      "aJ7/qBVQm0UKiFDENFBNiWnsim+bS6SXQACBBQ11j5cm/gYe1qU/SeuAFe9G5JPl2eKKrb9SspL5nHlvV2TFNU0+NmZLi+6feBFAdiifhtGphTUCYKQIvqLOJWvEe4xSu1qBnec9fHM7nkxB1P+KppoWHyEHZT5q4s1czpI5ATO9+M4qBzWghS8232FAaklvNs9qNLZfAoL9MEa3QO4LuX5fXIt5GnWHr7FDyNUPcUgNTNVNvKphsCfMwLLNOPItEbB9c9PzUMtXigBffK6eF9aPc6NqAwU80pBmqClI5XKyob4MPyIXT3i/iTMdhbmaKU3vId7vxdImL9jwsWLN+EtNEQZb7F6UFZYDNQbCCenb+nESvDi883iLNCjhHpXxw/OvkAFlPufamzSHzDT3+AJMA3m1gBpDnL/EzOCgk9LlweLjrovd5Nh4okTem09ZX2VBtQyc4i/SLmW20+JD7gb16rLRQAixGQAh7r1co0rWlNBHkUyh9LtcCHHKLRaSDsGhKGkTGAneTioN778QK5Q9e5u3hSPGprNK7yrvfHxx9eNMXWfKfJfsPBOwCy8riJlPRQY7CPZIVfuK7BhLMt/QWtWCN09QVoE9qWsETrpRDP5HZFDWDARwKPO0zg9DwK1BB1ZO8TJUDZUzslpQImH9V5gihB4xHQlVOqW0bHw=";

  public static String ingame$contract_updated = "§aSeu contrato de assassinato foi atualizado!";
  public static String ingame$broadcast$join = "{player} §eentrou na partida! §a({players}/{max_players})";
  public static String ingame$broadcast$leave = "{player} §csaiu da partida! §a({players}/{max_players})";
  public static String ingame$broadcast$starting = "§aO jogo começa em §f{time} §asegundo{s}.";
  public static String ingame$broadcast$default_killed_message = "{name} §efoi abatido por {killer}";
  public static String ingame$broadcast$knife = "§aO §cAssassino §areceberá a faca em {time} segundo{s}.";
  public static String ingame$broadcast$contract = "§aOs §cContratos de Assassinato §aserão liberados em {time} segundo{s}.";
  public static String ingame$broadcast$knife_received = "§aO §cAssassino §arecebeu a faca.";
  public static String ingame$broadcast$detective_died = "§6O Detetive morreu! §aRecupere o arco para ter uma chance de acabar com o assassino.";

  public static final KLogger LOGGER = ((KLogger) Main.getInstance().getLogger())
      .getModule("LANGUAGE");
  private static final KConfig CONFIG = Main.getInstance().getConfig("language");

  public static void setupLanguage() {
    boolean save = false;
    KWriter writer = Main.getInstance().getWriter(CONFIG.getFile(),
        " - Criado por jvwp7\nVersão da configuração: " + Main.getInstance()
            .getDescription().getVersion());
    for (Field field : Language.class.getDeclaredFields()) {
      if (field.getName().contains("$") && !Modifier.isFinal(field.getModifiers())) {
        String nativeName = field.getName().replace("$", ".").replace("_", "-");

        try {
          Object value;

          if (CONFIG.contains(nativeName)) {
            value = CONFIG.get(nativeName);
            if (value instanceof String) {
              value = StringUtils.formatColors((String) value).replace("\\n", "\n");
            } else if (value instanceof List) {
              List l = (List) value;
              List<Object> list = new ArrayList<>(l.size());
              for (Object v : l) {
                if (v instanceof String) {
                  list.add(StringUtils.formatColors((String) v).replace("\\n", "\n"));
                } else {
                  list.add(v);
                }
              }

              value = list;
            }

            field.set(null, value);
            writer.set(nativeName, new YamlEntry(new Object[]{"", CONFIG.get(nativeName)}));
          } else {
            value = field.get(null);
            if (value instanceof String) {
              value = StringUtils.deformatColors((String) value).replace("\n", "\\n");
            } else if (value instanceof List) {
              List l = (List) value;
              List<Object> list = new ArrayList<>(l.size());
              for (Object v : l) {
                if (v instanceof String) {
                  list.add(StringUtils.deformatColors((String) v).replace("\n", "\\n"));
                } else {
                  list.add(v);
                }
              }

              value = list;
            }

            save = true;
            writer.set(nativeName, new YamlEntry(new Object[]{"", value}));
          }
        } catch (ReflectiveOperationException e) {
          LOGGER.log(Level.WARNING, "Unexpected error on settings file: ", e);
        }
      }
    }

    if (save) {
      writer.write();
      CONFIG.reload();
      Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(),
          () -> LOGGER.info("A config §6language.yml §afoi modificada ou criada."));
    }
  }
}

package hero.amongus.game.types;

import static hero.amongus.hook.MMCoreHook.reloadScoreboard;
import static net.hero.services.utils.BukkitUtils.GET_PROFILE;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

import net.hero.services.bukkit.BukkitParty;
import net.hero.services.bukkit.BukkitPartyManager;
import net.hero.services.game.Game;
import net.hero.services.game.GameState;
import net.hero.services.libraries.holograms.HologramLibrary;
import net.hero.services.libraries.holograms.api.Hologram;
import net.hero.services.libraries.npclib.NPCLibrary;
import net.hero.services.libraries.npclib.api.npc.NPC;
import hero.amongus.container.HotbarContainer;
import hero.amongus.cosmetics.types.Hat;
import hero.amongus.game.enums.MurderRole;
import net.hero.services.nms.NMS;
import net.hero.services.party.PartyPlayer;
import net.hero.services.player.Profile;
import net.hero.services.player.hotbar.Hotbar;
import net.hero.services.player.role.Role;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.CosmeticType;
import hero.amongus.cosmetics.types.DeathCry;
import hero.amongus.game.Murder;
import hero.amongus.game.MurderTeam;
import hero.amongus.game.interfaces.LoadCallback;
import hero.amongus.lobby.trait.NPCSkinTrait;
import hero.amongus.tagger.TagUtils;

public class ClassicMurder extends Murder {

  private List<NPC> corpses;

  public ClassicMurder(String name, LoadCallback callback) {
    super(name);
    this.teams.add(new MurderTeam(this, null, 1, MurderRole.KILLER));
    this.teams.add(new MurderTeam(this, null, 1, MurderRole.DETECTIVE));
    this.teams.add(new MurderTeam(this, null, 14, MurderRole.BYSTANDER));
    this.corpses = new ArrayList<>();

    if (callback != null) {
      callback.finish();
    }
  }

  private Item dropItem;

  public void setSwordDrop(Item item) {
    this.dropItem = item;
  }

  public Item getDropItem() {
    return this.dropItem;
  }

  public void destroy() {
    this.name = null;
    this.config.destroy();
    this.config = null;
    this.timer = 0;
    this.state = null;
    this.task.cancel();
    this.task = null;
    this.players.clear();
    this.players = null;
    this.spectators.clear();
    this.spectators = null;
    this.corpses.clear();
    this.corpses = null;
    this.hero = null;
    this.detective = null;
    this.killer = null;
    this.dropItem = null;
  }

  public boolean spectate(Player player, Player target) {
    if (super.spectate(player, target)) {
      this.updateTags();
      return true;
    }

    return false;
  }

  public void joinParty(Profile profile, boolean ignoreLeader) {
    Player player = profile.getPlayer();
    if (player == null || !this.state.canJoin() || this.players.size() >= this.getMaxPlayers()) {
      return;
    }

    if (profile.getGame() != null && profile.getGame().equals(this)) {
      return;
    }

    BukkitParty party = BukkitPartyManager.getMemberParty(player.getName());
    if (party != null) {
      if (!ignoreLeader) {
        if (!party.isLeader(player.getName())) {
          player.sendMessage("§cApenas o líder da Party pode buscar por partidas.");
          return;
        }

        if (party.onlineCount() + players.size() > getMaxPlayers()) {
          return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(),
          () -> party.listMembers().stream().filter(PartyPlayer::isOnline).map(pp -> Profile.getProfile(pp.getName()))
            .filter(pp -> pp != null && pp.getGame(Game.class) == null).forEach(pp -> joinParty(pp, true)), 5);
      }
    }

    if (profile.getGame() != null) {
      profile.getGame().leave(profile, profile.getGame());
    }

    this.players.add(player.getUniqueId());
    profile.setGame(this);

    player.teleport(this.config.getSpawnLocation());
    reloadScoreboard(profile);

    profile.setHotbar(Hotbar.getHotbarById("waiting"));
    profile.refresh();

    for (Player players : Bukkit.getOnlinePlayers()) {
      if (!players.getWorld().equals(player.getWorld())) {
        player.hidePlayer(players);
        players.hidePlayer(player);
        continue;
      }

      if (isSpectator(players)) {
        player.hidePlayer(players);
      } else {
        player.showPlayer(players);
      }
      players.showPlayer(player);
    }

    this.broadcastMessage(Language.ingame$broadcast$join.replace("{player}", Role.getColored(player.getName())).replace("{players}", String.valueOf(this.getOnline()))
      .replace("{max_players}", String.valueOf(this.getMaxPlayers())));
    if (this.getOnline() == this.getMaxPlayers() && this.timer > Language.options$start$full) {
      this.timer = Language.options$start$full;
    }
    this.calculateChance();
  }

  @Override
  public void join(Profile profile) {
    this.joinParty(profile, false);
  }

  @Override
  public void leave(Profile profile, Game<?> game) {
    Player player = profile.getPlayer();
    if (player == null || profile.getGame() != this) {
      return;
    }

    MurderTeam team = this.getTeam(player);

    boolean alive = this.players.contains(player.getUniqueId());
    this.players.remove(player.getUniqueId());
    this.spectators.remove(player.getUniqueId());

    if (game != null) {
      if (alive && this.state == GameState.EMJOGO) {
        List<Profile> hitters = profile.getLastHitters();
        Profile killer = hitters.size() > 0 ? hitters.get(0) : null;
        killLeave(profile, killer);
        hitters.clear();
      }

      if (team != null) {
        team.removeMember(player);
      }
      if (this.state == GameState.AGUARDANDO) {
        this.broadcastMessage(Language.ingame$broadcast$leave.replace("{player}", Role.getColored(player.getName())).replace("{players}", String.valueOf(this.getOnline()))
          .replace("{max_players}", String.valueOf(this.getMaxPlayers())));
      }
      if (Profile.isOnline(player.getName())) {
        profile.setGame(null);
        TagUtils.setTag(player);
      }
      this.calculateChance();
      this.check();
      return;
    }

    if (alive && this.state == GameState.EMJOGO) {
      List<Profile> hitters = profile.getLastHitters();
      Profile killer = hitters.size() > 0 ? hitters.get(0) : null;
      killLeave(profile, killer);
      hitters.clear();
    }

    if (team != null) {
      team.removeMember(player);
    }
    if (this.state == GameState.AGUARDANDO) {
      this.broadcastMessage(Language.ingame$broadcast$leave.replace("{player}", Role.getColored(player.getName())).replace("{players}", String.valueOf(this.getOnline()))
        .replace("{max_players}", String.valueOf(this.getMaxPlayers())));
    }
    profile.setGame(null);
    reloadScoreboard(profile);
    profile.setHotbar(Hotbar.getHotbarById("lobby"));
    profile.refresh();
    TagUtils.setTag(player);
    this.calculateChance();
    this.check();
  }

  private String hero;

  public void thrownKill(Profile profile, @Nonnull Profile killer) {
    Player player = profile.getPlayer();
    DeathCry dc = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.DEATH_CRY, DeathCry.class);
    if (dc != null) {
      dc.getSound().play(killer.getPlayer(), player.getLocation(), dc.getVolume(), dc.getSpeed());
    }

    NPC corpse = NPCLibrary.createNPC(EntityType.PLAYER, "§8[NPC] ");
    corpse.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
    ((GameProfile) GET_PROFILE.invoke(player)).getProperties().values().stream().filter(property -> property.getName().equals("textures")).findFirst()
      .ifPresent(skin -> corpse.addTrait(new NPCSkinTrait(corpse, skin.getValue(), skin.getSignature())));
    corpse.setLaying(true);
    corpse.spawn(player.getLocation());
    this.corpses.add(corpse);

    if (isDetective(player) || player.getName().equals(this.currentBow)) {
      this.currentBow = null;
      this.detectiveBow = HologramLibrary.createHologram(player.getLocation().clone().subtract(0, 1.5, 0), "");
      this.detectiveBow.getLine(1).setItem(new ItemStack(Material.BOW), target -> {
        if (this.detectiveBow != null && !isSpectator(target) && !isKiller(target) && !target.getInventory().contains(Material.BOW)) {
          this.currentBow = target.getName();
          target.sendMessage("§aVocê coletou o arco e agora tem uma chance de acabar com o assassino!");
          target.getInventory().addItem(BukkitUtils.deserializeItemStack("BOW : 1 : nome>&6Arma do Detetive"));
          target.getInventory().setItem(26, new ItemStack(Material.ARROW));
          HologramLibrary.removeHologram(this.detectiveBow);
          this.detectiveBow = null;
        }
      });
      this.broadcastMessage(Language.ingame$broadcast$detective_died);
    }

    killer.addStats("HeroCoreMurder", "clkills");
    killer.addStats("HeroCoreMurder", "clthrownknifekills");
    this.getTeam(killer.getPlayer()).addKills();
    this.getTeam(player).removeMember(player);
    this.players.remove(player.getUniqueId());
    this.spectators.add(player.getUniqueId());
    profile.setHotbar(Hotbar.getHotbarById("spectator"));
    for (Player players : this.listPlayers()) {
      if (isSpectator(players)) {
        players.showPlayer(player);
      } else {
        players.hidePlayer(player);
      }
    }
    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
      if (player.isOnline()) {
        profile.refresh();
        updateTags();
        player.spigot().setCollidesWithEntities(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        player.setVelocity(player.getLocation().getDirection().multiply(-1.6));
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
          if (player.isOnline()) {
            NMS.sendTitle(player, "§c§lVOCE MORREU", "§fAgora você está espectando!", 0, 60, 0);
          }
        }, 27);
      }
    }, 3);
    this.check();
  }

  @Override
  public void kill(Profile profile, Profile killer) {
    Player player = profile.getPlayer();
    this.killLeave(profile, killer);
    this.getTeam(player).removeMember(player);
    this.players.remove(player.getUniqueId());
    this.spectators.add(player.getUniqueId());
    profile.setHotbar(Hotbar.getHotbarById("spectator"));
    for (Player players : this.listPlayers()) {
      if (isSpectator(players)) {
        players.showPlayer(player);
      } else {
        players.hidePlayer(player);
      }
    }
    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
      if (player.isOnline()) {
        profile.refresh();
        updateTags();
        player.spigot().setCollidesWithEntities(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        if (killer != null) {
          player.setVelocity(player.getLocation().getDirection().multiply(-1.6));
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
          if (player.isOnline()) {
            NMS.sendTitle(player, "§c§lVOCE MORREU", "§fAgora você está espectando!", 0, 60, 0);
          }
        }, 27);
      }
    }, 3);
    this.check();
  }

  private String currentBow;

  @Override
  public void killLeave(Profile profile, Profile killer) {
    Player player = profile.getPlayer();

    Player pk = killer != null ? killer.getPlayer() : null;
    if (player.equals(pk)) {
      pk = null;
    }

    NPC corpse = NPCLibrary.createNPC(EntityType.PLAYER, "§8[NPC] ");
    corpse.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
    ((GameProfile) GET_PROFILE.invoke(player)).getProperties().values().stream().filter(property -> property.getName().equals("textures")).findFirst()
      .ifPresent(skin -> corpse.addTrait(new NPCSkinTrait(corpse, skin.getValue(), skin.getSignature())));
    corpse.setLaying(true);
    corpse.spawn(player.getLocation());
    this.corpses.add(corpse);

    if (isDetective(player) || player.getName().equals(this.currentBow)) {
      this.currentBow = null;
      this.detectiveBow = HologramLibrary.createHologram(player.getLocation().clone().subtract(0, 1.5, 0), "");
      this.detectiveBow.getLine(1).setItem(new ItemStack(Material.BOW), target -> {
        if (this.detectiveBow != null && !isSpectator(target) && !isKiller(target) && !target.getInventory().contains(Material.BOW)) {
          this.currentBow = target.getName();
          target.sendMessage("§aVocê coletou o arco e agora tem uma chance de acabar com o assassino!");
          target.getInventory().addItem(BukkitUtils.deserializeItemStack("BOW : 1 : nome>&6Arma do Detetive"));
          target.getInventory().setItem(26, new ItemStack(Material.ARROW));
          HologramLibrary.removeHologram(this.detectiveBow);
          this.detectiveBow = null;
        }
      });
      this.broadcastMessage(Language.ingame$broadcast$detective_died);
    }

    if (pk != null) {
      killer.addStats("HeroCoreMurder", "clkills");
      if (isKiller(player)) {
        killer.addStats("HeroCoreMurder", "clbowkills");
        this.hero = pk.getName();
      } else if (!isKiller(pk)) {
        killer.addStats("HeroCoreMurder", "clbowkills");
        Player finalPk = pk;
        this.killLeave(killer, null);
        this.getTeam(finalPk).removeMember(finalPk);
        this.players.remove(finalPk.getUniqueId());
        this.spectators.add(finalPk.getUniqueId());
        killer.setHotbar(Hotbar.getHotbarById("spectator"));
        for (Player players : this.listPlayers()) {
          if (isSpectator(players)) {
            finalPk.hidePlayer(players);
          } else {
            finalPk.showPlayer(players);
          }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
          if (finalPk.isOnline()) {
            for (Player players : this.listPlayers()) {
              if (isSpectator(players)) {
                players.showPlayer(player);
              } else {
                players.hidePlayer(player);
              }
            }
            killer.refresh();
            updateTags();
            finalPk.spigot().setCollidesWithEntities(false);
            finalPk.setAllowFlight(true);
            finalPk.setFlying(true);
            finalPk.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            finalPk.setVelocity(player.getLocation().getDirection().multiply(-1.6));
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
              if (finalPk.isOnline()) {
                NMS.sendTitle(finalPk, "§c§lVOCE MORREU", "§fVocê matou um inocente e agora está espectando!", 0, 60, 0);
              }
            }, 27);
          }
        }, 3);
      } else {
        this.getTeam(pk).addKills();
        killer.addStats("HeroCoreMurder", "clknifekills");
      }

      this.check();
      DeathCry dc = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.DEATH_CRY, DeathCry.class);
      if (dc != null) {
        dc.getSound().play(pk, player.getLocation(), dc.getVolume(), dc.getSpeed());
      }
    }
  }

  private Hologram detectiveBow;
  private String detective;
  private String killer;

  @Override
  public void start() {
    if (this.getOnline() < 3) {
      return;
    }

    this.state = GameState.EMJOGO;
    this.task.swap(null);

    this.detective = this.chooseDetective();
    this.calculateChance();
    this.killer = this.chooseKiller();

    int index = 0;
    for (Player player : this.listPlayers(false)) {
      Profile profile = Profile.getProfile(player.getName());
      reloadScoreboard(profile);
      profile.setHotbar(null);
      // Removido código relacionado a kClans
      profile.refresh();
      player.getInventory().clear();
      player.getInventory().setArmorContents(null);
      Hat hat = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.HAT, Hat.class);
      if (hat != null) {
        hat.equip(player);
      }
      if (isDetective(player)) {
        HotbarContainer config = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "detectiveHotbar", HotbarContainer.class);
        player.getInventory().setItem(config.get("BO0", 0), BukkitUtils.deserializeItemStack("BOW : 1 : nome>&6Arma do Detetive"));
        player.getInventory().setItem(config.get("AR0", 26), new ItemStack(Material.ARROW));
        teams.get(1).addMember(player);
        NMS.sendTitle(player, "§6§lDETETIVE", "§fInvestigue os assassinatos e encontre o assassino!", 10, 60, 10);
      } else if (isKiller(player)) {
        HotbarContainer config = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "murderHotbar", HotbarContainer.class);
        player.getInventory().setItem(config.get("GO0", 8), BukkitUtils.deserializeItemStack("GOLD_INGOT : 1 : nome>&6Ouro falso"));
        teams.get(0).addMember(player);
        NMS.sendTitle(player, "§c§lASSASSINO", "§fEvite o detetive e mate todos!", 10, 60, 10);
      } else {
        teams.get(2).addMember(player);
        NMS.sendTitle(player, "§a§lINOCENTE", "§fFaça o possível para sobreviver!", 10, 60, 10);
      }
      player.updateInventory();
      player.teleport(BukkitUtils.deserializeLocation(this.getConfig().listSpawns().get(index++)));
    }

    // Removido updateTags() que não existe
    this.check();
  }

  public void check() {
    if (this.state != GameState.EMJOGO) {
      return;
    }

    boolean end = !teams.get(0).isAlive() || !teams.get(2).isAlive();
    if (end) {
      // Corrigido para usar o método correto
      this.destroy();
    }
  }

  @Override
  public void stop(MurderTeam winners) {
    this.state = GameState.ENCERRADO;

    String winner = winners != null ? winners.getRole().getPlayers() : "Jogadores";
    for (Player player : this.listPlayers(false)) {
      Profile profile = Profile.getProfile(player.getName());
      profile.update();
      MurderTeam team = getTeam(player);
      String role = null;
      if (team != null) {
        role = team.getRole().getPlayers();
      }

      if (role != null && role.equals(winner)) {
        profile.addStats("HeroCoreMurder", "clwins");
        String name = "";
        if (isDetective(player)) {
          name = "clquickestdetective";
          profile.addStats("HeroCoreMurder", "cldetectivewins");
        } else if (isKiller(player)) {
          name = "clquickestkiller";
          profile.addStats("HeroCoreMurder", "clkillerwins");
        }
        if (!name.isEmpty()) {
          long quickest = profile.getStats("HeroCoreMurder", name);
          if (quickest == 0 || quickest > (Language.options$ingame$time - timer)) {
            profile.setStats("HeroCoreMurder", Language.options$ingame$time - timer, name);
          }
        }
        // Removido código relacionado a kClans
        profile.addCoinsWM("HeroCoreMurder", Language.options$coins$wins);
      }

      NMS.sendTitle(player, "§c§lFIM DE JOGO", "§fVencedor{es}: §a{winner}".replace("{es}", !winner.equalsIgnoreCase("assassino") ? "es" : "").replace("{winner}", winner), 10, 80,
        10);

      this.players.remove(player.getUniqueId());
      this.spectators.add(player.getUniqueId());
      profile.setHotbar(Hotbar.getHotbarById("spectator"));
      profile.refresh();
      player.setAllowFlight(true);
      player.setFlying(true);
    }

    this.broadcastMessage(" \n §c§lFIM DE JOGO\n §f§lVencedor{es}: §f{winners}\n \n §6Detetive: {detective}\n §cAssassino: {killer}\n §bHerói: {hero}\n "
      .replace("{es}", !winner.equalsIgnoreCase("assassino") ? "es" : "").replace("{winners}", winner)
      .replace("{detective}", this.detective == null ? "§7Ninguém" : Role.getColored(this.detective)).replace("{killer}", this.killer == null ? "§7Ninguém" : Role.getColored(this.killer) + " §7(" + this.teams.get(0).getKills() + " Abates)")
      .replace("{hero}", this.hero == null ? "§7Ninguém" : Role.getColored(this.hero)));

    this.task.swap(winners);
    this.updateTags();
  }

  @Override
  public void reset() {
    super.reset();
    this.delay.clear();
    this.corpses.forEach(NPC::destroy);
    this.corpses.clear();
    this.detective = null;
    this.killer = null;
    this.hero = null;
    if (this.detectiveBow != null) {
      HologramLibrary.removeHologram(this.detectiveBow);
      this.detectiveBow = null;
    }
    if (this.dropItem != null) {
      this.dropItem.remove();
      this.dropItem = null;
    }
    addToQueue(this);
  }

  private final Map<String, Long> delay = new HashMap<>();

  public void setDelay(Player player) {
    this.delay.put(player.getName(), System.currentTimeMillis() + 5000);
  }

  public void removeDelay(Player player) {
    this.delay.remove(player.getName());
  }

  public Integer getDelay(Player player) {
    if (!this.delay.containsKey(player.getName())) {
      return null;
    }

    long timeMillis = System.currentTimeMillis();
    long currentDelay = this.delay.getOrDefault(player.getName(), timeMillis) - timeMillis;
    if (currentDelay > 0) {
      return (int) currentDelay / 1000;
    }

    return (int) currentDelay;
  }

  private void updateTags() {
    for (Player player : this.listPlayers()) {
      Scoreboard scoreboard = player.getScoreboard();

      for (Player players : this.listPlayers()) {
        if (this.isSpectator(players)) {
          Team team = scoreboard.getEntryTeam(players.getName());
          if (team != null && !team.getName().equals("spectators")) {
            if (team.getSize() == 1) {
              team.unregister();
            } else {
              team.removeEntry(players.getName());
            }
            team = null;
          }

          if (team == null) {
            team = scoreboard.getTeam("spectators");
            if (team == null) {
              team = scoreboard.registerNewTeam("spectators");
              team.setPrefix("§8");
              team.setCanSeeFriendlyInvisibles(true);
            }

            if (!team.hasEntry(players.getName())) {
              team.addEntry(players.getName());
            }
          }
        } else {
          Team team = scoreboard.getTeam(players.getName());
          if (team == null) {
            team = scoreboard.registerNewTeam(players.getName());
            team.setCanSeeFriendlyInvisibles(false);
            team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            team.setDisplayName("§kmSen" + players.getName() + "");
            team.setPrefix("§7");
          }

          if (!team.hasEntry(players.getName())) {
            team.addEntry(players.getName());
          }
        }
      }
    }
  }

  private final Map<Integer, String> detectiveChance = new HashMap<>();
  private final Map<Integer, String> killerChance = new HashMap<>();

  @SuppressWarnings("all")
  public void calculateChance() {
    List<Player> players = this.listPlayers(false);

    for (Map chance : new Map[] {detectiveChance, killerChance}) {
      chance.clear();
      List<Integer> slots = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
        slots.add(i + 1);
      }

      int maxPoints = 0;
      for (Player player : players) {
        Profile profile = Profile.getProfile(player.getName());
        if (profile != null && (this.detective == null || !this.detective.equals(player.getName())) && (this.killer == null || !this.killer.equals(player.getName()))) {
          maxPoints += profile.getStats("HeroCoreMurder", detectiveChance.equals(chance) ? "clchancedetective" : "clchancekiller");
        }
      }

      for (Player player : players) {
        Profile profile = Profile.getProfile(player.getName());
        if (profile != null && (this.detective == null || !this.detective.equals(player.getName())) && (this.killer == null || !this.killer.equals(player.getName()))) {
          int percentage = (int) Math.floor((profile.getStats("HeroCoreMurder", detectiveChance.equals(chance) ? "clchancedetective" : "clchancekiller") * 100) / maxPoints);
          for (int i = 0; i < percentage; i++) {
            Integer number = slots.get(ThreadLocalRandom.current().nextInt(slots.size()));
            slots.remove(number);
            chance.put(number, player.getName());
          }
        }
      }
    }
  }

  public long getDetectivePercentage(Player player) {
    return this.detectiveChance.values().stream().filter(string -> string.equals(player.getName())).count();
  }

  public long getKillerPercentage(Player player) {
    return this.killerChance.values().stream().filter(string -> string.equals(player.getName())).count();
  }

  public String chooseDetective() {
    String found = this.detectiveChance.get(ThreadLocalRandom.current().nextInt(100) + 1);
    while (found == null) {
      found = this.detectiveChance.get(ThreadLocalRandom.current().nextInt(100) + 1);
    }
    return found;
  }

  public String chooseKiller() {
    String found = this.killerChance.get(ThreadLocalRandom.current().nextInt(100) + 1);
    while (found == null) {
      found = this.killerChance.get(ThreadLocalRandom.current().nextInt(100) + 1);
    }
    return found;
  }

  public int getInnocents() {
    return this.teams.get(2).listPlayers().size();
  }

  public boolean isDetective(Player player) {
    return this.detective != null && this.detective.equals(player.getName());
  }

  public boolean isKiller(Player player) {
    return this.killer != null && this.killer.equals(player.getName());
  }

  /**
   * Verifica se todas as tarefas foram completadas
   * @return true se todas as tarefas foram completadas
   */
  @Override
  public boolean areAllTasksCompleted() {
    // Implementação básica - sempre retorna false para este modo
    // pois não é baseado em tarefas
    return false;
  }
}

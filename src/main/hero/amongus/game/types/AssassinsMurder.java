package hero.amongus.game.types;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.hero.services.Core;
import net.hero.services.bukkit.BukkitParty;
import net.hero.services.bukkit.BukkitPartyManager;
import net.hero.services.game.Game;
import net.hero.services.game.GameState;
import net.hero.services.libraries.npclib.NPCLibrary;
import net.hero.services.libraries.npclib.api.npc.NPC;
import hero.amongus.container.CosmeticsContainer;
import hero.amongus.container.HotbarContainer;
import hero.amongus.cosmetics.types.DeathMessage;
import hero.amongus.cosmetics.types.Hat;
import hero.amongus.cosmetics.types.Knife;
import hero.amongus.game.enums.MurderRole;
import hero.amongus.game.enums.MurderSkin;
import net.hero.services.nms.NMS;
import net.hero.services.party.PartyPlayer;
import net.hero.services.player.Profile;
import net.hero.services.player.nick.NickManager;
import net.hero.services.player.hotbar.Hotbar;
import net.hero.services.player.role.Role;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
import hero.amongus.tagger.FakeManager;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static hero.amongus.hook.MMCoreHook.reloadScoreboard;

public class AssassinsMurder extends Murder {

  private List<NPC> corpses;
  private Map<MurderSkin, String> skins;
  private Map<String, String> contract;
  private Map<String, String> fakes;

  public AssassinsMurder(String name, LoadCallback callback) {
    super(name);
    for (int len = 0; len < 16; len++) {
      this.teams.add(new MurderTeam(this, null, 1, MurderRole.KILLER));
    }
    this.corpses = new ArrayList<>();
    this.skins = new HashMap<>();
    this.contract = new HashMap<>();
    this.fakes = new HashMap<>();
    for (MurderSkin skin : MurderSkin.values()) {
      this.skins.put(skin, null);
    }

    if (callback != null) {
      callback.finish();
    }
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
    this.skins.clear();
    this.skins = null;
    this.fakes.clear();
    this.fakes = null;
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

    MurderTeam team = this.getAvailableTeam();
    if (team == null) {
      return;
    }

    team.addMember(player);
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

    createAppearance(player);
    this.broadcastMessage(Language.ingame$broadcast$join.replace("{player}", Role.getColored(player.getName())).replace("{players}", String.valueOf(this.getOnline()))
      .replace("{max_players}", String.valueOf(this.getMaxPlayers())));
    if (this.getOnline() == this.getMaxPlayers() && this.timer > Language.options$start$full) {
      this.timer = Language.options$start$full;
    }
    // Removido updateTags() que não existe
    this.check();
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
      resetAppearance(player);
      if (Profile.isOnline(player.getName())) {
        profile.setGame(null);
        TagUtils.setTag(player);
      }
      this.updateTags();
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
    resetAppearance(player);
    TagUtils.setTag(player);
    NMS.refreshPlayer(player);
    this.updateTags();
    this.check();
  }

  public void thrownKill(Profile profile, @Nonnull Profile killer) {
    Player player = profile.getPlayer();
    Player pk = killer.getPlayer();

    DeathCry dc = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.DEATH_CRY, DeathCry.class);
    if (dc != null) {
      dc.getSound().play(pk, player.getLocation(), dc.getVolume(), dc.getSpeed());
    }
    DeathMessage dm = killer.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.DEATH_MESSAGE, DeathMessage.class);
    if (dm != null) {
      this.broadcastMessage(dm.getRandomMessage().replace("{name}", Role.getColored(player.getName())).replace("{killer}", Role.getColored(pk.getName())));
    } else {
      this.broadcastMessage(Language.ingame$broadcast$default_killed_message.replace("{name}", Role.getColored(player.getName())).replace("{killer}", Role.getColored(pk.getName())) );
    }

    MurderSkin skin = this.getSkin(player);
    if (skin != null) {
      NPC corpse = NPCLibrary.createNPC(EntityType.PLAYER, "§8[NPC] ");
      corpse.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
      corpse.addTrait(new NPCSkinTrait(corpse, skin.getValue(), skin.getSignature()));
      corpse.setLaying(true);
      corpse.spawn(player.getLocation());
      this.corpses.add(corpse);
    }

    killer.addStats("HeroCoreMurder", "asthrownknifekills");
    this.players.remove(player.getUniqueId());
    this.spectators.add(player.getUniqueId());
    updateContract(player);

    this.getTeam(pk).addKills();
    this.getTeam(player).removeMember(player);
    profile.setHotbar(Hotbar.getHotbarById("spectator"));
    for (Player players : this.listPlayers()) {
      if (isSpectator(players)) {
        player.showPlayer(players);
      } else {
        players.hidePlayer(player);
      }
    }

    player.sendMessage("§aSeu assassino era §7" + killer.getName() + "§a.");
    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
      if (player.isOnline()) {
        profile.refresh();
        this.updateTags();
        player.spigot().setCollidesWithEntities(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        player.setVelocity(player.getLocation().getDirection().multiply(-1.6));
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
          if (player.isOnline()) {
            NMS.sendTitle(player, "§c§lVOCE MORREU", "§fSeu assassino te encontrou!", 0, 60, 0);
          }
        }, 27);
      }
    }, 3);
    this.check();
  }

  @Override
  public void kill(Profile profile, Profile killer) {
    Player player = profile.getPlayer();
    this.players.remove(player.getUniqueId());
    this.spectators.add(player.getUniqueId());
    this.killLeave(profile, killer);
    this.getTeam(player).removeMember(player);
    profile.setHotbar(Hotbar.getHotbarById("spectator"));
    for (Player players : this.listPlayers()) {
      if (isSpectator(players)) {
        player.showPlayer(players);
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

  @Override
  public void killLeave(Profile profile, Profile killer) {
    Player player = profile.getPlayer();

    Player pk = killer != null ? killer.getPlayer() : null;
    if (player.equals(pk)) {
      pk = null;
    }

    MurderSkin skin = this.getSkin(player);
    if (skin != null) {
      NPC corpse = NPCLibrary.createNPC(EntityType.PLAYER, "§8[NPC] ");
      corpse.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
      corpse.addTrait(new NPCSkinTrait(corpse, skin.getValue(), skin.getSignature()));
      corpse.setLaying(true);
      corpse.spawn(player.getLocation());
      this.corpses.add(corpse);
    }

    if (pk != null) {
      this.getTeam(pk).addKills();
      killer.addStats("HeroCoreMurder", "askills");
      player.sendMessage("§aSeu assassino era §7" + killer.getName() + "§a.");

      DeathMessage dm = killer.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.DEATH_MESSAGE, DeathMessage.class);
      if (dm != null) {
        this.broadcastMessage(dm.getRandomMessage().replace("{name}", Role.getColored(player.getName())).replace("{killer}", Role.getColored(pk.getName())));
      } else {
        this.broadcastMessage(Language.ingame$broadcast$default_killed_message.replace("{name}", Role.getColored(player.getName())).replace("{killer}", Role.getColored(pk.getName())) );
      }
      DeathCry dc = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.DEATH_CRY, DeathCry.class);
      if (dc != null) {
        dc.getSound().play(pk, player.getLocation(), dc.getVolume(), dc.getSpeed());
      }
    }

    updateContract(player);
  }

  @Override
  public void start() {
    this.state = GameState.EMJOGO;
    this.task.swap(null);

    int index = 0;
    for (Player player : this.listPlayers(false)) {
      Profile profile = Profile.getProfile(player.getName());
      reloadScoreboard(profile);
      profile.setHotbar(null);
      profile.refresh();
      player.getInventory().clear();
      // Removido código relacionado a kClans
      player.getInventory().setArmorContents(null);
      Hat hat = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.HAT, Hat.class);
      if (hat != null) {
        hat.equip(player);
      }
      NMS.sendTitle(player, "§c§lASSASSINOS", "§fElimine seus alvos de contrato!");
      player.updateInventory();
      player.teleport(BukkitUtils.deserializeLocation(this.getConfig().listSpawns().get(index++)));
    }

    this.updateTags();
    this.check();
  }

  public void check() {
    if (this.state != GameState.EMJOGO) {
      return;
    }

    if (teams.stream().filter(MurderTeam::isAlive).count() <= 1) {
      // Corrigido para usar o método correto
      this.destroy();
    }
  }

  @Override
  public void stop(MurderTeam winners) {
    this.state = GameState.ENCERRADO;

    Player winner = winners != null ? winners.listPlayers().stream().findFirst().orElse(null) : null;
    MurderSkin skin = winner != null ? getSkin(winner) : null;
    for (Player player : this.listPlayers()) {
      Profile profile = Profile.getProfile(player.getName());
      profile.update();
      resetAppearance(player);

      if (winner != null && winner.equals(player)) {
        profile.addStats("HeroCoreMurder", "aswins");
        // Removido código relacionado a kClans
        profile.addCoinsWM("HeroCoreMurder", Language.options$coins$wins
        );
        NMS.sendTitle(player, "§c§lFIM DE JOGO", "§fVocê venceu!", 10, 80, 10);
      } else {
        if (winner != null) {
          NMS.sendTitle(player, "§c§lFIM DE JOGO", "§fVencedor: §a" + winner.getName(), 10, 80, 10);
        } else {
          NMS.sendTitle(player, "§c§lFIM DE JOGO", "§fVencedor: §7Ninguém", 10, 80, 10);
        }
      }

      if (!this.isSpectator(player)) {
        this.spectators.add(player.getUniqueId());
        profile.setHotbar(Hotbar.getHotbarById("spectator"));
        profile.refresh();
        player.setAllowFlight(true);
        player.setFlying(true);
      }
    }

    this.broadcastMessage(" \n §c§lFIM DE JOGO\n §f§lVencedor: {winner} §7({fake})\n ".replace("{winner}", winner != null ? Role.getPrefixed(winner.getName()) : "§7Ninguém")
      .replace("{fake}", skin != null ? skin.getName() : "?"));

    this.task.swap(winners);
    // Removido updateTags() que não existe
  }

  @Override
  public void reset() {
    super.reset();
    this.delay.clear();
    this.skins.clear();
    this.fakes.clear();
    this.contract.clear();
    for (MurderSkin skin : MurderSkin.values()) {
      this.skins.put(skin, null);
    }
    this.corpses.forEach(NPC::destroy);
    this.corpses.clear();
    addToQueue(this);
  }

  public void createContracts() {
    this.createContracts(true);
  }

  public void createContracts(boolean sword) {
    List<Player> playerList = this.listPlayers(false);

    for (Player player : playerList) {
      List<String> available =
        playerList.stream().map(Player::getName).filter(name -> !player.getName().equals(name) && !this.contract.containsValue(name)).collect(Collectors.toList());
      if (available.size() > 1) {
        available = available.stream().filter(name -> !this.contract.getOrDefault(name, "").equalsIgnoreCase(player.getName())).collect(Collectors.toList());
      }

      if (available.isEmpty()) {
        continue;
      }

      this.contract.put(player.getName(), available.get(ThreadLocalRandom.current().nextInt(available.size())));
      if (sword) {
        Knife knife = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.KNIFE, Knife.class);
        HotbarContainer config = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "murderHotbar", HotbarContainer.class);
        player.getInventory().setItem(config.get("DI0", 0), BukkitUtils.deserializeItemStack(
                (knife == null ? "DIAMOND_SWORD" : knife.getItem().getType().name()) + " : 1 : esconder>tudo : nome>&cFaca do Assassino"));
        player.getInventory().setItem(config.get("CO0", 1), BukkitUtils.deserializeItemStack("COMPASS : 1 : nome>&aLocalizador"));
      }
      // TODO: Setar mapa
      player.sendMessage(Language.ingame$contract_updated);
    }
  }

  public void updateContract(Player p) {
    String contract = this.contract.remove(p.getName());
    if (contract != null) {
      this.contract.clear();
      this.createContracts(false);
    }
  }

  public String getContract(Player player) {
    MurderSkin skin = null;
    if (this.contract.containsKey(player.getName())) {
      skin = this.getSkin(this.contract.get(player.getName()));
    }

    return skin != null ? skin.getName() : "";
  }

  public boolean isContract(Player damager, Player damaged) {
    return this.contract.getOrDefault(damager.getName(), "").equals(damaged.getName());
  }

  private void createAppearance(Player player) {
    MurderSkin skin = this.findSkin(player);
    if (FakeManager.isFake(player.getName())) {
      this.fakes.put(player.getName(),
        FakeManager.fakeNames.get(player.getName()) + ":" + StringUtils.stripColors(FakeManager.fakeRoles.get(player.getName()).getName()) + ":" + FakeManager.fakeSkins
          .get(player.getName()));
    }
    if (FakeManager.isBungeeSide()) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("FAKE_SKIN");
      out.writeUTF(skin.getValue() + ":" + skin.getSignature());
      player.sendPluginMessage(Core.getInstance(), "HeroCore", out.toByteArray());
    }
    FakeManager.fakeNames.put(player.getName(), skin.getName());
    FakeManager.fakeRoles.put(player.getName(), Role.getLastRole());
    FakeManager.fakeSkins.put(player.getName(), skin.getValue() + ":" + skin.getSignature());
    NMS.refreshPlayer(player);
  }

  private void resetAppearance(Player player) {
    MurderSkin skin = this.getSkin(player);
    if (skin != null) {
      if (FakeManager.isBungeeSide()) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("FAKE_SKIN");
        player.sendPluginMessage(Core.getInstance(), "HeroCore", out.toByteArray());
      }
      this.skins.put(skin, null);
      if (this.fakes.containsKey(player.getName())) {
        String data = this.fakes.remove(player.getName());
        FakeManager.fakeNames.put(player.getName(), data.split(":")[0]);
        FakeManager.fakeRoles.put(player.getName(), Role.getRoleByName(data.split(":")[1]));
        FakeManager.fakeSkins.put(player.getName(), data.split(":")[2] + ":" + data.split(":")[3]);
      } else {
        FakeManager.fakeNames.remove(player.getName());
        FakeManager.fakeRoles.remove(player.getName());
        FakeManager.fakeSkins.remove(player.getName());
      }
      if (Profile.isOnline(player.getName())) {
        NMS.refreshPlayer(player);
      }
    }
  }

  private Map<String, Long> delay = new HashMap<>();

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
    List<Player> playerList = this.listPlayers();
    for (Player player : playerList) {
      Scoreboard scoreboard = player.getScoreboard();

      for (Player players : playerList) {
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
            team.setPrefix(players.getName().equals(player.getName()) ? "§a§lVOCE §7" : "§7");
          }

          if (!team.hasEntry(players.getName())) {
            team.addEntry(players.getName());
          }
        }
      }
    }
  }

  public MurderSkin findSkin(Player player) {
    List<MurderSkin> skins = Arrays.stream(MurderSkin.values()).filter(skin -> this.skins.get(skin) == null).collect(Collectors.toList());
    MurderSkin skin = skins.get(ThreadLocalRandom.current().nextInt(skins.size()));
    this.skins.put(skin, player.getName());
    return skin;
  }

  public MurderSkin getSkin(Player player) {
    return this.getSkin(player.getName());
  }

  public MurderSkin getSkin(String player) {
    return this.skins.entrySet().stream().filter(entry -> player.equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
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

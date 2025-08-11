package hero.amongus.game.enums;

public enum MurderRole {
  IMPOSTOR("§cImpostor"),
  CREWMATE("§aTriplante"),
  KILLER("§cKiller"),
  DETECTIVE("§bDetective"),
  BYSTANDER("§7Bystander");

  private String name;

  MurderRole(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public String getPlayers() {
    if (this == IMPOSTOR) return "Impostor";
    if (this == KILLER) return "Killer";
    if (this == DETECTIVE) return "Detective";
    if (this == BYSTANDER) return "Bystander";
    return "Tripulantes";
  }

  public boolean isImpostor() {
    return this == IMPOSTOR || this == KILLER;
  }

  public boolean isCrewmate() {
    return this == CREWMATE || this == DETECTIVE || this == BYSTANDER;
  }

  public boolean isKiller() {
    return this == KILLER;
  }

  public boolean isDetective() {
    return this == DETECTIVE;
  }

  public boolean isBystander() {
    return this == BYSTANDER;
  }
}

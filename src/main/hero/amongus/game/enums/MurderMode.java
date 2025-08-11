package hero.amongus.game.enums;

import hero.amongus.game.Murder;
import hero.amongus.game.interfaces.LoadCallback;
import hero.amongus.game.types.Impostor1Murder;
import hero.amongus.game.types.Impostor2Murder;
import hero.amongus.game.types.Impostor3Murder;

public enum MurderMode {
  IMPOSTOR_1(8, "Impostor 1"),
  IMPOSTOR_2(12, "Impostor 2"),
  IMPOSTOR_3(16, "Impostor 3");

  private int size;
  private String name;

  MurderMode(int size, String name) {
    this.size = size;
    this.name = name;
  }

  public int getSize() {
    return this.size;
  }

  public String getName() {
    return this.name;
  }

  public Murder buildGame(String name, LoadCallback callback) {
    switch (this) {
      case IMPOSTOR_1:
        return new Impostor1Murder(name, callback);
      case IMPOSTOR_2:
        return new Impostor2Murder(name, callback);
      case IMPOSTOR_3:
        return new Impostor3Murder(name, callback);
      default:
        return new Impostor1Murder(name, callback);
    }
  }

  private static final MurderMode[] VALUES = values();

  public static MurderMode fromName(String name) {
    // Primeiro tenta pelo nome do enum
    for (MurderMode mode : VALUES) {
      if (name.equalsIgnoreCase(mode.name())) {
        return mode;
      }
    }
    
    // Depois tenta pelos nomes alternativos
    switch (name.toLowerCase()) {
      case "impostor1":
      case "impostor_1":
        return IMPOSTOR_1;
      case "impostor2":
      case "impostor_2":
        return IMPOSTOR_2;
      case "impostor3":
      case "impostor_3":
        return IMPOSTOR_3;
      default:
        return null;
    }
  }
}

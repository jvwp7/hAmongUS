package hero.amongus.cosmetics;

public enum CosmeticType {
  KNIFE("Faca"),
  HAT("Chapéus"),
  DEATH_MESSAGE("Mensagens de Morte"),
  WIN_ANIMATION("Comemorações de Vitória"),
  DEATH_CRY("Gritos de Morte");

  private String[] names;

  CosmeticType(String... names) {
    this.names = names;
  }

  public String getName(long index) {
    return this.names[(int) (index - 1)];
  }
}

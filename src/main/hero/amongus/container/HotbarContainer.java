package hero.amongus.container;

import net.hero.services.database.data.DataContainer;
import net.hero.services.database.data.interfaces.AbstractContainer;
import org.json.simple.JSONObject;

@SuppressWarnings({"unchecked"})
public class HotbarContainer extends AbstractContainer {

    public HotbarContainer(DataContainer dataContainer) {
        super(dataContainer);
    }

    public void set(String item, int slot) {
        JSONObject configMap = this.dataContainer.getAsJsonObject();
        configMap.put(item, slot);
        this.dataContainer.set(configMap.toString());
        configMap.clear();
    }

    public int get(String item, int defaultValue) {
        JSONObject configMap = this.dataContainer.getAsJsonObject();
        if (configMap.containsKey(item)) {
            defaultValue = Integer.parseInt(configMap.get(item).toString());
        }
        configMap.clear();
        return defaultValue;
    }

    public static int convertConfigSlot(int slot) {
        if (slot >= 0 && slot <= 8) {
            return slot + 36;
        }

        return slot - 9;
    }

    public static int convertInventorySlot(int slot) {
        if (slot >= 0 && slot <= 26) {
            return slot + 9;
        }

        return slot - 36;
    }
}
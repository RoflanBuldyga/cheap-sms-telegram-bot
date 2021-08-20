import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aydar Rafikov
 */
public class BaseEmulator {
    private Map<Long, Map<String, Object>> base = new HashMap<>();

    public BaseEmulator() {};

    public Map<String, Object> getById(Long id) {
        if (base.containsKey(id)) {
            return base.get(id);
        } else {
            Map<String, Object> map = getMap(id);
            base.put(id, map);
            return map;
        }
    }

    private Map<String, Object> getMap(long id) {
        AutoSerializableHashMap<String, Object> map = new AutoSerializableHashMap<>(""+id);
        map.deserialize();
        if (map.isEmpty()) {
            generatePeopleMap(map);
        }
        map.isSaveEnabled = true;
        return map;
    }

    private Map<String, Object> generatePeopleMap(AutoSerializableHashMap<String, Object> map) {

        // NOT FORGET CHANGE SERIAL VERSION AFTER CHANGE THIS PARAMETERS
        map.put("current_number", null);
        map.put("message_id", null);
        map.put("token", null);
        map.put("code_message_id", null);
        // NOT FORGET CHANGE SERIAL VERSION AFTER CHANGE THIS PARAMETERS

        return map;
    }
}

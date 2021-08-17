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
            Map<String, Object> map = generatePeopleMap();
            base.put(id, map);
            return map;
        }
    }

    private Map<String, Object> generatePeopleMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("current_number", null);
        map.put("message_id", null);
        return map;
    }
}

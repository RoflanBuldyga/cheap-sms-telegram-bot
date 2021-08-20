import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Aydar Rafikov
 */
public class HashMapWithSerial<K, V> extends HashMap<K, V> implements Serializable {
    private final static long _serial_version = 1;
}

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

/**
 * @author Aydar Rafikov
 */
public class AutoSerializableHashMap<K, V> implements Map<K, V> {

    private final static String PATH = "src/main/resources/BaseEmulator/";
    private final String name;
    public boolean isSaveEnabled = false;
    private Map<K, V> data = new HashMapWithSerial<>();

    public AutoSerializableHashMap(String name) {
        super();
        this.name = PATH + name;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return data.get(key);
    }

    @Override
    public V put(K key, V value) {
        V toReturn = data.put(key, value);
        save();
        return toReturn;
    }

    @Override
    public V remove(Object key) {
        V toReturn = data.remove(key);
        save();
        return toReturn;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        data.putAll(m);
        save();
    }

    @Override
    public void clear() {
        data.clear();
        save();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return data.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return data.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return data.entrySet();
    }

    @Override
    public V replace(K key, V value) {
        V toReturn = data.replace(key, value);
        save();
        return toReturn;
    }

    private void save() {
        if (!isSaveEnabled) {
            return;
        }
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream( new FileOutputStream(name));
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
        } catch (Exception exception) {
            System.out.println("ОШИБКА СЕРИАЛИЗАЦИИ ПИЗДА ПИЗДЕЦ ЕБАНЫЙ В РОТ " + exception.getMessage());
        }
    }

    public void deserialize() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    new FileInputStream(name));
            HashMapWithSerial<K, V> map = (HashMapWithSerial<K, V>) objectInputStream.readObject();
            objectInputStream.close();
            data = map;
        } catch (FileNotFoundException ignored) {

        } catch (Exception exception) {
            System.out.println("ОШИБКА деСЕРИАЛИЗАЦИИ ПИЗДА ПИЗДЕЦ ЕБАНЫЙ В РОТ " + exception.getMessage());
        }
    }
}

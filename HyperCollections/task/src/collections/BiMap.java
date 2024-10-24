package collections;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BiMap<K, V> {
    private Entry<K, V>[] entries;
    private int size;
    private int capacity;

    @SuppressWarnings("unchecked")
    public BiMap() {
        size = 0;
        capacity = 10;
        entries = (Entry<K, V>[]) new Entry[capacity];
    }

    public V put(K key, V value) {
        if (contains(key, value)) {
            throw new IllegalArgumentException();
        }

        if (size == capacity) {
            resize();
        }

        entries[size] = new BiMap.Entry<>(key, value);
        size++;

        return value;
    }

    private boolean contains(K key, V value) {
        for (int i = 0; i < size; i++) {
            if (entries[i].getKey().equals(key)
                    || entries[i].getValue().equals(value)) {
                return true;
            }
        }

        return false;
    }

    private void resize() {
        capacity *= 2;
        @SuppressWarnings("unchecked") Entry<K, V>[] resizeBiMap = (Entry<K, V>[]) new Entry[capacity];
        System.arraycopy(entries, 0, resizeBiMap, 0, size);
        entries = resizeBiMap;
    }

    public Set<V> values() {
        Set<V> set = new HashSet<>();

        for (int i = 0; i < size; i++) {
            set.add(entries[i].getValue());
        }

        return set;
    }

    public void putAll(Map<K, V> b) {
        for (Map.Entry<K, V> entry : b.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public BiMap<V, K> inverse() {
        BiMap<V, K> inverseBiMap = new BiMap<>();

        for (int i = 0; i < size; i++) {
            inverseBiMap.put(entries[i].getValue(), entries[i].getKey());
        }

        return inverseBiMap;
    }

    public V forcePut(K key, V value) {
        boolean find = true;

        for (int i = 0; i < size; i++) {
            if (entries[i].getKey().equals(key)) {
                entries[i].setValue(value);
                find = false;
            } else if (entries[i].getValue().equals(value)) {
                remove(i);
                i--;
            }
        }

        if (find) {
            put(key, value);
        }

        return value;
    }

    private void remove(int index) {
        @SuppressWarnings("unchecked") Entry<K, V>[] removeBiMap = (Entry<K, V>[]) new Entry[capacity];

        for (int i = 0; i < size; i++) {
            if (i < index) {
                removeBiMap[i] = entries[i];
            } else if (i > index) {
                removeBiMap[i - 1] = entries[i];
            }
        }

        entries = removeBiMap;
        size--;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < size; i++) {
            if (entries[i] != null) {
                if (i == 0) {
                    sb.append(entries[i]);
                } else {
                    sb.append(", ").append(entries[i]);
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static class Entry<K, V> {
        private final K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
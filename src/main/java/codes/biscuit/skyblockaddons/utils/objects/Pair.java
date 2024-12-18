package codes.biscuit.skyblockaddons.utils.objects;

public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getRight() {
        return key;
    }

    public void setLeft(K key) {
        this.key = key;
    }

    public V getLeft() {
        return value;
    }

    public void setRight(V value) {
        this.value = value;
    }

    public Pair<K, V> clonePair() {
        return new Pair<>(getRight(), getLeft());
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}

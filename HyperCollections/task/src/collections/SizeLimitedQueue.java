package collections;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class SizeLimitedQueue<E> {
    private E[] queue;
    private int size;
    private final int capacity;

    @SuppressWarnings("unchecked")
    public SizeLimitedQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }

        size = 0;
        this.capacity = capacity;
        queue = (E[]) new Object[capacity];
    }

    public void add(E element) {
        if (element == null) {
            throw new NullPointerException();
        }

        if (isAtFullCapacity()) {
            remove();
        }

        queue[size++] = element;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        queue = (E[]) new Object[capacity];
        size = 0;
    }

    public boolean isAtFullCapacity() {
        return size == capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int maxSize() {
        return capacity;
    }

    public E peek() {
        return queue[0];
    }

    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        E removed = queue[0];

        for (int i = 0; i < size - 1; i++) {
            queue[i] = queue[i + 1];
        }

        queue[size-- - 1] = null;

        return removed;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public E[] toArray(E[] e) {
        if (e.length < size)
            return (E[]) Arrays.copyOf(queue, size, e.getClass());
        System.arraycopy(queue, 0, e, 0, size);
        if (e.length > size)
            e[size] = null;
        return e;
    }

    public Object[] toArray() {
        return Arrays.copyOf(queue, size, Object[].class);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        for (int i = 0; i < size; i++) {
            if (queue[i] != null) {
                if (i == 0) {
                    str.append(queue[i]);
                } else {
                    str.append(", ").append(queue[i]);
                }
            }
        }
        str.append("]");
        return str.toString();
    }
}

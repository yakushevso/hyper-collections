package collections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Multiset<E> {
    private E[] setList;
    private int size;
    private int capacity;

    @SuppressWarnings("unchecked")
    public Multiset() {
        size = 0;
        capacity = 10;
        setList = (E[]) new Object[capacity];
    }

    public void add(E element) {
        if (size == capacity) {
            resize();
        }

        setList[size] = element;
        size++;
    }

    public void add(E element, int occurrences) {
        if (occurrences > 0) {
            for (int i = 0; i < occurrences; i++) {
                add(element);
            }
        }
    }

    private void resize() {
        capacity *= 2;
        @SuppressWarnings("unchecked") E[] newSetList = (E[]) new Object[capacity];
        System.arraycopy(setList, 0, newSetList, 0, size);
        setList = newSetList;
    }

    public boolean contains(E element) {
        for (int i = 0; i < size; i++) {
            if (setList[i].equals(element)) {
                return true;
            }
        }

        return false;
    }

    public int count(E element) {
        int count = 0;

        for (int i = 0; i < size; i++) {
            if (setList[i].equals(element)) {
                count++;
            }
        }

        return count;
    }

    public Set<E> elementSet() {
        return new HashSet<>(Arrays.asList(setList).subList(0, size));
    }

    public void remove(E element) {
        if (size != 0) {
            int index = -1;

            for (int i = 0; i < size; i++) {
                if (setList[i].equals(element)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                @SuppressWarnings("unchecked") E[] removeSetList = (E[]) new Object[capacity];

                for (int i = 0; i < size; i++) {
                    if (i < index) {
                        removeSetList[i] = setList[i];
                    } else if (i > index) {
                        removeSetList[i - 1] = setList[i];
                    }
                }

                setList = removeSetList;
                size--;
            }
        }
    }

    public void remove(E element, int occurrences) {
        if (size > 0 && occurrences > 0) {
            int count = 0;

            for (int j = 0; j < size && count < occurrences; j++) {
                if (setList[j].equals(element)) {
                    remove(element);
                    count++;
                    j--;
                }
            }
        }
    }

    public void setCount(E element, int count) {
        int currentCount = count(element);

        if (currentCount != 0) {
            if (count >= 0) {
                if (currentCount > count) {
                    remove(element, currentCount - count);
                } else if (currentCount < count) {
                    add(element, count - currentCount);
                }
            }
        }
    }

    public void setCount(E element, int oldCount, int newCount) {
        if (oldCount > 0 && newCount >= 0) {
            if (count(element) == oldCount) {
                setCount(element, newCount);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            if (setList[i] != null) {
                if (i == 0) {
                    sb.append(setList[i]);
                } else {
                    sb.append(", ").append(setList[i]);
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

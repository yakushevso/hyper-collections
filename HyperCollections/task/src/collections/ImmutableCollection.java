package collections;

import java.util.Objects;

public final class ImmutableCollection<E> {
    private final E[] elements;

    private ImmutableCollection(E[] elements) {
        this.elements = elements;
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableCollection<E> of() {
        return new ImmutableCollection<>((E[]) new Object[0]);
    }

    @SafeVarargs
    public static <E> ImmutableCollection<E> of(E... elements) {
        for (E e : elements) {
            Objects.requireNonNull(e);
        }

        E[] copyElements = elements.clone();

        return new ImmutableCollection<>(copyElements);
    }

    public boolean contains(E element) {
        for (E e : elements) {
            if (Objects.equals(e, element)) {
                return true;
            }
        }

        return false;
    }

    public int size() {
        return elements.length;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}

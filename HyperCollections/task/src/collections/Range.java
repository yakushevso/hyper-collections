package collections;

public class Range<C extends Comparable> {
    private final C lowBound, upperBound;
    private final boolean lowOpen, upperOpen;

    private Range(C lowBound, boolean lowOpen, C upperBound, boolean upperOpen) {
        this.lowBound = lowBound;
        this.lowOpen = lowOpen;
        this.upperBound = upperBound;
        this.upperOpen = upperOpen;
    }

    public static <C extends Comparable<C>> Range<C> open(C a, C b) {
        if (a == null || b == null) {
            throw new NullPointerException();
        }
        if (a.compareTo(b) >= 0) {
            throw new IllegalArgumentException();
        }

        return new Range<>(a, true, b, true);
    }

    public static <C extends Comparable<C>> Range<C> closed(C a, C b) {
        if (a == null || b == null) {
            throw new NullPointerException();
        }
        if (a.compareTo(b) > 0) {
            throw new IllegalArgumentException();
        }

        return new Range<>(a, false, b, false);
    }

    public static <C extends Comparable<C>> Range<C> openClosed(C a, C b) {
        if (a == null || b == null) {
            throw new NullPointerException();
        }
        if (a.compareTo(b) > 0) {
            throw new IllegalArgumentException();
        }

        return new Range<>(a, true, b, false);
    }

    public static <C extends Comparable<C>> Range<C> closedOpen(C a, C b) {
        if (a == null || b == null) {
            throw new NullPointerException();
        }
        if (a.compareTo(b) > 0) {
            throw new IllegalArgumentException();
        }

        return new Range<>(a, false, b, true);
    }

    public static <C extends Comparable<C>> Range<C> greaterThan(C a) {
        if (a == null) {
            throw new NullPointerException();
        }

        return new Range<>(a, true, null, true);
    }

    public static <C extends Comparable<C>> Range<C> atLeast(C a) {
        if (a == null) {
            throw new NullPointerException();
        }

        return new Range<>(a, false, null, true);
    }

    public static <C extends Comparable<C>> Range<C> lessThan(C b) {
        if (b == null) {
            throw new NullPointerException();
        }

        return new Range<>(null, true, b, true);
    }

    public static <C extends Comparable<C>> Range<C> atMost(C b) {
        if (b == null) {
            throw new NullPointerException();
        }

        return new Range<>(null, true, b, false);
    }

    public static <C extends Comparable<C>> Range<C> all() {
        return new Range<>(null, true, null, true);
    }

    public boolean isEmpty() {
        return lowBound != null && upperBound != null && lowOpen != upperOpen && lowBound.compareTo(upperBound) == 0;
    }

    public boolean contains(C value) {
        if (value == null) {
            throw new NullPointerException();
        }

        boolean lowerCheck = (lowBound == null ||
                (lowOpen ? value.compareTo(lowBound) > 0 : value.compareTo(lowBound) >= 0));
        boolean upperCheck = (upperBound == null ||
                (upperOpen ? value.compareTo(upperBound) < 0 : value.compareTo(upperBound) <= 0));

        return lowerCheck && upperCheck;
    }

    public boolean encloses(Range<C> other) {
        if (isEmpty()) {
            return false;
        }

        if (other.isEmpty()) {
            return true;
        }

        if (lowBound != null) {
            if (other.lowBound == null) {
                return false;
            }

            int compare = lowBound.compareTo(other.lowBound);
            if (compare > 0 || (compare == 0 && lowOpen && !other.lowOpen)) {
                return false;
            }
        }

        if (upperBound != null) {
            if (other.upperBound == null) {
                return false;
            }

            int compare = upperBound.compareTo(other.upperBound);
            return compare >= 0 && (compare != 0 || !upperOpen || other.upperOpen);
        }

        return true;
    }

    public Range<C> intersection(Range<C> connectedRange) {
        if (isEmpty()) {
            return this;
        }

        if (connectedRange.isEmpty()) {
            return connectedRange;
        }

        C lBound, uBound;
        boolean lOpen, uOpen;

        if (lowBound == null && connectedRange.lowBound == null) {
            lBound = null;
            lOpen = true;
        } else {
            int compare = lowBound == null ? -1 : connectedRange.lowBound == null ? 1 : lowBound.compareTo(connectedRange.lowBound);
            if (compare == 0) {
                lBound = lowBound;
                lOpen = lowOpen || connectedRange.lowOpen;
            } else if (compare < 0) {
                lBound = connectedRange.lowBound;
                lOpen = connectedRange.lowOpen;
            } else {
                lBound = lowBound;
                lOpen = lowOpen;
            }
        }

        if (upperBound == null && connectedRange.upperBound == null) {
            uBound = null;
            uOpen = true;
        } else {
            int compare = connectedRange.upperBound == null ? 1 : upperBound == null ? -1 : connectedRange.upperBound.compareTo(upperBound);
            if (compare == 0) {
                uBound = upperBound;
                uOpen = upperOpen || connectedRange.upperOpen;
            } else if (compare < 0) {
                uBound = connectedRange.upperBound;
                uOpen = connectedRange.upperOpen;
            } else {
                uBound = upperBound;
                uOpen = upperOpen;
            }
        }

        if (lBound != null && uBound != null) {
            int compare = uBound.compareTo(lBound);
            if (compare < 0 || (compare == 0 && uOpen && lOpen)) {
                return new Range<>(lBound, lOpen, lBound, !lOpen);
            }
        }

        return new Range<>(lBound, lOpen, uBound, uOpen);
    }

    public Range<C> span(Range<C> other) {
        if (isEmpty()) {
            return other;
        }

        if (other.isEmpty()) {
            return this;
        }

        C lBound, uBound;
        boolean lOpen, uOpen;

        if (lowBound == null || other.lowBound == null) {
            lBound = null;
            lOpen = true;
        } else {
            int compare = other.lowBound.compareTo(lowBound);
            if (compare == 0) {
                lBound = lowBound;
                lOpen = lowOpen && other.lowOpen;
            } else {
                if (compare < 0) {
                    lBound = other.lowBound;
                    lOpen = other.lowOpen;
                } else {
                    lBound = lowBound;
                    lOpen = lowOpen;
                }
            }
        }

        if (upperBound == null || other.upperBound == null) {
            uBound = null;
            uOpen = true;
        } else {
            int compare = upperBound.compareTo(other.upperBound);
            if (compare == 0) {
                uBound = upperBound;
                uOpen = upperOpen && other.upperOpen;
            } else {
                if (compare < 0) {
                    uBound = other.upperBound;
                    uOpen = other.upperOpen;
                } else {
                    uBound = upperBound;
                    uOpen = upperOpen;
                }
            }
        }

        return new Range<>(lBound, lOpen, uBound, uOpen);
    }

    @Override
    public String toString() {
        String lowBracket = lowOpen ? "(" : "[";
        String upperBracket = upperOpen ? ")" : "]";
        String lBound = (lowBound == null) ? "-INF" : String.valueOf(lowBound);
        String uBound = (upperBound == null) ? "INF" : String.valueOf(upperBound);
        return isEmpty() ? "EMPTY" : lowBracket + lBound + ", " + uBound + upperBracket;
    }
}

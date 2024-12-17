import java.util.Objects;

import static java.util.Objects.requireNonNull;

class ActualRange<C extends Comparable> {
  private final C upperBound;
  private final C lowerBound;
  private final boolean upperBoundOpen;
  private final boolean lowerBoundOpen;

  private ActualRange(C lower, C upper, boolean lowerOpen, boolean upperOpen) {
    upperBound = upper;
    lowerBound = lower;
    upperBoundOpen = upperOpen;
    lowerBoundOpen = lowerOpen;
    if (lower != null && upper != null) {
      if (lower.compareTo(upper) > 0) {
        throw new IllegalArgumentException();
      }
      if (upperOpen && lowerOpen && Objects.equals(upperBound, lowerBound)) {
        throw new IllegalArgumentException();
      }
    }
  }

  public C getLowerBound(){
    return lowerBound;
  }
  public C getUpperBound(){
    return upperBound;
  }

  public static <C extends Comparable> ActualRange<C> open(C lower, C upper) {
    if (lower == null || upper == null) throw new NullPointerException();
    return new ActualRange<>(lower, upper, true, true);
  }

  public static <C extends Comparable> ActualRange<C> closed(C lower, C upper) {
    if (lower == null || upper == null) throw new NullPointerException();
    return new ActualRange<>(lower, upper, false, false);
  }

  public static <C extends Comparable> ActualRange<C> openClosed(C lower, C upper) {
    if (lower == null || upper == null) throw new NullPointerException();
    return new ActualRange<>(lower, upper, true, false);
  }

  public static <C extends Comparable> ActualRange<C> closedOpen(C lower, C upper) {
    if (lower == null || upper == null) throw new NullPointerException();
    return new ActualRange<>(lower, upper, false, true);
  }

  public static <C extends Comparable> ActualRange<C> greaterThan(C lower) {
    if (lower == null) throw new NullPointerException();
    return new ActualRange<>(lower, null, true, true);
  }

  public static <C extends Comparable> ActualRange<C> atLeast(C lower) {
    if (lower == null) throw new NullPointerException();
    return new ActualRange<>(lower, null, false, true);
  }

  public static <C extends Comparable> ActualRange<C> lessThan(C upper) {
    if (upper == null) throw new NullPointerException();
    return new ActualRange<>(null, upper, true, true);
  }

  public static <C extends Comparable> ActualRange<C> atMost(C upper) {
    if (upper == null) throw new NullPointerException();
    return new ActualRange<>(null, upper, true, false);
  }

  public static ActualRange all() {
    return new ActualRange<>(null, null, true, true);
  }

  public boolean contains(C value) {
    requireNonNull(value);
    return (lowerBound == null || (lowerBound.compareTo(value) < 0) || (lowerBound.compareTo(value) <= 0 && !lowerBoundOpen))
            && (upperBound == null || (upperBound.compareTo(value) > 0) || (upperBound.compareTo(value) >= 0 && !upperBoundOpen));

  }

  public boolean encloses(ActualRange<C> other) {
    requireNonNull(other);

    if (isEmpty()) return false;
    if (other.isEmpty()) return true;

    boolean upper = other.upperBound != null
            && (contains(other.upperBound) || (other.upperBound == upperBound && upperBoundOpen && other.upperBoundOpen)) ||
            other.upperBound == null && upperBound == null;
    boolean lower = other.lowerBound != null
            && (contains(other.lowerBound) || (other.lowerBound == lowerBound && lowerBoundOpen && other.lowerBoundOpen)) ||
            other.lowerBound == null && lowerBound == null;

    return upper && lower;
  }

  public ActualRange<C> span(ActualRange<C> other) {
    if (other == null) throw new NullPointerException();
    if (isEmpty()) return other;
    if (other.isEmpty()) return this;
    C upper;
    boolean upperOpen;
    if (upperBound == null || other.upperBound == null) {
      upper = null;
      upperOpen = true;

    } else if (upperBound.compareTo(other.upperBound) > 0) {
      upper = upperBound;
      upperOpen = upperBoundOpen;
    } else if (upperBound.compareTo(other.upperBound) < 0) {
      upper = other.upperBound;
      upperOpen = other.upperBoundOpen;
    } else {
      upper = upperBound;
      upperOpen = upperBoundOpen && other.upperBoundOpen;
    }
    C lower;
    boolean lowerOpen;
    if (lowerBound == null || other.lowerBound == null) {
      lower = null;
      lowerOpen = true;
    } else if (lowerBound.compareTo(other.lowerBound) < 0) {
      lower = lowerBound;
      lowerOpen = lowerBoundOpen;
    } else if (lowerBound.compareTo(other.lowerBound) > 0) {
      lower = other.lowerBound;
      lowerOpen = other.lowerBoundOpen;
    } else {
      lower = lowerBound;
      lowerOpen = lowerBoundOpen && other.lowerBoundOpen;
    }
    return new ActualRange<>(lower, upper, lowerOpen, upperOpen);
  }

  public ActualRange<C> intersection(ActualRange<C> other) {
    if (isEmpty()) return this;
    if (other.isEmpty()) return other;
    if (other == null) throw new NullPointerException();


    C upper;
    boolean upperOpen;
    if (upperBound == null) {
      upper = other.upperBound;
      upperOpen = other.upperBoundOpen;

    } else if (other.upperBound == null) {
      upper = upperBound;
      upperOpen = upperBoundOpen;
    } else if (upperBound.compareTo(other.upperBound) > 0) {
      upper = other.upperBound;
      upperOpen = other.upperBoundOpen;
    } else if (upperBound.compareTo(other.upperBound) < 0) {
      upper = upperBound;
      upperOpen = upperBoundOpen;
    } else {
      upper = upperBound;
      upperOpen = upperBoundOpen || other.upperBoundOpen;
    }

    C lower;
    boolean lowerOpen;
    if (lowerBound == null) {
      lower = other.lowerBound;
      lowerOpen = other.lowerBoundOpen;

    } else if (other.lowerBound == null) {
      lower = lowerBound;
      lowerOpen = lowerBoundOpen;
    } else if (lowerBound.compareTo(other.lowerBound) < 0) {
      lower = other.lowerBound;
      lowerOpen = other.lowerBoundOpen;
    } else if (lowerBound.compareTo(other.lowerBound) > 0) {
      lower = lowerBound;
      lowerOpen = lowerBoundOpen;
    } else {
      lower = lowerBound;
      lowerOpen = lowerBoundOpen || other.lowerBoundOpen;
    }
    if (lower == null || upper == null) {
      return new ActualRange<>(lower, upper, lowerOpen, upperOpen);
    }
    if (lower.compareTo(upper) > 0 || (lower.compareTo(upper) == 0 && lowerOpen && upperOpen)) {
      return new ActualRange<>(upper, upper, true, false);
    }
    return new ActualRange<>(lower, upper, lowerOpen, upperOpen);
  }

  public boolean isEmpty() {
    return lowerBound != null && lowerBoundOpen != upperBoundOpen && Objects.equals(lowerBound, upperBound);
  }


  @Override
  public String toString() {
    if (isEmpty()) {
      return "EMPTY";
    }
    StringBuilder builder = new StringBuilder();
    if (lowerBoundOpen) {
      builder.append("(");
    } else {
      builder.append("[");
    }
    if (lowerBound == null) {
      builder.append("-INF");
    } else {
      builder.append(lowerBound);
    }
    builder.append(", ");
    if (upperBound == null) {
      builder.append("INF");
    } else {
      builder.append(upperBound);
    }
    if (upperBoundOpen) {
      builder.append(")");
    } else {
      builder.append("]");
    }
    return builder.toString();
  }
}

import org.hyperskill.hstest.common.ReflectionUtils;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.UnexpectedError;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class RangeDelegateSearcher extends DelegateSearcher {
  void Initialize() {
    this.name = "Range";
  }

  void Validate() {
    if (original.getTypeParameters().length == 0) {
      throw new WrongAnswer(name + " Class must be a Generic Class.");
    }
    if (!original.getTypeParameters()[0].getBounds()[0].getTypeName().equals("java.lang.Comparable")) {
      throw new WrongAnswer(name + "'s type parameter must extend 'Comparable' interface");
    }
//    if (!original.getAnnotatedInterfaces()[0].getType().getTypeName().equals("java.io.Serializable")) {
//      throw new WrongAnswer(name + " Class must implement 'Serializable' interface");
//    }

    for (var c : original.getDeclaredConstructors()) {
      if (Modifier.isPublic(c.getModifiers())) {
        throw new WrongAnswer("None of the " + name + "'s constructors should be public");
      }
    }

    //Name, isStatic, returnType, argsType
    CustomMethod[] methods = new CustomMethod[]{
            new CustomMethod("contains", false, boolean.class, new Class[]{Comparable.class}),
            new CustomMethod("encloses", false, boolean.class, new Class[]{original}),
            new CustomMethod("intersection", false, original, new Class[]{original}),
            new CustomMethod("span", false, original, new Class[]{original}),
            new CustomMethod("isEmpty", false, boolean.class, new Class[]{}),
            new CustomMethod("open", true, original, new Class[]{Comparable.class, Comparable.class}),
            new CustomMethod("closed", true, original, new Class[]{Comparable.class, Comparable.class}),
            new CustomMethod("openClosed", true, original, new Class[]{Comparable.class, Comparable.class}),
            new CustomMethod("closedOpen", true, original, new Class[]{Comparable.class, Comparable.class}),
            new CustomMethod("greaterThan", true, original, new Class[]{Comparable.class}),
            new CustomMethod("atLeast", true, original, new Class[]{Comparable.class}),
            new CustomMethod("lessThan", true, original, new Class[]{Comparable.class}),
            new CustomMethod("atMost", true, original, new Class[]{Comparable.class}),
            new CustomMethod("all", true, original, new Class[]{}),
    };

    for (CustomMethod m : methods) {
      Method method;
      try {
        method = original.getMethod(m.getName(), m.getArgs());
      } catch (NoSuchMethodException e) {

        ArrayList<String> names = new ArrayList<>();
        for (Class<?> p : m.getArgs()) {
          if (p.equals(Comparable.class)) {
            names.add("C");
          } else {
            names.add(p.getSimpleName());
          }
        }

        if (names.isEmpty()) {
          throw new WrongAnswer(name + "'s " + m.getName() + "() method without args is not found");
        } else {
          throw new WrongAnswer(name + "'s " + m.getName() + "() method with args " + names + " is not found");
        }
      }

      if (m.isStatic() && !Modifier.isStatic(method.getModifiers())) {
        throw new WrongAnswer(name + "'s " + m.getName() + "() method must be static");
      }
      if (!m.isStatic() && Modifier.isStatic(method.getModifiers())) {
        throw new WrongAnswer(name + "'s " + m.getName() + "() method must not be static");
      }

      if (!method.getReturnType().equals(m.getReturnType())) {
        throw new WrongAnswer(name + "'s " + m.getName() + "() method must return " + m.getReturnType().getSimpleName());
      }

      if (!Modifier.isPublic(method.getModifiers())) {
        throw new WrongAnswer(name + "'s " + m.getName() + "() method must be public");
      }
    }
  }

  @Override
  protected Object invoke(String methodName, Class<?>[] args, Object... objs) {
    try {
      if (objs.length == 0) {
        Method method = original.getMethod(methodName);
        return method.invoke(instance);
      } else {
        Method method = original.getMethod(methodName, args);
        return ReflectionUtils.invokeMethod(method, instance, objs);
      }
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | UnexpectedError | StackOverflowError e) {
      if (e.getCause() != null && e.getCause().getClass().getSimpleName().equals("NullPointerException")) {
        throw new NullPointerException();
      } else if (e.getCause() != null && e.getCause().getClass().getSimpleName().equals("IllegalArgumentException")) {
        throw new IllegalArgumentException();
      }

      throw new WrongAnswer("Could not invoke " + name + "'s " + methodName + "() method correctly or it is throwing " +
              "wrong exception for a null element or invalid range");
    }
  }
}

class RangeBridge extends RangeDelegateSearcher {

  public boolean contains(Object a) {
    return (boolean) invoke("contains", new Class[]{Comparable.class}, a);
  }

  public boolean encloses(Object a) {
    return (boolean) invoke("encloses", new Class[]{original}, a);
  }

  public Object intersection(Object a) {
    return invoke("intersection", new Class[]{original}, a);
  }

  public Object span(Object a) {
    return invoke("span", new Class[]{original}, a);
  }

  public boolean isEmpty() {
    return (boolean) invoke("isEmpty", new Class[]{});
  }

  public Object open(Object a, Object b) {
    instance = invoke("open", new Class[]{Comparable.class, Comparable.class}, a, b);
    return instance;
  }

  public Object closed(Object a, Object b) {
    instance = invoke("closed", new Class[]{Comparable.class, Comparable.class}, a, b);
    return instance;
  }

  public Object openClosed(Object a, Object b) {
    instance = invoke("openClosed", new Class[]{Comparable.class, Comparable.class}, a, b);
    return instance;
  }

  public Object closedOpen(Object a, Object b) {
    instance = invoke("closedOpen", new Class[]{Comparable.class, Comparable.class}, a, b);
    return instance;
  }

  public Object greaterThan(Object a) {
    instance = invoke("greaterThan", new Class[]{Comparable.class}, a);
    return instance;
  }

  public Object atLeast(Object a) {
    instance = invoke("atLeast", new Class[]{Comparable.class}, a);
    return instance;
  }

  public Object lessThan(Object a) {
    instance = invoke("lessThan", new Class[]{Comparable.class}, a);
    return instance;
  }

  public Object atMost(Object a) {
    instance = invoke("atMost", new Class[]{Comparable.class}, a);
    return instance;
  }

  public Object all() {
    instance = invoke("all", new Class[]{});
    return instance;
  }


  public String toString() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    PrintStream old = System.out;
    System.setOut(ps);

    try {
      System.out.print(instance);
    } catch (Throwable e) {
      throw new WrongAnswer("Unexpected " + e.getClass().getSimpleName() + " while parsing output from " +
              "Range's toString()");
    }

    System.out.flush();
    System.setOut(old);
    return baos.toString();
  }
}

public class RangeTests extends StageTest {

  Pattern pattern = Pattern.compile("EMPTY|(\\(|\\[)((.+)|(-INF)), ((.+)|(INF))(\\)|\\])");

  enum Type {
    OPEN("open"),
    CLOSED("closed"),
    OPENCLOSED("openClosed"),
    CLOSEDOPEN("closedOpen"),
    LESSTHAN("lessThan"),
    GREATERTHAN("greaterThan"),
    ATLEAST("atLeast"),
    ATMOST("atMost"),
    ALL("all");

    String label;

    Type(String label) {
      this.label = label;
    }
  }

  Object[][] correct_data = {
          {5, 15}, {10, 20}, {15, 25},
          {'e', 'o'}, {'j', 't'}, {'o', 'y'},
          {"EE", "OO"}, {"JJ", "TT"}, {"OO", "YY"}
  };

  @DynamicTest(data = "correct_data", order = 0)
  CheckResult test_ex_1(Object a, Object b) {
    boolean c = false;
    // NPE for fabrics Type.OPEN, Type.CLOSED, Type.CLOSEDOPEN, Type.OPENCLOSED
    for (Type type : new Type[]{Type.OPEN, Type.CLOSED, Type.CLOSEDOPEN, Type.OPENCLOSED}) {
      for (boolean lower : new boolean[]{true, false}) {
        c = false;
        try {
          RangeBridge coll = new RangeBridge();
          if (lower) {
            switch (type) {
              case OPEN -> coll.open(null, a);
              case CLOSED -> coll.closed(null, a);
              case OPENCLOSED -> coll.openClosed(null, a);
              case CLOSEDOPEN -> coll.closedOpen(null, a);
            }
          } else {
            switch (type) {
              case OPEN -> coll.open(a, null);
              case CLOSED -> coll.closed(a, null);
              case OPENCLOSED -> coll.openClosed(a, null);
              case CLOSEDOPEN -> coll.closedOpen(a, null);
            }
          }

        } catch (NullPointerException e) {
          c = true;
        }
        if (!c) {
          return CheckResult.wrong("Range's " + type.label + "() method should throw a NullPointerException if " +
                  "any of provided arguments is equal to 'null'");
        }
      }
    }
    // NPE for fabrics Type.LESSTHAN, Type.GREATERTHAN, Type.ATLEAST, Type.ATMOST
    for (Type type : new Type[]{Type.LESSTHAN, Type.GREATERTHAN, Type.ATLEAST, Type.ATMOST}) {
      c = false;
      try {
        RangeBridge coll = new RangeBridge();
        switch (type) {
          case LESSTHAN -> coll.lessThan(null);
          case GREATERTHAN -> coll.greaterThan(null);
          case ATLEAST -> coll.atLeast(null);
          case ATMOST -> coll.atMost(null);
        }
      } catch (NullPointerException e) {
        c = true;
      }
      if (!c) {
        return CheckResult.wrong("Range's " + type.label + "() method should throw a NullPointerException if " +
                "provided argument is equal to 'null'");
      }
    }

    for (Type type : new Type[]{Type.OPEN, Type.CLOSED, Type.CLOSEDOPEN, Type.OPENCLOSED}) {
      c = false;
      try {
        RangeBridge coll = new RangeBridge();
        switch (type) {
          case OPEN -> coll.open(a, b);
          case CLOSED -> coll.closed(a, b);
          case OPENCLOSED -> coll.openClosed(a, b);
          case CLOSEDOPEN -> coll.closedOpen(a, b);
        }

      } catch (NullPointerException e) {
        c = true;
      }
      if (c) {
        return CheckResult.wrong("Range's " + type.label + "() method should not throw a NullPointerException if " +
                "none of provided arguments is equal to 'null'");
      }
    }

    for (Type type : new Type[]{Type.LESSTHAN, Type.GREATERTHAN, Type.ATLEAST, Type.ATMOST}) {
      c = false;
      try {
        RangeBridge coll = new RangeBridge();
        switch (type) {
          case LESSTHAN -> coll.lessThan(a);
          case GREATERTHAN -> coll.greaterThan(a);
          case ATLEAST -> coll.atLeast(a);
          case ATMOST -> coll.atMost(a);
        }
      } catch (NullPointerException e) {
        c = true;
      }
      if (c) {
        return CheckResult.wrong("Range's " + type.label + "() method should not throw a NullPointerException if " +
                "provided argument is not 'null'");
      }
    }
    // NPE for methods
    for (Type type : Type.values()) {
      c = false;
      RangeBridge coll = new RangeBridge();
      try {
        switch (type) {
          case OPEN -> coll.open(a, b);
          case CLOSED -> coll.closed(a, b);
          case OPENCLOSED -> coll.openClosed(a, b);
          case CLOSEDOPEN -> coll.closedOpen(a, b);
          case LESSTHAN -> coll.lessThan(a);
          case GREATERTHAN -> coll.greaterThan(a);
          case ATLEAST -> coll.atLeast(a);
          case ATMOST -> coll.atMost(a);
          case ALL -> coll.all();
        }
      } catch (NullPointerException | IllegalArgumentException e) {
        return CheckResult.wrong("Range's fabric " + type.label + "() method should not throw any exceptions if " +
                "all provided arguments are valid. Caught: "+e.getClass().getSimpleName());
      }
      for (String method : new String[]{"contains", "encloses", "intersection", "span"}) {
        try {
          switch (method) {
            case "contains" -> coll.contains(null);
            case "encloses" -> coll.encloses(null);
            case "intersection" -> coll.intersection(null);
            case "span" -> coll.span(null);
          }
        } catch (NullPointerException e) {
          c = true;
        }
        if (!c) {
          return CheckResult.wrong("Range's " + method + "() method should throw a NullPointerException if " +
                  "provided argument is equal to 'null'");
        }
      }
    }
    return CheckResult.correct();
  }

  Object[][] data_ex_2 = {
          {5}, {10}, {15},
          {'e'}, {'j'}, {'a'},
          {"HELLO"}, {"WORLD"}, {"!"}
  };

  @DynamicTest(data = "data_ex_2", order = 1)
  CheckResult test_ex_2(Object a) {
    boolean c = false;
    // Open, lowerBound == upperBound
    try {
      RangeBridge coll = new RangeBridge();
      coll.open(a, a);
    } catch (IllegalArgumentException e) {
      c = true;
    }
    if (!c) {
      return CheckResult.wrong("Range's open() method should throw an IllegalArgumentException if " +
              "provided LowerBound is equal UpperBound");
    }
    for (Type type : new Type[]{Type.CLOSED, Type.CLOSEDOPEN, Type.OPENCLOSED}) {
      c = false;
      try {
        RangeBridge coll = new RangeBridge();
        switch (type) {
          case CLOSED -> coll.closed(a, a);
          case OPENCLOSED -> coll.openClosed(a, a);
          case CLOSEDOPEN -> coll.closedOpen(a, a);
        }
      } catch (IllegalArgumentException e) {
        c = true;
      }
      if (c) {
        return CheckResult.wrong("Range's " + type.label + "() method should not throw an IllegalArgumentException if" +
                " provided LowerBound is equal UpperBound");
      }
    }
    return CheckResult.correct();
  }

  Object[][] data_ex_3 = {
          {Type.OPEN, 5, 10}, {Type.CLOSED, 5, 10}, {Type.OPENCLOSED, 5, 10}, {Type.CLOSEDOPEN, 5, 10},

          {Type.OPEN, 'e', 'j'}, {Type.CLOSED, 'e', 'j'}, {Type.OPENCLOSED, 'e', 'j'}, {Type.CLOSEDOPEN, 'e', 'j'},

          {Type.OPEN, "HELLO", "WORLD"}, {Type.CLOSED, "HELLO", "WORLD"}, {Type.OPENCLOSED, "HELLO", "WORLD"},
          {Type.CLOSEDOPEN, "HELLO", "WORLD"}
  };

  @DynamicTest(data = "data_ex_3", order = 2)
  CheckResult test_ex_3(Type type, Object a, Object b) {
    boolean c = false;
    // LowerBound greater than upperBound
    try {
      RangeBridge coll = new RangeBridge();
      switch (type) {
        case OPEN -> coll.open(b, a);
        case CLOSED -> coll.closed(b, a);
        case OPENCLOSED -> coll.openClosed(b, a);
        case CLOSEDOPEN -> coll.closedOpen(b, a);
      }
    } catch (IllegalArgumentException e) {
      c = true;
    }
    if (!c) {
      return CheckResult.wrong("Range's " + type.label + "() method should throw an IllegalArgumentException if " +
              "provided LowerBound greater than UpperBound");
    }
    c = false;
    try {
      RangeBridge coll = new RangeBridge();
      switch (type) {
        case OPEN -> coll.open(a, b);
        case CLOSED -> coll.closed(a, b);
        case OPENCLOSED -> coll.openClosed(a, b);
        case CLOSEDOPEN -> coll.closedOpen(a, b);
      }
    } catch (IllegalArgumentException e) {
      c = true;
    }
    if (c) {
      return CheckResult.wrong("Range's " + type.label + "() method should not throw an IllegalArgumentException if " +
              "provided LowerBound less than UpperBound");
    }
    return CheckResult.correct();
  }


  void InstanceCheck(RangeBridge coll, ActualRange<Comparable> actual, Comparable[] contains, boolean reveal) {
    String str = coll.toString();
    if (!pattern.matcher(str).matches()) {
      if (reveal)
        throw new WrongAnswer("Instance of Range object is not being printed correctly, check out the " +
                "examples.\n" +
                "Got: " + str + "\n");
      else
        throw new WrongAnswer("Instance of Range object is not being printed correctly, check out the " +
                "examples");
    }
    if (!str.equals(actual.toString())) {
      if (reveal)
        throw new WrongAnswer("Instance of Range object is not being printed correctly. Printed values or " +
                "their boundaries do not correspond to those provided\n" +
                "Expected: " + actual + "\n" +
                "Got: " + str + "\n");
      else
        throw new WrongAnswer("Instance of Range object is not being printed correctly. Printed values or " +
                "their boundaries do not correspond to those provided");
    }
    if (coll.isEmpty() != actual.isEmpty()) {
      if (reveal)
        throw new WrongAnswer("Incorrect result from Range's isEmpty() method.\n" +
                "Case: " + actual + "\n" +
                "Expected: " + actual.isEmpty() + "\n" +
                "Got: " + coll.isEmpty() + "\n");
      else
        throw new WrongAnswer("Incorrect result from Range's isEmpty() method.");
    }
    if (contains.length > 0)
      for (Comparable i : contains) {
        if (coll.contains(i) != actual.contains(i)) {
          if (reveal)
            throw new WrongAnswer("Incorrect result from Range's contains() method.\n" +
                    "Case: " + actual + ", (" + i + ")\n" +
                    "Expected: " + actual.contains(i) + "\n" +
                    "Got: " + coll.contains(i) + "\n");
          else
            throw new WrongAnswer("Incorrect result from Range's contains() method.");
        }
      }
  }

  void CheckEnclosesSpanIntersection(RangeBridge main, Object secondary, ActualRange actualMain,
                                     ActualRange actualSecondary, boolean reveal) {
    String method = "";
    try {
      method = "encloses()";
      boolean enclosesResult = main.encloses(secondary);
      if (enclosesResult != actualMain.encloses(actualSecondary)) {
        if (reveal)
          throw new WrongAnswer("Incorrect result from Range's encloses() method.\n" +
                  "Case: " + actualMain + ", " + actualSecondary + "\n" +
                  "Expected: " + actualMain.encloses(actualSecondary) + "\n" +
                  "Got: " + enclosesResult + "\n");
        else
          throw new WrongAnswer("Incorrect result from Range's encloses() method.");
      }
      method = "span()";
      Object spanResult = main.span(secondary);
      if (!pattern.matcher(spanResult.toString()).matches()) {
        if (reveal)
          throw new WrongAnswer("Instance of Range object is not being printed correctly, check out the " +
                  "examples.\n" +
                  "Got: " + spanResult + "\n");
        else
          throw new WrongAnswer("Instance of Range object is not being printed correctly, check out the " +
                  "examples");
      }
      if (!spanResult.toString().equals(actualMain.span(actualSecondary).toString())) {
        if (reveal)
          throw new WrongAnswer("Incorrect result from Range's span() method.\n" +
                  "Case: " + actualMain + ", " + actualSecondary + "\n" +
                  "Expected: " + actualMain.span(actualSecondary) + "\n" +
                  "Got: " + spanResult + "\n");
        else
          throw new WrongAnswer("Incorrect result from Range's span() method.");
      }
      method = "intersection()";
      Object intersectionResult = main.intersection(secondary);
      if (!pattern.matcher(intersectionResult.toString()).matches()) {
        if (reveal)
          throw new WrongAnswer("Instance of Range object is not being printed correctly, check out the " +
                  "examples.\n" +
                  "Got: " + intersectionResult + "\n");
        else
          throw new WrongAnswer("Instance of Range object is not being printed correctly, check out the " +
                  "examples");
      }
      if (!intersectionResult.toString().equals(actualMain.intersection(actualSecondary).toString())) {
        if (reveal)
          throw new WrongAnswer("Incorrect result from Range's intersection() method.\n" +
                  "Case: " + actualMain + ", " + actualSecondary + "\n" +
                  "Expected: " + actualMain.intersection(actualSecondary) + "\n" +
                  "Got: " + intersectionResult + "\n");
        else
          throw new WrongAnswer("Incorrect result from Range's intersection() method.");
      }
    } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
      throw new WrongAnswer("Incorrect result from Range's " + method + " method.\n" +
              "Got: " + e.getClass().getSimpleName() + "\n");
    }
  }

  //data, contains, nContains
  Object contains_empty_data() {
    Comparable[][] beta = new Comparable[][]{
            {5, 10, 15, 20, 25},
            {'e', 'j', 'o', 't', 'y'},
            {"e", "j", "o", "t", "y"}
    };
    List<Object[]> result = new ArrayList<>();
    for (Comparable[] i : beta) {
      Map<ActualRange, Type> data = new HashMap<>();
      for (int j = 0; j < 3; j++) {
        data.put(ActualRange.open(i[j], i[j + 2]), Type.OPEN);
        data.put(ActualRange.closed(i[j], i[j + 2]), Type.CLOSED);
        data.put(ActualRange.openClosed(i[j], i[j + 2]), Type.OPENCLOSED);
        data.put(ActualRange.closedOpen(i[j], i[j + 2]), Type.CLOSEDOPEN);
      }
      for (Comparable j : i) {
        data.put(ActualRange.closed(j, j), Type.CLOSED);
        data.put(ActualRange.openClosed(j, j), Type.OPENCLOSED);
        data.put(ActualRange.closedOpen(j, j), Type.CLOSEDOPEN);
        data.put(ActualRange.lessThan(j), Type.LESSTHAN);
        data.put(ActualRange.atMost(j), Type.ATMOST);
        data.put(ActualRange.greaterThan(j), Type.GREATERTHAN);
        data.put(ActualRange.atLeast(j), Type.ATLEAST);
      }
      data.put(ActualRange.all(), Type.ALL);
      result.add(new Object[]{data});
    }
    return result.toArray();
  }

  @DynamicTest(order = 3, data = "contains_empty_data")
  CheckResult test_contains_empty(Map<ActualRange, Type> m) {
    for (Map.Entry<ActualRange, Type> entry : m.entrySet()) {
      RangeBridge coll = new RangeBridge();
      Type type = entry.getValue();
      ActualRange data = entry.getKey();
      switch (type) {
        case OPEN -> coll.open(data.getLowerBound(), data.getUpperBound());
        case CLOSED -> coll.closed(data.getLowerBound(), data.getUpperBound());
        case OPENCLOSED -> coll.openClosed(data.getLowerBound(), data.getUpperBound());
        case CLOSEDOPEN -> coll.closedOpen(data.getLowerBound(), data.getUpperBound());
        case LESSTHAN -> coll.lessThan(data.getUpperBound());
        case GREATERTHAN -> coll.greaterThan(data.getLowerBound());
        case ATLEAST -> coll.atLeast(data.getLowerBound());
        case ATMOST -> coll.atMost(data.getUpperBound());
        case ALL -> coll.all();
      }

      switch (data.getClass().getSimpleName()) {
        case "Integer":
          InstanceCheck(coll, data, new Comparable[]{0, 5, 10, 15, 20, 25, 30}, true);
        case "Character":
          InstanceCheck(coll, data, new Comparable[]{'a', 'e', 'j', 'o', 't', 'y', 'z'}, false);
        case "String":
          InstanceCheck(coll, data, new Comparable[]{"a", "e", "j", "o", "t", "y", "z"}, false);
      }
    }

    return CheckResult.correct();
  }

  @DynamicTest(order = 4, data = "contains_empty_data")
  CheckResult test_actions(Map<ActualRange, Type> m) {
    for (Map.Entry<ActualRange, Type> entry1 : m.entrySet()) {
      RangeBridge coll1 = new RangeBridge();
      Type type = entry1.getValue();
      ActualRange data = entry1.getKey();
      switch (type) {
        case OPEN -> coll1.open(data.getLowerBound(), data.getUpperBound());
        case CLOSED -> coll1.closed(data.getLowerBound(), data.getUpperBound());
        case OPENCLOSED -> coll1.openClosed(data.getLowerBound(), data.getUpperBound());
        case CLOSEDOPEN -> coll1.closedOpen(data.getLowerBound(), data.getUpperBound());
        case LESSTHAN -> coll1.lessThan(data.getUpperBound());
        case GREATERTHAN -> coll1.greaterThan(data.getLowerBound());
        case ATLEAST -> coll1.atLeast(data.getLowerBound());
        case ATMOST -> coll1.atMost(data.getUpperBound());
        case ALL -> coll1.all();
      }
      for (Map.Entry<ActualRange, Type> entry2 : m.entrySet()) {
        RangeBridge coll2 = new RangeBridge();
        Type type2 = entry2.getValue();
        ActualRange data2 = entry2.getKey();
        switch (type2) {
          case OPEN -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.open(data2.getLowerBound(), data2.getUpperBound()),
                  data, data2, data2.getLowerBound().getClass().getSimpleName().equals("Integer"));
          case CLOSED -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.closed(data2.getLowerBound(), data2.getUpperBound()),
                  data, data2, data2.getLowerBound().getClass().getSimpleName().equals("Integer"));
          case OPENCLOSED -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.openClosed(data2.getLowerBound(), data2.getUpperBound()),
                  data, data2, data2.getLowerBound().getClass().getSimpleName().equals("Integer"));
          case CLOSEDOPEN -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.closedOpen(data2.getLowerBound(), data2.getUpperBound()),
                  data, data2, data2.getLowerBound().getClass().getSimpleName().equals("Integer"));
          case LESSTHAN -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.lessThan(data2.getUpperBound()),
                  data, data2, data2.getUpperBound().getClass().getSimpleName().equals("Integer"));
          case GREATERTHAN -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.greaterThan(data2.getLowerBound()),
                  data, data2, data2.getLowerBound().getClass().getSimpleName().equals("Integer"));
          case ATLEAST -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.atLeast(data2.getLowerBound()),
                  data, data2, data2.getLowerBound().getClass().getSimpleName().equals("Integer"));
          case ATMOST -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.atMost(data2.getUpperBound()),
                  data, data2, data2.getUpperBound().getClass().getSimpleName().equals("Integer"));
          case ALL -> CheckEnclosesSpanIntersection(
                  coll1,
                  coll2.all(),
                  data, data2, false);
        }
      }
    }
    return CheckResult.correct();
  }
}
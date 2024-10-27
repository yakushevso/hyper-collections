import org.hyperskill.hstest.common.ReflectionUtils;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.UnexpectedError;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

class SizeLimitedQueueDelegateSearcher extends DelegateSearcher {
  void Initialize() {
    this.name = "SizeLimitedQueue";
  }

  void Validate() {
    if (original.getTypeParameters().length == 0) {
      throw new WrongAnswer(name + " Class must be a Generic Class.");
    }
    try {
      Constructor<?> constr = original.getDeclaredConstructor(int.class);
      if (!Modifier.isPublic(constr.getModifiers())) {
        throw new WrongAnswer(name + "'s constructor with args [" + int.class.getSimpleName() + "] should be public");
      }
    } catch (NoSuchMethodException e) {
      throw new WrongAnswer(name + "'s public constructor with args [" + int.class.getSimpleName() + "] is not found");
    }
    //Name, isStatic, returnType, argsType
    CustomMethod[] methods = new CustomMethod[]{
            new CustomMethod("add", false, void.class, new Class[]{Object.class}),
            new CustomMethod("clear", false, void.class, new Class[]{}),
            new CustomMethod("isAtFullCapacity", false, boolean.class, new Class[]{}),
            new CustomMethod("isEmpty", false, boolean.class, new Class[]{}),
            new CustomMethod("maxSize", false, int.class, new Class[]{}),
            new CustomMethod("peek", false, Object.class, new Class[]{}),
            new CustomMethod("remove", false, Object.class, new Class[]{}),
            new CustomMethod("size", false, int.class, new Class[]{}),
            new CustomMethod("toArray", false, Object[].class, new Class[]{Object[].class}),
            new CustomMethod("toArray", false, Object[].class, new Class[]{}),
    };

    for (CustomMethod m : methods) {
      Method method;
      try {
        method = original.getMethod(m.getName(), m.getArgs());
      } catch (NoSuchMethodException e) {

        ArrayList<String> names = new ArrayList<>();
        for (Class<?> p : m.getArgs()) {
          if (p.equals(Object.class)) {
            names.add("E");
          } else if (p.equals(Object[].class)) {
            names.add("E[]");
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
        if (m.getReturnType().equals(Object.class)) {
          throw new WrongAnswer(name + "'s " + m.getName() + "() method must return E");
        } else if (m.getReturnType().equals(Object[].class)) {
          if (m.getArgs().length != 0) {
            throw new WrongAnswer(name + "'s " + m.getName() + "() method with args E[] must return E[]");
          } else {
            throw new WrongAnswer(name + "'s " + m.getName() + "() method without args must return Object[]");
          }
        } else {
          throw new WrongAnswer(name + "'s " + m.getName() + "() method must return " + m.getReturnType().getSimpleName());
        }
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
      String add = ".";

      if (methodName.equals("remove")) {
        if (e.getCause() != null && e.getCause().getClass().getSimpleName().equals("NoSuchElementException")) {
          throw new NoSuchElementException();
        } else {
          add = " or it is throwing wrong exception for empty queue";
        }
      }

      if (methodName.equals("add")) {
        if (e.getCause() != null && e.getCause().getClass().getSimpleName().equals("NullPointerException")) {
          throw new NullPointerException();
        } else {
          add = " or it is throwing wrong exception for a null object argument";
        }
      }

      throw new WrongAnswer("Could not invoke " + name + "'s " + methodName + "() method correctly" + add);
    }
  }
}

class SizeLimitedQueueBridge extends SizeLimitedQueueDelegateSearcher {

  public SizeLimitedQueueBridge(int limit) {
    try {
      Constructor<?> constr = original.getDeclaredConstructor(int.class);
      instance = constr.newInstance(limit);
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      if (e.getCause().getClass().getSimpleName().equals("IllegalArgumentException")) {
        throw new IllegalArgumentException();
      }
      throw new WrongAnswer("Could not create new " + name + "'s instance or constructor is throwing wrong exception " +
              "for non-positive limit");
    }
  }

  public void add(Object element) {
    invoke("add", new Class[]{Object.class}, element);
  }

  public void clear() {
    invoke("clear", new Class[]{});
  }

  public boolean isAtFullCapacity() {
    return (boolean) invoke("isAtFullCapacity", new Class[]{});
  }

  public boolean isEmpty() {
    return (boolean) invoke("isEmpty", new Class[]{});
  }

  public int maxSize() {
    return (int) invoke("maxSize", new Class[]{});
  }

  public Object peek() {
    return invoke("peek", new Class[]{});
  }

  public Object remove() {
    return invoke("remove", new Class[]{});
  }

  public int size() {
    return (int) invoke("size", new Class[]{});
  }

  public Object[] toArray(Object[] objs) {
    return (Object[]) invoke("toArray", new Class[]{Object[].class}, new Object[]{objs});
  }

  public Object[] toArray() {
    return (Object[]) invoke("toArray", new Class[]{});
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
              "SizeLimitedQueue's toString()");
    }

    System.out.flush();
    System.setOut(old);
    return baos.toString();
  }
}

public class QueueTests extends StageTest {
  Pattern pattern = Pattern.compile("\\[\\w+(?:, \\w+)*\\]|\\[\\]");

  void Check(SizeLimitedQueueBridge collection, Queue<Object> q, int limit, String methodName, Object[] ref,
             Class<?> cl) {
    if (!pattern.matcher(collection.toString()).matches()) {
      throw new WrongAnswer("Instance of SizeLimitedQueue object should be printed as a list, just like in the " +
              "example.");
    }
    if (!methodName.equals(""))
      if (!collection.toString().equals(Arrays.toString(q.toArray()))) {
        throw new WrongAnswer("SizeLimitedQueue's " + methodName + "() method not working correctly");
      }

    //Side methods check
    if ((q.size() == limit) != collection.isAtFullCapacity()) {
      throw new WrongAnswer("Incorrect result from SizeLimitedQueue's isAtFullCapacity() method.");
    }
    if ((q.isEmpty()) != collection.isEmpty()) {
      throw new WrongAnswer("Incorrect result from SizeLimitedQueue's isEmpty() method.");
    }
    if (collection.maxSize() != limit) {
      throw new WrongAnswer("Incorrect result from SizeLimitedQueue's maxSize() method.");
    }
    if (collection.peek() != q.peek()) {
      throw new WrongAnswer("Incorrect result from SizeLimitedQueue's peek() method.");
    }
    if (collection.size() != q.size()) {
      throw new WrongAnswer("Incorrect result from SizeLimitedQueue's size() method.");
    }

    //Arrays check
    if (!methodName.equals("")) {
      if (collection.toArray().length != q.toArray().length) {
        throw new WrongAnswer("Incorrect result from SizeLimitedQueue's toArray() method.");
      }
      if (collection.toArray(ref).length != q.toArray().length) {
        throw new WrongAnswer("Incorrect result from SizeLimitedQueue's toArray(E[]) method.");
      }

      if (!collection.toArray().getClass().getSimpleName().equals(Object[].class.getSimpleName())) {
        throw new WrongAnswer("Incorrect return type from SizeLimitedQueue's toArray() method.");
      }
      if (!collection.toArray(ref).getClass().getSimpleName().equals(cl.getSimpleName())) {
        throw new WrongAnswer("Incorrect return type from SizeLimitedQueue's toArray(E[]) method.");
      }

      for (int j = 0; j < q.toArray().length; j++) {
        if (collection.toArray()[j] != q.toArray()[j]) {
          throw new WrongAnswer("Incorrect result from SizeLimitedQueue's toArray() method.");
        }
        if (collection.toArray(ref)[j] != q.toArray()[j]) {
          throw new WrongAnswer("Incorrect result from SizeLimitedQueue's toArray(E[]) method.");
        }
      }
    }
  }

  @DynamicTest()
  CheckResult test_ex() {
    boolean c = false;
    //Non-positive limit
    try {
      new SizeLimitedQueueBridge(-1);
    } catch (IllegalArgumentException e) {
      c = true;
    }
    if (!c) {
      return CheckResult.wrong("SizeLimitedQueue's constructor should throw an IllegalArgumentException if provided" +
              " limit is negative");
    }
    c = false;
    try {
      new SizeLimitedQueueBridge(0);
    } catch (IllegalArgumentException e) {
      c = true;
    }
    if (!c) {
      return CheckResult.wrong("SizeLimitedQueue's constructor should throw an IllegalArgumentException if provided" +
              " limit is equal to zero");
    }
    //Positive limit
    SizeLimitedQueueBridge collection;
    try {
      collection = new SizeLimitedQueueBridge(1);
    } catch (IllegalArgumentException e) {
      return CheckResult.wrong("SizeLimitedQueue's constructor should not throw an IllegalArgumentException if " +
              "provided limit is positive");
    }
    //Remove for empty
    c = false;
    try {
      collection.remove();
    } catch (NoSuchElementException e) {
      c = true;
    }
    if (!c) {
      return CheckResult.wrong("SizeLimitedQueue's remove() method should throw an NoSuchElementException for an " +
              "empty queue");
    }
    //Add null
    c = false;
    try {
      collection.add(null);
    } catch (NullPointerException e) {
      c = true;
    }
    if (!c) {
      return CheckResult.wrong("SizeLimitedQueue's add() method should throw an NullPointerException for a null " +
              "object argument");
    }
    //Peek for empty
    if (collection.peek() != null) {
      return CheckResult.wrong("SizeLimitedQueue's peek() method should return null for an empty queue");
    }
    //Add not null
    try {
      collection.add(1);
    } catch (NullPointerException e) {
      return CheckResult.wrong("SizeLimitedQueue's add() method should not throw an NullPointerException for a " +
              "non-null object arguments");
    }
    //Remove for non empty
    try {
      collection.remove();
    } catch (NoSuchElementException e) {
      return CheckResult.wrong("SizeLimitedQueue's remove() method should not throw an NoSuchElementException for a " +
              "non-empty queue");
    }

    return CheckResult.correct();
  }

  @DynamicTest()
  CheckResult test_empty() {
    int limit = 5;
    SizeLimitedQueueBridge collection = new SizeLimitedQueueBridge(limit);
    Deque<Object> q = new ArrayDeque<>();
    Check(collection, q, limit, "", new Object[0], Object.class);
    return CheckResult.correct();
  }

  Object[][] test_data = {
          {3, new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10}, new Integer[0], Integer[].class},
          {3, new Character[]{'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'}, new Character[0], Character[].class},
          {3, new String[]{"BCD", "CDE", "DEF", "EFG", "FGH", "GHI", "HIJ", "IJK", "JKL"}, new String[0],
                  String[].class},
          {5, new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10}, new Integer[0], Integer[].class},
          {5, new Character[]{'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'}, new Character[0], Character[].class},
          {5, new String[]{"BCD", "CDE", "DEF", "EFG", "FGH", "GHI", "HIJ", "IJK", "JKL"}, new String[0],
                  String[].class},
          {8, new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10}, new Integer[0], Integer[].class},
          {8, new Character[]{'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'}, new Character[0], Character[].class},
          {8, new String[]{"BCD", "CDE", "DEF", "EFG", "FGH", "GHI", "HIJ", "IJK", "JKL"}, new String[0],
                  String[].class}
  };

  @DynamicTest(data = "test_data")
  CheckResult test_actions(int limit, Object[] array, Object[] ref, Class<?> cl) {
    SizeLimitedQueueBridge collection = new SizeLimitedQueueBridge(limit);
    Deque<Object> q = new ArrayDeque<>();
    //Add
    for (Object i : array) {
      collection.add(i);
      if (q.size() == limit) {
        q.removeFirst();
      }
      q.add(i);
      Check(collection, q, limit, "add", ref, cl);
    }

    //Remove
    for (int i = 0; i < limit; i++) {
      Object a = q.remove();
      Object b = collection.remove();
      if(a!=b){
        return CheckResult.wrong("Incorrect result from SizeLimitedQueue's remove() method.");
      }
      Check(collection, q, limit, "remove", ref, cl);
    }

    //Remove after adding more
    q.add(array[0]);
    collection.add(array[0]);
    Check(collection, q, limit, "add", ref, cl);
    Object a = q.remove();
    Object b = collection.remove();
    if(a!=b){
      return CheckResult.wrong("Incorrect result from SizeLimitedQueue's remove() method.");
    }
    Check(collection, q, limit, "remove", ref, cl);

    //Add and clear
    for (int i = 0; i < limit; i++) {
      q.add(array[1]);
      collection.add(array[1]);
      Check(collection, q, limit, "add", ref, cl);
    }
    collection.clear();
    q.clear();
    Check(collection, q, limit, "clear", ref, cl);

    //Recheck add
    for (Object i : array) {
      collection.add(i);
      if (q.size() == limit) {
        q.removeFirst();
      }
      q.add(i);
      Check(collection, q, limit, "add", ref, cl);
    }

    return CheckResult.correct();
  }
}
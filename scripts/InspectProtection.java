public class InspectProtection {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("dev.ftb.mods.ftbchunks.api.Protection");
    System.out.println("Class=" + c.getName() + " enum=" + c.isEnum());
    for (var m : c.getDeclaredMethods()) {
      System.out.println("M " + m.toString());
    }
    for (var f : c.getDeclaredFields()) {
      System.out.println("F " + java.lang.reflect.Modifier.toString(f.getModifiers()) + " " + f.getType().getName() + " " + f.getName());
    }
    Object[] constants = c.isEnum() ? c.getEnumConstants() : null;
    if (constants != null) {
      for (Object o : constants) {
        System.out.println("C " + o.toString());
      }
    }
  }
}

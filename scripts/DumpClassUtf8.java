import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class DumpClassUtf8 {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) throw new IllegalArgumentException("jar classEntry");
    try (JarFile jf = new JarFile(args[0])) {
      JarEntry e = jf.getJarEntry(args[1]);
      if (e == null) throw new RuntimeException("missing entry");
      byte[] b;
      try (InputStream in = jf.getInputStream(e)) { b = in.readAllBytes(); }
      DataInputStream d = new DataInputStream(new ByteArrayInputStream(b));
      if (d.readInt() != 0xCAFEBABE) throw new RuntimeException("not class");
      d.readUnsignedShort(); d.readUnsignedShort();
      int cpCount = d.readUnsignedShort();
      List<String> utf8 = new ArrayList<>();
      for (int i=1;i<cpCount;i++) {
        int tag = d.readUnsignedByte();
        switch(tag) {
          case 1 -> utf8.add(d.readUTF());
          case 3,4 -> d.readInt();
          case 5,6 -> { d.readLong(); i++; }
          case 7,8,16,19,20 -> d.readUnsignedShort();
          case 9,10,11,12,18 -> { d.readUnsignedShort(); d.readUnsignedShort(); }
          case 15 -> { d.readUnsignedByte(); d.readUnsignedShort(); }
          case 17 -> { d.readUnsignedShort(); d.readUnsignedShort(); }
          default -> throw new RuntimeException("unknown tag " + tag + " at " + i);
        }
      }
      utf8.stream().filter(s -> s.toUpperCase().contains("PVP") || s.toUpperCase().contains("BLOCK") || s.toUpperCase().contains("INTERACT") || s.toUpperCase().contains("ATTACK") || s.toUpperCase().contains("EDIT") || s.toUpperCase().contains("BREAK") || s.toUpperCase().contains("PLACE") || s.toUpperCase().contains("ALLOW")).distinct().forEach(System.out::println);
      System.out.println("---ALL SMALL SAMPLE---");
      utf8.stream().filter(s -> s.length() < 60).distinct().limit(120).forEach(System.out::println);
    }
  }
}

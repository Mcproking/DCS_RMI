import java.util.zip.*;
public class ListZip {
    public static void main(String[] a) throws Exception {
        ZipFile z = new ZipFile(a[0]);
        z.stream()
         .map(ZipEntry::getName)
         .filter(name -> name.toLowerCase().contains("cyberpunk") || name.toLowerCase().contains("dark") || name.toLowerCase().contains("neon"))
         .forEach(System.out::println);
    }
}

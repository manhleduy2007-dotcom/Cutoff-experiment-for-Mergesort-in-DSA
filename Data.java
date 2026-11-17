import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
public class Data {

    static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};
    static final String[] DATA_TYPES = {"random", "sorted", "reversed", "nearly_sorted", "few_unique"};
    static final long DEFAULT_SEED = 20261116L;

    public static void main(String[] args) throws IOException {
        Path outDir = Paths.get(args.length > 0 ? args[0] : "data");
        long seed = args.length > 1 ? Long.parseLong(args[1]) : DEFAULT_SEED;
        Files.createDirectories(outDir);

        for (int n : SIZES) 
        {
            for (String type : DATA_TYPES) 
            {
                Random rnd = new Random(seed ^ ((long) n * 31 + type.hashCode()));
                int[] a = generate(type, n, rnd);
                Path file = outDir.resolve(type + "_" + n + ".txt");
                write(file, a);
                System.out.printf("Da ghi %-28s (%,d phan tu)%n", file, n);
            }
        }
        System.out.println("Xong. Seed = " + seed);
    }

    static int[] generate(String type, int n, Random rnd) {
        int[] a = new int[n];
        switch (type) {
            case "random":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(Integer.MAX_VALUE);
                break;
            case "sorted":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(Integer.MAX_VALUE);
                Arrays.sort(a);
                break;
            case "reversed":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(Integer.MAX_VALUE);
                Arrays.sort(a);
                for (int i = 0, j = n - 1; i < j; i++, j--) {
                    int t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            case "nearly_sorted":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(Integer.MAX_VALUE);
                Arrays.sort(a);
                for (int s = 0; s < Math.max(1, n / 100); s++) {
                    int i = rnd.nextInt(n), j = rnd.nextInt(n);
                    int t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            case "few_unique":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(10);
                break;
            default:
                throw new IllegalArgumentException("Kieu du lieu khong ho tro: " + type);
        }
        return a;
    }

    static void write(Path file, int[] a) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            for (int v : a) {
                w.write(Integer.toString(v));
                w.newLine();
            }
        }
    }
}
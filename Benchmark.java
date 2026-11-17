import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
@SuppressWarnings({"rawtypes"})
public class Benchmark {

    static final int[] CUTOFFS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 24, 28, 32, 40, 48, 64};
    static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};
    static final String[] DATA_TYPES = {"random", "sorted", "reversed", "nearly_sorted", "few_unique"};
    static final int BASELINE_CUTOFF = 1;
    static final int MIN_ELEMENTS_PER_MEASUREMENT = 200_000;
    static final long ORDER_SEED = 42L;

    public static void main(String[] args) throws IOException {
        int runs = args.length > 0 ? Integer.parseInt(args[0]) : 25;
        int warmupRounds = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        Path dataDir = Paths.get(args.length > 2 ? args[2] : "data");
        Path outDir = Paths.get(args.length > 3 ? args[3] : "results");
        Files.createDirectories(outDir);

        writeEnvironment(outDir.resolve("environment.txt"), runs, warmupRounds);

        Random orderRnd = new Random(ORDER_SEED);
        List<String> bestLines = new ArrayList<>();

        try (BufferedWriter raw = Files.newBufferedWriter(outDir.resolve("raw.csv"));
             BufferedWriter summary = Files.newBufferedWriter(outDir.resolve("summary.csv"))) {

            raw.write("data_type,n,cutoff,run,time_ns\n");
            summary.write("data_type,n,cutoff,runs,median_ms,mean_ms,std_ms,min_ms,ratio_vs_baseline\n");

            for (String type : DATA_TYPES) {
                for (int n : SIZES) {
                    Integer[] base = load(dataDir.resolve(type + "_" + n + ".txt"), n);
                    Integer[] expected = base.clone();
                    Arrays.sort(expected);

                    int copies = Math.max(1, MIN_ELEMENTS_PER_MEASUREMENT / n);
                    Comparable[][] work = new Comparable[copies][n];

                    System.out.printf("%n== %s, N = %,d (%d ban sao moi lan do) ==%n", type, n, copies);
                    for (int round = 0; round < warmupRounds; round++) {
                        for (int cutoff : CUTOFFS) {
                            timeOnce(base, work, cutoff);
                            if (round == 0 && !Arrays.equals(work[0], expected)) {
                                throw new IllegalStateException(String.format(
                                    "Ket qua SAI voi cutoff=%d, kieu=%s, N=%d. Kiem tra lai MergeCutoff.", cutoff, type, n));
                            }
                        }
                    }
                    double[][] times = new double[CUTOFFS.length][runs];
                    int[] order = new int[CUTOFFS.length];
                    for (int i = 0; i < order.length; i++) order[i] = i;

                    for (int run = 0; run < runs; run++) {
                        shuffle(order, orderRnd);
                        System.gc();
                        for (int idx : order) {
                            double ns = timeOnce(base, work, CUTOFFS[idx]);
                            times[idx][run] = ns;
                            raw.write(String.format(Locale.ROOT, "%s,%d,%d,%d,%d%n",
                                type, n, CUTOFFS[idx], run + 1, Math.round(ns)));
                        }
                    }
                    raw.flush();
                    double baselineMedian = Double.NaN;
                    double[] medians = new double[CUTOFFS.length];
                    for (int i = 0; i < CUTOFFS.length; i++) {
                        medians[i] = median(times[i]);
                        if (CUTOFFS[i] == BASELINE_CUTOFF) baselineMedian = medians[i];
                    }
                    int best = 0;
                    for (int i = 0; i < CUTOFFS.length; i++) {
                        if (medians[i] < medians[best]) best = i;
                        summary.write(String.format(Locale.ROOT, "%s,%d,%d,%d,%.6f,%.6f,%.6f,%.6f,%.4f%n",
                            type, n, CUTOFFS[i], runs,
                            medians[i] / 1e6, mean(times[i]) / 1e6, std(times[i]) / 1e6, min(times[i]) / 1e6,
                            medians[i] / baselineMedian));
                    }
                    summary.flush();
                    String line = String.format(Locale.ROOT,
                        "%-14s N=%-9d cutoff tot nhat = %-3d (%.4f ms, bang %.1f%% so voi cutoff=1)",
                        type, n, CUTOFFS[best], medians[best] / 1e6, 100.0 * medians[best] / baselineMedian);
                    bestLines.add(line);
                    System.out.println(line);
                }
            }
        }
        System.out.println("\n===== TONG KET (theo median) =====");
        for (String line : bestLines) System.out.println(line);
        System.out.println("\nDa ghi ket qua vao thu muc: " + outDir.toAbsolutePath());
    }
    private static double timeOnce(Integer[] base, Comparable[][] work, int cutoff) {
        for (Comparable[] w : work) System.arraycopy(base, 0, w, 0, base.length);

        long start = System.nanoTime();
        for (Comparable[] w : work) {
            MergeSortCutOff.sort(w, cutoff);         
        }
        long elapsed = System.nanoTime() - start;

        if (!isSorted(work[work.length - 1])) {
            throw new IllegalStateException("Mang chua duoc sap xep voi cutoff = " + cutoff);
        }
        return (double) elapsed / work.length;
    }

    @SuppressWarnings("unchecked")
    private static boolean isSorted(Comparable[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i].compareTo(a[i - 1]) < 0) return false;
        }
        return true;
    }

    private static Integer[] load(Path file, int n) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("Khong tim thay " + file + ". Hay chay 'java DataGen' truoc.");
        }
        List<String> lines = Files.readAllLines(file);
        if (lines.size() != n) {
            throw new IOException(file + " co " + lines.size() + " dong, mong doi " + n);
        }
        Integer[] a = new Integer[n];
        for (int i = 0; i < n; i++) a[i] = Integer.valueOf(lines.get(i).trim());
        return a;
    }

    private static void shuffle(int[] a, Random rnd) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    private static double median(double[] x) {
        double[] s = x.clone();
        Arrays.sort(s);
        int m = s.length / 2;
        return (s.length % 2 == 1) ? s[m] : (s[m - 1] + s[m]) / 2.0;
    }

    private static double mean(double[] x) {
        double sum = 0;
        for (double v : x) sum += v;
        return sum / x.length;
    }

    private static double std(double[] x) {
        if (x.length < 2) return 0;
        double m = mean(x), ss = 0;
        for (double v : x) ss += (v - m) * (v - m);
        return Math.sqrt(ss / (x.length - 1));
    }

    private static double min(double[] x) {
        double m = x[0];
        for (double v : x) m = Math.min(m, v);
        return m;
    }

    private static void writeEnvironment(Path file, int runs, int warmupRounds) throws IOException {
        Runtime rt = Runtime.getRuntime();
        List<String> lines = new ArrayList<>();
        lines.add("os.name            = " + System.getProperty("os.name"));
        lines.add("os.version         = " + System.getProperty("os.version"));
        lines.add("os.arch            = " + System.getProperty("os.arch"));
        lines.add("java.version       = " + System.getProperty("java.version"));
        lines.add("java.vm.name       = " + System.getProperty("java.vm.name"));
        lines.add("java.vm.version    = " + System.getProperty("java.vm.version"));
        lines.add("available CPUs     = " + rt.availableProcessors());
        lines.add("max heap (MB)      = " + rt.maxMemory() / (1024 * 1024));
        lines.add("runs per config    = " + runs);
        lines.add("warm-up rounds     = " + warmupRounds);
        lines.add("cutoffs            = " + Arrays.toString(CUTOFFS));
        lines.add("sizes              = " + Arrays.toString(SIZES));
        lines.add("data types         = " + Arrays.toString(DATA_TYPES));
        lines.add("(Tu bo sung: ten CPU, dung luong RAM, may cam sac hay dung pin.)");
        Files.write(file, lines);
    }
}
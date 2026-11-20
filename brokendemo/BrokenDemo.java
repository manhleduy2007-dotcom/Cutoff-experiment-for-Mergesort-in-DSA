import java.util.Arrays;
import java.util.Random;

/**
 * Thi nghiem minh hoa cho muc f (kiem tra tinh dung).
 *
 * So sanh thoi gian chay cua ban dung (MergeSortCutOff) va ban loi co y (BrokenSort)
 * tren cung mot mang 10.000 so ngau nhien, voi nhieu gia tri CUTOFF.
 * Dong thoi kiem tra ban loi co sap xep dung hay khong.
 *
 * Bien dich va chay (dung o thu muc project):
 *     javac -d brokendemo/out MergeSortCutOff.java brokendemo/*.java
 *     java -cp brokendemo/out BrokenDemo
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BrokenDemo {

    private static final int N = 10_000;
    private static final long SEED = 1L;
    private static final int[] CUTOFFS = {1, 4, 7, 10, 16, 32, 64};

    private static final int COPIES = 20;   // so ban sao sap xep trong mot lan bam gio
    private static final int WARMUP = 5;    // so lan chay bo, khong tinh
    private static final int RUNS = 25;     // so lan do that

    /** Giao dien chung de goi ca hai ban sap xep. */
    private interface Sorter {
        void sort(Comparable[] a, int cutoff);
    }

    public static void main(String[] args) {
        Integer[] base = randomArray(N, SEED);

        Sorter correct = MergeSortCutOff::sort;
        Sorter broken = BrokenSort::sort;

        System.out.println("N = " + N + ", du lieu ngau nhien, thoi gian median (ms)");
        System.out.printf("%-8s %-10s %-10s %s%n", "CUTOFF", "Ban dung", "Ban loi", "Ban loi sap xep dung?");

        for (int cutoff : CUTOFFS) {
            double correctMs = medianTime(correct, base, cutoff);
            double brokenMs = medianTime(broken, base, cutoff);

            Comparable[] check = base.clone();
            broken.sort(check, cutoff);
            String verdict = isSorted(check) ? "Dung" : "SAI";

            System.out.printf("%-8d %-10.3f %-10.3f %s%n", cutoff, correctMs, brokenMs, verdict);
        }
    }

    /** Thoi gian median (ms) cua mot lan sap xep, sau WARMUP lan chay bo. */
    private static double medianTime(Sorter sorter, Integer[] base, int cutoff) {
        double[] times = new double[RUNS];
        Comparable[][] work = new Comparable[COPIES][];

        for (int run = 0; run < WARMUP + RUNS; run++) {
            for (int k = 0; k < COPIES; k++) {
                work[k] = base.clone();                 // copy truoc khi bam gio
            }

            long start = System.nanoTime();
            for (Comparable[] w : work) {
                sorter.sort(w, cutoff);
            }
            long elapsed = System.nanoTime() - start;

            if (run >= WARMUP) {
                times[run - WARMUP] = elapsed / (double) COPIES / 1e6;
            }
        }

        Arrays.sort(times);
        return times[RUNS / 2];
    }

    private static Integer[] randomArray(int n, long seed) {
        Random rnd = new Random(seed);
        Integer[] a = new Integer[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt();
        }
        return a;
    }

    private static boolean isSorted(Comparable[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i].compareTo(a[i - 1]) < 0) {
                return false;
            }
        }
        return true;
    }
}
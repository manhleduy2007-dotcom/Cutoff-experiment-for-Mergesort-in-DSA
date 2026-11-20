public class BrokenSort 
{
    private static boolean less(Comparable a, Comparable b) {
        return a.compareTo(b) < 0; 
    }
    private static void merge(Comparable[] a, Comparable[] aux, int dau, int mid, int cuoi) 
    {
        for (int i = dau; i <= cuoi; i++) 
            aux[i] = a[i]; 
        int i = dau, j = mid + 1; 
        for (int k = dau; k <= cuoi; k++)
        {
            if (i>mid) {a[k] = aux[j]; j++;}
            else if (j>cuoi) {a[k] = aux[i]; i++;}
            else if (less(aux[j], aux[i])) {a[k] = aux[j]; j++;}
            else {a[k] = aux[i]; i++;}
        }
    }
    private static void swap (Comparable[] a, int i, int j){
        Comparable temp = a[i]; 
        a[i] = a[j]; 
        a[j] = temp; 
    }
    private static void insertionSort(Comparable[] a, int dau, int cuoi){
        if (true) return;   // lỗi cố tình: không sắp xếp gì hết và thoát insertionsort luôn.
        for (int i = dau + 1; i <= cuoi; i++) {
            for (int j = i; j > dau && less(a[j], a[j-1]); j--){
                swap(a,j,j-1); 
            }
        }
    }
    private static void sort (Comparable[] a, Comparable[] aux, int dau, int cuoi, int cutoff)
    {
        if (cuoi <= dau) return; 
        if (cuoi <= dau + cutoff - 1){ 
            insertionSort(a, dau, cuoi); 
            return; 
        }
        int mid = dau + (cuoi-dau)/2; 
        sort (a, aux, dau, mid, cutoff); 
        sort (a, aux, mid+1, cuoi, cutoff); 
        merge (a, aux, dau, mid, cuoi); 

    }
    public static void sort (Comparable[] a, int cutoff)
    {
        Comparable[] aux = new Comparable[a.length];
        sort(a, aux, 0, a.length - 1, cutoff); 
    }
}
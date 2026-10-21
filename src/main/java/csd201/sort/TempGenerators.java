package csd201.sort;

import java.util.Arrays;
import java.util.SplittableRandom;

/** BẢN TẠM cho D1-D5. Thay bằng generator của Người 2 khi họ bàn giao. Khóa không âm. */
public final class TempGenerators {

    private TempGenerators() {
    }

    public static int[] generate(String data, int n, long seed, int k) {
        SplittableRandom r = new SplittableRandom(seed);
        int[] a = new int[n];

        switch (data) {
            case "D1":
                for (int i = 0; i < n; i++) {
                    a[i] = r.nextInt() >>> 1;
                }
                break;
            case "D2":
                a = generate("D1", n, seed, k);
                Arrays.sort(a);
                break;
            case "D3": {
                a = generate("D2", n, seed, k);
                for (int i = 0; i < n / 1000; i++) {
                    int x = r.nextInt(n);
                    int y = r.nextInt(n);
                    int t = a[x];
                    a[x] = a[y];
                    a[y] = t;
                }
                break;
            }
            case "D4": {
                a = generate("D2", n, seed, k);
                for (int i = 0, j = n - 1; i < j; i++, j--) {
                    int t = a[i];
                    a[i] = a[j];
                    a[j] = t;
                }
                break;
            }
            case "D5": {
                int[] vals = new int[k];
                for (int i = 0; i < k; i++) {
                    vals[i] = r.nextInt() >>> 1;
                }
                for (int i = 0; i < n; i++) {
                    a[i] = vals[r.nextInt(k)];
                }
                break;
            }
            default:
                throw new IllegalArgumentException("data khong hop le: " + data);
        }
        return a;
    }
}
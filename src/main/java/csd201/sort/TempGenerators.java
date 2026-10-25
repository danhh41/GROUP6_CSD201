package csd201.sort;

/**
 * @deprecated Bản tạm ban đầu. Đã được thay thế hoàn toàn bởi {@link DataGenerator}.
 * Lớp này được giữ lại để đảm bảo tính tương thích ngược với các module khác.
 */
@Deprecated
public final class TempGenerators {

    private TempGenerators() {
    }

    public static int[] generate(String data, int n, long seed, int k) {
        return DataGenerator.generate(data, n, seed, k);
    }
}
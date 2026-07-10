// ============================================================
// ADVANCED JAVA · HAPPY NUMBER · ENTERPRISE LUXE
// ============================================================
// domain: number theory, OOP, memoization, concurrency-ready
// references: Bloch, J. (2018). Effective Java (3rd ed.).
//             Sedgewick, R., & Wayne, K. (2017). Algorithms (4th ed.).
// ============================================================

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Immutable result record for happy number analysis.
 * Using Java 14+ record for value objects.
 */
record HappyAnalysis(int number, boolean isHappy, List<Integer> trace, boolean cycleDetected) {
    @Override
    public String toString() {
        return String.format("HappyAnalysis[number=%d, isHappy=%b, trace=%s, cycle=%b]",
                number, isHappy, trace, cycleDetected);
    }
}

/**
 * Statistical summary record.
 */
record HappyStatistics(int limit, int total, int happyCount, int unhappyCount,
                       double density, List<Integer> happySample) {
    @Override
    public String toString() {
        return String.format("Statistics{limit=%d, density=%.2f%%, sample=%s}",
                limit, density * 100, happySample);
    }
}

/**
 * Thread-safe happy number engine with caching and analysis capabilities.
 */
public class HappyNumberEngine {
    private final Map<Integer, Boolean> cache;
    private final int maxCacheSize;

    /**
     * Constructs a new engine with a bounded cache.
     * @param maxCacheSize maximum number of entries to retain
     */
    public HappyNumberEngine(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        this.cache = new ConcurrentHashMap<>(maxCacheSize);
    }

    /**
     * Default constructor with 10,000 cache size.
     */
    public HappyNumberEngine() {
        this(10_000);
    }

    /**
     * Computes sum of squares of digits using integer arithmetic.
     * Time complexity: O(log n), Space: O(1)
     */
    private static int sumOfSquares(int n) {
        int total = 0;
        int num = n;
        while (num > 0) {
            int digit = num % 10;
            total += digit * digit;
            num /= 10;
        }
        return total;
    }

    /**
     * Returns true if the number is happy, false otherwise.
     * Uses memoization for repeated queries.
     */
    public boolean isHappy(int n) {
        if (n <= 0) {
            return false;
        }

        // check cache
        Boolean cached = cache.get(n);
        if (cached != null) {
            return cached;
        }

        Set<Integer> seen = new HashSet<>();
        int current = n;

        while (current != 1 && !seen.contains(current)) {
            seen.add(current);
            current = sumOfSquares(current);
        }

        boolean result = (current == 1);

        // store in cache if within limit
        if (cache.size() < maxCacheSize) {
            cache.put(n, result);
        }

        return result;
    }

    /**
     * Provides detailed analysis including the full trace.
     */
    public HappyAnalysis analyze(int n) {
        if (n <= 0) {
            return new HappyAnalysis(n, false, List.of(), false);
        }

        Set<Integer> seen = new HashSet<>();
        List<Integer> trace = new ArrayList<>();
        int current = n;
        boolean cycleDetected = false;

        while (current != 1 && !seen.contains(current)) {
            seen.add(current);
            trace.add(current);
            current = sumOfSquares(current);
        }

        trace.add(current);
        if (current != 1) {
            cycleDetected = true;
        }

        return new HappyAnalysis(n, current == 1, Collections.unmodifiableList(trace), cycleDetected);
    }

    /**
     * Batch process multiple numbers.
     */
    public List<HappyAnalysis> batchAnalyze(int[] numbers) {
        List<HappyAnalysis> results = new ArrayList<>(numbers.length);
        for (int num : numbers) {
            results.add(analyze(num));
        }
        return results;
    }

    /**
     * Generate statistical summary for range [1, limit].
     * Uses parallel streams for improved performance.
     */
    public HappyStatistics statistics(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be >= 1");
        }

        // parallel processing for large ranges
        List<Integer> happyNumbers = IntStream.rangeClosed(1, limit)
                .parallel()
                .filter(this::isHappy)
                .boxed()
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        return new HappyStatistics(
                limit,
                limit,
                happyNumbers.size(),
                limit - happyNumbers.size(),
                (double) happyNumbers.size() / limit,
                happyNumbers.stream().limit(20).toList()
        );
    }

    /**
     * Clear the result cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Get current cache size.
     */
    public int cacheSize() {
        return cache.size();
    }

    /**
     * Main entry point for demonstration and testing.
     */
    public static void main(String[] args) {
        HappyNumberEngine engine = new HappyNumberEngine();

        // Test data: [number, expected]
        int[][] testData = {
                {1, 1}, {2, 0}, {7, 1}, {10, 1}, {13, 1},
                {19, 1}, {23, 1}, {28, 1}, {31, 1}, {32, 1}, {33, 0}
        };

        System.out.println("🧪 HAPPY NUMBER JAVA TEST SUITE");
        System.out.println("=".repeat(50));

        boolean allPassed = true;
        for (int[] pair : testData) {
            int num = pair[0];
            boolean expected = pair[1] == 1;
            boolean result = engine.isHappy(num);
            HappyAnalysis analysis = engine.analyze(num);
            String status = (result == expected) ? "✅" : "❌";
            String traceStr = analysis.trace().stream()
                    .limit(5)
                    .map(String::valueOf)
                    .reduce((a, b) -> a + " → " + b)
                    .orElse("");
            if (analysis.trace().size() > 5) traceStr += " …";

            System.out.printf("%s happy(%d) → %b (expected %b)  trace: %s%n",
                    status, num, result, expected, traceStr);

            if (result != expected) allPassed = false;
        }

        System.out.println("=".repeat(50));
        System.out.printf("OVERALL: %s%n",
                allPassed ? "✅ ALL TESTS PASSED" : "❌ SOME TESTS FAILED");

        // Statistics
        HappyStatistics stats = engine.statistics(100);
        System.out.printf("%n📊 STATISTICAL ANALYSIS (1–100)%n");
        System.out.printf("  Density: %.2f%%%n", stats.density() * 100);
        System.out.printf("  First 10 happy numbers: %s%n",
                stats.happySample().stream().limit(10).toList());

        // Performance benchmark
        long start = System.nanoTime();
        for (int i = 1; i <= 10_000; i++) {
            engine.isHappy(i);
        }
        long elapsed = System.nanoTime() - start;
        double ms = elapsed / 1_000_000.0;
        System.out.printf("%n⚡ Performance: checked 10,000 numbers in %.2fms%n", ms);
    }
}

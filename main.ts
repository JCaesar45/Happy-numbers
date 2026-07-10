// ============================================================
// ADVANCED TYPESCRIPT · HAPPY NUMBER · TYPE-SAFE LUXE
// ============================================================
// domain: number theory, functional programming, type predicates
// references: Pierce, B.C. (2002). Types and Programming Languages.
//             Hutton, G. (2016). Programming in Haskell (2nd ed.).
// ============================================================

type HappyResult = {
  readonly number: number;
  readonly isHappy: boolean;
  readonly trace: readonly number[];
  readonly cycleDetected: boolean;
};

type Statistics = {
  readonly limit: number;
  readonly total: number;
  readonly happyCount: number;
  readonly unhappyCount: number;
  readonly density: number;
  readonly happySample: readonly number[];
};

class HappyNumberAnalyzer {
  private readonly cache: Map<number, boolean> = new Map();

  /**
   * Compute sum of squares of digits without string conversion.
   * O(log n) time, O(1) space.
   */
  private static sumOfSquares(n: number): number {
    let total = 0;
    let num = n;
    while (num > 0) {
      const digit = num % 10;
      total += digit * digit;
      num = Math.floor(num / 10);
    }
    return total;
  }

  /**
   * Core algorithm with type-safe result.
   * Uses Set for O(1) lookups during cycle detection.
   */
  public isHappy(n: number): boolean {
    if (n <= 0 || !Number.isInteger(n)) {
      return false;
    }

    // cache hit
    if (this.cache.has(n)) {
      return this.cache.get(n)!;
    }

    const seen = new Set<number>();
    let current = n;

    while (current !== 1 && !seen.has(current)) {
      seen.add(current);
      current = HappyNumberAnalyzer.sumOfSquares(current);
    }

    const result = current === 1;
    this.cache.set(n, result);
    return result;
  }

  /**
   * Returns detailed analysis including the full trace.
   */
  public analyze(n: number): HappyResult {
    if (n <= 0 || !Number.isInteger(n)) {
      return {
        number: n,
        isHappy: false,
        trace: [],
        cycleDetected: false,
      };
    }

    const seen = new Set<number>();
    const trace: number[] = [];
    let current = n;
    let cycleDetected = false;

    while (current !== 1 && !seen.has(current)) {
      seen.add(current);
      trace.push(current);
      current = HappyNumberAnalyzer.sumOfSquares(current);
    }

    trace.push(current);
    if (current !== 1) {
      cycleDetected = true;
    }

    return {
      number: n,
      isHappy: current === 1,
      trace: trace as readonly number[],
      cycleDetected,
    };
  }

  /**
   * Batch process multiple numbers efficiently.
   */
  public batchCheck(numbers: readonly number[]): readonly HappyResult[] {
    return numbers.map((n) => this.analyze(n));
  }

  /**
   * Generate statistical summary for range [1, limit].
   */
  public statistics(limit: number): Statistics {
    if (limit < 1) {
      throw new Error('Limit must be >= 1');
    }

    const happyNumbers: number[] = [];
    for (let i = 1; i <= limit; i++) {
      if (this.isHappy(i)) {
        happyNumbers.push(i);
      }
    }

    return {
      limit,
      total: limit,
      happyCount: happyNumbers.length,
      unhappyCount: limit - happyNumbers.length,
      density: happyNumbers.length / limit,
      happySample: happyNumbers.slice(0, 20) as readonly number[],
    };
  }

  /**
   * Clear the internal cache to free memory.
   */
  public clearCache(): void {
    this.cache.clear();
  }
}

// ============================================================
// FUNCTIONAL EXPORT (compatible with test harness)
// ============================================================

export function happy(n: number): boolean {
  const analyzer = new HappyNumberAnalyzer();
  return analyzer.isHappy(n);
}

// ============================================================
// SELF-TEST & DEMONSTRATION
// ============================================================

if (require.main === module) {
  const analyzer = new HappyNumberAnalyzer();

  const testData: [number, boolean][] = [
    [1, true],
    [2, false],
    [7, true],
    [10, true],
    [13, true],
    [19, true],
    [23, true],
    [28, true],
    [31, true],
    [32, true],
    [33, false],
  ];

  console.log('🧪 HAPPY NUMBER TYPE-SAFE TEST SUITE');
  console.log('='.repeat(50));

  let allPassed = true;
  for (const [num, expected] of testData) {
    const result = analyzer.isHappy(num);
    const analysis = analyzer.analyze(num);
    const status = result === expected ? '✅' : '❌';
    const traceStr = analysis.trace.slice(0, 5).join(' → ');
    const suffix = analysis.trace.length > 5 ? ' …' : '';
    console.log(`${status} happy(${num}) → ${result} (expected ${expected})  trace: ${traceStr}${suffix}`);
    if (result !== expected) allPassed = false;
  }

  console.log('='.repeat(50));
  console.log(`OVERALL: ${allPassed ? '✅ ALL TESTS PASSED' : '❌ SOME TESTS FAILED'}`);

  const stats = analyzer.statistics(100);
  console.log('\n📊 STATISTICAL ANALYSIS (1–100)');
  console.log(`  Density: ${(stats.density * 100).toFixed(2)}%`);
  console.log(`  First 10 happy numbers: ${stats.happySample.slice(0, 10).join(', ')}`);

  // performance benchmark
  const start = performance.now();
  for (let i = 1; i <= 10000; i++) {
    analyzer.isHappy(i);
  }
  const elapsed = performance.now() - start;
  console.log(`\n⚡ Performance: checked 10,000 numbers in ${elapsed.toFixed(2)}ms`);
}

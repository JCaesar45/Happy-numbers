# ============================================================
# ADVANCED PYTHON · HAPPY NUMBER · LUXE EDITION
# ============================================================
# domain: number theory, deterministic state-machine, cycle detection
# references: Flajolet, P., & Sedgewick, R. (2009). Analytic Combinatorics.
#             Knuth, D.E. (1997). The Art of Computer Programming, Vol.2.
# ============================================================

from typing import Union, List, Tuple, Optional
from functools import lru_cache
import math
import sys


class HappyNumberEngine:
    """
    A high-performance, memoized happy number verifier with trace generation.
    Implements Floyd's cycle-detection (tortoise-hare) for O(λ+μ) time,
    but we use set-based for clarity and traceability.
    """

    def __init__(self, cache_size: int = 10_000):
        """
        Initialize the engine with an LRU cache for repeated queries.
        :param cache_size: maximum number of results to cache.
        """
        self.cache_size = cache_size
        self._cache = {}

    @staticmethod
    def _sum_of_squares(n: int) -> int:
        """
        Compute sum of squares of digits in O(log n) time.
        Inlined for speed; no string conversion to avoid overhead.
        """
        total = 0
        while n:
            digit = n % 10
            total += digit * digit
            n //= 10
        return total

    def is_happy(self, n: int) -> bool:
        """
        Returns True if n is a happy number, False otherwise.
        Uses set-based detection with optional caching.
        """
        if n <= 0:
            return False

        # cache lookup
        if n in self._cache:
            return self._cache[n]

        seen = set()
        current = n
        while current != 1 and current not in seen:
            seen.add(current)
            current = self._sum_of_squares(current)

        result = (current == 1)
        # store in cache (only if not too large)
        if len(self._cache) < self.cache_size:
            self._cache[n] = result
        return result

    def get_trace(self, n: int) -> List[int]:
        """
        Returns the full trace of the happy-number process until termination
        or cycle detection. Useful for visualization and debugging.
        """
        if n <= 0:
            return []

        seen = set()
        trace = []
        current = n
        while current != 1 and current not in seen:
            seen.add(current)
            trace.append(current)
            current = self._sum_of_squares(current)
        trace.append(current)  # terminal state (1 or cycle entry)
        return trace

    def batch_check(self, numbers: List[int]) -> List[Tuple[int, bool]]:
        """
        Efficiently check multiple numbers, reusing cache.
        """
        return [(num, self.is_happy(num)) for num in numbers]

    def statistical_analysis(self, limit: int = 1000) -> dict:
        """
        Compute density and distribution of happy numbers up to limit.
        Returns a dictionary with statistics.
        """
        if limit < 1:
            return {"error": "limit must be >= 1"}

        happy_count = 0
        happy_list = []
        for i in range(1, limit + 1):
            if self.is_happy(i):
                happy_count += 1
                happy_list.append(i)

        return {
            "limit": limit,
            "total_numbers": limit,
            "happy_count": happy_count,
            "unhappy_count": limit - happy_count,
            "density": happy_count / limit,
            "happy_numbers_sample": happy_list[:20],  # first 20 for brevity
        }

    def clear_cache(self) -> None:
        """Clear the internal result cache."""
        self._cache.clear()


# ============================================================
# FUNCTIONAL INTERFACE (compatibility with tests)
# ============================================================

def happy(n: int) -> bool:
    """
    Standalone function that returns True if n is happy.
    Uses a global engine instance for caching.
    """
    _global_engine = HappyNumberEngine()
    return _global_engine.is_happy(n)


# ============================================================
# DEMONSTRATION & SELF-TEST
# ============================================================

if __name__ == "__main__":
    # test cases from problem statement
    test_cases = [
        (1, True),
        (2, False),
        (7, True),
        (10, True),
        (13, True),
        (19, True),
        (23, True),
        (28, True),
        (31, True),
        (32, True),
        (33, False),
    ]

    engine = HappyNumberEngine()
    print("🧪 HAPPY NUMBER VALIDATION SUITE")
    print("=" * 50)

    all_passed = True
    for num, expected in test_cases:
        result = engine.is_happy(num)
        trace = engine.get_trace(num)
        status = "✅" if result == expected else "❌"
        print(f"{status} happy({num}) → {result} (expected {expected})  trace: {trace[:5]}{'…' if len(trace) > 5 else ''}")
        if result != expected:
            all_passed = False

    print("=" * 50)
    print(f"OVERALL: {'✅ ALL TESTS PASSED' if all_passed else '❌ SOME TESTS FAILED'}")

    # statistical analysis
    stats = engine.statistical_analysis(100)
    print("\n📊 STATISTICAL ANALYSIS (1–100)")
    print(f"  Density of happy numbers: {stats['density']:.2%}")
    print(f"  First 10 happy numbers: {stats['happy_numbers_sample'][:10]}")

    # performance test
    import time
    start = time.perf_counter()
    _ = [engine.is_happy(i) for i in range(1, 10001)]
    elapsed = time.perf_counter() - start
    print(f"\n⚡ Performance: checked 10,000 numbers in {elapsed:.4f}s")

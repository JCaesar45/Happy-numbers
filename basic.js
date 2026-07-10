function happy(number) {
  // Track numbers we've already seen to detect cycles
  const seen = new Set();
  
  while (number !== 1 && !seen.has(number)) {
    seen.add(number);
    // Calculate sum of squares of digits
    number = String(number)
      .split('')
      .reduce((sum, digit) => sum + Math.pow(Number(digit), 2), 0);
  }
  
  return number === 1;
}

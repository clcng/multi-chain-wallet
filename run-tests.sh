#!/bin/bash

echo "=== Running Multi-Chain Wallet Test Suite ==="
echo ""

# Run all tests
echo "Running all tests..."
mvn clean test

# Check result
if [ $? -eq 0 ]; then
    echo ""
    echo "✓ All tests passed!"
    echo ""

    # Generate test report
    echo "Generating test report..."
    mvn surefire-report:report

    echo ""
    echo "Test report generated at: target/site/surefire-report.html"
else
    echo ""
    echo "✗ Some tests failed. Check output above."
    exit 1
fi
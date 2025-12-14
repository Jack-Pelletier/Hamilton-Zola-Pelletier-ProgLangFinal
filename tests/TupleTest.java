import org.junit.Test;

/**
 * JUnit tests for tuple features: tuple literals, proj, swap, destruct,
 * and tuples inside lists / nested tuple operations.
 *
 * NOTE ON EXPECTED EVAL STRINGS:
 * These assume your interpreter prints:
 *  - tuples as [a, b, c]  (since runtime tuples are LinkedList<Object>)
 *  - lists as  [a, b, c]
 *
 * If any eval tests fail ONLY due to formatting, paste the actual output
 * and I’ll adjust the expected strings to match your printer exactly.
 */
public class TupleTest extends LangTest
{
    /*
     * Tests for tuple literals and basic typing.
     */
    @Test
    public void simpleTupleLiteral()
    {
        runTypeTest("simpleTupleLiteral", "(1, 2);", "[int, int]");
        runEvalTest("simpleTupleLiteral", "(1, 2);", "[1, 2]");
    }

    @Test
    public void mixedTupleLiteral()
    {
        runTypeTest("mixedTupleLiteral", "(1, true, \"hi\");", "[int, bool, string]");
        runEvalTest("mixedTupleLiteral", "(1, true, \"hi\");", "[1, true, hi]");
    }

    /*
     * Tests for proj (0-based).
     * proj(i tupleExpr)
     */
    @Test
    public void projFirst()
    {
        runTypeTest("projFirst", "proj(0 (10, 20));", "int");
        runEvalTest("projFirst", "proj(0 (10, 20));", "10");
    }

    @Test
    public void projSecond()
    {
        runTypeTest("projSecond", "proj(1 (10, 20));", "int");
        runEvalTest("projSecond", "proj(1 (10, 20));", "20");
    }

    @Test
    public void projFromMixedTuple()
    {
        runTypeTest("projFromMixedTuple", "proj(2 (1, true, \"abc\"));", "string");
        runEvalTest("projFromMixedTuple", "proj(2 (1, true, \"abc\"));", "abc");
    }

    /*
     * Tests for swap.
     * swap(tupleExpr) expects a 2-tuple.
     */
    @Test
    public void simpleSwap()
    {
        runTypeTest("simpleSwap", "swap((1, 2));", "[int, int]");
        runEvalTest("simpleSwap", "swap((1, 2));", "[2, 1]");
    }

    @Test
    public void swapMixed()
    {
        runTypeTest("swapMixed", "swap((\"a\", 7));", "[int, string]");
        runEvalTest("swapMixed", "swap((\"a\", 7));", "[7, a]");
    }

    @Test
    public void swapThenProj()
    {
        runTypeTest("swapThenProj", "proj(0 swap((9, 8)));", "int");
        runEvalTest("swapThenProj", "proj(0 swap((9, 8)));", "8");
    }

    /*
     * Tests for destruct (tuple -> list).
     * destruct(tupleExpr) returns a list of elements.
     * Typing only works for uniform tuples (all same element type).
     */
    @Test
    public void destructUniformTuple()
    {
        runTypeTest("destructUniformTuple", "destruct((1, 2, 3));", "[ int ]");
        runEvalTest("destructUniformTuple", "destruct((1, 2, 3));", "[1, 2, 3]");
    }

    @Test
    public void destructThenHead()
    {
        runTypeTest("destructThenHead", "hd(destruct((4, 5, 6)));", "int");
        runEvalTest("destructThenHead", "hd(destruct((4, 5, 6)));", "4");
    }

    /*
     * Tuples inside lists.
     */
    @Test
    public void listOfTuples()
    {
        runTypeTest("listOfTuples", "[(1, 2), (3, 4)];", "[ [int, int] ]");
        runEvalTest("listOfTuples", "[(1, 2), (3, 4)];", "[[1, 2], [3, 4]]");
    }

    @Test
    public void projTupleInsideList()
    {
        runTypeTest("projTupleInsideList", "proj(1 hd([(10, 11), (20, 21)]));", "int");
        runEvalTest("projTupleInsideList", "proj(1 hd([(10, 11), (20, 21)]));", "11");
    }

    /*
     * Nested tuple operations.
     */
    @Test
    public void nestedTupleProj()
    {
        runTypeTest("nestedTupleProj", "proj(0 proj(1 ((1, 2), (3, 4))));", "int");
        runEvalTest("nestedTupleProj", "proj(0 proj(1 ((1, 2), (3, 4))));", "3");
    }

    @Test
    public void swapNestedTuple()
    {
        runTypeTest("swapNestedTuple", "swap(( (1, 2), (3, 4) ));", "[[int, int], [int, int]]");
        runEvalTest("swapNestedTuple", "swap(( (1, 2), (3, 4) ));", "[[3, 4], [1, 2]]");
    }
}


/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import org.junit.Test;

public class ListTests extends LangTest
{
    /*
     * Test the simple list construction.
     */

    @Test
    public void simpleIntList()
    {
        runTypeTest("simpleIntList", "[3, 5, 7];", "[ int ]");
        runEvalTest("simpleIntList", "[3, 5, 7];", "[3, 5, 7]");
    }

    @Test
    public void simpleRealList()
    {
        runTypeTest("simpleRealList", "[3.2, 4.1, 2.7];", "[ real ]");
        runEvalTest("simpleRealList", "[3.2, 4.1, 2.7];", "[3.2, 4.1, 2.7]");
    }

    @Test
    public void simpleBooleanList()
    {
        runTypeTest("simpleBooleanList", "[true, false, true];", "[ bool ]");
        runEvalTest("simpleBooleanList", "[true, false, true];",
                "[true, false, true]");
    }

    /*
     * Test of list constructor with expressions.
     */

    @Test
    public void intExprList()
    {
        runTypeTest("intExprList", "[3 * 2, 5 + 4, 7];", "[ int ]");
        runEvalTest("intExprList", "[3 * 2, 5 + 4, 7];", "[6, 9, 7]");
    }

    @Test
    public void realExprList()
    {
        runTypeTest("realExprList", "[3.0 * 2.0, 5.1 + 4.1, 7.0];", "[ real ]");
        runEvalTest("realExprList", "[3.0 * 2.0, 5.1 + 4.1, 7.0];",
                "[6.0, 9.2, 7.0]");
    }

    @Test
    public void boolExprList()
    {
        runTypeTest("boolExprList", "[7 > 2, 2 >= 2, 1 != 7];", "[ bool ]");
        runEvalTest("boolExprList", "[7 > 2, 2 >= 2, 1 != 7];",
                "[true, true, true]");
    }

    /*
     * Test the length of a list.
     */

    @Test
    public void zeroLenList()
    {
        runTypeTest("zeroLenList", "len([]);", "int");
        runEvalTest("zeroLenList", "len([]);", "0");
    }

    @Test
    public void nonzeroLenList()
    {
        runTypeTest("nonzeroLenList", "len([1, 3, 5]);", "int");
        runEvalTest("nonzeroLenList", "len([1, 3, 5]);", "3");
    }

    /*
     * List concatenation tests
     */
    @Test
    public void intListConcat()
    {
        runTypeTest("intListConcat", "[1, 3, 5] ++ [2, 3];", "[ int ]");
        runEvalTest("intListConcat", "[1, 3, 5] ++ [2, 3];", "[1, 3, 5, 2, 3]");
    }

    @Test
    public void realListConcat()
    {
        runTypeTest("realListConcat", "[1.0, 3.0, 5.0] ++ [];", "[ real ]");
        runEvalTest("realListConcat", "[1.0, 3.0, 5.0] ++ [];",
                "[1.0, 3.0, 5.0]");
    }

    @Test
    public void boolListConcat()
    {
        runTypeTest("boolListConcat", "[true, false, true] ++ [true];",
                "[ bool ]");
        runEvalTest("boolListConcat", "[true, false, true] ++ [true];",
                "[true, false, true, true]");
    }

    /*
     * List head test.
     */
    @Test
    public void intListHead()
    {
        runTypeTest("intListHead", "hd([3 * 2, 5 + 4, 7]);", "int");
        runEvalTest("intListHead", "hd([3 * 2, 5 + 4, 7]);", "6");
    }

    @Test
    public void realListHead()
    {
        runTypeTest("realListHead", "hd([3.0 * 2.0, 5.1 + 4.1, 7.0]);", "real");
        runEvalTest("realListHead", "hd([3.0 * 2.0, 5.1 + 4.1, 7.0]);", "6.0");
    }

    @Test
    public void boolListHead()
    {
        runTypeTest("boolListHead", "hd([7 > 2, 2 >= 2, 1 != 7]);", "bool");
        runEvalTest("boolListHead", "hd([7 > 2, 2 >= 2, 1 != 7]);", "true");
    }

    /*
     * Tail tests.
     */
    @Test
    public void intListTail()
    {
        runTypeTest("intListTail", "tl([3 * 2, 5 + 4, 7]);", "[ int ]");
        runEvalTest("intListTail", "tl([3 * 2, 5 + 4, 7]);", "[9, 7]");
    }

    @Test
    public void realListTail()
    {
        runTypeTest("realListTail", "tl([3.0 * 2.0]);", "[ real ]");
        runEvalTest("realListTail", "tl([3.0 * 2.0]);", "[]");
    }

    @Test
    public void boolListTail()
    {
        runTypeTest("boolListTail", "tl([7 > 2, 2 < 2]);", "[ bool ]");
        runEvalTest("boolListTail", "tl([7 > 2, 2 < 2]);", "[false]");
    }

    /*
     * Compound tests.
     */
    @Test
    public void hdConcatTest()
    {
        runTypeTest("hdConcatTest", "[hd([3, 5, 7])] ++ [4, 9];", "[ int ]");
        runEvalTest("hdConcatTest", "[hd([3, 5, 7])] ++ [4, 9];", "[3, 4, 9]");
    }

    @Test
    public void tlConcatTest()
    {
        runTypeTest("hdConcatTest", "tl([3]) ++ [1, 2];", "[ int ]");
        runEvalTest("hdConcatTest", "tl([3]) ++ [1, 2];", "[1, 2]");
    }

    @Test
    public void hdTlConcatTest()
    {
        runTypeTest("hdConcatTest", "[hd([3, 5])] ++ tl([6, 9, 12]);",
                "[ int ]");
        runEvalTest("hdConcatTest", "[hd([3, 5])] ++ tl([6, 9, 12]);",
                "[3, 9, 12]");
    }

    @Test
    public void nestedHdTest()
    {
        runTypeTest("nestedHdTest", "hd(tl([3, 4, 5]));",
                "int");
        runEvalTest("nestedHdTest", "hd(tl([3, 4, 5]));",
                "4");
    }

    @Test 
    public void nestedTlTest()
    {
        runTypeTest("nestedTlTest", "tl(tl([3, 4, 5]));",
                "[ int ]");
        runEvalTest("nestedTlTest", "tl(tl([3, 4, 5]));",
                "[5]");
    }
}

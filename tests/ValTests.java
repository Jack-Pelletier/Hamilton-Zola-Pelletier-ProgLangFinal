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

public class ValTests extends LangTest
{
    /**
     * Test the functionality of a simple val with an integer.
     */
    @Test
    public void simpleIntVal()
    {
        runTypeTest("simpleIntVal", "val x := 3;", "int");
        runEvalTest("simpleIntVal", "val x := 3;", "x");
    }

    /**
     * Test the functionality of a simple val with a real number.
     */
    @Test
    public void simpleRealVal()
    {
        runTypeTest("simpleRealVal", "val pi := 3.1415926;", "real");
        runEvalTest("simpleRealVal", "val pi := 3.1415926;", "pi");
    }

    @Test 
    public void simpleBooleanVal()
    {
        runTypeTest("simpleBooleanVal", "val flag := true;", "bool");
        runEvalTest("simpleBooleanVal", "val flag := true;", "flag");
    }

    @Test
    public void simpleListVal()
    {
        runTypeTest("simpleListVal", "val lst := [3, 6, 9];", "[ int ]");
        runEvalTest("simpleListVal", "val lst := [3, 6, 9];", "lst");
    }

    /**
     * Test a value with an integer arithmetic expression.
     */
    @Test
    public void valWithIntArith()
    {
        runTypeTest("valWithArith", "val y := 3 * 7 + 4;", "int");
        runEvalTest("valWithArith", "val y := 3 * 7 + 4;", "y");
    }

    /**
     * Test a value with an real arithmetic expression.
     */
    @Test
    public void valWithRealArith()
    {
        runTypeTest("valWithRealArith", "val z := 3.0 / 2.0 * 6.1 - .3;",
                "real");
        runEvalTest("valWithRealArith", "val z := 3.0 / 2.0 * 6.1 - .3;",
                "z");
    }

    @Test
    public void valWithListArith()
    {
        runTypeTest("valWithListArith", "val lst := [hd([3, 5])] ++ tl([6, 9, 12]);", "[ int ]");
        runEvalTest("valWithListArith", "val lst := [hd([3, 5])] ++ tl([6, 9, 12]);", "lst");
    }

    @Test
    public void namedFunc()
    {
        runTypeTest("namedFunc", "val inc := fn x -> x + 1;", "int -> int");
        runEvalTest("namedFunc", "val inc := fn x -> x + 1;", "inc");
    }

    @Test
    public void identityFunc()
    {
        runTypeTest("identityFunc", "val id := fn x -> x;", "t0 -> t0");
        runEvalTest("identityFunc", "val id := fn x -> x;", "id");
    }

    @Test 
    public void fabsFunc()
    {
        runTypeTest("fabsFunc", "val fabs := fn x -> if x < 0.0 then -1.0 * x else x;", "real -> real");
        runEvalTest("fabsFunc", "val fabs := fn x -> if x < 0.0 then -1.0 * x else x;", "fabs");
    }

    @Test 
    public void absFunc()
    {
        runTypeTest("absFunc", "val abs := fn x -> if x < 0 then -1 * x else x;", "int -> int");
        runEvalTest("absFunc", "val abs := fn x -> if x < 0 then -1 * x else x;", "abs");
    }
}


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

public class CompoundTests extends LangTest
{
    @Test
    public void equalNotEqual()
    {
        runTypeTest("equalNotEqual", "(5 != 3) or (2 = 2);", "bool");
        runEvalTest("equalNotEqual", "(5 != 3) or (2 = 2);", "true");
    }

    @Test
    public void falseAndExpr()
    {
        runTypeTest("falseAndExpr", "2 > 3 and 2 = 2;", "bool");
        runEvalTest("falseAndExpr", "2 > 3 and 2 = 2;", "false");
    }

    @Test
    public void trueAndExpr()
    {
        runTypeTest("trueAndExpr", "2 < 3 and 2 = 2;", "bool");
        runEvalTest("trueAndExpr", "2 < 3 and 2 = 2;", "true");
    }

    @Test
    public void falseOrExpr()
    {
        runTypeTest("falseOrExpr", "2 > 3 or 2 = 2;", "bool");
        runEvalTest("falseOrExpr", "2 > 3 or 2 = 2;", "true");
    }

    @Test
    public void trueOrExpr()
    {
        runTypeTest("trueOrExpr", "2 < 3 or 6 > 4;", "bool");
        runEvalTest("trueOrExpr", "2 < 3 or 6 > 4;", "true");
    }

    @Test
    public void lessAndMore()
    {
        runTypeTest("lessAndMore", "(5 < 7) and (7 > 3);", "bool");
        runEvalTest("lessAndMore", "(5 < 7) and (7 > 3);", "true");
    }

    @Test
    public void listLenLess()
    {
        runTypeTest("listLenLess", "len([5, 7]) < 4;", "bool");
        runEvalTest("listLenLess", "len([5, 7]) < 4;", "true");
    }

    @Test
    public void multiStatement()
    {
        runTypeTest("multiStatement",
                "val x := 3 + 5 * 2;\nval y := 4 + x;\nx > y;", "bool");
        runEvalTest("multiStatement",
                "val x := 3 + 5 * 2;\nval y := 4 + x;\nx > y;", "false");
    }

    @Test
    public void isEvenNamedFunc()
    {
        runTypeTest("isEvenNamedFunc",
                "val isEven := fn num -> num mod 2 = 0;\nisEven(4);", "bool");
        runEvalTest("isEvenNamedFunc",
                "val isEven := fn num -> num mod 2 = 0;\nisEven(4);", "true");
    }

    @Test
    public void recursionTest()
    {
        runTypeTest("recursionTest",
                "val fib := fn n -> if n = 1 or n = 2 then\n1\nelse\n fib(n - 1) + fib(n - 2);\nfib (4);",
                "int");
        runEvalTest("recursionTest",
                "val fib := fn n -> if n = 1 or n = 2 then\n1\nelse\n fib(n - 1) + fib(n - 2);\nfib (4);",
                "3");
    }

    @Test
    public void fabsEval()
    {
        runTypeTest("fabsEval",
                "val fabs := fn x -> if x < 0.0 then -1.0 * x else x;\nfabs(-3.2);",
                "real");
        runEvalTest("fabsFunc",
                "val fabs := fn x -> if x < 0.0 then -1.0 * x else x;\nfabs(-3.2);",
                "3.2");
    }

    @Test
    public void absEval()
    {
        runTypeTest("absFunc",
                "val abs := fn x -> if x < 0 then -1 * x else x;\nabs(2);",
                "int");
        runEvalTest("absFunc",
                "val abs := fn x -> if x < 0 then -1 * x else x;\nabs(2);", "2");
    }

    @Test
    public void mapEval()
    {
        String prog = "val foo := fn f -> fn lst -> \n"
                + "   if len(lst) = 0 then\n" + "      []\n" + "   else\n"
                + "      [f(hd(lst))] ++ (foo(f))(tl(lst));\n"
                + "(foo(fn x -> x + 1)) ([1, 3, 5]);";
        runTypeTest("mapEval", prog, "[ int ]");
        runEvalTest("mapEval", prog, "[2, 4, 6]");
    }

    @Test
    public void squareList()
    {
        String prog = "val foo := fn f -> fn lst -> \n"
                + "   if len(lst) = 0 then\n" + "      []\n" + "   else\n"
                + "      [f(hd(lst))] ++ (foo(f))(tl(lst));\n"
                + "(foo(fn x -> x * x))([2.0, 3.0, 5.0]);";
        runTypeTest("mapEval", prog, "[ real ]");
        runEvalTest("mapEval", prog, "[4.0, 9.0, 25.0]");
    }

    @Test
    public void reverseList()
    {
        String prog = "val revLst := fn lst ->\n"
                + "          if len(lst) = 1 then\n" 
                + "              lst\n" 
                + "          else\n"
                + "            revLst(tl(lst)) ++ [hd(lst)];\n"
                + "revLst([2, 4, 6]);";
        runTypeTest("revLst", prog, "[ int ]");
        runEvalTest("revLst", prog, "[6, 4, 2]");
    }
}

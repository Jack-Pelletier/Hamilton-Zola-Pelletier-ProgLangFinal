
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

public class ArithmeticTests extends LangTest
{
    /**
     * Test the functionality of simple all real arithmetic.
     */
    @Test
    public void allRealArith()
    {
        runTypeTest("allRealArith", "5.2 / 0.3 + .4 - 1.0 * 6.0;", "real");
        runEvalTest("allRealArith", "5.2 / 0.3 + .4 - 1.0 * 6.0;",
                "11.733333333333334");
    }

    /**
     * Test good modulus.
     */
    @Test
    public void modTest()
    {
        runTypeTest("modTest", "3 mod 2;", "int");
        runEvalTest("modTest", "3 mod 2;", "1");
    }

    /**
     * Test modulus and multiplication together along with parens.
     */
    @Test
    public void modAndMultTest()
    {
        runTypeTest("modTest", "(3 * 2) mod 4;", "int");
        runEvalTest("modTest", "(3 * 2) mod 4;", "2");
    }

    @Test
    public void precedenceTest()
    {
        runTypeTest("precedenceTest", "3.0 + 5.0/2.0;", "real");
        runEvalTest("precedenceTest", "3.0 + 5.0/2.0;", "5.5");
    }

    @Test
    public void assocAddSubTest()
    {
        runTypeTest("assocAddSubTest", "3 - 5 + 6;", "int");
        runEvalTest("assocAddSubTest", "3 - 5 + 6;", "4");
    }

    @Test
    public void assocMultDivTest()
    {
        runTypeTest("assocMultDivTest", "3.0 / 2.0 * 4.0;", "real");
        runEvalTest("assocMultDivTest", "3.0 / 2.0 * 4.0;", "6.0");
    }
}

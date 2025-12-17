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

public class BinOpTests extends LangTest
{
    /* ---------- Arithmetic ---------- */

    @Test
    public void intAddition()
    {
        runTypeTest("intAddition", "3 + 4;", "int");
        runEvalTest("intAddition", "3 + 4;", "7");
    }

    @Test
    public void realMultiplication()
    {
        runTypeTest("realMultiplication", "2.5 * 4.0;", "real");
        runEvalTest("realMultiplication", "2.5 * 4.0;", "10.0");
    }

    @Test
    public void intDivision()
    {
        runTypeTest("intDivision", "10 / 2;", "int");
        runEvalTest("intDivision", "10 / 2;", "5");
    }

    @Test
    public void modOperation()
    {
        runTypeTest("modOperation", "7 mod 3;", "int");
        runEvalTest("modOperation", "7 mod 3;", "1");
    }

    /* ---------- Boolean ---------- */

    @Test
    public void andOperation()
    {
        runTypeTest("andOperation", "true and false;", "bool");
        runEvalTest("andOperation", "true and false;", "false");
    }

    @Test
    public void orOperation()
    {
        runTypeTest("orOperation", "true or false;", "bool");
        runEvalTest("orOperation", "true or false;", "true");
    }

    /* ---------- List Concatenation ---------- */

    @Test
    public void concatIntLists()
    {
        runTypeTest("concatIntLists", "[1, 2] ++ [3, 4];", "[ int ]");
        runEvalTest("concatIntLists", "[1, 2] ++ [3, 4];", "[1, 2, 3, 4]");
    }

    @Test
    public void concatEmptyLeft()
    {
        runTypeTest("concatEmptyLeft", "[] ++ [5];", "[ int ]");
        runEvalTest("concatEmptyLeft", "[] ++ [5];", "[5]");
    }

    @Test
    public void concatEmptyRight()
    {
        runTypeTest("concatEmptyRight", "[5] ++ [];", "[ int ]");
        runEvalTest("concatEmptyRight", "[5] ++ [];", "[5]");
    }

    /* ---------- Precedence ---------- */

    @Test
    public void arithmeticPrecedence()
    {
        runTypeTest("arithmeticPrecedence", "3 + 5 * 2;", "int");
        runEvalTest("arithmeticPrecedence", "3 + 5 * 2;", "13");
    }

    /* ---------- Error Cases (optional but good) ---------- */

    @Test(expected = RuntimeException.class)
    public void mixedTypeAddError()
    {
        runEvalTest("mixedTypeAddError", "3 + 4.0;", "");
    }

    @Test(expected = RuntimeException.class)
    public void modWithRealError()
    {
        runEvalTest("modWithRealError", "3.0 mod 2.0;", "");
    }
}

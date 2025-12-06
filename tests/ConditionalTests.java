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

public class ConditionalTests extends LangTest
{
    /**
     * Test the functionality of a simple if statement with integers.
     */
    @Test
    public void simpleIntIfTrue()
    {
        runTypeTest("simpleIntIfTrue", "if 3 > 2 then 4 else 5;", "int");
        runEvalTest("simpleIntIfTrue", "if 3 > 2 then 4 else 5;", "4");
    }

    @Test
    public void simpleIntIfFalse()
    {
        runTypeTest("simpleIntIfFalse", "if 3 < 2 then 4 else 5;", "int");
        runEvalTest("simpleIntIfFalse", "if 3 < 2 then 4 else 5;", "5");
    }

    @Test
    public void compoundIf()
    {
        runTypeTest("simpleIntIfFalse", "if (3 < 2) or (7 < 2) then 4 else 5;", "int");
        runEvalTest("simpleIntIfFalse", "if (3 < 2) or (7 < 2) then 4 else 5;", "5");
    }
}

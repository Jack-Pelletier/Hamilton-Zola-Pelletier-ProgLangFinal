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

public class LogicalTests extends LangTest
{

    /*
     * Tests for or.
     */
    @Test
    public void simpleFalseOr()
    {
        runTypeTest("simpleFalseOr", "false or false;", "bool");
        runEvalTest("simpleFalseOr", "false or false;", "false");
    }

    @Test
    public void simpleTrueOrRight()
    {
        runTypeTest("simpleTrueOrRight", "true or false;", "bool");
        runEvalTest("simpleTrueOrRight", "true or false;", "true");
    }

    @Test
    public void simpleTrueOrLeft()
    {
        runTypeTest("simpleTrueOrLeft", "false or true;", "bool");
        runEvalTest("simpleTrueOrLeft", "false or true;", "true");
    }

    /*
     * Not expressions
     */
    @Test
    public void notTrue()
    {
        runTypeTest("notTrue", "not true;", "bool");
        runEvalTest("notTrue", "not true;", "false");
    }

    @Test
    public void notFalse()
    {
        runTypeTest("notFalse", "not false;", "bool");
        runEvalTest("notFalse", "not false;", "true");
    }
}

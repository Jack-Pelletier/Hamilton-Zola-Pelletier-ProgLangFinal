
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

public class RelationalTests extends LangTest
{
    /*
     * Tests for =.
     */
    @Test
    public void simpleIntEq()
    {
        runTypeTest("simpleIntEq", "5 = 5;", "bool");
        runEvalTest("simpleIntEq", "5 = 5;", "true");
    }

    @Test
    public void simpleRealEq()
    {
        runTypeTest("simpleRealEq", "5.3 = 5.3;", "bool");
        runEvalTest("simpleRealEq", "5.3 = 5.3;", "true");
    }

    /*
     * Test for !=
     */
    @Test
    public void simpleIntNotEq()
    {
        runTypeTest("simpleIntNotEqual", "5 != 3;", "bool");
        runEvalTest("simpleIntNotEqual", "5 != 3;", "true");
    }

    @Test
    public void simpleRealNotEq()
    {
        runTypeTest("simpleRealNotEqual", "5.02 != 3.145;", "bool");
        runEvalTest("simpleRealNotEqual", "5.02 != 3.145;", "true");
    }

    /*
     * Tests for >.
     */
    @Test
    public void simpleIntGT()
    {
        runTypeTest("simpleIntGT", "5 > 3;", "bool");
        runEvalTest("simpleIntGT", "5 > 3;", "true");
    }

    @Test
    public void simpleRealGT()
    {
        runTypeTest("simpleRealGT", "5.3 > 2.1;", "bool");
        runEvalTest("simpleRealGT", "5.3 > 2.1;", "true");
    }

    @Test
    public void simpleIntNotGT()
    {
        runTypeTest("simpleIntNotGT", "5 > 5;", "bool");
        runEvalTest("simpleIntNotGT", "5 > 5;", "false");
    }

    @Test
    public void simpleRealNotGT()
    {
        runTypeTest("simpleRealNotGT", "3.145 > 5.0;", "bool");
        runEvalTest("simpleRealNotGT", "3.145 > 5.0;", "false");
    }

    /*
     * Tests for >=.
     */
    @Test
    public void simpleIntGTE()
    {
        runTypeTest("simpleIntGTE", "5 >= 5;", "bool");
        runEvalTest("simpleIntGTE", "5 >= 5;", "true");
    }

    @Test
    public void simpleRealGTE()
    {
        runTypeTest("simpleRealGTE", "5.3 >= 2.1;", "bool");
        runEvalTest("simpleRealGTE", "5.3 >= 2.1;", "true");
    }

    @Test
    public void simpleIntNotGTE()
    {
        runTypeTest("simpleIntNotGTE", "5 >= 6;", "bool");
        runEvalTest("simpleIntNotGTE", "5 >= 6;", "false");
    }

    @Test
    public void simpleRealNotGTE()
    {
        runTypeTest("simpleRealNotGTE", "3.131592 >= 5.0;", "bool");
        runEvalTest("simpleRealNotGTE", "3.131592 >= 5.0;", "false");
    }

    /*
     * Tests for <.
     */
    @Test
    public void simpleIntLT()
    {
        runTypeTest("simpleIntLT", "3 < 5;", "bool");
        runEvalTest("simpleIntLT", "3 < 5;", "true");
    }

    @Test
    public void simpleRealLT()
    {
        runTypeTest("simpleRealLT", "2.1 < 5.3;", "bool");
        runEvalTest("simpleRealLT", "2.1 < 5.3;", "true");
    }

    @Test
    public void simpleIntNotLT()
    {
        runTypeTest("simpleIntNotLT", "5 < 5;", "bool");
        runEvalTest("simpleIntNotLT", "5 < 5;", "false");
    }

    @Test
    public void simpleRealNotLT()
    {
        runTypeTest("simpleRealNotLT", "5.0 < 3.1415;", "bool");
        runEvalTest("simpleRealNotLT", "5.0 < 3.1415;", "false");
    }

    /*
     * Tests for <=.
     */
    @Test
    public void simpleIntLTE()
    {
        runTypeTest("simpleIntLTE", "3 <= 5;", "bool");
        runEvalTest("simpleIntLTE", "3 <= 5;", "true");
    }

    @Test
    public void simpleRealLTE()
    {
        runTypeTest("simpleRealLTE", "2.1 <= 2.1;", "bool");
        runEvalTest("simpleRealLTE", "2.1 <= 2.1;", "true");
    }

    @Test
    public void simpleIntNotLTE()
    {
        runTypeTest("simpleIntNotLTE", "5 <= 6;", "bool");
        runEvalTest("simpleIntNotLTE", "5 <= 6;", "true");
    }

    @Test
    public void simpleRealNotLTE()
    {
        runTypeTest("simpleRealNotLTE", "3.1415 <= 3.0;", "bool");
        runEvalTest("simpleRealNotLTE", "3.1415 <= 3.0;", "false");
    }
}

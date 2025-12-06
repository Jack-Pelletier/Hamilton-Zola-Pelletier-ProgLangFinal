
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

public class HigherOrderFunctionsTests extends LangTest
{
    /**
     * Test basic map, foldl, and foldr
     */
    @Test
    public void basicMap()
    {
        runTypeTest("basicMap", "map((fn x -> x + 1) [1, 3, 5]);", "[ int ]");
        runEvalTest("basicMap", "map((fn x -> x + 1) [1, 3, 5]);", "[2, 4, 6]");
    }

    @Test
    public void absMap()
    {
        runTypeTest("basicMap", "map((fn x -> if x < 0 then -1*x else x) [-1, -3, -5]);", "[ int ]");
        runEvalTest("basicMap", "map((fn x -> if x < 0 then -1*x else x) [-1, -3, -5]);", "[1, 3, 5]");
    }

    @Test
    public void basicLeftFold()
    {
        runTypeTest("basicLeftFold",
                "foldl((fn x -> fn y -> x + y) 0 [1, 3, 5]);", "int");
        runEvalTest("basicLeftFold",
                "foldl((fn x -> fn y -> x + y) 0 [1, 3, 5]);", "9");
    }

    @Test
    public void basicRightFold()
    {
        runTypeTest("basicRightFold",
                "foldr((fn x -> fn y -> x + y) 0 [1, 3, 5]);", "int");
        runEvalTest("basicRightFold",
                "foldr((fn x -> fn y -> x + y) 0 [1, 3, 5]);", "9");
    }

    @Test
    public void leftFold()
    {
        runTypeTest("leftFold", "foldl((fn x -> fn y -> 2*x + y) 4 [1, 2, 3]);",
                "int");
        runEvalTest("leftFold", "foldl((fn x -> fn y -> 2*x + y) 4 [1, 2, 3]);",
                "43");
    }

    @Test
    public void rightFold()
    {
        runTypeTest("rightFold",
                "foldr((fn x -> fn y -> (x + y)/2.0) 54.0 [12.0, 4.0, 10.0, 6.0]);",
                "real");
        runEvalTest("rightFold",
                "foldr((fn x -> fn y -> (x + y)/2.0) 54.0 [12.0, 4.0, 10.0, 6.0]);",
                "12.0");
    }
}

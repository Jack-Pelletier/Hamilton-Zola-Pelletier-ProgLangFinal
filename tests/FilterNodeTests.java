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

public class FilterNodeTests extends LangTest
{
    @Test
    public void filterEvenNumbers()
    {
        String prog = ""
                + "val nums := [1, 2, 3, 4, 5, 6];\n"
                + "val isEven := fn x -> x mod 2 = 0;\n"
                + "filter isEven nums;";

        runTypeTest("filterEvenNumbers", prog, "[ int ]");
        runEvalTest("filterEvenNumbers", prog, "[2, 4, 6]");
    }

    @Test
    public void filterGreaterThanThree()
    {
        String prog = ""
                + "val nums := [1, 2, 3, 4, 5];\n"
                + "val gt3 := fn x -> x > 3;\n"
                + "filter gt3 nums;";

        runTypeTest("filterGreaterThanThree", prog, "[ int ]");
        runEvalTest("filterGreaterThanThree", prog, "[4, 5]");
    }

    @Test
    public void filterEmptyResult()
    {
        String prog = ""
                + "val nums := [1, 2, 3];\n"
                + "val gt10 := fn x -> x > 10;\n"
                + "filter gt10 nums;";

        runTypeTest("filterEmptyResult", prog, "[ int ]");
        runEvalTest("filterEmptyResult", prog, "[]");
    }
}

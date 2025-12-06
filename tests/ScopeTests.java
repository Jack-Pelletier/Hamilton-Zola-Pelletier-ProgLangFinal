
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

public class ScopeTests extends LangTest
{
    @Test
    public void basicLet()
    {
        runTypeTest("basicLet", "let x := 3 in x + 2;", "int");
        runEvalTest("basicLet", "let x := 3 in x + 2;", "5");
    }

    @Test
    public void listLet()
    {
        runTypeTest("listLet", "let lst := [3.1, 2.0] in hd(lst);", "real");
        runEvalTest("listLet", "let lst := [3.1, 2.0] in hd(lst);", "3.1");
    }

    @Test
    public void shadowScope()
    {
         runTypeTest("shadowScope", "let x := 5 in (let x := 3 in x * 2) + 5;",
                "int");
        runEvalTest("shadowScope", "let x := 5 in (let x := 3 in x * 2) + 5;",
                "11");
    }

    @Test
    public void nestedScope()
    {
        runTypeTest("nestedScope", "let x := 5 in let y := 7 in x * y;", "int");
        runEvalTest("nestedScope", "let x := 5 in let y := 7 in x * y;", "35");
    }

    @Test
    public void shadowLetValScope()
    {
        runTypeTest("shadowLetValScope", "val x := 5;\nlet x := 3 in x * 2 + 5;",
                "int");
        runEvalTest("shadowLetValScope", "val x := 5;\nlet x := 3 in x * 2 + 5;",
                "11");
    }

    @Test
    public void letWithIf()
    {
        runTypeTest("letWithIf", "let x := 7\nin if x mod 2 = 1 then\ntrue\nelse false;",
                "bool");
        runEvalTest("letWithIf", "let x := 7\nin if x mod 2 = 1 then\ntrue\nelse false;",
                "true");
    }
}

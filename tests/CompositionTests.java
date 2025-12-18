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

public class CompositionTests extends LangTest
{
    /**
     * f ∘ g where:
     *   f(x) = x + 1
     *   g(x) = x * 2
     *
     * (f ∘ g)(3) = 7
     */
    @Test
    public void functionCompositionEval()
    {
        String prog =
            "val f := fn x -> x + 1;\n" +
            "val g := fn x -> x * 2;\n" +
            "(f o g)(3);";

        runTypeTest("functionCompositionEval", prog, "int");
        runEvalTest("functionCompositionEval", prog, "7");
    }

    /**
     * Pipeline sugar:
     *   a |> f  == f(a)
     */
    @Test
    public void pipelineEval()
    {
        String prog =
            "val f := fn x -> x + 10;\n" +
            "5 |> f;";

        runTypeTest("pipelineEval", prog, "int");
        runEvalTest("pipelineEval", prog, "15");
    }

    /**
     * Pipeline chaining:
     *   5 |> f |> g == g(f(5))
     */
    @Test
    public void pipelineChainEval()
    {
        String prog =
            "val f := fn x -> x + 1;\n" +
            "val g := fn x -> x * 3;\n" +
            "5 |> f |> g;";

        runTypeTest("pipelineChainEval", prog, "int");
        runEvalTest("pipelineChainEval", prog, "18");
    }

    /**
     * Composition with higher-order functions
     */
    @Test
    public void compositionHigherOrder()
    {
        String prog =
            "val inc := fn x -> x + 1;\n" +
            "val square := fn x -> x * x;\n" +
            "(square o inc)(4);";

        runTypeTest("compositionHigherOrder", prog, "int");
        runEvalTest("compositionHigherOrder", prog, "25");
    }
}

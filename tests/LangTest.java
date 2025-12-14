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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ast.EvaluationException;
import ast.SyntaxTree;
import ast.typesystem.TypeException;
import parser.MFLParser;
import parser.ParseException;

/**
 * A super class for all language tests.
 */
public class LangTest
{
    /**
     * Evaluate the expression and return the result as a string or throw an
     * exception if it does not succeed.
     *
     * @param name      the name of the test.
     * @param statement the statement to evaluate.
     * @param expected  the expected output of the test.
     */
    public void runEvalTest(String name, String statement, String expected)
    {
        MFLParser p = new MFLParser(statement);
        SyntaxTree ast = null;

        try
        {
            ast = p.parse();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertFalse(name + ": Bad parse " + e, true);
        }

        Object res = null;

        try
        {
            res = ast.evaluate();
        }
        catch (EvaluationException ex)
        {
            ex.printStackTrace(); // FULL STACK TRACE
            throw new AssertionError(name + ": Unexpected Exception.", ex);
        }

        assertEquals(name + ":", expected, res.toString());
    }

    /**
     * Evaluate the type of the expression and return the result as a string or
     * throw an exception if it does not succeed.
     *
     * @param name      the name of the test.
     * @param statement the statement to evaluate.
     * @param expected  the expected output of the test.
     */
    public void runTypeTest(String name, String statement, String expected)
    {
        MFLParser p = new MFLParser(statement);
        SyntaxTree ast = null;

        try
        {
            ast = p.parse();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertFalse(name + ": Bad parse " + e, true);
        }

        Object res = null;

        try
        {
            res = ast.getType();
        }
        catch (TypeException ex)
        {
            ex.printStackTrace(); // FULL STACK TRACE
            throw new AssertionError(name + ": Unexpected Exception.", ex);
        }

        assertEquals(name + ":", expected, res.toString());
    }
}

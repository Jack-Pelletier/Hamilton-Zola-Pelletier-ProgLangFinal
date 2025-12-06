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
package ast;

import ast.nodes.SyntaxNode;
import environment.Environment;
import lexer.Token;

/**
 * Represents a closure for functions.
 * @author Zach Kissel
 */
public class Closure
{
    Environment closureEnv; // The environment when the lambda expression was
                            // defined.
    Token var; // The free variable.
    SyntaxNode body; // The body of the lambda expression.

    /**
     * Constructs a new closure.
     * 
     * @param var        the variable associated with the lambda expression.
     * @param body       the body of the lambda expression.
     * @param closureEnv the environment when the lambda expression was defined.
     */
    public Closure(Token var, SyntaxNode body, Environment closureEnv)
    {
        this.var = var;
        this.body = body;
        this.closureEnv = closureEnv;
    }

    /**
     * Gets the variable associated with the lambda expression.
     * 
     * @return the variable associated with the lambda expression.
     */
    public Token getVar()
    {
        return this.var;
    }

    /**
     * Gets the body of the lambda expression.
     * 
     * @return the body associated with the lambda expression.
     */
    public SyntaxNode getBody()
    {
        return this.body;
    }

    /**
     * Get the environment associated with the lambda epxrssion definition time.
     * 
     * @return the environment during at the time of definition.
     */
    public Environment getEnv()
    {
        return closureEnv;
    }

    /**
     * Construct a string representation of a closure.
     * 
     * @return the string representation of a closure.
     */
    @Override
    public String toString()
    {
        return "fn";
    }
}

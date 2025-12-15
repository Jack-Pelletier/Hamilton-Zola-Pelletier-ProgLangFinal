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
package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * Represents a variable reference (identifier usage).
 *
 * @author Zach Kissel
 */
public final class VariableNode extends SyntaxNode
{
    private final Token name;

    /**
     * Constructs a variable node.
     *
     * @param name the identifier token
     * @param line the line number associated with this node
     */
    public VariableNode(int name, String line)
    {
        super(string);
        this.name = i;
    }

    /**
     * Returns the token representing the variable name.
     */
    public Token getName()
    {
        return name;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object val = env.lookup(name);
        if (val == null)
        {
            logError(name.getValue() + " is undefined.");
            throw new EvaluationException();
        }
        return val;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t = tenv.lookup(name);
        if (t == null)
        {
            throw new TypeException(
                buildErrorMessage(name.getValue() + " is undefined.")
            );
        }
        return t;
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Var[" + name.getValue() + "]", indentAmt);
    }
}

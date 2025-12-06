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
import ast.typesystem.types.BoolType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * This node represents the a token in the grammar.
 * 
 * @author Zach Kissel
 */
public final class TokenNode extends SyntaxNode
{
    private Token token; // The token type.

    /**
     * Constructs a new token node.
     * 
     * @param token the token to associate with the node.
     * @param line  the line of code the node is associated with.
     */
    public TokenNode(Token token, long line)
    {
        super(line);
        this.token = token;
    }

    /**
     * Grab the raw token from the token node.
     * @return the token associated with this TokenNode.
     */
    public Token getToken()
    {
        return this.token;
    }

    /**
     * Evaluate the node.
     * 
     * @param env the executional environment we should evaluate the node under.
     * @return the object representing the result of the evaluation.
     * @throws EvaluationException if the evaluation fails.
     */
    public Object evaluate(Environment env) throws EvaluationException
    {
        switch (token.getType())
        {
        case INT:
            return Integer.valueOf(token.getValue());
        case REAL:
            return Double.valueOf(token.getValue());
        case TRUE:
            return Boolean.valueOf(true);
        case FALSE:
            return Boolean.valueOf(false);
        case ID:
            Object val = env.lookup(token);
            if (val == null)
            {
                logError("undefined value " + token.getValue() + ".");
                throw new EvaluationException();
            }

            return val;
        default:
            return token;
        }
    }

    /**
     * Determine the type of the syntax node. In particluar bool, int, real,
     * generic, or function.
     * 
     * @param tenv       the type environment.
     * @param inferencer the type inferencer
     * @return The type of the syntax node.
     * @throws TypeException if there is a type error.
     */
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        switch (token.getType())
        {
        case INT:
            return new IntType();
        case REAL:
            return new RealType();
        case TRUE:
            return new BoolType();
        case FALSE:
            return new BoolType();
        case ID:
            Type tval = (Type) tenv.lookup(token);

            // Identifier was not found, so it must not have
            // been suitably declared.
            if (tval == null)
            {
                throw new TypeException(buildErrorMessage(
                        "Unknown identifier " + token.getValue() + "."));
            }
            return tval;
        default:
            logError("Unknown token.");
            throw new TypeException();
        }
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("Token(" + token + ")", indentAmt);
    }
}

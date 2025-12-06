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

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * This node represents the program.
 * 
 * @author Zach Kissel
 */
public final class ProgNode extends SyntaxNode
{
    private LinkedList<SyntaxNode> exprs;

    /**
     * Constructs a new program node which represents a list of expressions.
     * 
     * @param exprs a linked list of expressions (AST inferencertress).
     * @param line  the line of code the node is associated with.
     */
    public ProgNode(LinkedList<SyntaxNode> exprs, long line)
    {
        super(line);
        this.exprs = exprs;
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
        Object res = null;

        // Loop over the expressions evaluating every node.
        for (SyntaxNode expr : exprs)
            res = expr.evaluate(env);
        return res;
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
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
    {
        Type rv = null;
        for (SyntaxNode ex : exprs)
            rv = ex.typeOf(tenv, inferencer);

        if (exprs.size() == 0)
            throw new TypeException("Inavalid expression.");
        
        return rv;
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("Prog(", indentAmt);
        for (SyntaxNode expr : exprs)
            expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Type check the program.
     * 
     * @param tenv       the type environment.
     * @param inferencer the type inferencer.
     * @return true if the program type checks; otherwise, false.
     */
    public boolean typeCheck(TypeEnvironment tenv, Inferencer inferencer)
    {
        try
        {
            for (SyntaxNode expr : exprs)
            {
                expr.typeOf(tenv, inferencer);
            }

        }
        catch (TypeException ex)
        {
            System.out.println("Type Error: " + ex.getMessage());
            return false;
        }

        return true;
    }
}

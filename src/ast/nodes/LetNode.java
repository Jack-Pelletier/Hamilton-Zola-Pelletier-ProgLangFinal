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
import lexer.Token;

/**
 * This node represents a let expression.
 * 
 * @author Zach Kissel
 */
public final class LetNode extends SyntaxNode
{
    private Token var;
    private SyntaxNode varExpr;
    private SyntaxNode expr;

    /**
     * Constructs a new binary operation syntax node.
     * 
     * @param var     the variable identifier.
     * @param varExpr the expression that give the varaible value.
     * @param expr    the expression that uses the variables value.
     * @param line    the line of code the node is associated with.
     */
    public LetNode(Token var, SyntaxNode varExpr, SyntaxNode expr, long line)
    {
        super(line);
        this.var = var;
        this.varExpr = varExpr;
        this.expr = expr;
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

        Object varVal = null;
        Environment envCopy = env.copy(); // Copy the environment to create a
                                          // new scope.

        varVal = varExpr.evaluate(env);

        if (varVal instanceof Integer || varVal instanceof Double
                || varVal instanceof Boolean || varVal instanceof LinkedList)
            envCopy.updateEnvironment(var, varVal);
        else
            logError("[Internal] Failed to add " + var + " with  value "
                    + varVal.getClass());

        Object value = expr.evaluate(envCopy);
        return value;
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
        Type valType = varExpr.typeOf(tenv, inferencer);

        TypeEnvironment cpy = tenv.copy();
        cpy.updateEnvironment(var, valType);

        return expr.typeOf(cpy, inferencer);
    }

    /**
     * Display a AST subtree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("let[" + var.getValue() + "](", indentAmt);
        varExpr.displaySubtree(indentAmt + 2);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

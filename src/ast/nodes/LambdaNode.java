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

import ast.Closure;
import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.FunType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * This node represents a lambda expression.
 * 
 * @author Zach Kissel
 */
public final class LambdaNode extends SyntaxNode
{
    private Token var;
    private SyntaxNode expr;

    /**
     * Constructs a new function node which represents a function declaration.
     * 
     * @param var  the free variable in the expression.
     * @param expr the expression to excute.
     * @param line the line of code the node is associated with.
     */
    public LambdaNode(Token var, SyntaxNode expr, long line)
    {
        super(line);
        this.var = var;
        this.expr = expr;
    }

    /**
     * Get the parameter of the function.
     * 
     * @return a Token representing the parameter name.
     */
    public Token getVar()
    {
        return var;
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
        return new Closure(var, expr, env);
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
        Type varType = tenv.getTypeVariable();
        TypeEnvironment scope = tenv.copy();
        scope.updateEnvironment(var, varType);
        Type bodyType = expr.typeOf(scope, inferencer);

        return new FunType(varType, bodyType);
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("lambda[" + var.getValue() + "](", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

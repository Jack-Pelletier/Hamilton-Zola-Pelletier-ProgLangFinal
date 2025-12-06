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
import ast.typesystem.types.FunType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * This node represents a global value.
 * 
 * @author Zach Kissel
 */
public final class ValNode extends SyntaxNode
{
    private Token name;
    private SyntaxNode expr;

    /**
     * Constructs a new value node that represents a global value.
     * 
     * @param name the name of the value.
     * @param expr the value of the name.
     * @param line the line of code the node is associated with.
     */
    public ValNode(Token name, SyntaxNode expr, long line)
    {
        super(line);
        this.name = name;
        this.expr = expr;
    }

    /**
     * Returns the name of the value.
     * 
     * @return a Token representing the name of the function.
     */
    public Token getName()
    {
        return name;
    }

    /**
     * Returns the expression associated with the value.
     * 
     * @return the root of the AST that represents the value associated with the
     *         name.
     */
    public SyntaxNode getExpression()
    {
        return expr;
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
        Object val = expr.evaluate(env);
        if (env.lookup(name) == null)
            env.updateEnvironment(name, val);
        else 
        {
            logError(name.getValue() + " already defined.");
            throw new EvaluationException();
        }
        return name.getValue();
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
        
        // In order to support recursive definitions we need to preload 
        // a type into the type environment if the expression is a LambdaNode.
        if (expr instanceof LambdaNode)
            tenv.updateEnvironment(name, new FunType(tenv.getTypeVariable(), tenv.getTypeVariable()));

        Type valType = inferencer.getSubstitutions().apply(expr.typeOf(tenv, inferencer));
        
        tenv.updateEnvironment(name, valType);
        return valType;
    }
    
    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("Val[" + name.getValue() + "](", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

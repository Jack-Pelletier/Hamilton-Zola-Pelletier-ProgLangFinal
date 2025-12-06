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

/**
 * This node represents the unary op node.
 * 
 * @author Zach Kissel
 */
public final class ApplyNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode arg;

    /**
     * Constructs a new node that represents function application.
     * 
     * @param func the function to apply.
     * @param arg  the argument to apply the function to.
     * @param line the line of code the node is associated with.
     */
    public ApplyNode(SyntaxNode func, SyntaxNode arg, long line)
    {
        super(line);
        this.func = func;
        this.arg = arg;
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
        Object proc = func.evaluate(env);
        Closure closure;

        // Make sure after the above executes we have a closure. If not,
        // we don't have a function to apply.
        if (proc instanceof Closure)
        {
            closure = (Closure) proc;

            Environment newScope = closure.getEnv().copy();
            newScope.updateEnvironment(closure.getVar(), arg.evaluate(env));
            return closure.getBody().evaluate(newScope);
        }
        else
        {
            logError("Can't apply expression to argument.");
            throw new EvaluationException();
        }

    }

    /**
     * Determine the type of the syntax node. In particluar bool, int, real,
     * generic, or function.
     * 
     * @param tenv       the type environment.
     * @param inferencer the type inferencer.
     * @return The type of the syntax node.
     * @throws TypeException if there is a type error.
     */
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type funType = func.typeOf(tenv, inferencer);
        Type argType = arg.typeOf(tenv, inferencer);
        Type resultType = tenv.getTypeVariable();
        
        inferencer.unify(funType, new FunType(argType, resultType),
                buildErrorMessage("Function application type mismatch."));
        
        return inferencer.getSubstitutions().apply(resultType);
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("Apply(", indentAmt);
        func.displaySubtree(indentAmt + 2);
        arg.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

}

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
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * This node represents an if-then-else statement.
 * 
 * @author Zach Kissel
 */
public final class IfNode extends SyntaxNode
{
    private SyntaxNode cond;
    private SyntaxNode trueBranch;
    private SyntaxNode falseBranch;

    /**
     * Constructs a new conditional node.
     * 
     * @param cond        the boolean condition.
     * @param trueBranch  the code in the true branch.
     * @param falseBranch the code in the false branch.
     * @param line        the line of code the node is associated with.
     */
    public IfNode(SyntaxNode cond, SyntaxNode trueBranch,
            SyntaxNode falseBranch, long line)
    {
        super(line);
        this.cond = cond;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
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
        Object res;
        Boolean condVal;

        // Evaluate the condition.
        res = cond.evaluate(env);

        if (res == null || !(res instanceof Boolean))
        {
            logError("condition must evaluate to a Boolean.");
            throw new EvaluationException();
        }

        // Evaluate the expression.
        condVal = (Boolean) res;
        if (condVal)
            return trueBranch.evaluate(env);
        return falseBranch.evaluate(env);
    }

    /**
     * Determine the type of the syntax node. In particluar bool, int, real,
     * generic, or function.
     * 
     * @param tenv the type environment.
     * @param inferencer  the type inferencer.
     * @return The type of the syntax node.
     * @throws TypeException if there is a type error.
     */
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type condType = cond.typeOf(tenv, inferencer);
        inferencer.unify(condType, new BoolType(), buildErrorMessage("Boolean condition expected."));

        Type trueType = trueBranch.typeOf(tenv, inferencer);
        Type falseType = falseBranch.typeOf(tenv, inferencer);

        inferencer.unify(trueType, falseType, buildErrorMessage("Both branches must have same type."));

        return trueType;
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("if(", indentAmt);
        cond.displaySubtree(indentAmt + 2);
        trueBranch.displaySubtree(indentAmt + 2);
        falseBranch.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

}

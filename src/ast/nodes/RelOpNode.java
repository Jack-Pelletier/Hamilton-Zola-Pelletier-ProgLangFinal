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
import lexer.TokenType;

/**
 * This node represents relational operations.
 * 
 * @author Zach Kissel
 */
public final class RelOpNode extends SyntaxNode
{
    private TokenType op;
    private SyntaxNode leftExpr;
    private SyntaxNode rightExpr;

    /**
     * Constructs a new binary operation syntax node.
     * 
     * @param lexpr the left operand.
     * @param op    the binary operation to perform.
     * @param rexpr the right operand.
     * @param line  the line of code the node is associated with.
     */
    public RelOpNode(SyntaxNode lexpr, TokenType op, SyntaxNode rexpr,
            long line)
    {
        super(line);
        this.op = op;
        this.leftExpr = lexpr;
        this.rightExpr = rexpr;
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
        Object lval;
        Object rval;
        boolean useDouble = false;

        lval = leftExpr.evaluate(env);
        rval = rightExpr.evaluate(env);

        // Make sure the type is sound.
        if (!(lval instanceof Integer || lval instanceof Double)
                && !(rval instanceof Double || rval instanceof Integer))
            throw new EvaluationException();

        if (lval.getClass() != rval.getClass())
        {
            logError("mixed type expression.");
            return null;
        }

        if (lval instanceof Double)
            useDouble = true;

        // Perform the operation base on the type.
        switch (op)
        {
        case LT:
            if (useDouble)
                return (Double) lval < (Double) rval;
            return (Integer) lval < (Integer) rval;
        case LTE:
            if (useDouble)
                return (Double) lval <= (Double) rval;
            return (Integer) lval <= (Integer) rval;
        case GT:
            if (useDouble)
                return (Double) lval > (Double) rval;
            return (Integer) lval > (Integer) rval;
        case GTE:
            if (useDouble)
                return (Double) lval >= (Double) rval;
            return (Integer) lval >= (Integer) rval;
        case EQ:
            return lval.equals(rval);
        case NEQ:
            return !(lval.equals(rval));
        default:
            throw new EvaluationException();
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
        Type leftType = leftExpr.typeOf(tenv, inferencer);
        Type rightType = rightExpr.typeOf(tenv, inferencer);

        inferencer.unify(leftType, rightType, buildErrorMessage(
                "relation operator requries left and right expressions to have same type."));

        leftType = inferencer.getSubstitutions().apply(leftType);
        rightType = inferencer.getSubstitutions().apply(rightType);
        
        if (leftType.equals(new IntType()) || leftType.equals(new RealType()))
            return new BoolType();
        throw new TypeException("Type Error: integer or real expected.");
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("RelOp[" + op + "](", indentAmt);
        leftExpr.displaySubtree(indentAmt + 2);
        rightExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

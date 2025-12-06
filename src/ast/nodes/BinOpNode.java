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
import ast.typesystem.types.BoolType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.TokenType;

/**
 * This node represents a binary operation.
 * 
 * @author Zach Kissel
 */
public final class BinOpNode extends SyntaxNode
{
    private TokenType op;
    private SyntaxNode leftTerm;
    private SyntaxNode rightTerm;

    /**
     * Constructs a new binary operation syntax node.
     * 
     * @param lterm the left operand.
     * @param op    the binary operation to perform.
     * @param rterm the right operand.
     * @param line  the line of code the node is associated with.
     */
    public BinOpNode(SyntaxNode lterm, TokenType op, SyntaxNode rterm,
            long line)
    {
        super(line);
        this.op = op;
        this.leftTerm = lterm;
        this.rightTerm = rterm;
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

        lval = leftTerm.evaluate(env);
        rval = rightTerm.evaluate(env);

        if (op == TokenType.CONCAT)
            return handleConcat(lval, rval);

        // Make sure the type is sound.
        if (!(lval instanceof Integer || lval instanceof Double
                || lval instanceof Boolean)
                && !(rval instanceof Double || rval instanceof Integer
                        || lval instanceof Boolean))
            throw new EvaluationException();

        if (lval.getClass() != rval.getClass())
        {
            logError("mixed type expression.");
            throw new EvaluationException();
        }
        if (lval instanceof Double)
            useDouble = true;

        // Perform the operation base on the type.
        switch (op)
        {
        case ADD:
            if (useDouble)
                return (Double) lval + (Double) rval;
            return (Integer) lval + (Integer) rval;
        case SUB:
            if (useDouble)
                return (Double) lval - (Double) rval;
            return (Integer) lval - (Integer) rval;
        case MULT:
            if (useDouble)
                return (Double) lval * (Double) rval;
            return (Integer) lval * (Integer) rval;
        case DIV:
            if (useDouble)
                return (Double) lval / (Double) rval;
            return (Integer) lval / (Integer) rval;
        case MOD:
            if (useDouble)
            {
                logError("Error: Mod requires integer arguments.");
                throw new EvaluationException();
            }
            return (Integer) lval % (Integer) rval;
        case AND:
            return (Boolean) lval && (Boolean) rval;
        case OR:
            return (Boolean) lval || (Boolean) rval;
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
        Type left = leftTerm.typeOf(tenv, inferencer);
        Type right = rightTerm.typeOf(tenv, inferencer);

        inferencer.unify(left, right,
                buildErrorMessage("Mixed mode expressions not supported."));

        left = inferencer.getSubstitutions().apply(left);
        right = inferencer.getSubstitutions().apply(right);

        if (left instanceof VarType && right instanceof VarType)
            return left;

        switch (op)
        {
        case CONCAT:
            inferencer.unify(left, new ListType(tenv.getTypeVariable()),
                     buildErrorMessage("List expected."));
            return left;
        case ADD:
        case SUB:
        case MULT:
        case DIV:
            if (left.equals(new IntType()))
                return new IntType();
            else if (left.equals(new RealType()))
                return new RealType();
            throw new TypeException("Type Error: integer or real expected.");
        case MOD:
            if (left.equals(new IntType()))
                return new IntType();
             throw new TypeException("Type Error: integer expected.");
        case AND:
        case OR:
            if (left.equals(new BoolType()))
                return new BoolType();
            throw new TypeException("Type Error: integer or real expected.");
        default:
            logError("Unknown binary operation.");
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
        printIndented("BinOp[" + op + "](", indentAmt);
        leftTerm.displaySubtree(indentAmt + 2);
        rightTerm.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Handles the concatenation operation returning the concatenated form of
     * the list.
     * 
     * @return the concatenation of the two lists.
     * @throws EvaluationException if the evaluation fails.
     */
    @SuppressWarnings("unchecked")
    private Object handleConcat(Object lval, Object rval)
            throws EvaluationException
    {
        if (!(lval instanceof LinkedList) || !(rval instanceof LinkedList))
            return null;
        else
        {
            LinkedList<Object> leftList = (LinkedList<Object>) lval;
            LinkedList<Object> rightList = (LinkedList<Object>) rval;

            if (leftList.size() == 0)
                return rightList;
            else if (rightList.size() == 0)
                return leftList;
            else
            {
                if (leftList.getFirst().getClass() != rightList.getFirst()
                        .getClass())
                {
                    logError("mixed type list not supported.");
                    throw new EvaluationException();
                }

                leftList.addAll(rightList);

                return leftList;
            }
        }
    }
}

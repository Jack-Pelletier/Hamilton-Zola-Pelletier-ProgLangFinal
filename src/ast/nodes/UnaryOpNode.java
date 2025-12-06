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
 * This node represents the unary op node.
 * 
 * @author Zach Kissel
 */
public final class UnaryOpNode extends SyntaxNode
{
    private TokenType op;
    private SyntaxNode expr;

    /**
     * Constructs a new binary operation syntax node.
     * 
     * @param expr the operand.
     * @param op   the binary operation to perform.
     * @param line the line of code the node is associated with.
     */
    public UnaryOpNode(SyntaxNode expr, TokenType op, long line)
    {
        super(line);
        this.op = op;
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
        Object val;

        val = expr.evaluate(env);

        // Make sure the type is sound.
        

        // Perform the operation based on the type.
        switch (op)
        {
        case NOT:
            if (!(val instanceof Boolean))
            {
                logError("Boolean expected.");
                throw new EvaluationException();
            }
            return !((Boolean) val);
        case SUB:
            if (!(val instanceof Integer) && !(val instanceof Double))
            {
                logError("Integer or real expected.");
                throw new EvaluationException();
            }
            if (val instanceof Integer)
                return -1 * (Integer) val;
            else 
                return -1 * (Double) val;
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
        Type argType = expr.typeOf(tenv, inferencer);

        if (op == TokenType.NOT)
        {
            inferencer.unify(argType, new BoolType(),
                    buildErrorMessage("Boolean type expected."));
            return new BoolType();
        }
        else if (op == TokenType.SUB)
        {
            if (argType.equals(new IntType()) || argType.equals(new RealType()))
                return argType;
            logError("Integer or real type expected.");
            throw new TypeException();
        }
        
        logError("Unknown unary operation");
        throw new TypeException();
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("UnaryOp[" + op + "](", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

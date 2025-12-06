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
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents the lentght built in function.
 * 
 * @author Zach Kissel
 */
public class LenNode extends SyntaxNode
{
    private SyntaxNode lst;

    /**
     * Constructs a new len syntax node.
     * 
     * @param lst  the list to get the lenght of.
     * @param line the line of code the node is associated with.
     */
    public LenNode(SyntaxNode lst, long line)
    {
        super(line);
        this.lst = lst;
    }

    /**
     * Evaluate the node.
     * 
     * @param env the executional environment we should evaluate the node under.
     * @return the object representing the result of the evaluation.
     * @throws EvaluationException if the evaluation fails.
     */
    @SuppressWarnings("unchecked")
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object res = lst.evaluate(env);

        if (!(res instanceof LinkedList))
        {
            logError("Linked list expected.");
            throw new EvaluationException();
        }

        LinkedList<Object> theList = (LinkedList<Object>) res;
        return theList.size();
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
        Type lType = lst.typeOf(tenv, inferencer);
        
        inferencer.unify(lType, new ListType(tenv.getTypeVariable()), buildErrorMessage("List type expected."));
        return new IntType();
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("len(", indentAmt);
        lst.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

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
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * This node represents the tail node structure.
 * 
 * @author Zach Kissel
 */
public final class TailNode extends SyntaxNode
{
    private SyntaxNode list;

    /**
     * Constructs a new list syntax node.
     * 
     * @param list the list to apply the tail operation to.
     * @param line the line of code the node is associated with.
     */
    public TailNode(SyntaxNode list, long line)
    {
        super(line);
        this.list = list;
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
        Object res;

        res = list.evaluate(env);
        if (res instanceof LinkedList)
        {
            LinkedList<Object> lst = (LinkedList<Object>) res;
            if (lst.size() < 1)
            {
                logError("can't find tail of list.");
                throw new EvaluationException();
            }
          
            res = lst.clone();
            ((LinkedList<Object>)res).remove();
        }
        else
        {
            logError("list expected.");
            throw new EvaluationException();
        }

        return res;
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
        Type lstType = list.typeOf(tenv, inferencer);
        inferencer.unify(lstType, new ListType(tenv.getTypeVariable()), buildErrorMessage("List expected."));
        return lstType;
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("tail(", indentAmt);
        list.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

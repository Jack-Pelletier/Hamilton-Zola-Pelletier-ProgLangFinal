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
 * This node represents the list structure.
 * 
 * @author Zach Kissel
 */
public final class ListNode extends SyntaxNode
{
    private LinkedList<SyntaxNode> entries;

    /**
     * Constructs a new list syntax node.
     * 
     * @param entries the linked list of syntax node entries.
     * @param line    the line of code the node is associated with.
     */
    public ListNode(LinkedList<SyntaxNode> entries, long line)
    {
        super(line);
        this.entries = entries;
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
        Object currVal;
        Object firstVal;
        LinkedList<Object> lst = new LinkedList<>();

        // Handle the empty list.
        if (entries.size() == 0)
            return lst;

        // The type of the list is the type of the first element
        // of the list.
        firstVal = entries.getFirst().evaluate(env);

        if (firstVal instanceof TokenNode)
        {
            TokenNode tok = (TokenNode) firstVal;
            firstVal = tok.evaluate(env);
            return firstVal;
        }
        else if (firstVal instanceof Integer || firstVal instanceof Double || firstVal instanceof Boolean)
            lst.add(firstVal);
        else if (firstVal instanceof LinkedList)
        {
            logError("nested lists not supported.");
            throw new EvaluationException();
        }
        else
        {
            logError("unknown list type.");
            throw new EvaluationException();
        }

        // Walk the list evaluating each node if the node
        // is of the correct type, we add it to the current list.
        for (int i = 1; i < entries.size(); i++)
        {
            currVal = entries.get(i).evaluate(env);

            if (!(currVal instanceof Integer) && !(currVal instanceof Double)
                    && !(currVal instanceof LinkedList)
                    && !(currVal instanceof Boolean))
            {
                logError("unknown element type.");
                throw new EvaluationException();
            }

            if (firstVal.getClass() != currVal.getClass())
            {
                logError("Mixed mode list not supported.");
                throw new EvaluationException();
            }

            lst.add(currVal);
        }
        return lst;
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
        if (entries.isEmpty())
            return new ListType(tenv.getTypeVariable());
        else
        {
            // Ensure that all elements are of the same type t, and then return
            // ListType of t.
            Type lstType = entries.get(0).typeOf(tenv, inferencer);
            for (SyntaxNode entry : entries)
                inferencer.unify(lstType, entry.typeOf(tenv, inferencer),
                        buildErrorMessage("All elements must be of the same type."));

            return new ListType(lstType);
        }
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("list(", indentAmt);
        for (SyntaxNode node : entries)
            node.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

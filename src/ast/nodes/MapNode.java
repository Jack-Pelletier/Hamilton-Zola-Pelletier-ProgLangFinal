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

import ast.Closure;
import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.FunType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * Represents the map operation for lists (built in)
 * 
 * @author Zach Kissel
 */
public class MapNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode lst;

    /**
     * Constructs a new map syntax node.
     * 
     * @param func the function to apply to each element.
     * @param lst  the list to apply the function to.
     * @param line the line of code the node is associated with.
     */
    public MapNode(SyntaxNode func, SyntaxNode lst, long line)
    {
        super(line);
        this.func = func;
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
        Object res = func.evaluate(env);

        if (!(res instanceof Closure))
        {
            logError("Map's first argument must be a function.");
            throw new EvaluationException();
        }
        Closure lexpr = (Closure) res;

        Object list = lst.evaluate(env);

        if (!(list instanceof LinkedList))
        {
            logError("Map's second argument must be a list.");
            throw new EvaluationException();
        }

        Token var = lexpr.getVar();
        LinkedList<Object> theList = (LinkedList<Object>) list;
        LinkedList<Object> result = new LinkedList<Object>();
        for (Object obj : theList)
        {
            Environment cpy = lexpr.getEnv().copy();
            cpy.updateEnvironment(var, obj);
            result.add(lexpr.getBody().evaluate(cpy));
        }

        return result;
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
        Type lstType = lst.typeOf(tenv, inferencer);

        inferencer.unify(lstType, new ListType(tenv.getTypeVariable()),
                buildErrorMessage("Map requires a list."));

        // Ensure we have a function t0 -> t1.
        inferencer.unify(funType,
                new FunType(tenv.getTypeVariable(), tenv.getTypeVariable()),
                buildErrorMessage("Map requires a function."));

        // Make sure the list elemnt type and function argument type match.
        inferencer.unify(((ListType) lstType).getElementType(),
                ((FunType) funType).getArgType(),
                buildErrorMessage("Parameter and list element type mismatch."));

        return new ListType(inferencer.getSubstitutions().apply(((FunType)funType).getBodyType()));
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("map(", indentAmt);
        func.displaySubtree(indentAmt + 2);
        lst.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

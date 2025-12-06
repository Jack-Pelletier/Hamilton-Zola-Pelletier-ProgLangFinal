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

import java.util.Iterator;
import java.util.LinkedList;

import ast.Closure;
import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.FunType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents both a left and right fold operation (a built in function).
 * 
 * @author Zach Kissel
 */
public class FoldNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode ival;
    private SyntaxNode lst;
    private boolean leftFold;

    /**
     * Constructs a new map syntax node.
     * 
     * @param func     the function to apply to each element.
     * @param ival     the initial value for the fold operation.
     * @param lst      the list to apply the function to.
     * @param leftFold true if a left fold and false if a right fold.
     * @param line     the line of code the node is associated with.
     */
    public FoldNode(SyntaxNode func, SyntaxNode ival, SyntaxNode lst,
            boolean leftFold, long line)
    {
        super(line);
        this.func = func;
        this.lst = lst;
        this.ival = ival;
        this.leftFold = leftFold;
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
        Object fun = func.evaluate(env);
        Object currVal;

        if (!(fun instanceof Closure))
        {
            logError("Function with two inputs expected.");
            throw new EvaluationException();
        }

        Closure theFunction = (Closure) fun;

        // Determine the starting value.
        currVal = ival.evaluate(env);

        // Make sure we have a list.
        Object list = lst.evaluate(env);

        if (!(list instanceof LinkedList))
        {
            logError("List expected.");
            throw new EvaluationException();
        }

        LinkedList<Object> theList = (LinkedList<Object>) list;

        if (leftFold)
        {
            for (Object obj : theList)
            {
                Environment cpy = theFunction.getEnv().copy();
                cpy.updateEnvironment(theFunction.getVar(), currVal);
                Object partialEval = theFunction.getBody().evaluate(cpy);

                if (!(partialEval instanceof Closure))
                {
                    logError("Fold requires a binary function.");
                    throw new EvaluationException();
                }

                Closure innerFunc = (Closure) partialEval;

                cpy = innerFunc.getEnv().copy();
                cpy.updateEnvironment(innerFunc.getVar(), obj);
                currVal = innerFunc.getBody().evaluate(cpy);
            }
        } 
        else 
        {
            theList = reverse(theList);

            for (Object obj : theList)
            {
                Environment cpy = theFunction.getEnv().copy();
                cpy.updateEnvironment(theFunction.getVar(), obj);
                Object partialEval = theFunction.getBody().evaluate(cpy);

                if (!(partialEval instanceof Closure))
                {
                    logError("Fold requires a binary function.");
                    throw new EvaluationException();
                }

                Closure innerFunc = (Closure) partialEval;

                cpy = innerFunc.getEnv().copy();
                cpy.updateEnvironment(innerFunc.getVar(), currVal);
                currVal = innerFunc.getBody().evaluate(cpy);
            }
        }
        return currVal;
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
        Type ivalType = ival.typeOf(tenv, inferencer);

        inferencer.unify(lstType, new ListType(tenv.getTypeVariable()),
                buildErrorMessage("fold" + ((leftFold) ? "l" : "r")
                        + " requires a list."));

        if (leftFold)
        {
            // The type of the function for fold must be t0 -> t1 -> t0.
            VarType tv = tenv.getTypeVariable();
            inferencer.unify(funType,
                    new FunType(tv, new FunType(tenv.getTypeVariable(), tv)),
                    buildErrorMessage("foldl requires a curried function."));

            // Make sure the type of the initial value matches the type of first
            // arguemnt to our curried function.
            inferencer.unify(((FunType) funType).getArgType(), ivalType,
                    buildErrorMessage(
                            "Initial value and first argument must be of same type."));

            // Make sure the inner function argument type matches the type of
            // the
            // list element type.
            FunType body = (FunType) ((FunType) funType).getBodyType();
            inferencer.unify(((ListType) lstType).getElementType(),
                    body.getArgType(),
                    buildErrorMessage("Parameter and list type mismatch."));
        }
        else // A right fold.
        {
            // The type of the function for fold must be t0 -> t1 -> t1.
            VarType tv = tenv.getTypeVariable();
            inferencer.unify(funType,
                    new FunType(tenv.getTypeVariable(), new FunType(tv, tv)),
                    buildErrorMessage("foldr requires a curried function."));

            // Make sure the type of the list element matches the first argument
            // type.
            inferencer.unify(((FunType) funType).getArgType(),
                    ((ListType) lstType).getElementType(), buildErrorMessage(
                            "List element and first argument must be of same type."));

            // Make sure the inner function argument type matches the type of
            // the initial value.
            FunType body = (FunType) ((FunType) funType).getBodyType();
            inferencer.unify(ivalType, body.getArgType(), buildErrorMessage(
                    "initial value and function argument type mismatch."));
        }

        return inferencer.getSubstitutions().apply(ivalType);
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt)
    {
        printIndented("fold(", indentAmt);
        func.displaySubtree(indentAmt + 2);
        ival.displaySubtree(indentAmt + 2);
        lst.displaySubtree(indentAmt + 2);
        printIndented(String.valueOf(leftFold), indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Creates a new list that is the reverse of {@code theList}.
     * 
     * @param theList the list to reverse.
     * @return the reverse of {@code theList}.
     */
    private LinkedList<Object> reverse(LinkedList<Object> theList)
    {
        Iterator<Object> itr = theList.descendingIterator();
        LinkedList<Object> res = new LinkedList<>();

        while (itr.hasNext())
            res.add(itr.next());
        return res;
    }
}

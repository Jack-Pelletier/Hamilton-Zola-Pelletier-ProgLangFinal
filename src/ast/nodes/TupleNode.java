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
import java.util.List;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.TupleType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents a tuple literal: (e1, e2, ..., en)
 *
 * Runtime value: java.util.List<Object> (LinkedList) of evaluated element values.
 * Type: TupleType(List<Type>) with element types in order.
 */
public final class TupleNode extends SyntaxNode
{
    private final List<SyntaxNode> elems;

    /**
     * Constructs a tuple literal node.
     *
     * @param elems the element expressions (should contain at least 2 elems).
     * @param line the source line number.
     */
    public TupleNode(List<SyntaxNode> elems, long line)
    {
        super(line);
        this.elems = elems;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        if (elems == null || elems.size() < 2)
        {
            logError("Tuple literal requires at least 2 elements.");
            throw new EvaluationException();
        }

        LinkedList<Object> values = new LinkedList<Object>();

        for (SyntaxNode n : elems)
        {
            if (n == null)
            {
                logError("Tuple element is null.");
                throw new EvaluationException();
            }
            values.add(n.evaluate(env));
        }

        return values;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        if (elems == null || elems.size() < 2)
            throw new TypeException(buildErrorMessage("Tuple literal requires at least 2 elements."));

        LinkedList<Type> tys = new LinkedList<Type>();

        for (SyntaxNode n : elems)
        {
            Type t = n.typeOf(tenv, inferencer);
            t = inferencer.getSubstitutions().apply(t);
            tys.add(t);
        }

        return new TupleType(tys);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Tuple(", indentAmt);
        for (SyntaxNode n : elems)
            n.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

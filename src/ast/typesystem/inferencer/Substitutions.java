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
package ast.typesystem.inferencer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ast.typesystem.types.BoolType;
import ast.typesystem.types.FunType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.StringType;
import ast.typesystem.types.TupleType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.TypeEnvironment;

/**
 * This data structure tracks the substitution map created by the unification algorithm.
 */
public class Substitutions {

    private HashMap<VarType, Type> subst; // The substituion map.

    /**
     * Construct a new substitution.
     */
    public Substitutions()
    {
        this.subst = new HashMap<>();
    }

    /**
     * Converts the type to an externalizable form. This is a form where type
     * variables are rewritten so that they start with the smallest serial
     * number possible.
     *
     * @param type the type to externalize.
     * @return the externalized form of the type.
     */
    public Type externalize(Type type)
    {
        HashMap<VarType, VarType> extSubst = new HashMap<>();
        return externalizeHelper(extSubst, new TypeEnvironment(), type);
    }

    /**
     * This method takes and applies the known substitutions to the given type
     * returning a potentially new type.
     *
     * @param type the type to apply the substitution to.
     * @return a new type.
     */
    public Type apply(Type type)
    {
        // No substitution is needed in this case.
        if (type instanceof BoolType || type instanceof IntType
                || type instanceof RealType || type instanceof StringType)
            return type;

        // Handle the list type.
        else if (type instanceof ListType)
        {
            return new ListType(apply(((ListType) type).getElementType()));
        }

        // Handle tuple types.
        else if (type instanceof TupleType)
        {
            TupleType tt = (TupleType) type;
            List<Type> els = tt.getElementTypes();

            LinkedList<Type> newEls = new LinkedList<Type>();
            for (Type t : els)
                newEls.add(apply(t));

            return new TupleType(newEls);
        }

        // Handle the var type.
        else if (type instanceof VarType)
        {
            Type res = subst.get((VarType) type);
            if (res != null)
                return res;
            return type;
        }

        // Handle the function type.
        else if (type instanceof FunType)
        {
            return new FunType(apply(((FunType) type).getArgType()),
                    apply(((FunType) type).getBodyType()));
        }
        else
            return null;
    }

    /**
     * Add the substition to the map of known substitutions and cascade the new
     * information.
     *
     * @param tv      the type variable to substitute
     * @param newType the new type for tv.
     */
    public void updateSubstitutions(VarType tv, Type newType)
    {
        // Propigate the substitution information tv := newType into
        // the existing rules.
        for (VarType lhs : subst.keySet())
            subst.replace(lhs, propagateSubstitution(tv, newType, subst.get(lhs)));

        // Add the new type information to the substitution map.
        subst.put(tv, newType);
    }

    /**
     * Get the string form of the substition
     *
     * @return A string representation of the known substitutions.
     */
    @Override
    public String toString()
    {
        return "Substitutions: " + subst;
    }

    /**
     * Propigates the new type information to the rest of the substitutions.
     * This method potentially updates the substitutions.
     *
     * @param tv       the type variable with a new substitiution
     * @param newType  the new substitution
     * @param currType the current type (before new substitution information is
     *                 applied).
     * @return A new type that takes into account the new type information tv :=
     *         newType.
     */
    private Type propagateSubstitution(VarType tv, Type newType, Type currType)
    {
        // No variable in this rhs just return.
        if (currType instanceof IntType || currType instanceof RealType
                || currType instanceof BoolType || currType instanceof StringType)
            return currType;

        // Handle function substitutions.
        else if (currType instanceof FunType)
        {
            return new FunType(
                    propagateSubstitution(tv, newType, ((FunType) currType).getArgType()),
                    propagateSubstitution(tv, newType, ((FunType) currType).getBodyType()));
        }

        // Handle variable substitutions.
        else if (currType instanceof VarType)
        {
            if (tv.equals((VarType) currType))
            {
                if (newType instanceof VarType)
                    ((VarType) newType).copyConstraints(tv);
                return newType;
            }
            else
                return currType;
        }

        // Handle list substitutions.
        else if (currType instanceof ListType)
        {
            return new ListType(propagateSubstitution(tv, newType,
                    ((ListType) currType).getElementType()));
        }

        // Handle tuple substitutions.
        else if (currType instanceof TupleType)
        {
            TupleType tt = (TupleType) currType;
            List<Type> els = tt.getElementTypes();

            LinkedList<Type> newEls = new LinkedList<Type>();
            for (Type t : els)
                newEls.add(propagateSubstitution(tv, newType, t));

            return new TupleType(newEls);
        }

        // When in doubt, do nothing.
        return currType;
    }

    /**
     * A private helper for actually carrying out the externalization work.
     *
     * @param exSubst the externalized substitution map.
     * @param tenv    the type environment which is only used for generating fresh
     *                type variables.
     * @param type    the type to externalize.
     */
    private Type externalizeHelper(HashMap<VarType, VarType> exSubst, TypeEnvironment tenv,
            Type type)
    {
        // If we have a non type variable we are fine there is nothing to do.
        if (type instanceof BoolType || type instanceof IntType
                || type instanceof RealType || type instanceof StringType)
            return type;

        // Handle the list type.
        else if (type instanceof ListType)
            return new ListType(externalizeHelper(exSubst, tenv,
                    ((ListType) type).getElementType()));

        // Handle tuple types.
        else if (type instanceof TupleType)
        {
            TupleType tt = (TupleType) type;
            List<Type> els = tt.getElementTypes();

            LinkedList<Type> newEls = new LinkedList<Type>();
            for (Type t : els)
                newEls.add(externalizeHelper(exSubst, tenv, t));

            return new TupleType(newEls);
        }

        // Handle the var type.
        else if (type instanceof VarType)
        {
            if (exSubst.containsKey((VarType) type))
                return exSubst.get((VarType) type);

            exSubst.put((VarType) type, tenv.getTypeVariable());
            return exSubst.get((VarType) type);
        }

        // Handle the function type.
        else if (type instanceof FunType)
        {
            return new FunType(
                    externalizeHelper(exSubst, tenv, (((FunType) type).getArgType())),
                    externalizeHelper(exSubst, tenv, ((FunType) type).getBodyType()));
        }
        else
            return null;
    }
}

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

import ast.typesystem.TypeException;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.FunType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.StringType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;

/**
 * Represents the core type infrencer. It amasses a set of type equations and
 * solves them through unification. The solutions can then be applied to type
 * variables by calling {@code applySubstitution} to add new constraints the
 * consumer must call {@code unify}.
 * 
 * @author Zach Kissel
 */
public class Inferencer
{
    private Substitutions subst; // The current type equation solutions.

    /**
     * The default constructor builds a new type substitution.
     */
    public Inferencer()
    {
        subst = new Substitutions();
    }

    /**
     * Get the string form of infrerencer.
     * 
     * @return A string representation of the known substitutions.
     */
    @Override
    public String toString()
    {
        return subst.toString();
    }

    /**
     * Get the substitution map from the inferencer.
     * 
     * @return the substition map of the inferencer.
     */
    public Substitutions getSubstitutions()
    {
        return subst;
    }

    /**
     * Unifies the first and second type updating the substitution map if
     * needed. In particular this method attempts to find a set of substitutions
     * that makes type1 = type2. If no such set of substitutions exist, a type
     * exception is thrown.
     * 
     * @param type1 the first type
     * @param type2 the second type
     * @param msg the text to include in the error message, in case of error.
     * @throws TypeException if the types can not be unified.
     */
    public void unify(Type type1, Type type2, String msg) throws TypeException
    {
        // Apply the known substitutions.
        type1 = subst.apply(type1);
        type2 = subst.apply(type2);

        if (type1 == null || type2 == null)
            throw new TypeException("Invalid type or unknown value.");

        // The types are equal nothing else to do.
        if (type1.equals(type2))
            return;

        // Bind type1 to type2 if possible.
        if (type1 instanceof VarType)
        {
            if (((VarType) type1).checkConstraint(type2)
                    && noOccurrence((VarType) type1, type2))
                subst.updateSubstitutions((VarType) type1, type2);
            else
                throw new TypeException("Unification error: " + msg);
        }

        // Bind type2 to type1 if possible.
        else if (type2 instanceof VarType)
        {
            if (((VarType) type2).checkConstraint(type1)
                    && noOccurrence((VarType) type2, type1))
                subst.updateSubstitutions((VarType) type2, type1);
            else
                throw new TypeException("Unifcation error: " + msg);
        }

        // unify the arguments and bodies of the two lambda exprssions.
        else if (type1 instanceof FunType && type2 instanceof FunType)
        {
            unify(((FunType) type1).getArgType(),
                    ((FunType) type2).getArgType(), msg);
            unify(((FunType) type1).getBodyType(),
                    ((FunType) type2).getBodyType(), msg);
        }

        // Unify list types.
        else if (type1 instanceof ListType && type2 instanceof ListType)
        {
            unify(((ListType) type1).getElementType(),
                    ((ListType) type2).getElementType(), msg);
        }

        /*
         * NOTE:
         * For atomic types (int/real/bool/string), equality is handled by the
         * type1.equals(type2) check above. If StringType.equals is implemented
         * like IntType.equals (instanceof), it will unify correctly.
         */

        else
            throw new TypeException("Unification failed: " + msg);
    }

    /**
     * Makes sure that tv does not appear in ty. This is used by unification to
     * ensure that a type variable on the left-hand side of an equation does not
     * appear on the right hand side of the equation.
     * 
     * @param tv the type variable.
     * @param ty the type we want it bound to.
     * @return true if tv does not appear in ty.
     */
    private boolean noOccurrence(VarType tv, Type ty)
    {
        if (ty instanceof IntType || ty instanceof RealType
                || ty instanceof BoolType || ty instanceof StringType)
            return true;
        else if (ty instanceof FunType)
        {
            return noOccurrence(tv, ((FunType) ty).getArgType())
                    && noOccurrence(tv, ((FunType) ty).getBodyType());
        }
        else if (ty instanceof VarType)
        {
            return !tv.equals((VarType) ty);
        }
        else if (ty instanceof ListType)
        {
            return noOccurrence(tv, ((ListType) ty).getElementType());
        }

        return false;
    }
}

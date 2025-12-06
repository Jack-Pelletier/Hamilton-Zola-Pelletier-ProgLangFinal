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
package ast.typesystem.types;

import ast.typesystem.inferencer.Inferencer;

/**
 * Represents a function type. 
 * @author Zach Kissel
 */
public final class FunType extends Type
{
    private Type argType;       // The type of the argument.
    private Type bodyType;      // The type of the body.

    /**
     * Construct a new function type
     * @param argType the type of the argument.
     * @param bodyType the type of the body.
     */
    public FunType(Type argType, Type bodyType)
    {
        this.argType = argType;
        this.bodyType = bodyType;
    }

    /**
     * Get the type of the argument.
     * @return the argument type.
     */
    public Type getArgType()
    {
        return this.argType;
    }

    /**
     * Get the type of the body.
     * @return the body type.
     */
    public Type getBodyType()
    {
        return this.bodyType;
    }

    /**
     * Set the body type to the given type.
     * 
     * @param newType the new type for the body.
     */
    public void setBodyType(Type newType)
    {
        this.bodyType = newType;
    }

    /** 
     * Set the arg type to the given type
     * 
     * @param newType the new type for the argument.
     */
    public void setArgType(Type newType)
    {
        this.argType = newType;
    }

    /**
     * Check equality of function types.
     * 
     * @param obj the object to test.
     */
    @Override
    public boolean equals(Object obj)
    {
        // Check to see if we are comparing to ourself.
        if (obj == this)
            return true;

        // Make sure we are looking at a list type.
        if (!(obj instanceof FunType))
            return false;

        FunType rhs = (FunType) obj;

        return rhs.argType.equals(this.argType) && rhs.bodyType.equals(this.bodyType);
    }

    /**
     * Gets the type as a string.
     * 
     * @return a the type as a string.
     */
    @Override
    public String toString()
    {
        Inferencer inf = new Inferencer();
        FunType t = (FunType) inf.getSubstitutions().externalize(new FunType(argType, bodyType));
        return t.getArgType() + " -> " + t.getBodyType();
    }
}

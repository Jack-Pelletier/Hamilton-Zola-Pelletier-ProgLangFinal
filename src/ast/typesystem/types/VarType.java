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

import java.util.LinkedList;

/**
 * Represents a type variable/generic type.
 * 
 * @author Zach Kissel
 */
public final class VarType extends Type
{
    private int serialNumber; // The unique identifier for this type variable.
    private LinkedList<Type> constraints; // Tracks type constraints for thsi variable.

    /**
     * Construct a new type variable.
     * 
     * @param serialNumber the identifier for the new variable type.
     */
    public VarType(int serialNumber)
    {
        this.serialNumber = serialNumber;
        this.constraints = new LinkedList<>();
    }

    /**
     * Sets {@code this} VarType's serial number to {@code type}'s serial
     * number.
     * 
     * @param type the type {@code this} should match.
     */
    public void setEqual(VarType type)
    {
        serialNumber = type.serialNumber;
    }

    /**
     * Check equality of variable types.
     * 
     * @param obj the object to test.
     */
    @Override
    public boolean equals(Object obj)
    {
        // Check to see if we are comparing to ourself.
        if (obj == this)
            return true;

        // Make sure we are looking at a variable type.
        if (!(obj instanceof VarType))
            return false;

        VarType rhs = (VarType) obj;

        return rhs.serialNumber == this.serialNumber;
    }

    /**
     * Gets the type as a string.
     * 
     * @return a the type as a string.
     */
    @Override
    public String toString()
    {
        if (constraints.isEmpty())
            return "t" + String.valueOf(serialNumber);
        else
            return "t" + String.valueOf(serialNumber) + "/Constraints: "
                    + constraints;
    }

    /**
     * Adds a constraint to the possible values the type variable can take on.
     * 
     * @param t the type to add to the constraints.
     */
    public void addConstraint(Type t)
    {
        constraints.add(t);
    }

    /**
     * Copies the constraints from one type variable to this.
     * 
     * @param type a VarType to copy the contraints of.
     */
    public void copyConstraints(VarType type)
    {
        this.constraints.addAll(type.constraints);
    }

    /**
     * Checks if the constraints are satisified.
     * 
     * @param t the type to check against constraints.
     * @return {@code true} if the constraint is satisifed, {@code false}
     *         otherwise.
     */
    public boolean checkConstraint(Type t)
    {
        if (constraints.isEmpty())
            return true;

        if (t instanceof VarType)
            return true;

        for (Type c : constraints)
            if (t.getClass() == c.getClass())
                return true;
        return false;
    }
}

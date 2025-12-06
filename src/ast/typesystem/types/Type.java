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

/**
 * Represents a simple type.
 * 
 * @author Zach Kissel
 */
public abstract class Type
{

    /**
     * Default constructor.
     */
    public Type() {}
    
    /**
     * Provide a basic equals method that just checks to make sure the object is
     * a type. Descendants of {@code Type} should be more specific.
     * 
     * @param obj the object to test.
     */
    @Override
    public boolean equals(Object obj)
    {
        // Check to see if we are comparing to ourself.
        if (obj == this)
            return true;

        return (obj instanceof Type);
    }

    /**
     * Gets the type as a string.
     * 
     * @return a the type as a string.
     */
    @Override
    public String toString()
    {
        return "?";
    }

}

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
package environment;

import java.util.HashMap;
import java.util.Set;

import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import lexer.Token;

/**
 * A simple representation of an type environment.
 * 
 * @author Zach Kissel
 */
public class TypeEnvironment
{
    /**
     * A private inner class to maintain the serial number logic. It 
     * is responsible for maintaining the uniqueness of type variables.
     */
    private class VarGenerator
    {
        private int currentSerialNumber;       // The last serial number used.

        /**
         * Initialize the variable generator.
         */
        public VarGenerator()
        {
            currentSerialNumber = -1;
        }

        /**
         * Generate a new unique type variable.
         * @return A new type variable with a unique serial number.
         */
        public VarType getNextVar()
        {
            currentSerialNumber++;
            return new VarType(currentSerialNumber);
        }
    }

    private VarGenerator gen;               // The type varaible generator.
    private HashMap<String, Type> env;      // The environemnt

    /**
     * Sets up the initial environment.
     */
    public TypeEnvironment()
    {
        env = new HashMap<>();
        gen = new VarGenerator();
    }

    /**
     * Returns the type associated with a token.
     * 
     * @param tok the token to look up the value of.
     * @return the type of {@code tok} in the environment. A value of null is
     *         returned if the token is not in the environment.
     */
    public Type lookup(Token tok)
    {
        return env.get(tok.getValue());
    }

    /**
     * Clears the contents of the tenv.
     */
    public void clearTenv()
    {
        env.clear();
    }

    /**
     * Update the environment such that token {@code tok} has the given value
     * {@code val}.
     * 
     * @param tok the token to update.
     * @param type the type to associate with the token.
     */
    public void updateEnvironment(Token tok, Type type)
    {
        if (env.replace(tok.getValue(), type) == null)
            env.put(tok.getValue(), type);
    }

    /**
     * Makes a copy of the current environment.
     * 
     * @return a copy of the environment.
     */
    public TypeEnvironment copy()
    {
        TypeEnvironment newEnv = new TypeEnvironment();
        newEnv.env.putAll(env);

        // Don't make a copy of the variable generator since
        // we want to maintain the invariant that all type 
        // variables are unique across all copies of 
        // a given type environment.
        newEnv.gen = this.gen;
        return newEnv;
    }

    /**
     * Get all of the variables in the tenv.
     * @return The set of all variables in the tenv.
     */
    public Set<String> getKnowVariables()
    {
        return env.keySet();
    }

    /**
     * Get a fresh type variable. 
     * 
     * @return a new unique type variable.
     */
    public VarType getTypeVariable()
    {
       return gen.getNextVar();
    }

    /**
     * Provides a string representing the environment.
     * 
     * @return a string representation of the environment.
     */
    @Override
    public String toString()
    {
        return env.toString();
    }
}

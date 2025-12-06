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
package ast;

import ast.nodes.ProgNode;
import ast.nodes.SyntaxNode;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents a syntax tree for the language.
 * 
 * @author Zach Kissel
 */
public class SyntaxTree
{
    SyntaxNode root; // The root of the syntax tree.
    Environment env; // The executional environment.
    TypeEnvironment tenv; // The type environment.
    Inferencer inferencer;   // The type substitutions.

    /**
     * Constructs a new syntax tree with root {@code root}.
     * 
     * @param root the root node of the tree.
     */
    public SyntaxTree(SyntaxNode root)
    {
        this.root = root;
        this.env = new Environment();
        this.tenv = new TypeEnvironment();
        this.inferencer = new Inferencer();
    }

    /**
     * Construct an empty syntax tree.
     */
    public SyntaxTree()
    {
        this(null);
    }

    /**
     * Sets the root node to {@code root}
     * 
     * @param root the object to set the root node to.
     */
    public void setRootNode(SyntaxNode root)
    {
        this.root = root;
    }

    /**
     * Gets the root node of the tree.
     * 
     * @return a reference to the root node of the tree.
     */
    public SyntaxNode getRootNode()
    {
        return this.root;
    }

    /**
     * Evaluate the syntax tree.
     * 
     * @return the object representing the result of the evaluation.
     * @throws EvaluationException if the evaluation fails.
     */
    public Object evaluate() throws EvaluationException
    {
         return root.evaluate(env);
    }

    /**
     * Get a copy of the current executional evironment.
     * 
     * @return the environment associated with this exeuction.
     */
    public Environment getEnvironment()
    {
        return env;
    }

    /**
     * Get a copy of the current type environment.
     * 
     * @return the type environment associated with the tree.
     */
    public TypeEnvironment getTypeEnvironment()
    {
        return tenv;
    }

    /**
     * Set the executional environment to {@code env}
     * 
     * @param env the executional environment.
     */
    public void setEnvironment(Environment env)
    {
        this.env = env;
    }

    /**
     * Set the type environment to {@code tenv}
     * 
     * @param tenv the type environment.
     */
    public void setTypeEnvironment(TypeEnvironment tenv)
    {
        this.tenv = tenv;
    }

    /**
     * Get the type of the statement as a string.
     * @return the syntax tree's type.
     * @throws TypeException when the type of the tree can not 
     * be determined.
     */
    public String getType() throws TypeException
    {
        if (root == null)
            throw new TypeException("Empty tree -- no type.");
            
        Type typ = root.typeOf(tenv, inferencer);
        if (typ == null)
            throw new TypeException("Unknown value.");
        return typ.toString();  
    }

    /**
     * Type checks the program.
     * @return true if the program type checks; otherwise false.
     */
    public boolean typeCheck()
    {
        if (root instanceof ProgNode)
            return ((ProgNode)root).typeCheck(tenv, inferencer);
        return false;
    }
    /**
     * Displays the syntax tree to the screen in a nicely formatted manner.
     */
    public void printTree()
    {
        if (root != null)
            root.displaySubtree(0);
        else 
            System.out.println("EMPTY TREE");
    }
}

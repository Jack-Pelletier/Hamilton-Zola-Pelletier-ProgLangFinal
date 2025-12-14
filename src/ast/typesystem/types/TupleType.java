package ast.typesystem.types;

import java.util.List;

/**
 * Represents a tuple type.
 * Example: (int, bool, string)
 */
public final class TupleType extends Type
{
    /**
     * The types of the tuple elements.
     */
    private final List<Type> elementTypes;

    /**
     * Constructs a tuple type with the given element types.
     *
     * @param elementTypes the types of each element in the tuple
     */
    public TupleType(List<Type> elementTypes)
    {
        this.elementTypes = elementTypes;
    }

    /**
     * Returns the type of the element at the given index.
     *
     * @param index the index of the element
     * @return the element type
     */
    public Type get(int index)
    {
        return elementTypes.get(index);
    }

    /**
     * Returns the number of elements in the tuple.
     *
     * @return tuple arity
     */
    public int size()
    {
        return elementTypes.size();
    }

    /**
     * Returns the list of element types.
     *
     * @return element types
     */
    public List<Type> getElementTypes()
    {
        return elementTypes;
    }

    /**
     * Check equality of tuple types.
     *
     * @param obj the object to test.
     */
    @Override
    public boolean equals(Object obj)
    {
        // Check to see if we are comparing to ourself.
        if (obj == this)
            return true;

        // Make sure we are looking at a tuple type.
        if (!(obj instanceof TupleType))
            return false;

        TupleType other = (TupleType) obj;

        // Tuple types are equal if their element types are equal (in order)
        return elementTypes.equals(other.elementTypes);
    }

    /**
     * Gets the type as a string.
     *
     * @return the type as a string.
     */
    @Override
    public String toString()
    {
        return elementTypes.toString();
    }
}

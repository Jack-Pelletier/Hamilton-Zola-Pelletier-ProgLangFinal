package ast.typesystem.types;

/**
 * Represents a string type.
 * 
 */
public final class StringType extends Type
{
    /**
     * Default constructor.
     */
    public StringType() {}

    /**
     * Check equality of string types.
     *
     * @param obj the object to test.
     */
    @Override
    public boolean equals(Object obj)
    {
        // Check to see if we are comparing to ourself.
        if (obj == this)
            return true;

        // Make sure we are looking at a string type.
        return (obj instanceof StringType);
    }

    /**
     * Gets the type as a string.
     *
     * @return the type as a string.
     */
    @Override
    public String toString()
    {
        return "string";
    }
}

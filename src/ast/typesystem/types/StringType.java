package ast.typesystem.types;

/**
 * Represents the string type.
 */
public final class StringType extends Type
{
    public StringType() {}

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof StringType;
    }

    @Override
    public int hashCode()
    {
        return StringType.class.hashCode();
    }

    @Override
    public String toString()
    {
        return "string";
    }
}

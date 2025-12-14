package ast.nodes;

import java.util.ArrayList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.ListType;
import ast.typesystem.types.StringType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents explode: (explode s)
 * Converts a string into a list of 1-character strings.
 */
public final class ExplodeNode extends SyntaxNode
{
    private SyntaxNode expr;

    /**
     * Constructs a new explode node.
     *
     * @param expr the expression to explode.
     * @param line the source line.
     */
    public ExplodeNode(SyntaxNode expr, long line)
    {
        super(line);
        this.expr = expr;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object v = expr.evaluate(env);

        if (!(v instanceof String))
        {
            logError("explode expects a string.");
            throw new EvaluationException();
        }

        String s = (String) v;
        ArrayList<Object> result = new ArrayList<Object>();

        for (int i = 0; i < s.length(); i++)
            result.add(String.valueOf(s.charAt(i)));

        return result;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t = expr.typeOf(tenv, inferencer);

        inferencer.unify(t, new StringType(),
                buildErrorMessage("explode expects a string."));

        // list[string]
        return new ListType(new StringType());
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Explode(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.IntType;
import ast.typesystem.types.StringType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents the string length operation: (strlen s)
 *
 * @author Zach Kissel
 */
public final class StrLenNode extends SyntaxNode
{
    private SyntaxNode expr;

    /**
     * Constructs a new strlen node.
     *
     * @param expr the expression to compute the string length of.
     * @param line the source line.
     */
    public StrLenNode(SyntaxNode expr, long line)
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
            logError("strlen expects a string.");
            throw new EvaluationException();
        }

        return ((String) v).length();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t = expr.typeOf(tenv, inferencer);

        inferencer.unify(t, new StringType(),
                buildErrorMessage("strlen expects a string."));

        return new IntType();
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("StrLen(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.StringType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents string concatenation: (strcat s1 s2)
 *
 */
public final class StrCatNode extends SyntaxNode
{
    private SyntaxNode left;
    private SyntaxNode right;

    /**
     * Constructs a new strcat node.
     *
     * @param left the left string expression.
     * @param right the right string expression.
     * @param line the source line.
     */
    public StrCatNode(SyntaxNode left, SyntaxNode right, long line)
    {
        super(line);
        this.left = left;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object v1 = left.evaluate(env);
        Object v2 = right.evaluate(env);

        if (!(v1 instanceof String) || !(v2 instanceof String))
        {
            logError("strcat expects two strings.");
            throw new EvaluationException();
        }

        return ((String) v1) + ((String) v2);
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t1 = left.typeOf(tenv, inferencer);
        Type t2 = right.typeOf(tenv, inferencer);

        inferencer.unify(t1, new StringType(),
                buildErrorMessage("strcat expects first argument to be a string."));
        inferencer.unify(t2, new StringType(),
                buildErrorMessage("strcat expects second argument to be a string."));

        return new StringType();
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("StrCat(", indentAmt);
        left.displaySubtree(indentAmt + 2);
        right.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

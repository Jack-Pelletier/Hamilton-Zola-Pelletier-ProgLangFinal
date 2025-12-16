package ast.nodes;

import java.util.List;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents the isempty operation.
 *
 * Usage:
 *   isempty(list)
 *
 * Returns true if the list is empty, false otherwise.
 */
public final class IsEmptyNode extends SyntaxNode
{
    private final SyntaxNode expr;

    public IsEmptyNode(SyntaxNode expr, long line)
    {
        super(line);
        this.expr = expr;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object value = expr.evaluate(env);

        if (!(value instanceof List))
        {
            logError("isempty expects a list.");
            throw new EvaluationException();
        }

        return ((List<?>) value).isEmpty();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t = expr.typeOf(tenv, inferencer);

        // Use unification instead of manual instanceof check
        inferencer.unify(
            t,
            new ListType(tenv.getTypeVariable()),
            buildErrorMessage("isempty expects a list.")
        );
        

        return new BoolType();
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("IsEmpty(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

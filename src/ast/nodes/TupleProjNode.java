package ast.nodes;

import java.util.List;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.TupleType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents tuple projection: (proj i t)
 * Returns the i-th element of tuple t (0-based).
 *
 * NOTE: The index is stored as an int (parsed from an INT token),
 * so we do not require an IntNode class.
 */
public final class TupleProjNode extends SyntaxNode
{
    private int index;
    private SyntaxNode tupleExpr;

    /**
     * Constructs a new tuple projection node.
     *
     * @param index the element index (0-based).
     * @param tupleExpr expression that evaluates to a tuple.
     * @param line the source line.
     */
    public TupleProjNode(int index, SyntaxNode tupleExpr, long line)
    {
        super(line);
        this.index = index;
        this.tupleExpr = tupleExpr;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object tupV = tupleExpr.evaluate(env);

        /*
         * IMPORTANT:
         * Do not require a specific List implementation (ArrayList vs LinkedList).
         * A tuple runtime value is represented as a Java List.
         */
        if (!(tupV instanceof List))
        {
            logError("proj expects a tuple.");
            throw new EvaluationException();
        }

        @SuppressWarnings("unchecked")
        List<Object> tup = (List<Object>) tupV;

        if (index < 0 || index >= tup.size())
        {
            logError("proj index out of bounds.");
            throw new EvaluationException();
        }

        return tup.get(index);
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t = tupleExpr.typeOf(tenv, inferencer);
        t = inferencer.getSubstitutions().apply(t);

        if (!(t instanceof TupleType))
            throw new TypeException(buildErrorMessage("proj expects a tuple."));

        TupleType tt = (TupleType) t;

        if (index < 0 || index >= tt.size())
            throw new TypeException(buildErrorMessage("proj index out of bounds."));

        return tt.get(index);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("TupleProj[" + index + "](", indentAmt);
        tupleExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

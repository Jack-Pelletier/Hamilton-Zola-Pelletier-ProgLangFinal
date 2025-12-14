package ast.nodes;

import java.util.ArrayList;
import java.util.List;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.TupleType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents tuple swapping: (swap t)
 * For a 2-tuple (a, b), returns (b, a).
 */
public final class TupleSwapNode extends SyntaxNode
{
    private SyntaxNode tupleExpr;

    /**
     * Constructs a new tuple swap node.
     *
     * @param tupleExpr the tuple expression to swap.
     * @param line the source line.
     */
    public TupleSwapNode(SyntaxNode tupleExpr, long line)
    {
        super(line);
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
            logError("swap expects a tuple.");
            throw new EvaluationException();
        }

        @SuppressWarnings("unchecked")
        List<Object> tup = (List<Object>) tupV;

        if (tup.size() != 2)
        {
            logError("swap expects a 2-tuple.");
            throw new EvaluationException();
        }

        ArrayList<Object> result = new ArrayList<Object>();
        result.add(tup.get(1));
        result.add(tup.get(0));

        return result;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t = tupleExpr.typeOf(tenv, inferencer);

        /*
         * swap requires a 2-tuple. To express that in the type system we unify
         * the expression type with a fresh 2-tuple (a, b).
         */
        Type a = tenv.getTypeVariable();
        Type b = tenv.getTypeVariable();

        ArrayList<Type> expectedElems = new ArrayList<Type>();
        expectedElems.add(a);
        expectedElems.add(b);

        TupleType expected = new TupleType(expectedElems);

        inferencer.unify(t, expected,
                buildErrorMessage("swap expects a tuple."));

        /*
         * Apply substitutions so nested/variable cases resolve properly.
         * After unify, expected is effectively (T1, T2). swap returns (T2, T1).
         */
        TupleType resolved = (TupleType) inferencer.getSubstitutions().apply(expected);

        ArrayList<Type> swapped = new ArrayList<Type>();
        swapped.add(resolved.get(1));
        swapped.add(resolved.get(0));

        return new TupleType(swapped);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("TupleSwap(", indentAmt);
        tupleExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

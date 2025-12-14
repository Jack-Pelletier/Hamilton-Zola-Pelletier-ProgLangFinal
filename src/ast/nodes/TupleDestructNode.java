package ast.nodes;

import java.util.LinkedList;
import java.util.List;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.ListType;
import ast.typesystem.types.TupleType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents tuple destructing: (destruct t)
 * Converts a tuple into a list of its elements.
 *
 * Typing: only allowed if all tuple elements unify to the same type T,
 * returns list[T].
 */
public final class TupleDestructNode extends SyntaxNode
{
    private SyntaxNode tupleExpr;

    /**
     * Constructs a new tuple destruct node.
     *
     * @param tupleExpr the tuple expression to destruct.
     * @param line the source line.
     */
    public TupleDestructNode(SyntaxNode tupleExpr, long line)
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
            logError("destruct expects a tuple.");
            throw new EvaluationException();
        }

        @SuppressWarnings("unchecked")
        List<Object> tup = (List<Object>) tupV;

        /*
         * IMPORTANT:
         * The result of destruct is a *language list*, which in this interpreter
         * is represented as a java.util.LinkedList (so hd/tl/len work).
         */
        return new LinkedList<Object>(tup);
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type t = tupleExpr.typeOf(tenv, inferencer);
        t = inferencer.getSubstitutions().apply(t);

        if (!(t instanceof TupleType))
            throw new TypeException(buildErrorMessage("destruct expects a tuple."));

        TupleType tt = (TupleType) t;

        if (tt.size() == 0)
            throw new TypeException(buildErrorMessage("destruct expects a non-empty tuple."));

        // Enforce homogeneity: all elements unify with first element type.
        Type elemT = tt.get(0);
        for (int i = 1; i < tt.size(); i++)
        {
            inferencer.unify(tt.get(i), elemT,
                    buildErrorMessage("destruct requires a tuple with uniform element types."));
        }

        // Apply substitutions so elemT reflects any unification constraints.
        elemT = inferencer.getSubstitutions().apply(elemT);

        return new ListType(elemT);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("TupleDestruct(", indentAmt);
        tupleExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

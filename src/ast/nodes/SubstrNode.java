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
 * Represents substring: (substr s start len)
 *
 */
public final class SubstrNode extends SyntaxNode
{
    private SyntaxNode strExpr;
    private SyntaxNode startExpr;
    private SyntaxNode lenExpr;

    /**
     * Constructs a new substr node.
     *
     * @param strExpr the string expression.
     * @param startExpr the starting index (0-based).
     * @param lenExpr the length.
     * @param line the source line.
     */
    public SubstrNode(SyntaxNode strExpr, SyntaxNode startExpr, SyntaxNode lenExpr, long line)
    {
        super(line);
        this.strExpr = strExpr;
        this.startExpr = startExpr;
        this.lenExpr = lenExpr;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object sv = strExpr.evaluate(env);
        Object iv = startExpr.evaluate(env);
        Object lv = lenExpr.evaluate(env);

        if (!(sv instanceof String))
        {
            logError("substr expects first argument to be a string.");
            throw new EvaluationException();
        }

        if (!(iv instanceof Integer))
        {
            logError("substr expects second argument (start) to be an int.");
            throw new EvaluationException();
        }

        if (!(lv instanceof Integer))
        {
            logError("substr expects third argument (len) to be an int.");
            throw new EvaluationException();
        }

        String s = (String) sv;
        int start = (Integer) iv;
        int len = (Integer) lv;

        if (start < 0 || len < 0)
        {
            logError("substr start and len must be non-negative.");
            throw new EvaluationException();
        }

        if (start > s.length() || start + len > s.length())
        {
            logError("substr range out of bounds.");
            throw new EvaluationException();
        }

        return s.substring(start, start + len);
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type tStr = strExpr.typeOf(tenv, inferencer);
        Type tStart = startExpr.typeOf(tenv, inferencer);
        Type tLen = lenExpr.typeOf(tenv, inferencer);

        inferencer.unify(tStr, new StringType(),
                buildErrorMessage("substr expects first argument to be a string."));
        inferencer.unify(tStart, new IntType(),
                buildErrorMessage("substr expects second argument (start) to be an int."));
        inferencer.unify(tLen, new IntType(),
                buildErrorMessage("substr expects third argument (len) to be an int."));

        return new StringType();
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Substr(", indentAmt);
        strExpr.displaySubtree(indentAmt + 2);
        startExpr.displaySubtree(indentAmt + 2);
        lenExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

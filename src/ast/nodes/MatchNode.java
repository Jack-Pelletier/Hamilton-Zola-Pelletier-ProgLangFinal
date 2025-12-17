package ast.nodes;

import java.util.ArrayList;
import java.util.List;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.StringType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * Represents a match expression similar to ML.
 *
 * match e with
 *   | p1 => e1
 *   | p2 => e2
 * end
 */
public final class MatchNode extends SyntaxNode
{
    public static final class Case
    {
        private final PatternNode pattern;
        private final SyntaxNode body;

        public Case(PatternNode pattern, SyntaxNode body)
        {
            this.pattern = pattern;
            this.body = body;
        }

        public PatternNode getPattern()
        {
            return pattern;
        }

        public SyntaxNode getBody()
        {
            return body;
        }
    }

    private final SyntaxNode scrutinee;
    private final List<Case> cases;

    public MatchNode(SyntaxNode scrutinee, List<Case> cases, long line)
    {
        super(line);
        this.scrutinee = scrutinee;
        this.cases = cases == null ? new ArrayList<>() : cases;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object v = scrutinee.evaluate(env);

        for (Case c : cases)
        {
            Environment extended = env.clone();

            if (c.getPattern().matchAndBind(v, extended))
            {
                return c.getBody().evaluate(extended);
            }
        }

        logError("non exhaustive match.");
        throw new EvaluationException();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
    {
        Type scrutType = scrutinee.typeOf(tenv, inferencer);

        if (cases.isEmpty())
        {
            throw new TypeException(buildErrorMessage("match requires at least one case."));
        }

        Type resultType = inferencer.freshVar();

        for (Case c : cases)
        {
            TypeEnvironment extended = tenv.clone();

            Type patType = c.getPattern().typeOf(extended, inferencer);

            inferencer.unify(
                    scrutType,
                    patType,
                    buildErrorMessage("pattern does not match scrutinee type.")
            );

            Type branchType = c.getBody().typeOf(extended, inferencer);

            inferencer.unify(
                    resultType,
                    branchType,
                    buildErrorMessage("all match branches must return the same type.")
            );
        }

        return resultType;
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Match", indentAmt);
        scrutinee.displaySubtree(indentAmt + 2);

        for (Case c : cases)
        {
            printIndented("Case", indentAmt + 2);
            c.getPattern().displaySubtree(indentAmt + 4);
            c.getBody().displaySubtree(indentAmt + 4);
        }
    }

    /**
     * Minimal pattern base class.
     * You can later split this into separate files if you want.
     */
    public static abstract class PatternNode extends SyntaxNode
    {
        public PatternNode(long line)
        {
            super(line);
        }

        public abstract boolean matchAndBind(Object value, Environment env) throws EvaluationException;

        public abstract Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException;

        @Override
        public Object evaluate(Environment env) throws EvaluationException
        {
            logError("patterns cannot be evaluated directly.");
            throw new EvaluationException();
        }
    }

    /**
     * Wildcard pattern: _
     */
    public static final class WildcardPattern extends PatternNode
    {
        public WildcardPattern(long line)
        {
            super(line);
        }

        @Override
        public boolean matchAndBind(Object value, Environment env)
        {
            return true;
        }

        @Override
        public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
        {
            return inferencer.freshVar();
        }

        @Override
        public void displaySubtree(int indentAmt)
        {
            printIndented("Pattern(_)", indentAmt);
        }
    }

    /**
     * Variable binding pattern: x
     */
    public static final class VarPattern extends PatternNode
    {
        private final Token id;

        public VarPattern(Token id, long line)
        {
            super(line);
            this.id = id;
        }

        @Override
        public boolean matchAndBind(Object value, Environment env)
        {
            env.put(id.getLexeme(), value);
            return true;
        }

        @Override
        public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
        {
            Type tv = inferencer.freshVar();
            tenv.put(id.getLexeme(), tv);
            return tv;
        }

        @Override
        public void displaySubtree(int indentAmt)
        {
            printIndented("PatternVar(" + id.getLexeme() + ")", indentAmt);
        }
    }

    /**
     * Literal patterns: int, bool, string
     */
    public static final class LiteralPattern extends PatternNode
    {
        private final Object literal;

        public LiteralPattern(Object literal, long line)
        {
            super(line);
            this.literal = literal;
        }

        @Override
        public boolean matchAndBind(Object value, Environment env)
        {
            if (value == null)
                return false;
            return value.equals(literal);
        }

        @Override
        public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
        {
            if (literal instanceof Integer)
                return new IntType();
            if (literal instanceof Boolean)
                return new BoolType();
            if (literal instanceof String)
                return new StringType();
            return inferencer.freshVar();
        }

        @Override
        public void displaySubtree(int indentAmt)
        {
            printIndented("PatternLit(" + literal + ")", indentAmt);
        }
    }
}

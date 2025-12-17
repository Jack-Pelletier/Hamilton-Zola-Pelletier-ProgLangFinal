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
 * Match expression node.
 */
public final class MatchNode extends SyntaxNode
{
    public static final class Case
    {
        public final PatternNode pattern;
        public final SyntaxNode body;

        public Case(PatternNode pattern, SyntaxNode body)
        {
            this.pattern = pattern;
            this.body = body;
        }
    }

    private final SyntaxNode scrutinee;
    private final List<Case> cases;

    public MatchNode(SyntaxNode scrutinee, List<Case> cases, long line)
    {
        super(line);
        this.scrutinee = scrutinee;
        this.cases = (cases == null) ? new ArrayList<Case>() : cases;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object value = scrutinee.evaluate(env);

        for (Case c : cases)
        {
            Environment extended = new Environment();

            if (c.pattern.matchAndBind(value, extended))
            {
                return c.body.evaluate(extended);
            }
        }

        logError("non exhaustive match expression.");
        throw new EvaluationException();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type scrutType = scrutinee.typeOf(tenv, inferencer);
        Type resultType = inferencer.freshVar();

        for (Case c : cases)
        {
            TypeEnvironment extended = new TypeEnvironment();

            Type patType = c.pattern.typeOf(extended, inferencer);
            inferencer.unify(
                    scrutType,
                    patType,
                    buildErrorMessage("pattern does not match scrutinee type.")
            );

            Type branchType = c.body.typeOf(extended, inferencer);
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
            c.pattern.displaySubtree(indentAmt + 4);
            c.body.displaySubtree(indentAmt + 4);
        }
    }

    public static abstract class PatternNode extends SyntaxNode
    {
        public PatternNode(long line)
        {
            super(line);
        }

        @Override
        public final Object evaluate(Environment env) throws EvaluationException
        {
            logError("pattern nodes are not directly evaluatable.");
            throw new EvaluationException();
        }

        public abstract boolean matchAndBind(Object value, Environment env)
                throws EvaluationException;
    }

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
            env.put(id.getValue(), value);
            return true;
        }

        @Override
        public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
        {
            Type tv = inferencer.freshVar();
            tenv.put(id.getValue(), tv);
            return tv;
        }

        @Override
        public void displaySubtree(int indentAmt)
        {
            printIndented("PatternVar(" + id.getValue() + ")", indentAmt);
        }
    }

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
            return value != null && value.equals(literal);
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

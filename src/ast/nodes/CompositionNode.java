package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;
import ast.nodes.FunctionCallNode;

/**
 * Handles syntactic sugar for composition.
 *
 *   f ∘ g   ==>  x -> f(g(x))
 *   a |> f  ==>  f(a)
 */
public final class CompositionNode extends SyntaxNode
{
    public enum CompositionKind
    {
        FUNCTION_COMPOSITION,
        PIPELINE
    }

    private final SyntaxNode left;
    private final SyntaxNode right;
    private final CompositionKind kind;

    public CompositionNode(SyntaxNode left,
                           SyntaxNode right,
                           CompositionKind kind,
                           long line)
    {
        super(line);
        this.left = left;
        this.right = right;
        this.kind = kind;
    }

    /**
     * Desugar the syntactic sugar into core AST nodes.
     */
    private SyntaxNode desugar()
    {
        switch (kind)
        {
            case FUNCTION_COMPOSITION:
            {
                // f ∘ g  ==>  x -> f(g(x))

                Token xTok = new Token(Token.Type.IDENTIFIER, "__comp_x");
                VariableNode x = new VariableNode(xTok, -1);

                SyntaxNode gx = new FunctionCallNode(right, x, -1);
                SyntaxNode fgx = new FunctionCallNode(left, gx, -1);

                return new LambdaNode(xTok, fgx, -1);
            }

            case PIPELINE:
                // a |> f  ==>  f(a)
                return new FunctionCallNode(right, left, -1);

            default:
                throw new IllegalStateException(
                        buildErrorMessage("Unknown composition kind.")
                );
        }
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        return desugar().evaluate(env);
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        return desugar().typeOf(tenv, inferencer);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Composition[" + kind + "]", indentAmt);
        left.displaySubtree(indentAmt + 2);
        right.displaySubtree(indentAmt + 2);
    }
}

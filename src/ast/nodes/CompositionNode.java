package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import ast.nodes.VariableNode; // Added import for VariableNode
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Handles syntactic sugar related to composition operations.
 * This node desugars composition expressions into core AST nodes
 * before evaluation or type inference.
 *
 * Examples:
 *   f ∘ g   →   x -> f(g(x))
 *   a |> f  →   f(a)
 *
 * @author Zach Kissel
 */
public class CompositionNode extends SyntaxNode
{
    public enum CompositionKind
    {
        FUNCTION_COMPOSITION,   // f ∘ g
        PIPELINE                // a |> f
    }

    private final SyntaxNode left;
    private final SyntaxNode right;
    private final CompositionKind kind;

    public CompositionNode(long lineNumber,
                           SyntaxNode left,
                           SyntaxNode right,
                           CompositionKind kind)
    {
        super(lineNumber);
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
                /*
                 * f ∘ g  ==>  (x -> f(g(x)))
                 */
                VariableNode x = new VariableNode(-1, "__comp_x");

                return new LambdaNode(
                        -1,
                        x,
                        new FunctionCallNode(
                                -1,
                                left,
                                new FunctionCallNode(-1, right, x)
                        )
                );

            case PIPELINE:
                /*
                 * a |> f  ==>  f(a)
                 */
                return new FunctionCallNode(-1, right, left);

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
        printIndented("CompositionNode (" + kind + ")", indentAmt);
        left.displaySubtree(indentAmt + 2);
        right.displaySubtree(indentAmt + 2);
    }
}

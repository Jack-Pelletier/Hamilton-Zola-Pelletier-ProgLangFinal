package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.IntType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;
import lexer.TokenType;

/**
 * Represents an integer literal.
 */
public final class IntNode extends SyntaxNode
{
    private final int value;

    /**
     * Construct an integer literal node.
     *
     * @param tok  the INT token
     * @param line the source line
     */
    public IntNode(Token tok, long line)
    {
        super(line);

        if (tok.getType() != TokenType.INT)
        {
            logError("IntNode expects an INT token.");
            throw new IllegalArgumentException();
        }

        this.value = Integer.parseInt(tok.getValue());
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        return value;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        return new IntType();
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Int(" + value + ")", indentAmt);
    }
}

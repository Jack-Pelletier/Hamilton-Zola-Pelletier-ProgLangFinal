package ast.nodes;

import java.util.ArrayList;
import java.util.List;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;
import lexer.TokenType;

/**
 * Handles syntactic sugar for filter.
 *
 * filter f lst
 *
 * Desugars to a recursive lambda-based definition.
 */
public final class FilterNode extends SyntaxNode {
    private final SyntaxNode predicate;
    private final SyntaxNode listExpr;

    public FilterNode(SyntaxNode predicate,
            SyntaxNode listExpr,
            long line) {
        super(line);
        this.predicate = predicate;
        this.listExpr = listExpr;
    }

    private Token freshToken(String prefix) {
        return new Token(
                TokenType.ID,
                "__filter_" + prefix + "_" + System.identityHashCode(this));
    }

    /**
     * Desugar filter into core language constructs.
     */
    private SyntaxNode desugar() {
        // fresh variables
        Token fTok = freshToken("f");
        Token lstTok = freshToken("lst");

        VariableNode fVar = new VariableNode(fTok, -1);
        VariableNode lstVar = new VariableNode(lstTok, -1);

        /// len(lst) = 0
        SyntaxNode lenCall = new FunctionCallNode(
                new VariableNode(new Token(TokenType.LEN, "len"), -1L),
                lstVar,
                -1L);

        // int literal 0
        Token zeroTok = new Token(TokenType.INT, "0");
        SyntaxNode zero = new IntNode(zeroTok, -1L);

        SyntaxNode condEmpty = new BinOpNode(lenCall, TokenType.EQ, zero, -1L);

        // hd(lst)
        SyntaxNode hdCall = new FunctionCallNode(
                new VariableNode(new Token(TokenType.LST_HD, "hd"), -1L),
                lstVar,
                -1L);

        // tl(lst)
        SyntaxNode tlCall = new FunctionCallNode(
                new VariableNode(new Token(TokenType.LST_TL, "tl"), -1L),
                lstVar,
                -1L);

        // recursive filter f (tl lst)
        SyntaxNode recur = new FilterNode(fVar, tlCall, -1L);

        // [hd(lst)] ++ recur
        List<SyntaxNode> singleton = new ArrayList<>();
        singleton.add(hdCall);

        SyntaxNode cons = new BinOpNode(
                new ListNode(singleton, -1L),
                TokenType.CONCAT,
                recur,
                -1L);

        // if len(lst)=0 then [] else ...
        SyntaxNode baseIf = new IfNode(
                condEmpty,
                new ListNode(-1L),
                ifNode,
                -1L);

                return new LambdaNode(
                    fTok,
                    new LambdaNode(lstTok, baseIf, -1),
                    -1);
            
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        return desugar().evaluate(env);
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException {
        return desugar().typeOf(tenv, inferencer);
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Filter(", indentAmt);
        predicate.displaySubtree(indentAmt + 2);
        listExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}

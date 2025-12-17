package ast.nodes;

import java.util.ArrayList;
import java.util.LinkedList;
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

        VariableNode fVar = new VariableNode(fTok, -1L);
        VariableNode lstVar = new VariableNode(lstTok, -1L);

        // len(lst) = 0
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

        // f(hd(lst))
        SyntaxNode predApply = new FunctionCallNode(fVar, hdCall, -1L);

        // recursive filter f (tl lst)
        SyntaxNode recur = new FilterNode(fVar, tlCall, -1L);

        // [hd(lst)] ++ recur
        LinkedList<SyntaxNode> singleton = new LinkedList<>();
        singleton.add(hdCall);

        SyntaxNode cons = new BinOpNode(
                new ListNode(singleton, -1L),
                TokenType.CONCAT,
                recur,
                -1L);

        // if f(hd(lst)) then cons else recur
        SyntaxNode ifNode = new IfNode(predApply, cons, recur, -1L);

        // if len(lst) = 0 then [] else ...
        SyntaxNode baseIf = new IfNode(
                condEmpty,
                new ListNode(new LinkedList<>(), -1L), // empty list
                ifNode,
                -1L);

        // lambda f -> lambda lst -> baseIf
        SyntaxNode filterFn = new LambdaNode(
                fTok,
                new LambdaNode(lstTok, baseIf, -1L),
                -1L);

        // apply to the actual arguments
        return new FunctionCallNode(
                new FunctionCallNode(filterFn, predicate, -1L),
                listExpr,
                -1L);
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

package ast.nodes;

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
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

        /*
         * NOTE:
         * The original version implemented filter using explicit recursion over:
         *   if len(lst)=0 then [] else ...
         * but that relies on building a new FilterNode inside desugar(), which causes
         * repeated desugaring during type inference (and can blow up).
         *
         * To preserve the overall structure (lambda f -> lambda lst -> <body>),
         * we keep the same outer shape and replace only the inner "baseIf" body
         * with an equivalent foldr-based definition:
         *
         * filter f lst
         *   = foldr (fn x -> fn acc -> if f(x) then [x] ++ acc else acc) [] lst
         */

        // foldr step variables
        Token xTok = freshToken("x");
        Token accTok = freshToken("acc");

        VariableNode xVar = new VariableNode(xTok, -1L);
        VariableNode accVar = new VariableNode(accTok, -1L);

        // f(x)
        SyntaxNode predApply = new FunctionCallNode(fVar, xVar, -1L);

        // [x] ++ acc
        LinkedList<SyntaxNode> singleton = new LinkedList<>();
        singleton.add(xVar);

        SyntaxNode cons = new BinOpNode(
                new ListNode(singleton, -1L),
                TokenType.CONCAT,
                accVar,
                -1L);

        // if f(x) then cons else acc
        SyntaxNode ifNode = new IfNode(predApply, cons, accVar, -1L);

        // fn x -> fn acc -> ifNode
        SyntaxNode stepFn = new LambdaNode(
                xTok,
                new LambdaNode(accTok, ifNode, -1L),
                -1L);

        // empty list
        SyntaxNode empty = new ListNode(new LinkedList<>(), -1L);

        // foldr(stepFn, [], lst)
        SyntaxNode baseIf = new FoldNode(stepFn, empty, lstVar, false, -1L);

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

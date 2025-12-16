/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import ast.SyntaxTree;
import ast.nodes.ApplyNode;
import ast.nodes.BinOpNode;
import ast.nodes.ExplodeNode;
import ast.nodes.FoldNode;
import ast.nodes.HeadNode;
import ast.nodes.IfNode;
import ast.nodes.LambdaNode;
import ast.nodes.LenNode;
import ast.nodes.LetNode;
import ast.nodes.ListNode;
import ast.nodes.MapNode;
import ast.nodes.ProgNode;
import ast.nodes.RelOpNode;
import ast.nodes.StrCatNode;
import ast.nodes.StrLenNode;
import ast.nodes.SubstrNode;
import ast.nodes.SyntaxNode;
import ast.nodes.TailNode;
import ast.nodes.TokenNode;
import ast.nodes.TupleDestructNode;
import ast.nodes.TupleNode;
import ast.nodes.TupleProjNode;
import ast.nodes.TupleSwapNode;
import ast.nodes.UnaryOpNode;
import ast.nodes.ValNode;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

/**
 * <p>
 * Parser for the MFL language. This is largely private methods where
 * there is one method the "eval" method for each non-terminal of the grammar.
 * There are also a collection of private "handle" methods that handle one
 * production associated with a non-terminal.
 * </p>
 * <p>
 * Each of the private methods operates on the token stream. It is important to
 * remember that all of our non-terminal processing methods maintain the invariant
 * that each method leaves the concludes such that the next unprocessed token is at
 * the front of the token stream. This means each method can assume the current token
 * has not yet been processed when the method begins. The methods {@code checkMatch}
 * and {@code match} are methods that maintain this invariant in the case of a match.
 * The method {@code tokenIs} does NOT advnace the token stream. To advance the token
 * stream the {@code nextTok} method can be used. In the rare cases that the token
 * at the head of the stream must be accessed directly, the {@code getCurrToken}
 * method can be used.
 * </p>
 *
 * @author Zach Kissel
 */
public class MFLParser extends Parser
{

    /**
     * Constructs a new parser for the file {@code source} by setting up lexer.
     *
     * @param src the source code file to parse.
     * @throws FileNotFoundException if the file can not be found.
     */
    public MFLParser(File src) throws FileNotFoundException
    {
        super(new Lexer(src));
    }

    /**
     * Construct a parser that parses the string {@code str}.
     *
     * @param str the code to evaluate.
     */
    public MFLParser(String str)
    {
        super(new Lexer(str));
    }

    /**
     * Parses the file according to the grammar.
     *
     * @return the abstract syntax tree representing the parsed program.
     * @throws ParseException when parsing fails.
     */
    public SyntaxTree parse() throws ParseException
    {
        SyntaxTree ast;

        nextToken(); // Get the first token.
        ast = new SyntaxTree(evalProg()); // Start processing at the root of the tree.

        match(TokenType.EOF, "EOF");

        return ast;
    }

    /************
     * Evaluation methods to constrct the AST associated with the non-terminals
     ***********/

    /**
     * Method to evaluate the program non-terminal. <prog> -> <expr> { <expr> }
     *
     * @throws ParseException if the evaluation of an expression fails.
     */
    private SyntaxNode evalProg() throws ParseException
    {
        LinkedList<SyntaxNode> exprs = new LinkedList<>();

        trace("Enter <prog>");
        while (!checkMatch(TokenType.EOF))
        {
            SyntaxNode currNode = evalValues();
            if (currNode == null)
                break;

            // Make sure we have a semi colon ending the line.
            if (match(TokenType.SEMI, ";"))
                exprs.add(currNode);
        }

        // We have an empty collection of expressions.
        if (exprs.size() == 0)
            return null;

        trace("Exit <prog>");
        return new ProgNode(exprs, super.getCurrLine());
    }

    /**
     * Method to evaluate the <values> non-terminal
     *
     * @throws ParseException if there is an error during parsing
     */
    private SyntaxNode evalValues() throws ParseException
    {
        // Function definition.
        if (checkMatch(TokenType.VAL))
            return getGoodParse(handleValues());
        else // Just an expression.
            return getGoodParse(evalExpr());
    }

    /**
     * Method to evaluate the expression non-terminal <expr>
     *
     * @throws ParseException if there is an error during parsing.
     */
    private SyntaxNode evalExpr() throws ParseException
    {
        trace("Enter <expr>");
        SyntaxNode expr = null;

        // Are we looking at a let expression?
        if (checkMatch(TokenType.LET))
            return handleLet();

        // Are we looking at an if-then-else expresison?
        if (checkMatch(TokenType.IF))
            return handleIf();

        // We are handling some other expression.
        expr = getGoodParse(evalLambda());

        trace("Exit <expr>");
        return expr;
    }

    /**
     * Method to evaluate the lambda non-terminal.
     *
     * @return The syntax tree resulting from evaluation
     * @throws ParseException when the evaluation fails.
     */
    private SyntaxNode evalLambda() throws ParseException
    {
        SyntaxNode expr = null;

        trace("Enter <evalLambda>");
        if (checkMatch(TokenType.FN))
            return handleLambdaExpr();

        expr = evalBoolExpr();
        trace("Exit <evalLambda>");
        return expr;
    }

    /**
     * Evaluates the bool expression non-terminal (bexpr).
     *
     * @return the resulting subtree.
     * @throws ParseException when the parsing fails.
     */
    private SyntaxNode evalBoolExpr() throws ParseException
    {
        SyntaxNode rexpr;
        TokenType op;
        SyntaxNode expr = null;

        trace("Enter <evalBoolExpr>");

        expr = getGoodParse(evalRexpr());

        op = getCurrToken().getType(); // Save off the supposed operation.

        while (checkMatch(TokenType.AND) || checkMatch(TokenType.OR))
        {
            rexpr = getGoodParse(evalRexpr());
            expr = new BinOpNode(expr, op, rexpr, getCurrLine());
            op = getCurrToken().getType();
        }

        trace("Exit <evalBoolExpr>");

        return expr;
    }

    /**
     * Evaluates relational expressions (the <rexpr> non-terminal)
     *
     * @return a SyntaxNode representing the relation expression.
     * @throws ParseException when parsing fails.
     */
    private SyntaxNode evalRexpr() throws ParseException
    {
        SyntaxNode left = null;
        SyntaxNode right = null;
        TokenType op;

        left = getGoodParse(evalMexpr());

        op = getCurrToken().getType(); // Save off what should be the operator.
        if (checkMatch(TokenType.LT) || checkMatch(TokenType.LTE)
                || checkMatch(TokenType.GT) || checkMatch(TokenType.GTE)
                || checkMatch(TokenType.EQ) || checkMatch(TokenType.NEQ))
        {
            right = getGoodParse(evalMexpr());
            return new RelOpNode(left, op, right, getCurrLine());
        }

        return left;
    }

    /**
     * evaluates the math expression non-terminal (mexpr).
     *
     * @return a SyntaxNode representing the expression.
     * @throws ParseException when parsing fails.
     */
    private SyntaxNode evalMexpr() throws ParseException
    {
        SyntaxNode expr = null;
        SyntaxNode rterm = null;
        TokenType op;

        expr = getGoodParse(evalTerm());

        op = getCurrToken().getType(); // This should be an operator.
        while (checkMatch(TokenType.ADD) || checkMatch(TokenType.SUB))
        {
            rterm = getGoodParse(evalTerm());
            expr = new BinOpNode(expr, op, rterm, getCurrLine());
            op = getCurrToken().getType(); // Save off the next operator(?).
        }

        return expr;
    }

    /**
     * Method to evaluate the term nonterminal.
     *
     * @return the subtree representing the expression.
     * @throws ParseException when the parsing fails.
     */
    private SyntaxNode evalTerm() throws ParseException
    {
        SyntaxNode rfact;
        TokenType op;
        SyntaxNode term;

        trace("Enter <term>");

        // Handle unary not.
        if (checkMatch(TokenType.NOT))
        {
            SyntaxNode expr = getGoodParse(evalRexpr());
            return new UnaryOpNode(expr, TokenType.NOT, getCurrLine());
        }

        term = getGoodParse(evalFactor());

        // Handle the higher level binary operations.
        op = getCurrToken().getType();
        while (checkMatch(TokenType.MULT) || checkMatch(TokenType.DIV)
                || checkMatch(TokenType.MOD) || checkMatch(TokenType.CONCAT))
        {
            rfact = getGoodParse(evalFactor());
            term = new BinOpNode(term, op, rfact, getCurrLine());
            op = getCurrToken().getType();
        }
        trace("Exit <term>");
        return term;
    }

    /**
     * Method to evaluate the factor non-terminal (the tightest binding operations).
     *
     * @return the subtree resulting from the parse.
     * @throws ParseException when parsing fails.
     */
    private SyntaxNode evalFactor() throws ParseException
{
    trace("Enter <factor>");
    SyntaxNode fact = null;

    // Do we have a unary sub (i.e., a negative).
    if (checkMatch(TokenType.SUB))
    {
        SyntaxNode expr = getGoodParse(evalFactor());
        return new UnaryOpNode(expr, TokenType.SUB, getCurrLine());
    }

    // list head built in function.
    else if (checkMatch(TokenType.LST_HD))
    {
        if (match(TokenType.LPAREN, "("))
        {
            fact = getGoodParse(evalExpr());
            if (match(TokenType.RPAREN, ")"))
                return new HeadNode(fact, getCurrLine());
        }
        return null;
    }

    // list tail built in function.
    else if (checkMatch(TokenType.LST_TL))
    {
        if (match(TokenType.LPAREN, "("))
        {
            fact = getGoodParse(evalExpr());
            if (match(TokenType.RPAREN, ")"))
                return new TailNode(fact, getCurrLine());
        }
        return null;
    }

    // Len built in function.
    else if (checkMatch(TokenType.LEN))
    {
        if (match(TokenType.LPAREN, "("))
        {
            fact = getGoodParse(evalExpr());
            if (match(TokenType.RPAREN, ")"))
                return new LenNode(fact, getCurrLine());
        }
        return null;
    }

    // ---- Tuple builtins by TokenType (works if lexer maps keywords) ----

    else if (checkMatch(TokenType.TUPLEPROJ))
    {
        if (match(TokenType.LPAREN, "("))
        {
            Token idxTok = getCurrToken();
            if (!match(TokenType.INT, "integer"))
                return null;

            int idx = Integer.parseInt(idxTok.getValue());

            SyntaxNode t = getGoodParse(evalExpr());

            if (match(TokenType.RPAREN, ")"))
                return new TupleProjNode(idx, t, getCurrLine());
        }
        return null;
    }

    else if (checkMatch(TokenType.TUPLESWAP))
    {
        if (match(TokenType.LPAREN, "("))
        {
            fact = getGoodParse(evalExpr());
            if (match(TokenType.RPAREN, ")"))
                return new TupleSwapNode(fact, getCurrLine());
        }
        return null;
    }

    // ---- String builtins by TokenType (works if lexer maps keywords) ----

    else if (checkMatch(TokenType.STRLEN))
    {
        if (match(TokenType.LPAREN, "("))
        {
            fact = getGoodParse(evalExpr());
            if (match(TokenType.RPAREN, ")"))
                return new StrLenNode(fact, getCurrLine());
        }
        return null;
    }

    else if (checkMatch(TokenType.STRCAT))
    {
        if (match(TokenType.LPAREN, "("))
        {
            SyntaxNode left = getGoodParse(evalExpr());
            SyntaxNode right = getGoodParse(evalExpr());

            if (match(TokenType.RPAREN, ")"))
                return new StrCatNode(left, right, getCurrLine());
        }
        return null;
    }

    else if (checkMatch(TokenType.STREXPLODE))
    {
        if (match(TokenType.LPAREN, "("))
        {
            fact = getGoodParse(evalExpr());
            if (match(TokenType.RPAREN, ")"))
                return new ExplodeNode(fact, getCurrLine());
        }
        return null;
    }

    else if (checkMatch(TokenType.SUBSTR))
    {
        if (match(TokenType.LPAREN, "("))
        {
            SyntaxNode s = getGoodParse(evalExpr());
            SyntaxNode start = getGoodParse(evalExpr());
            SyntaxNode len = getGoodParse(evalExpr());

            if (match(TokenType.RPAREN, ")"))
                return new SubstrNode(s, start, len, getCurrLine());
        }
        return null;
    }

    // map builtin function.
    else if (checkMatch(TokenType.MAP))
    {
        if (match(TokenType.LPAREN, "("))
        {
            fact = getGoodParse(evalExpr());
            SyntaxNode expr2 = getGoodParse(evalExpr());

            if (match(TokenType.RPAREN, ")"))
                return new MapNode(fact, expr2, getCurrLine());
        }
        return null;
    }

    // foldr builtin function.
    else if (checkMatch(TokenType.FOLDR))
    {
        if (match(TokenType.LPAREN, "()"))
        {
            SyntaxNode func = getGoodParse(evalExpr());
            SyntaxNode ival = getGoodParse(evalExpr());
            fact = getGoodParse(evalExpr());

            if (match(TokenType.RPAREN, ")"))
                return new FoldNode(func, ival, fact, false, getCurrLine());
        }
        return null;
    }

    // foldl builtin function.
    else if (checkMatch(TokenType.FOLDL))
    {
        if (match(TokenType.LPAREN, "()"))
        {
            SyntaxNode func = getGoodParse(evalExpr());
            SyntaxNode ival = getGoodParse(evalExpr());
            fact = getGoodParse(evalExpr());

            if (match(TokenType.RPAREN, ")"))
                return new FoldNode(func, ival, fact, true, getCurrLine());
        }
        return null;
    }

    // List constructor.
    else if (checkMatch(TokenType.LBRACK))
        return getGoodParse(evalListExpr());

    // Parenthsized expression -- could be a call could just be precedence adjusting.
    else if (checkMatch(TokenType.LPAREN))
    {
        SyntaxNode first = getGoodParse(evalExpr());

        if (checkMatch(TokenType.COMMA))
        {
            LinkedList<SyntaxNode> elems = new LinkedList<>();
            elems.add(first);

            // We already consumed the first comma. Now parse the remaining elements.
            SyntaxNode nxt = getGoodParse(evalExpr());
            elems.add(nxt);

            while (checkMatch(TokenType.COMMA))
            {
                SyntaxNode more = getGoodParse(evalExpr());
                elems.add(more);
            }

            match(TokenType.RPAREN, ")");
            return new TupleNode(elems, getCurrLine());
        }

        match(TokenType.RPAREN, ")");

        // Is this a function call?
        if (checkMatch(TokenType.LPAREN))
            return handleCall(first);

        return first;
    }

    // Handle the literals.
    else if (tokenIs(TokenType.INT) || tokenIs(TokenType.REAL)
            || tokenIs(TokenType.TRUE) || tokenIs(TokenType.FALSE)
            || tokenIs(TokenType.STRING))
    {
        fact = new TokenNode(getCurrToken(), getCurrLine());
        nextToken();
        return fact;
    }

    // ---- String builtin fallback when lexer returns ID(...) ----
    else if (tokenIs(TokenType.ID) && isStringBuiltinName(getCurrToken().getValue()))
    {
        String name = getCurrToken().getValue();
        nextToken(); // consume the ID

        if (!match(TokenType.LPAREN, "("))
            return null;

        if (name.equals("strlen"))
        {
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            return new StrLenNode(e, getCurrLine());
        }
        else if (name.equals("explode"))
        {
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            return new ExplodeNode(e, getCurrLine());
        }
        else if (name.equals("strcat"))
        {
            SyntaxNode l = getGoodParse(evalExpr());
            SyntaxNode r = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            return new StrCatNode(l, r, getCurrLine());
        }
        else // substr
        {
            SyntaxNode s = getGoodParse(evalExpr());
            SyntaxNode start = getGoodParse(evalExpr());
            SyntaxNode len = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            return new SubstrNode(s, start, len, getCurrLine());
        }
    }

    // ---- Tuple builtin fallback when lexer returns ID(...) ----
    else if (tokenIs(TokenType.ID) && isTupleBuiltinName(getCurrToken().getValue()))
    {
        String name = getCurrToken().getValue();
        nextToken(); // consume the ID

        if (!match(TokenType.LPAREN, "("))
            return null;

        if (name.equals("swap"))
        {
            SyntaxNode t = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            return new TupleSwapNode(t, getCurrLine());
        }
        else if (name.equals("proj"))
        {
            Token idxTok = getCurrToken();
            if (!match(TokenType.INT, "integer"))
                return null;

            int idx = Integer.parseInt(idxTok.getValue());

            SyntaxNode t = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            return new TupleProjNode(idx, t, getCurrLine());
        }
        else // destruct
        {
            SyntaxNode t = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            return new TupleDestructNode(t, getCurrLine());
        }
    }

    // Handle an identifier could be:
    //  - just the identifier
    //  - a function call.
    else if (tokenIs(TokenType.ID))
    {
        Token module = getCurrToken();
        nextToken();

        fact = new TokenNode(module, getCurrLine());

        if (checkMatch(TokenType.LPAREN))
            return handleCall(fact);
    }

    trace("Exit <factor>");
    return fact;
}

/**
 * True if the identifier should be treated as a string builtin even when lexed as ID.
 */
private boolean isStringBuiltinName(String s)
{
    return "strlen".equals(s) || "strcat".equals(s) || "explode".equals(s) || "substr".equals(s);
}

private boolean isTupleBuiltinName(String s)
{
    return "proj".equals(s) || "swap".equals(s) || "destruct".equals(s);
}


    /**
     * Method to handle the listExpr non-terminal.
     * <listExpr> -> [ <expr> {, <expr>} ]
     *
     * @throws ParseException when parsing fails.
     */
    private SyntaxNode evalListExpr() throws ParseException
    {
        LinkedList<SyntaxNode> entries = new LinkedList<>();
        ListNode lst = null;
        SyntaxNode expr = null;

        trace("Enter <listExpr>");

        // We could have an empty list.
        if (checkMatch(TokenType.RBRACK))
        {
            lst = new ListNode(entries, getCurrLine());
            return lst;
        }

        expr = getGoodParse(evalExpr());
        entries.add(expr);

        while (checkMatch(TokenType.COMMA))
        {
            expr = getGoodParse(evalExpr());
            entries.add(expr);
        }

        if (match(TokenType.RBRACK, "]"))
            lst = new ListNode(entries, getCurrLine());

        trace("Exit <listExpr>");
        return lst;
    }

    /***********
     * Methods for handling a specific rule of a non-terminal
     ***********/

    /**
     * This method handles a value definition. <id> := <expr>
     */
    private SyntaxNode handleValues() throws ParseException
    {
        Token id = getCurrToken();
        SyntaxNode expr;

        if (match(TokenType.ID, "identifier"))
        {
            if (match(TokenType.ASSIGN, ":="))
            {
                expr = evalExpr();
                return new ValNode(id, expr, getCurrLine());
            }
        }

        return null;
    }

    /**
     * Handle a lambda exrpession definition <id> => <expr>
     */
    private SyntaxNode handleLambdaExpr() throws ParseException
    {
        Token var = getCurrToken();
        SyntaxNode expr;

        if (match(TokenType.ID, "identifier"))
        {
            if (match(TokenType.TO, "=>"))
            {
                expr = evalExpr();
                return new LambdaNode(var, expr, getCurrLine());
            }
        }

        return null;
    }

    /**
     * This method handles conditionals. if <expr> then <expr> else <expr>
     */
    private SyntaxNode handleIf() throws ParseException
    {
        SyntaxNode cond;
        SyntaxNode trueBranch;
        SyntaxNode falseBranch;

        cond = getGoodParse(evalExpr());

        if (match(TokenType.THEN, "then"))
        {
            trueBranch = getGoodParse(evalExpr());
            if (match(TokenType.ELSE, "else"))
            {
                falseBranch = getGoodParse(evalExpr());
                return new IfNode(cond, trueBranch, falseBranch, getCurrLine());
            }
        }
        return null;
    }

    /**
     * This method handles a let expression <id> := <expr> in <expr>
     */
    private SyntaxNode handleLet() throws ParseException
    {
        Token var = getCurrToken();
        SyntaxNode varExpr;
        SyntaxNode expr;

        trace("enter handleLet");

        if (match(TokenType.ID, "identifier"))
        {
            if (match(TokenType.ASSIGN, ":="))
            {
                varExpr = getGoodParse(evalExpr());

                if (match(TokenType.IN, "in"))
                {
                    expr = getGoodParse(evalExpr());
                    return new LetNode(var, varExpr, expr, getCurrLine());
                }
            }
        }
        trace("exit handleLet");
        logError("Missing value.");
        throw new ParseException("Malformed let expression");
    }

    /**
     * Handles a function argument. We assume that the leading paren has
     * already been stripped off.
     */
    private SyntaxNode handleCall(SyntaxNode fun) throws ParseException
    {
        SyntaxNode arg = getGoodParse(evalExpr());
        match(TokenType.RPAREN, ")");
        return new ApplyNode(fun, arg, getCurrLine());
    }

    @Override
    protected SyntaxNode parseVariable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseVariable'");
    }

    @Override
    protected SyntaxNode parseExpression() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseExpression'");
    }
}

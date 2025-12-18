/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and or modify
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
import ast.nodes.CompositionNode;
import ast.nodes.CompositionNode.CompositionKind;
import ast.nodes.ExplodeNode;
import ast.nodes.FilterNode;
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
 * Parser for the MFL language.
 *
 * Each non terminal has an eval method that builds the corresponding AST node.
 * The invariant is that each method finishes with the next unprocessed token
 * at the head of the token stream.
 *
 * @author Zach Kissel
 */
public class MFLParser extends Parser
{
    /**
     * Constructs a new parser for the file src by setting up lexer.
     *
     * @param src the source code file to parse.
     * @throws FileNotFoundException if the file can not be found.
     */
    public MFLParser(File src) throws FileNotFoundException
    {
        super(new Lexer(src));
    }

    /**
     * Construct a parser that parses the string str.
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
    @Override
    public SyntaxTree parse() throws ParseException
    {
        nextToken();
        SyntaxTree ast = new SyntaxTree(evalProg());
        match(TokenType.EOF, "EOF");
        return ast;
    }

    /**
     * <prog> -> <values> ; { <values> ; }
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

            match(TokenType.SEMI, ";");
            exprs.add(currNode);
        }

        if (exprs.size() == 0)
            return null;

        trace("Exit <prog>");
        return new ProgNode(exprs, getCurrLine());
    }

    /**
     * <values> -> val <id> := <expr> | <expr>
     */
    private SyntaxNode evalValues() throws ParseException
    {
        if (checkMatch(TokenType.VAL))
            return getGoodParse(handleValues());
        return getGoodParse(evalExpr());
    }

    /**
     * <expr> -> let ... | if ... | <lambda>
     */
    private SyntaxNode evalExpr() throws ParseException
    {
        trace("Enter <expr>");

        if (checkMatch(TokenType.LET))
            return handleLet();

        if (checkMatch(TokenType.IF))
            return handleIf();

        SyntaxNode expr = getGoodParse(evalLambda());

        trace("Exit <expr>");
        return expr;
    }

    /**
     * <lambda> -> fn <id> -> <expr> | <composePipe>
     */
    private SyntaxNode evalLambda() throws ParseException
    {
        trace("Enter <evalLambda>");

        if (checkMatch(TokenType.FN))
            return handleLambdaExpr();

        SyntaxNode expr = evalComposePipe();

        trace("Exit <evalLambda>");
        return expr;
    }

    /**
     * Handles composition and pipeline.
     */
    private SyntaxNode evalComposePipe() throws ParseException
    {
        trace("Enter <evalComposePipe>");

        SyntaxNode expr = getGoodParse(evalBoolExpr());

        while (tokenIs(TokenType.PIPE) || tokenIs(TokenType.COMPOSE))
        {
            TokenType op = getCurrToken().getType();
            nextToken();

            SyntaxNode right = getGoodParse(evalBoolExpr());

            if (op == TokenType.PIPE)
                expr = new CompositionNode(expr, right, CompositionKind.PIPELINE, getCurrLine());
            else
                expr = new CompositionNode(expr, right, CompositionKind.FUNCTION_COMPOSITION, getCurrLine());
        }

        trace("Exit <evalComposePipe>");
        return expr;
    }

    /**
     * <bexpr> -> <rexpr> { (and | or) <rexpr> }
     */
    private SyntaxNode evalBoolExpr() throws ParseException
    {
        trace("Enter <evalBoolExpr>");

        SyntaxNode expr = getGoodParse(evalRexpr());

        while (tokenIs(TokenType.AND) || tokenIs(TokenType.OR))
        {
            TokenType op = getCurrToken().getType();
            nextToken();

            SyntaxNode rexpr = getGoodParse(evalRexpr());
            expr = new BinOpNode(expr, op, rexpr, getCurrLine());
        }

        trace("Exit <evalBoolExpr>");
        return expr;
    }

    /**
     * <rexpr> -> <mexpr> [ relop <mexpr> ]
     */
    private SyntaxNode evalRexpr() throws ParseException
    {
        SyntaxNode left = getGoodParse(evalMexpr());

        if (tokenIs(TokenType.LT) || tokenIs(TokenType.LTE)
                || tokenIs(TokenType.GT) || tokenIs(TokenType.GTE)
                || tokenIs(TokenType.EQ) || tokenIs(TokenType.NEQ))
        {
            TokenType op = getCurrToken().getType();
            nextToken();

            SyntaxNode right = getGoodParse(evalMexpr());
            return new RelOpNode(left, op, right, getCurrLine());
        }

        return left;
    }

    /**
     * <mexpr> -> <term> { (+ | -) <term> }
     */
    private SyntaxNode evalMexpr() throws ParseException
    {
        SyntaxNode expr = getGoodParse(evalTerm());

        while (tokenIs(TokenType.ADD) || tokenIs(TokenType.SUB))
        {
            TokenType op = getCurrToken().getType();
            nextToken();

            SyntaxNode rterm = getGoodParse(evalTerm());
            expr = new BinOpNode(expr, op, rterm, getCurrLine());
        }

        return expr;
    }

    /**
     * <term> -> <factor> { (* | / | mod | concat) <factor> }
     */
    private SyntaxNode evalTerm() throws ParseException
    {
        trace("Enter <term>");

        if (checkMatch(TokenType.NOT))
        {
            SyntaxNode expr = getGoodParse(evalRexpr());
            trace("Exit <term>");
            return new UnaryOpNode(expr, TokenType.NOT, getCurrLine());
        }

        SyntaxNode term = getGoodParse(evalFactor());

        while (tokenIs(TokenType.MULT) || tokenIs(TokenType.DIV)
                || tokenIs(TokenType.MOD) || tokenIs(TokenType.CONCAT)
                || tokenIs(TokenType.INCREMENT))
        {
            TokenType op = getCurrToken().getType();
            nextToken();

            if (op == TokenType.INCREMENT)
                op = TokenType.CONCAT;

            SyntaxNode rfact = getGoodParse(evalFactor());
            term = new BinOpNode(term, op, rfact, getCurrLine());
        }

        trace("Exit <term>");
        return term;
    }

    /**
     * <factor> includes literals, identifiers, calls, lists, tuples, and builtins.
     */
    private SyntaxNode evalFactor() throws ParseException
    {
        trace("Enter <factor>");

        SyntaxNode fact = null;

        if (checkMatch(TokenType.SUB))
        {
            SyntaxNode expr = getGoodParse(evalFactor());
            trace("Exit <factor>");
            return new UnaryOpNode(expr, TokenType.SUB, getCurrLine());
        }

        if (checkMatch(TokenType.LST_HD))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new HeadNode(e, getCurrLine());
        }

        if (checkMatch(TokenType.LST_TL))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new TailNode(e, getCurrLine());
        }

        if (checkMatch(TokenType.LEN))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new LenNode(e, getCurrLine());
        }

        if (checkMatch(TokenType.FILTER))
        {
            SyntaxNode pred = getGoodParse(evalExpr());
            SyntaxNode lst = getGoodParse(evalExpr());
            trace("Exit <factor>");
            return new FilterNode(pred, lst, getCurrLine());
        }

        if (checkMatch(TokenType.TUPLEPROJ))
        {
            match(TokenType.LPAREN, "(");

            Token idxTok = getCurrToken();
            match(TokenType.INT, "integer");
            int idx = Integer.parseInt(idxTok.getValue());

            SyntaxNode t = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");

            trace("Exit <factor>");
            return new TupleProjNode(idx, t, getCurrLine());
        }

        if (checkMatch(TokenType.TUPLESWAP))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new TupleSwapNode(e, getCurrLine());
        }

        if (checkMatch(TokenType.STRLEN))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new StrLenNode(e, getCurrLine());
        }

        if (checkMatch(TokenType.STRCAT))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode left = getGoodParse(evalExpr());
            SyntaxNode right = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new StrCatNode(left, right, getCurrLine());
        }

        if (checkMatch(TokenType.STREXPLODE))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode e = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new ExplodeNode(e, getCurrLine());
        }

        if (checkMatch(TokenType.SUBSTR))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode s = getGoodParse(evalExpr());
            SyntaxNode start = getGoodParse(evalExpr());
            SyntaxNode len = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new SubstrNode(s, start, len, getCurrLine());
        }

        if (checkMatch(TokenType.MAP))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode f = getGoodParse(evalExpr());
            SyntaxNode lst = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new MapNode(f, lst, getCurrLine());
        }

        if (checkMatch(TokenType.FOLDR))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode func = getGoodParse(evalExpr());
            SyntaxNode ival = getGoodParse(evalExpr());
            SyntaxNode lst = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new FoldNode(func, ival, lst, false, getCurrLine());
        }

        if (checkMatch(TokenType.FOLDL))
        {
            match(TokenType.LPAREN, "(");
            SyntaxNode func = getGoodParse(evalExpr());
            SyntaxNode ival = getGoodParse(evalExpr());
            SyntaxNode lst = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new FoldNode(func, ival, lst, true, getCurrLine());
        }

        if (checkMatch(TokenType.LBRACK))
        {
            SyntaxNode e = getGoodParse(evalListExpr());
            trace("Exit <factor>");
            return e;
        }

        if (checkMatch(TokenType.LPAREN))
        {
            SyntaxNode first = getGoodParse(evalExpr());

            if (checkMatch(TokenType.COMMA))
            {
                LinkedList<SyntaxNode> elems = new LinkedList<>();
                elems.add(first);

                SyntaxNode nxt = getGoodParse(evalExpr());
                elems.add(nxt);

                while (checkMatch(TokenType.COMMA))
                    elems.add(getGoodParse(evalExpr()));

                match(TokenType.RPAREN, ")");
                trace("Exit <factor>");
                return new TupleNode(elems, getCurrLine());
            }

            match(TokenType.RPAREN, ")");

            if (checkMatch(TokenType.LPAREN))
            {
                SyntaxNode call = handleCall(first);
                trace("Exit <factor>");
                return call;
            }

            trace("Exit <factor>");
            return first;
        }

        if (tokenIs(TokenType.INT) || tokenIs(TokenType.REAL)
                || tokenIs(TokenType.TRUE) || tokenIs(TokenType.FALSE)
                || tokenIs(TokenType.STRING))
        {
            fact = new TokenNode(getCurrToken(), getCurrLine());
            nextToken();
            trace("Exit <factor>");
            return fact;
        }

        if (tokenIs(TokenType.ID) && isStringBuiltinName(getCurrToken().getValue()))
        {
            String name = getCurrToken().getValue();
            nextToken();

            match(TokenType.LPAREN, "(");

            if ("strlen".equals(name))
            {
                SyntaxNode e = getGoodParse(evalExpr());
                match(TokenType.RPAREN, ")");
                trace("Exit <factor>");
                return new StrLenNode(e, getCurrLine());
            }

            if ("explode".equals(name))
            {
                SyntaxNode e = getGoodParse(evalExpr());
                match(TokenType.RPAREN, ")");
                trace("Exit <factor>");
                return new ExplodeNode(e, getCurrLine());
            }

            if ("strcat".equals(name))
            {
                SyntaxNode l = getGoodParse(evalExpr());
                SyntaxNode r = getGoodParse(evalExpr());
                match(TokenType.RPAREN, ")");
                trace("Exit <factor>");
                return new StrCatNode(l, r, getCurrLine());
            }

            SyntaxNode s = getGoodParse(evalExpr());
            SyntaxNode start = getGoodParse(evalExpr());
            SyntaxNode len = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new SubstrNode(s, start, len, getCurrLine());
        }

        if (tokenIs(TokenType.ID) && isTupleBuiltinName(getCurrToken().getValue()))
        {
            String name = getCurrToken().getValue();
            nextToken();

            match(TokenType.LPAREN, "(");

            if ("swap".equals(name))
            {
                SyntaxNode t = getGoodParse(evalExpr());
                match(TokenType.RPAREN, ")");
                trace("Exit <factor>");
                return new TupleSwapNode(t, getCurrLine());
            }

            if ("proj".equals(name))
            {
                Token idxTok = getCurrToken();
                match(TokenType.INT, "integer");
                int idx = Integer.parseInt(idxTok.getValue());

                SyntaxNode t = getGoodParse(evalExpr());
                match(TokenType.RPAREN, ")");
                trace("Exit <factor>");
                return new TupleProjNode(idx, t, getCurrLine());
            }

            SyntaxNode t = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");
            trace("Exit <factor>");
            return new TupleDestructNode(t, getCurrLine());
        }

        if (tokenIs(TokenType.ID))
        {
            Token idTok = getCurrToken();
            nextToken();

            fact = new TokenNode(idTok, getCurrLine());

            if (checkMatch(TokenType.LPAREN))
            {
                SyntaxNode call = handleCall(fact);
                trace("Exit <factor>");
                return call;
            }

            trace("Exit <factor>");
            return fact;
        }

        trace("Exit <factor>");
        return fact;
    }

    private boolean isStringBuiltinName(String s)
    {
        return "strlen".equals(s) || "strcat".equals(s) || "explode".equals(s) || "substr".equals(s);
    }

    private boolean isTupleBuiltinName(String s)
    {
        return "proj".equals(s) || "swap".equals(s) || "destruct".equals(s);
    }

    /**
     * <listExpr> -> ] | <expr> { , <expr> } ]
     * Assumes leading left bracket already consumed.
     */
    private SyntaxNode evalListExpr() throws ParseException
    {
        LinkedList<SyntaxNode> entries = new LinkedList<>();

        trace("Enter <listExpr>");

        if (checkMatch(TokenType.RBRACK))
        {
            trace("Exit <listExpr>");
            return new ListNode(entries, getCurrLine());
        }

        entries.add(getGoodParse(evalExpr()));

        while (checkMatch(TokenType.COMMA))
            entries.add(getGoodParse(evalExpr()));

        match(TokenType.RBRACK, "]");

        trace("Exit <listExpr>");
        return new ListNode(entries, getCurrLine());
    }

    /**
     * Handles a value definition: <id> := <expr>
     */
    private SyntaxNode handleValues() throws ParseException
    {
        Token id = getCurrToken();

        match(TokenType.ID, "identifier");
        match(TokenType.ASSIGN, ":=");

        SyntaxNode expr = getGoodParse(evalExpr());
        return new ValNode(id, expr, getCurrLine());
    }

    /**
     * Handles a lambda expression definition: <id> -> <expr>
     */
    private SyntaxNode handleLambdaExpr() throws ParseException
    {
        Token var = getCurrToken();

        match(TokenType.ID, "identifier");
        match(TokenType.TO, "->");

        SyntaxNode expr = getGoodParse(evalExpr());
        return new LambdaNode(var, expr, getCurrLine());
    }

    /**
     * Handles conditionals: if <expr> then <expr> else <expr>
     */
    private SyntaxNode handleIf() throws ParseException
    {
        SyntaxNode cond = getGoodParse(evalExpr());

        match(TokenType.THEN, "then");
        SyntaxNode trueBranch = getGoodParse(evalExpr());

        match(TokenType.ELSE, "else");
        SyntaxNode falseBranch = getGoodParse(evalExpr());

        return new IfNode(cond, trueBranch, falseBranch, getCurrLine());
    }

    /**
     * Handles let expressions: let <id> := <expr> in <expr>
     */
    private SyntaxNode handleLet() throws ParseException
    {
        trace("Enter handleLet");

        Token var = getCurrToken();

        match(TokenType.ID, "identifier");
        match(TokenType.ASSIGN, ":=");

        SyntaxNode varExpr = getGoodParse(evalExpr());

        match(TokenType.IN, "in");
        SyntaxNode expr = getGoodParse(evalExpr());

        trace("Exit handleLet");
        return new LetNode(var, varExpr, expr, getCurrLine());
    }

    /**
     * Handles a function call argument.
     * Assumes the leading left paren has already been consumed.
     */
    private SyntaxNode handleCall(SyntaxNode fun) throws ParseException
    {
        SyntaxNode arg = getGoodParse(evalExpr());
        match(TokenType.RPAREN, ")");
        return new ApplyNode(fun, arg, getCurrLine());
    }
}

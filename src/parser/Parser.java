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

import ast.SyntaxTree;
import ast.nodes.SyntaxNode;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

/**
 * An abstract class that represents the methods common to parsers.
 * 
 */
public abstract class Parser {
    private Lexer lex; // The lexer for the parser.
    private boolean errorFound; // True if ther was a parser error.
    private boolean doTracing; // True if we should run parser tracing.
    private Token nextTok; // The current token being analyzed.

    /**
     * This constructs a parser object.
     * @param lex the lexer the parser should use.
     */
    public Parser(Lexer lex)
    {
        this.lex = lex;
        this.errorFound = false;
        this.doTracing = false;
        this.nextTok = null;
    }

    /**
     * Turns tracing on an off.
     */
    public void toggleTracing() {
        doTracing = !doTracing;
    }

    /**
     * Determines if the program has any errors that would prevent evaluation.
     * 
     * @return true if the program has syntax errors; otherwise, false.
     */
    public boolean hasError() {
        return errorFound;
    }

    /**
     * Logs an error to the console.
     * 
     * @param msg the error message to dispaly.
     */
    public void logError(String msg) {
        System.err.println(
                "Syntax Error (line " + lex.getLineNumber() + "): " + msg);
        errorFound = true;
    }

    /**
     * This prints a message to the screen on if {@code doTracing} is true.
     * 
     * @param msg the message to display to the screen.
     */
    public void trace(String msg) {
        if (doTracing)
            System.out.println(msg);
    }

    /**
     * Advances the token stream.
     */
    public void nextToken() {
        // Read the next non comment token.
        do {
            nextTok = lex.nextToken();
        } while (nextTok.getType() == TokenType.COMMENT);

        if (doTracing)
            System.out.println("nextToken: " + nextTok);

    }

    /**
     * Tries to match the token to the type, if they match the token is
     * advanced. Otherwise, an error message is output and false is returned.
     * 
     * @param type the type expected
     * @param sym  the symbol expected.
     * @return true if the token was matched and false otherwise.
     * @throws ParseException if the the token is not of the given type.
     */
    public boolean  match(TokenType type, String sym) throws ParseException {
        if (nextTok.getType() == type) {
            nextToken();
            return true;
        } else {
            logError("expected " + sym + ", saw " + nextTok.getValue() + ".");
            throw new ParseException();
        }
    }

    /**
     * Checks if the token is of the given type, if it is get the next token.
     * 
     * @param type the type to check the token against.
     * @return true if the token matches; otherwise, false;
     */
    public boolean checkMatch(TokenType type) {
        if (nextTok.getType() == type) {
            nextToken();
            return true;
        }
        return false;
    }

    /**
     * Checks if the token is the given type. The stream is *not* advanced.
     * @param type the type to check.
     * @return {@code true} if the token is of the given type; otherwise 
     * {@code false}.
     */
    public boolean tokenIs(TokenType type)
    {
        return nextTok.getType() == type;
    }

    /**
     * If the syntax node is non-null retrun it; otherwise, throw a
     * {@code Parse Exception}
     * 
     * @param node the sytanx node to check.
     * @return the syntax node in the case its non null (i.e. identity function).
     * @throws ParseException if the syntax node {@code node} is {@code null}.
     */
    public SyntaxNode getGoodParse(SyntaxNode node) throws ParseException {
        if (node == null) {
            logError("Missing value.");
            throw new ParseException();
        }
        return node;
    }

    /**
     * Gets the current token. This should be used rarely, instead it is 
     * recommend that the {@code match}, {@code checkMath}, and {@code tokenIs}
     * methods are used to check properties of the token at the head of the stream.
     * @return the token at the head of the stream.
     */
    public Token getCurrToken()
    {
        return nextTok;
    }

    /**
     * The current line number associated with the token.
     * @return the current line number.
     */
    public long getCurrLine()
    {
        return lex.getLineNumber();
    }

    /**
     * Parses the stream of tokens per the grammar rules.
     * @return the syntax tree reprsenting the program.
     * @throws ParseException when a stage of parsing fails.
     */
    public abstract SyntaxTree parse() throws ParseException;
}

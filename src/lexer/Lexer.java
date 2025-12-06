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
package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * This file implements a basic lexical analyzer.
 * 
 * @author Zach Kissel
 */
public class Lexer
{
    // The dictionary of language keywords
    private HashMap<String, TokenType> keywords;
 
    // Stream of characters to generate token stream from.
    private CharacterStream stream;

    /**
     * Constructs a new lexical analyzer whose source input is a file.
     * 
     * @param file the file to open for lexical analysis.
     * @throws FileNotFoundException if the file can not be opened.
     */
    public Lexer(File file) throws FileNotFoundException
    {
        stream = new CharacterStream(file);
        loadKeywords();
    }

    /**
     * Constructs a new lexical analyzer whose source is a string.
     * 
     * @param input the input to lexically analyze.
     */
    public Lexer(String input)
    {
        stream = new CharacterStream(input);
        loadKeywords();
    }

    /**
     * Gets the next token from the stream.
     * 
     * @return the next token.
     */
    public Token nextToken()
    {
        String value = ""; // The value to be associated with the token.

        stream.advanceToNonBlank();
        switch (stream.getCurrentClass())
        {

        // The state where we are recognizing identifiers.
        // Regex: [A-Za-Z][0-9a-zA-z]*
        case LETTER:
            value += stream.getCurrentChar();
            stream.advance();      // advance the stream.

            // Read the rest of the identifier.
            while (stream.getCurrentClass() == CharacterClass.DIGIT
                    || stream.getCurrentClass() == CharacterClass.LETTER)
            {
                value += stream.getCurrentChar();
                stream.advance();
            }
            stream.skipNextAdvance(); // The symbol just read is part of the next token.

            // This could be an identifier or a token, if it's not in
            // the keyword dictionary, it is an indentifier.
            if (keywords.containsKey(value))
                return new Token(keywords.get(value), value);
            return new Token(TokenType.ID, value);

        // The state where we are recognizing digits.
        // Regex: [0-9]+
        case DIGIT:
            value += stream.getCurrentChar();
            stream.advance();

            while (stream.getCurrentClass() == CharacterClass.DIGIT)
            {
                value += stream.getCurrentChar();
                stream.advance();
            }

            if (stream.getCurrentChar() == '.') // Decimal point.
            {
                value += stream.getCurrentChar();
                stream.advance();
                while (stream.getCurrentClass() == CharacterClass.DIGIT)
                {
                    value += stream.getCurrentChar();
                    stream.advance();
                }
                stream.skipNextAdvance();
                return new Token(TokenType.REAL, value);
            }
            stream.skipNextAdvance(); // The symbol just read is part of the next token.

            return new Token(TokenType.INT, value);

        // Handles all special character symbols.
        case OTHER:
            return lookup();

        // We reached the end of our input.
        case END:
            return new Token(TokenType.EOF, "");

        // This should never be reached.
        default:
            return new Token(TokenType.UNKNOWN, "");
        }
    }

    /**
     * Get the current line number being processed.
     * 
     * @return the current line number being processed.
     */
    public long getLineNumber() {
        return stream.getLineNumber();
    }

    /************
     * Private Methods
     ************/

    /**
     * Processes the next character and return the resulting token.
     * 
     * @return the new token.
     */
    private Token lookup()
    {
        String value = "";

        switch (stream.getCurrentChar())
        {
        case '.': // A double with just a leading dot.
            value += ".";
            stream.advance();
        
            while (stream.getCurrentClass() == CharacterClass.DIGIT)
            {
                value += stream.getCurrentChar();
                stream.advance();
            }
            stream.skipNextAdvance();
            return new Token(TokenType.REAL, value);
        case ':': // A Pascal style assignment.
            stream.advance();
            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.ASSIGN, "");
            else
            {
                stream.skipNextAdvance(); // In case the character is part of a different token.
                return new Token(TokenType.UNKNOWN,
                        ":" + String.valueOf(stream.getCurrentChar()));
            }

        // Semi colon.
        case ';':
            return new Token(TokenType.SEMI, ";");
        case '+':
            stream.advance();
            if (stream.getCurrentChar() == '+')
                return new Token(TokenType.CONCAT, "++");
            else
            {
                stream.skipNextAdvance(); // Charater is part of a different token.
                return new Token(TokenType.ADD, "+");
            }
        case '-':
            // This could be start of an function arrow.
            stream.advance();
            if (stream.getCurrentChar() == '>')
                return new Token(TokenType.TO, "->");
            stream.skipNextAdvance();
            return new Token(TokenType.SUB, "-");
        case '*':
            return new Token(TokenType.MULT, "*");
        case '/':
            return new Token(TokenType.DIV, "/");
        case '(':
            // This could be the start of a block comment.
            stream.advance();
            if (stream.getCurrentChar() == '*')
                return consumeComment();
            else
            {
                stream.skipNextAdvance();
                return new Token(TokenType.LPAREN, "(");
            }
        case ')':
            return new Token(TokenType.RPAREN, ")");
        case ',':
            return new Token(TokenType.COMMA, ",");
        case '=':
            return new Token(TokenType.EQ, "=");
        case '!':
            stream.advance();
            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.NEQ, "!=");
            else
            {
                stream.skipNextAdvance();
                return new Token(TokenType.UNKNOWN, "");
            }
        case '>':
            stream.advance();
            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.GTE, ">=");
            else
            {
                stream.skipNextAdvance();
                return new Token(TokenType.GT, ">");
            }
        case '<':
            stream.advance();
            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.LTE, "<=");
            else
            {
                stream.skipNextAdvance();
                return new Token(TokenType.LT, "<");
            }
        case '[':
            return new Token(TokenType.LBRACK, "[");
        case ']':
            return new Token(TokenType.RBRACK, "]");
        default:
            return new Token(TokenType.UNKNOWN, String.valueOf(stream.getCurrentChar()));
        }
    }

    /**
     * Sets up the dictionary with all of the keywords.
     */
    private void loadKeywords()
    {
        keywords = new HashMap<String, TokenType>();
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
        keywords.put("val", TokenType.VAL);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("mod", TokenType.MOD);
        keywords.put("let", TokenType.LET);
        keywords.put("in", TokenType.IN);
        keywords.put("hd", TokenType.LST_HD);
        keywords.put("tl", TokenType.LST_TL);
        keywords.put("len", TokenType.LEN);
        keywords.put("if", TokenType.IF);
        keywords.put("then", TokenType.THEN);
        keywords.put("else", TokenType.ELSE);
        keywords.put("fn", TokenType.FN);
        keywords.put("map", TokenType.MAP);
        keywords.put("foldl", TokenType.FOLDL);
        keywords.put("foldr", TokenType.FOLDR);
    }

    /**
     * This method consumes the comented out characters until the close comment
     * character is found.
     */
    private Token consumeComment()
    {
        boolean done = false;
        while (!done)
        {
            stream.advance();
            if (stream.getCurrentChar() == '*')
            {
                stream.advance();
                if (stream.getCurrentChar() == ')')
                    done = true;
            }

            if (stream.getCurrentClass() == CharacterClass.END)
                return new Token(TokenType.EOF, "Unfinished comment.");
        }
        return new Token(TokenType.COMMENT, "");
    }
}

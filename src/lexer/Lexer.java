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

        // NOTE:
        // Some terminals / encodings will turn the composition operator '∘'
        // into '?' (or a CharacterStream may classify '?' as LETTER).
        // We intercept both characters here so composition works reliably.
        //
        // Also: some environments decode unknown UTF-8 as the replacement
        // character '�' (U+FFFD). Many error printers display that as '?'.
        // We intercept that too.
        if (stream.getCurrentClass() != CharacterClass.END
                && (stream.getCurrentChar() == '∘'
                    || stream.getCurrentChar() == '?'
                    || stream.getCurrentChar() == '\uFFFD'))
        {
            stream.advance();
            stream.skipNextAdvance();
            return new Token(TokenType.COMPOSE, "∘");
        }

        switch (stream.getCurrentClass())
        {
        // The state where we are recognizing identifiers.
        // Regex: [A-Za-z][0-9A-Za-z]*
        case LETTER:
            value += stream.getCurrentChar();
            stream.advance();

            while (stream.getCurrentClass() == CharacterClass.DIGIT
                    || stream.getCurrentClass() == CharacterClass.LETTER)
            {
                value += stream.getCurrentChar();
                stream.advance();
            }

            stream.skipNextAdvance();

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

            if (stream.getCurrentChar() == '.')
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

            stream.skipNextAdvance();
            return new Token(TokenType.INT, value);

        // Handles all special character symbols (including strings).
        case OTHER:
            if (stream.getCurrentChar() == '"')
                return consumeStringLiteral();
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
    public long getLineNumber()
    {
        return stream.getLineNumber();
    }

    /************
     * Private Methods
     ************/

    /**
     * Consumes a string literal beginning with '"'.
     * Supports: \n, \t, \r, \", \\
     * Returns STRING token value without surrounding quotes.
     *
     * This follows the "read one char too far then skipNextAdvance()" convention
     * used by the other scanners.
     */
    private Token consumeStringLiteral()
    {
        StringBuilder sb = new StringBuilder();

        // Current char is the opening quote '"'
        stream.advance();

        while (stream.getCurrentClass() != CharacterClass.END
                && stream.getCurrentChar() != '"')
        {
            char c = stream.getCurrentChar();

            if (c == '\\')
            {
                stream.advance();

                if (stream.getCurrentClass() == CharacterClass.END)
                    return new Token(TokenType.EOF, "Unterminated string literal.");

                char esc = stream.getCurrentChar();
                switch (esc)
                {
                case 'n':
                    sb.append('\n');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case '"':
                    sb.append('"');
                    break;
                case '\\':
                    sb.append('\\');
                    break;
                default:
                    sb.append(esc);
                    break;
                }

                stream.advance();
                continue;
            }

            sb.append(c);
            stream.advance();
        }

        if (stream.getCurrentClass() == CharacterClass.END)
            return new Token(TokenType.EOF, "Unterminated string literal.");

        // Currently on closing quote
        stream.advance();

        stream.skipNextAdvance();

        return new Token(TokenType.STRING, sb.toString());
    }

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
        case '.':
            value += ".";
            stream.advance();

            while (stream.getCurrentClass() == CharacterClass.DIGIT)
            {
                value += stream.getCurrentChar();
                stream.advance();
            }

            stream.skipNextAdvance();
            return new Token(TokenType.REAL, value);

        case ':':
            stream.advance();
            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.ASSIGN, ":=");
            stream.skipNextAdvance();
            return new Token(TokenType.UNKNOWN, ":");

        case ';':
            return new Token(TokenType.SEMI, ";");

        case '+':
            stream.advance();

            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.ADD_ASSIGN, "+=");

            if (stream.getCurrentChar() == '+')
                return new Token(TokenType.INCREMENT, "++");

            stream.skipNextAdvance();
            return new Token(TokenType.ADD, "+");

        case '-':
            stream.advance();

            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.SUB_ASSIGN, "-=");

            if (stream.getCurrentChar() == '-')
                return new Token(TokenType.DECREMENT, "--");

            if (stream.getCurrentChar() == '>')
                return new Token(TokenType.TO, "->");

            stream.skipNextAdvance();
            return new Token(TokenType.SUB, "-");

        case '*':
            stream.advance();

            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.MULT_ASSIGN, "*=");

            stream.skipNextAdvance();
            return new Token(TokenType.MULT, "*");

        case '/':
            stream.advance();

            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.DIV_ASSIGN, "/=");

            stream.skipNextAdvance();
            return new Token(TokenType.DIV, "/");

        case '%':
            stream.advance();

            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.MOD_ASSIGN, "%=");

            stream.skipNextAdvance();
            return new Token(TokenType.MOD, "%");

        case '(':
            stream.advance();

            if (stream.getCurrentChar() == '*')
                return consumeComment();

            stream.skipNextAdvance();
            return new Token(TokenType.LPAREN, "(");

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

            stream.skipNextAdvance();
            return new Token(TokenType.UNKNOWN, "!");

        case '>':
            stream.advance();

            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.GTE, ">=");

            stream.skipNextAdvance();
            return new Token(TokenType.GT, ">");

        case '<':
            stream.advance();

            if (stream.getCurrentChar() == '=')
                return new Token(TokenType.LTE, "<=");

            stream.skipNextAdvance();
            return new Token(TokenType.LT, "<");

        case '[':
            return new Token(TokenType.LBRACK, "[");

        case ']':
            return new Token(TokenType.RBRACK, "]");

        case '∘':
        case '?':
        case '\uFFFD':
            // Treat Unicode composition operator (and common mangled variants)
            // as the COMPOSE token.
            stream.advance();
            stream.skipNextAdvance();
            return new Token(TokenType.COMPOSE, "∘");

        case '|':
            stream.advance();

            if (stream.getCurrentChar() == '>')
                return new Token(TokenType.PIPE, "|>");

            stream.skipNextAdvance();
            return new Token(TokenType.BAR, "|");

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
        keywords.put("filter", TokenType.FILTER);
        keywords.put("foldl", TokenType.FOLDL);
        keywords.put("foldr", TokenType.FOLDR);

        keywords.put("strlen", TokenType.STRLEN);
        keywords.put("strcat", TokenType.STRCAT);
        keywords.put("explode", TokenType.STREXPLODE);
        keywords.put("substr", TokenType.SUBSTR);

        keywords.put("proj", TokenType.TUPLEPROJ);
        keywords.put("swap", TokenType.TUPLESWAP);

        keywords.put("match", TokenType.MATCH);
        keywords.put("with", TokenType.WITH);

        // ML-style function composition operator (ASCII form).
        // This makes: (f o g)(3) work exactly like: (f ∘ g)(3)
        keywords.put("o", TokenType.COMPOSE);
    }

    /**
     * This method consumes the commented out characters until the close comment
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

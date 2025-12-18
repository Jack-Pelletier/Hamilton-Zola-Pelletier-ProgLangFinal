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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * A character stream object. The stream maintains the head of the stream and
 * gives two classes of operations to manipulate the head of the stream:
 * <ol>
 *   <li>
 *     Operations that advance the stream head.
 *     <ul>
 *       <li>{@code advance}: advance the stream head to the next character.</li>
 *       <li>{@code advanceToNonBlank}: advance the stream head to the next non blank character.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     Operation that prevents the next advance operation from moving the head of the stream.
 *     <ul>
 *       <li>{@code skipNextAdvance}</li>
 *     </ul>
 *   </li>
 * </ol>
 * The stream head exposes two relevant pieces of information:
 * <ol>
 *   <li>The character at the head of the stream: {@code getCurrentChar}</li>
 *   <li>The class of the character at the head of the stream: {@code getCurrentClass}</li>
 * </ol>
 */
public class CharacterStream
{
    private BufferedReader input; // The input to the lexer.
    private char nextChar; // The current character at the head of the stream.
    private boolean skipRead; // Whether to skip the next char read.
    private long currentLineNumber; // The current line number being processed.
    private CharacterClass nextClass; // The class of the current character.

    /**
     * Constructs a new character stream whose source input is a file.
     *
     * @param file the file to open for lexical analysis.
     * @throws FileNotFoundException if the file can not be opened.
     */
    public CharacterStream(File file) throws FileNotFoundException
    {
        input = new BufferedReader(new FileReader(file));
        currentLineNumber = 1;
        nextChar = '\0';
        nextClass = CharacterClass.WHITE_SPACE;
        skipRead = false;
    }

    /**
     * Constructs a new character stream whose source is a string.
     *
     * @param input the input to lexically analyze.
     */
    public CharacterStream(String input)
    {
        this.input = new BufferedReader(new StringReader(input == null ? "" : input));
        currentLineNumber = 1;
        nextChar = '\0';
        nextClass = CharacterClass.WHITE_SPACE;
        skipRead = false;
    }

    /**
     * Get the current line number being processed.
     *
     * @return the current line number being processed.
     */
    public long getLineNumber()
    {
        return currentLineNumber;
    }

    /**
     * Get the value of the current character.
     *
     * @return the character at the head of the stream.
     */
    public char getCurrentChar()
    {
        return nextChar;
    }

    /**
     * Get the class of the current character.
     *
     * @return the class of character at the head of the stream.
     */
    public CharacterClass getCurrentClass()
    {
        return nextClass;
    }

    /**
     * Advances the stream one character.
     */
    public void advance()
    {
        int c;

        if (skipRead)
        {
            skipRead = false;
            return;
        }

        try
        {
            c = input.read();
        }
        catch (IOException ioe)
        {
            System.err.println("Internal error (advance): " + ioe);
            nextChar = '\0';
            nextClass = CharacterClass.END;
            return;
        }

        if (c == -1)
        {
            nextChar = '\0';
            nextClass = CharacterClass.END;
            return;
        }

        nextChar = (char) c;

        if (Character.isLetter(nextChar))
            nextClass = CharacterClass.LETTER;
        else if (Character.isDigit(nextChar))
            nextClass = CharacterClass.DIGIT;
        else if (Character.isWhitespace(nextChar))
            nextClass = CharacterClass.WHITE_SPACE;
        else
            nextClass = CharacterClass.OTHER;

        if (nextChar == '\n')
            currentLineNumber++;
    }

    /**
     * Advances the stream to the next non blank character.
     */
    public void advanceToNonBlank()
    {
        advance();

        while (nextClass != CharacterClass.END
                && nextClass == CharacterClass.WHITE_SPACE)
            advance();
    }

    /**
     * Skips the next advance call. Multiple calls will not go back further than one character.
     */
    public void skipNextAdvance()
    {
        skipRead = true;
    }
}

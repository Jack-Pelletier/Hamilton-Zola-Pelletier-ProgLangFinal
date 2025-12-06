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
 * 
 * A character stream object. The stream maintains the head of the stream and 
 * gives two classes of operations to mainuplate the head of the stream:
 * <ol>
 *   <li> Operations that advance the stream ahead some number of characters. The
 *      operations are:
 *      <ul>
 *          <li> {@code advance}: advance the stream head to the next character. </li>
 *          <li> {@code advanceToNonBlank}: advance the stream head to the next non-blank char. </li>
 *      </ul>
 *   </li>
 *   <li> Operations that prevent the next advance operation from moving the 
 *      head of the stream.
 *          <ul><li>This operation is called {@code skipNextAdvance}</li></ul>
 *   </li>
 * </ol>
 * Unless the stream position has been explicity manipulate the head of the stream stays 
 * fixed. There are two pieces of information that are relevant with respect to the 
 * stream head.
 * <ol>
 *   <li> The character at the head of the stream {@code getCurrentChar} </li>
 *   <li> The class of the character at the head of the stream {@code getCurrentClass} </li>
 * </ol>
 */
public class CharacterStream {

    private BufferedReader input; // The input to the lexer.
    private char nextChar; // The next character read.
    private boolean skipRead; // Whether or not to skip the next char
                              // read.
    private long currentLineNumber; // The current line number being processed.
    CharacterClass nextClass;

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
    }

    /**
     * Constructs a new character whose source is a string.
     * 
     * @param input the input to lexically analyze.
     */
    public CharacterStream(String input)
    {
        this.input = new BufferedReader(new StringReader(input));
        currentLineNumber = 1;
    }

    /**
     * Get the current line number being processed.
     * 
     * @return the current line number being processed.
     */
    public long getLineNumber() {
        return currentLineNumber;
    }

    /**
     * Get the value of the current character.
     * @return the character at the head of the stream.
     */
    public char getCurrentChar()
    {
        return nextChar;
    }

    /**
     * Get the value of the current class.
     * @return the class of character at the head of the
     * stream.
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
        int c = -1;

        // Handle the unread operation.
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
            System.err.println("Internal error (getChar()): " + ioe);
            nextChar = '\0';
            nextClass = CharacterClass.END;
        }

        if (c == -1) // If there is no character to read, we've reached the end.
        {
            nextChar = '\0';
            nextClass = CharacterClass.END;
            return;
        }

        // Set the character and determine it's class.
        nextChar = (char) c;
        if (Character.isLetter(nextChar))
            nextClass = CharacterClass.LETTER;
        else if (Character.isDigit(nextChar))
            nextClass = CharacterClass.DIGIT;
        else if (Character.isWhitespace(nextChar))
            nextClass = CharacterClass.WHITE_SPACE;
        else
            nextClass = CharacterClass.OTHER;

        // Update the line counter for error checking.
        if (nextChar == '\n')
            currentLineNumber++;
    }

    /**
     * Advances the stream to the next non-blank character.
     */
    public void advanceToNonBlank() {
        advance();

        while (nextClass != CharacterClass.END
                && Character.isWhitespace(nextChar))
            advance();
    }

    /**
     * Skips the next advance call. Multiple calls 
     * will *not* go back further than one character.
     */
    public void skipNextAdvance() {
        skipRead = true;
    }    
}

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

/**
 * An enumeration of token types.
 */
public enum TokenType
{
    /**
     * An integer token.
     */
    INT,

    /**
     * A real number token.
     */
    REAL,

    /**
     * A string literal token (e.g., "hello").
     */
    STRING,

    /**
     * An identifier token.
     */
    ID,

    /**
     * Add operation token.
     */
    ADD,

    /**
     * Subtract operation token.
     */
    SUB,

    /**
     * Multiply operation token.
     */
    MULT,

    /**
     * Divide operation token.
     */
    DIV,

    /**
     * Modulus operation token.
     */
    MOD,

    /**
     * Assign operation.
     */
    ASSIGN,
    /**
     * Add and assign operation.
     */
    ADD_ASSIGN,   
    /**
     * Subtract and assign operation.
     */
    SUB_ASSIGN,   
    /**
     * Multiply and assign operation.
     */
    MULT_ASSIGN,   
    /**
     * Divide and assign operation.
     */
    DIV_ASSIGN, 
    /**
     * Modulus and assign operation.
     */
    INCREMENT, 
    /**
     * Decrement and assign operation.
     */
    DECREMENT,

    /**
     * A left parenthesis.
     */
    LPAREN,

    /**
     * A right parenthesis
     */
    RPAREN,

    /**
     * Boolean AND.
     */
    AND,

    /**
     * Boolean OR.
     */
    OR,

    /**
     * Boolean NOT.
     */
    NOT,

    /**
     * Equality.
     */
    EQ,

    /**
     * less than.
     */
    LT,

    /**
     * Greater than.
     */
    GT,

    /**
     * Less than or equal.
     */
    LTE,

    /**
     * Greater than or equal.
     */
    GTE,

    /**
     * Not equal.
     */
    NEQ,

    /**
     * An unknown token.
     */
    UNKNOWN,

    /**
     * True value.
     */
    TRUE,

    /**
     * False value.
     */
    FALSE,

    /**
     * Indicates a comment -- just to simplify the token processing.
     */
    COMMENT,

    /**
     * A global value.
     */
    VAL,

    /**
     * A Let expression.
     */
    LET,

    /**
     * Marker for the beginning of the let body.
     */
    IN,

    /**
     * Left bracket.
     */
    LBRACK,

    /**
     * Right bracket.
     */
    RBRACK,

    /**
     * List concatenation
     */
    CONCAT,

    /**
     * Tail of list.
     */
    LST_TL,

    /**
     * Head of list.
     */
    LST_HD,

    /**
     * Comma
     */
    COMMA,

    /**
     * Tuple projection operation keyword (e.g., proj 0 (1,2)).
     */
    TUPLEPROJ,

    /**
     * Tuple swap operation keyword (e.g., swap (1,2) -> (2,1)).
     */
    TUPLESWAP,

    /**
     * The length of data types that support the operation.
     */
    LEN,

    /**
     * String length operation (for strings specifically).
     */
    STRLEN,

    /**
     * String concatenation operation (for strings specifically).
     */
    STRCAT,

    /**
     * Convert a string to a list of characters/strings.
     */
    STREXPLODE,

    /**
     * Substring operation (for strings specifically).
     */
    SUBSTR,

    /**
     * The if keyword.
     */
    IF,

    /**
     * The then keyword.
     */
    THEN,

    /**
     * The else keyword.
     */
    ELSE,

    /**
     * Lambda expression.
     */
    FN,

    /**
     * The to arrow.
     */
    TO,

    /**
     * The map built in operation.
     */
    MAP,

    /**
     * Left fold built in operation.
     */
    FOLDL,

    /**
     * Right fold built in operation.
     */
    FOLDR,

    /**
     * The Semi colon.
     */
    SEMI,

    /**
     * The end of the file token.
     */
    EOF,
}

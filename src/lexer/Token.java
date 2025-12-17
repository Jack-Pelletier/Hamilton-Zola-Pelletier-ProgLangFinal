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

import environment.Environment;

/**
 * Implements a basic token class.
 *
 * @author Zach Kissel
 */
public class Token
{
    private String val;      // The value of the token.
    private TokenType type;  // The type of token represented.

    /**
     * This is the default constructor.
     */
    public Token()
    {
        val = "";
        type = TokenType.UNKNOWN;
    }

    /**
     * This is the overloaded constructor it sets the value and the token type.
     *
     * @param type the type of the token.
     * @param val  the value stored in the token.
     */
    public Token(TokenType type, String val)
    {
        this.type = type;
        this.val = val;
    }

    /**
     * Get the current value associated with the token.
     *
     * @return the string representing the value of the token.
     */
    public String getValue()
    {
        return val;
    }

    /**
     * Get the current type associated with the token.
     *
     * @return the type of token.
     */
    public TokenType getType()
    {
        return type;
    }

    /**
     * Set the value associated with the token.
     *
     * @param val the value of the token.
     */
    public void setValue(String val)
    {
        this.val = val;
    }

    /**
     * Sets the type of token.
     *
     * @param type the type of token.
     */
    public void setType(TokenType type)
    {
        this.type = type;
    }

    /**
     * Determines if two tokens are equal.
     *
     *
     * @return true if they are equal and false otherwise.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        Token tok = (Token) obj;
        return this.val.equals(tok.val);
    }

    /**
     * Return a String representation of the Token.
     *
     * @return a string representing the token.
     */
    @Override
    public String toString()
    {
        switch (type)
        {
        case UNKNOWN:
            return "UNKNOWN(" + val + ")";
        case INT:
            return "INT(" + val + ")";
        case REAL:
            return "REAL(" + val + ")";
        case STRING:
            return "STRING(" + val + ")";

        case ADD:
            return "ADD";
        case SUB:
            return "SUB";
        case MULT:
            return "MULT";
        case DIV:
            return "DIV";
        case LPAREN:
            return "LPAREN";
        case RPAREN:
            return "RPAREN";
        case ID:
            return "ID(" + val + ")";
        case AND:
            return "AND";
        case OR:
            return "OR";
        case NOT:
            return "NOT";
        case EQ:
            return "EQ";
        case NEQ:
            return "NEQ";
        case LT:
            return "LT";
        case GT:
            return "GT";
        case LTE:
            return "LTE";
        case GTE:
            return "GTE";
        case TRUE:
            return "TRUE";
        case FALSE:
            return "FALSE";
        case COMMENT:
            return "COMMENT";
        case VAL:
            return "VAL";
        case ASSIGN:
            return "ASSIGN";
        case MOD:
            return "MOD";
        case LET:
            return "LET";
        case IN:
            return "IN";
        case LBRACK:
            return "LBRACK";
        case RBRACK:
            return "RBRACK";
        case COMMA:
            return "COMMA";
        case LST_HD:
            return "HD";
        case LST_TL:
            return "TL";
        case CONCAT:
            return "CONCAT";
        case LEN:
            return "LEN";

        // --- String-specific operations ---
        case SUBSTR:
            return "SUBSTR";
        case STRLEN:
            return "STRLEN";
        case STRCAT:
            return "STRCAT";
        case STREXPLODE:
            return "STREXPLODE";

        // --- Tuple-specific operations ---
        case TUPLEPROJ:
            return "TUPLE_PROJ";
        case TUPLESWAP:
            return "TUPLE_SWAP";

        // --- NEW: pipeline / composition ---
        case PIPE:
            return "PIPE(|>)";
        case COMPOSE:
            return "COMPOSE(∘)";

        case IF:
            return "IF";
        case THEN:
            return "THEN";
        case ELSE:
            return "ELSE";
        case FN:
            return "FN";
        case TO:
            return "=>";
        case MAP:
            return "MAP";
        case FOLDL:
            return "FOLDL";
        case FOLDR:
            return "FOLDR";
        case SEMI:
            return "SEMI";
        case EOF:
            return "EOF";
        default:
            return "";
        }
    }
}

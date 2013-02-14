package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 * Tokenize an identifier or keyword.
 */
public class LexIdentifier {

    public static Token read(final LexerStream in_stream) throws IOException, LexerException
    {
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(start_line, start_col, "Unexpected end of input stream when parsing identifier");
        }
        if (!Character.isLetter(next) && next != '_') {
            throw new LexerException(start_line, start_col, "Invalid character for start of identifier: "+(char)next);
        }


        StringBuilder sb = new StringBuilder();

        while (valid_ident_char(next)) {
            sb.append(Character.toChars(in_stream.read()));
            next = in_stream.peek();
        }

        String result = sb.toString();

        // Check if is a known/reserved keyword
        TokenType type = TokenType.forValue(result);
        if (type == null) {
            type = TokenType.Identifier;
        }

        // Check for reserved identifier
        if (type == TokenType.Identifier && result.startsWith("__")) {
            throw new LexerException(start_line, start_col, "Unknown reserved identifier");
        }


        return new Token(type, start_line, start_col, in_stream.getLine(), in_stream.getCol(), result);
    }

    private static boolean valid_ident_char(int c) {
        if (c == -1) {
            return false;
        }
        if (c == '_') {
            return true;
        }
        return Character.isLetterOrDigit(c);
    }
}

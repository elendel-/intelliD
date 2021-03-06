package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenFactory;
import com.wyrdtech.parsed.lexer.token.TokenType;

import java.io.IOException;

/**
 * Encapsulates logic for lexing out a hex string literal
 * x"0A" // same as "\x0A"
 * x"00 FBCD 32FD 0A" // same as // "\x00\xFB\xCD\x32\xFD\x0A"
 *
 * TODO: check that number of hex characters is multiple of 2
 */
public class LexHexLiteral {

    private final TokenFactory factory;
    private final LexerStream in_stream;

    public LexHexLiteral(TokenFactory factory, LexerStream in_stream) {
        this.factory = factory;
        this.in_stream = in_stream;
    }

    public Token read() throws IOException, LexerException
    {
        int start_index = in_stream.getPosition();
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(start_line, start_col, "Unexpected end of input stream when parsing string literal");
        }
        if (next != 'x') {
            throw new LexerException(start_line, start_col, "Not a literal hex string");
        }

        in_stream.read();
        next = in_stream.peek();
        if (next != '"') {
            throw new LexerException(start_line, start_col, "Not a literal hex string");
        }

        in_stream.read(); // consume opening quote
        next = in_stream.peek();

        // Read whole thing, normalizing whitespace for easy splitting
        StringBuilder sb = new StringBuilder();
        boolean pair = false;
        while (next != -1 && is_valid_hex(next)) {
            if (!Character.isWhitespace(next)) {
                sb.append(Character.toChars(next));
                if (pair) {
                    sb.append(' ');
                }
                pair = !pair;
            }
            in_stream.read();
            next = in_stream.peek();
        }

        if (next != '"') {
            throw new LexerException(start_line, start_col, "Unterminated literal hex string");
        }
        in_stream.read(); // consume closing quote

        // Parse out contents, each character pair is one byte
        StringBuilder result = new StringBuilder();

        String raw = sb.toString();
        for (String bite : raw.split(" ")) {
            result.append(Character.toChars(Integer.parseInt(bite, 16)));
        }

        return factory.create(TokenType.LiteralHex,
                              start_index,
                              start_line,
                              start_col,
                              in_stream.getPosition(),
                              in_stream.getLine(),
                              in_stream.getCol(),
                              result.toString());
    }

    private static boolean is_valid_hex(int chr)
    {
        return Character.isWhitespace(chr) ||
               Character.isDigit(chr) ||
               ('A' <= chr && chr <= 'F') || ('a' <= chr && chr <= 'f');
    }

}

package com.sialpi.lox;

import static com.sialpi.lox.TokenType.*;
import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>(); 
    private int start = 0;
    private int current = 0;
    private int line = 1;
    public Scanner(String source) {
        this.source = source;
    }
    
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    // A comment that goes to the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;

            default:
                Lox.error(line, "Unexpected character.");
                break;
        }
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        StringBuilder value = new StringBuilder();
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                Lox.error(line, "String cannot span multiple lines. Use '\\n' instead.");
                return;
            }
            
            char c = advance();

            if (c == '\\') {
                if (isAtEnd()) {
                    Lox.error(line, "Undetermined string.");
                    return;
                }

                if (peek() == '\n') {
                    Lox.error(line, 
                        "String cannot span multiple lines. Use '\\n' instead."
                    );
                    return;
                }

                char escaped = advance();

                switch (escaped) {
                    case 'n':
                        value.append('\n');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case '"':
                        value.append('"');
                        break;
                    case '\\':
                        value.append('\\');
                        break;
                    default:
                        Lox.error(line, "Invalid escape sequence: \\" + escaped);
                        return;
                }
            } else {
                value.append(c);
            }
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();
        addToken(STRING, value.toString());
    }

    
}
package Project11;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JackTokenizer {

    private List<String> tokens;
    private int currIndex;
    private String currToken;

    private static final String[] KEYWORD = {
            "class", "constructor", "function", "method",
            "field", "static", "var", "int", "char", "boolean",
            "void", "true", "false", "null", "this",
            "let", "do", "if", "else", "while", "return"
    };

    private static final char[] SYMBOLS = {
            '{', '}', '(', ')', '[', ']', '.', ',', ';',
            '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'
    };

    public JackTokenizer(String inputFile) throws IOException {
        String input = Files.readString(new File(inputFile).toPath());
        input = removeComments(input);
        tokens = tokenize(input);
        currIndex = 0;
        currToken = null;
    }

    public boolean hasMoreTokens() {
        return currIndex < tokens.size();
    }

    public void advance() {
        currToken = tokens.get(currIndex);
        currIndex++;
    }

    public String tokenType() {
        for (String keyword : KEYWORD) {
            if (keyword.equals(currToken))
                return "KEYWORD";
        }

        if (currToken.length() == 1 && symbolReturn(currToken.charAt(0))) {
            return "SYMBOL";
        }

        if (Character.isLetter(currToken.charAt(0)) || currToken.charAt(0) == '_') {
            return "IDENTIFIER";
        }

        if (Character.isDigit(currToken.charAt(0))) {
            return "INT_CONST";
        }

        if (currToken.startsWith("\"")) {
            return "STRING_CONST";
        }

        return null;
    }

    public String keyWord() {
        return currToken;
    }

    public char symbol() {
        return currToken.charAt(0);
    }

    public String identifier() {
        return currToken;
    }

    public int intVal() {
        return Integer.parseInt(currToken);
    }

    public String stringVal() {
        return currToken.substring(1, currToken.length() - 1);
    }

    private boolean symbolReturn(char c) {
        for (char symbol : SYMBOLS) {
            if (symbol == c) {
                return true;
            }
        }
        return false;
    }

    private String removeComments(String input) {
        input = input.replaceAll("(?s)/\\*.*?\\*/", "");
        input = input.replaceAll("//.*", "");
        return input;
    }

    private List<String> tokenize(String input) {
        List<String> token = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char currChar = input.charAt(i);

            if (Character.isWhitespace(currChar)) {
                i++;
                continue;
            }

            if (symbolReturn(currChar)) {
                token.add(String.valueOf(currChar));
                i++;
                continue;
            }

            if (currChar == '"') {
                int j = i + 1;
                while (input.charAt(j) != '"') {
                    j++;
                }

                token.add(input.substring(i, j + 1));
                i = j + 1;
                continue;
            }

            int j = i;
            while (j < input.length() && !Character.isWhitespace(input.charAt(j)) && !symbolReturn(input.charAt(j))) {
                j++;
            }

            token.add(input.substring(i, j));
            i = j;
        }
        return token;
    }
}

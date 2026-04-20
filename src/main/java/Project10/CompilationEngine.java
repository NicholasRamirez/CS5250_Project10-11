package Project10;

import java.io.*;

public class CompilationEngine {

    private JackTokenizer tokenizer;
    private BufferedWriter writer;

    public CompilationEngine(JackTokenizer tokenizer, String output) throws IOException {
        writer = new BufferedWriter(new FileWriter(output));
        this.tokenizer = tokenizer;
    }

    public void compileClass() throws IOException {
        // compiles: class Name {classVarDec subroutineDec}
        writer.write("<class>\n");

        tokenizer.advance();
        writeTokenType();
        tokenizer.advance();
        writeTokenType();
        tokenizer.advance();
        writeTokenType();

        tokenizer.advance();
        while (tokenizer.keyWord().equals("static") || tokenizer.keyWord().equals("field")) {
            compileClassVarDec();
            tokenizer.advance();
        }
        while (tokenizer.keyWord().equals("constructor") || tokenizer.keyWord().equals("function")
            || tokenizer.keyWord().equals("method")) {
            compileSubroutine();
            tokenizer.advance();
        }
        writeTokenType();
        writer.write("</class>\n");
    }

    public void close() throws IOException {
        writer.close();
    }

    private void writeTokenType() throws IOException {
        String type = tokenizer.tokenType();

        if (type.equals("KEYWORD")) {
            writer.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
        } else if (type.equals("SYMBOL")) {
            char symbol = tokenizer.symbol();
            if (symbol == '<') {
                writer.write("<symbol> &lt; </symbol>\n");
            } else if (symbol == '>') {
                writer.write("<symbol> &gt; </symbol>\n");
            } else if (symbol == '&') {
                writer.write("<symbol> &amp; </symbol>\n");
            } else {
                writer.write("<symbol> " + symbol + " </symbol>\n");
            }
        } else if (type.equals("IDENTIFIER")) {
            writer.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
        } else if (type.equals("INT_CONST")) {
            writer.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");
        } else if (type.equals("STRING_CONST")) {
            writer.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
        }
    }

    private void compileClassVarDec() throws IOException {
        writer.write("<classVarDec>\n");
        writeTokenType();

        tokenizer.advance();
        writeTokenType();

        tokenizer.advance();
        writeTokenType();

        tokenizer.advance();
        while (tokenizer.symbol() == ',') {
            writeTokenType();
            tokenizer.advance();
            writeTokenType();
            tokenizer.advance();
        }
        writeTokenType();
        writer.write("</classVarDec>\n");
    }

    private void compileSubroutine() throws IOException {
        // compiles: constructor | function | method
        writer.write("<subroutineDec>\n");

        writeTokenType();

        tokenizer.advance();
        writeTokenType();

        tokenizer.advance();
        writeTokenType();

        tokenizer.advance();
        writeTokenType();

        tokenizer.advance();
        compileParameterList();

        writeTokenType();
        tokenizer.advance();
        writer.write("<subroutineBody>\n");

        writeTokenType();
        tokenizer.advance();
        while (tokenizer.keyWord().equals("var")) {
            compileVarDec();
            tokenizer.advance();
        }

        compileStatements();
        writeTokenType();

        writer.write("</subroutineBody>\n");
        writer.write("</subroutineDec>\n");
    }

    private void compileParameterList() throws IOException {
        // compiles parameter list: (type varName (,type varName)*)?
        writer.write("<parameterList>\n");

        if (tokenizer.symbol() != ')') {
            writeTokenType();
            tokenizer.advance();

            writeTokenType();
            tokenizer.advance();

            while (tokenizer.symbol() == ',') {
                writeTokenType();
                tokenizer.advance();

                writeTokenType();
                tokenizer.advance();

                writeTokenType();
                tokenizer.advance();
            }
        }
        writer.write("</parameterList>\n");
    }

    private void compileVarDec() throws IOException {
        // compiles: var type varName (,varName);
        writer.write("<varDec>\n");
        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();
        while (tokenizer.symbol() == ',') {
            writeTokenType();
            tokenizer.advance();
            writeTokenType();
            tokenizer.advance();
        }
        writeTokenType();
        writer.write("</varDec>\n");
    }

    private void compileStatements() throws IOException {
        // compiles: let | if | while | do | return
        writer.write("<statements>\n");

        while (tokenizer.keyWord().equals("let") || tokenizer.keyWord().equals("if")
            || tokenizer.keyWord().equals("while") || tokenizer.keyWord().equals("do")
            || tokenizer.keyWord().equals("return")) {

            if (tokenizer.keyWord().equals("let")) {
                compileLet();
            } else if (tokenizer.keyWord().equals("if")) {
                compileIf();
            } else if  (tokenizer.keyWord().equals("while")) {
                compileWhile();
            } else if (tokenizer.keyWord().equals("do")) {
                compileDo();
            } else if  (tokenizer.keyWord().equals("return")) {
                compileReturn();
            }
        }
        writer.write("</statements>\n");
    }

    private void compileDo() throws IOException {
        // compiles: "do subroutineCall ;". also handles the optional "."
        writer.write("<doStatement>\n");
        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();

        if (tokenizer.symbol() == '.') {
            writeTokenType();
            tokenizer.advance();

            writeTokenType();
            tokenizer.advance();
        }
        writeTokenType();
        tokenizer.advance();

        compileExpressionList();
        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();
        writer.write("</doStatement>\n");
    }

    private void compileLet() throws IOException {
        // compiles: "let variable name ([expression])? = expression ;"
        writer.write("<letStatement>\n");
        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();

        if (tokenizer.symbol() == '[') {
            writeTokenType();
            tokenizer.advance();

            compileExpression();
            writeTokenType();
            tokenizer.advance();
        }
        writeTokenType();
        tokenizer.advance();

        compileExpression();
        writeTokenType();
        tokenizer.advance();
        writer.write("</letStatement>\n");
    }

    private void compileWhile() throws IOException {
        // compiles: "while (expression) {statements}
        writer.write("<whileStatement>\n");
        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();

        compileExpression();
        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();
        compileStatements();

        writeTokenType();
        tokenizer.advance();
        writer.write("</whileStatement>\n");
    }

    private void compileReturn() throws IOException {
        // compiles: "return expression? ;"
        writer.write("<returnStatement>\n");
        writeTokenType();
        tokenizer.advance();

        if (tokenizer.symbol() != ';') {
            compileExpression();
        }

        writeTokenType();
        tokenizer.advance();
        writer.write("</returnStatement>\n");
    }

    private void compileIf() throws IOException {
        // compiles: "if (expression) {statements} else {statements})?"
        writer.write("<ifStatement>\n");
        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();
        compileExpression();

        writeTokenType();
        tokenizer.advance();

        writeTokenType();
        tokenizer.advance();
        compileStatements();

        writeTokenType();
        tokenizer.advance();
        if (tokenizer.keyWord().equals("else")) {
            writeTokenType();
            tokenizer.advance();

            writeTokenType();
            tokenizer.advance();
            compileStatements();

            writeTokenType();
            tokenizer.advance();
        }
        writer.write("</ifStatement>\n");
    }

    private void compileExpression() throws IOException {
        // compiles: "term (op term)*"
        writer.write("<expression>\n");
        compileTerm();

        while (tokenizer.symbol() == '+' ||  tokenizer.symbol() == '-' || tokenizer.symbol() == '*'
            || tokenizer.symbol() == '/' ||  tokenizer.symbol() == '&' || tokenizer.symbol() == '|'
            || tokenizer.symbol() == '<' || tokenizer.symbol() == '>' || tokenizer.symbol() == '=') {

            writeTokenType();
            tokenizer.advance();
            compileTerm();
        }
        writer.write("</expression>\n");
    }

    private void compileTerm() throws IOException {
        // compiles: "term: constant | var | array | subroutineCall | (expression) | op term"
        writer.write("<term>\n");

        if (tokenizer.tokenType().equals("INT_CONST") ||  tokenizer.tokenType().equals("STRING_CONST")
            || tokenizer.tokenType().equals("KEYWORD")) {

            writeTokenType();
            tokenizer.advance();
        } else if (tokenizer.tokenType().equals("IDENTIFIER")) {
            writeTokenType();
            tokenizer.advance();

            if (tokenizer.symbol() == '[') {
                writeTokenType();
                tokenizer.advance();
                compileExpression();

                writeTokenType();
                tokenizer.advance();
            } else if (tokenizer.symbol() == '(') {
                writeTokenType();
                tokenizer.advance();
                compileExpressionList();

                writeTokenType();
                tokenizer.advance();
            } else if (tokenizer.symbol() == '.') {
                writeTokenType();
                tokenizer.advance();

                writeTokenType();
                tokenizer.advance();

                writeTokenType();
                tokenizer.advance();
                compileExpressionList();

                writeTokenType();
                tokenizer.advance();
            }
        } else if (tokenizer.symbol() == '(') {
            writeTokenType();
            tokenizer.advance();
            compileExpression();

            writeTokenType();
            tokenizer.advance();
        }  else if (tokenizer.symbol() == '-' ||  tokenizer.symbol() == '~') {
            writeTokenType();
            tokenizer.advance();
            compileTerm();
        }
        writer.write("</term>\n");
    }

    private void compileExpressionList() throws IOException {
        // compiles: "(expression (, expression)*?"
        writer.write("<expressionList>\n");

        if (tokenizer.symbol() != ')') {
            compileExpression();
            while (tokenizer.symbol() == ',') {
                writeTokenType();
                tokenizer.advance();
                compileExpression();
            }
        }
        writer.write("</expressionList>\n");
    }
}

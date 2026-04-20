package Project11;

import java.io.*;

public class CompilationEngine {

    private JackTokenizer tokenizer;
    private VMWriter vmWriter;
    private SymbolTable symbolTable;

    private String className;
    private int labelCounter;

    public CompilationEngine(JackTokenizer tokenizer, String outputFile) throws  IOException {
        this.tokenizer = tokenizer;
        this.vmWriter = new VMWriter(outputFile);
        this.symbolTable = new SymbolTable();
        this.className = "";
        this.labelCounter = 0;
    }

    public void close() throws IOException {
        vmWriter.close();
    }

    public void compileClass() throws IOException {
        // compiles: class class Name {classVarDec subroutineDec}

        tokenizer.advance();
        tokenizer.advance();
        className = tokenizer.identifier();
        tokenizer.advance();

        tokenizer.advance();
        while (tokenizer.keyWord().equals("static") || tokenizer.keyWord().equals("field")) {
            compileClassVarDec();
            tokenizer.advance();
        }
        while (tokenizer.keyWord().equals("constructor") || tokenizer.keyWord().equals("function")
                || tokenizer.keyWord().equals("method")) {
            compileSubroutine();
        }
    }

    private void compileClassVarDec() throws IOException {
        // compiles: static | field type varName (',' varName)
       String kind = tokenizer.keyWord().toUpperCase();

        tokenizer.advance();
        String type;

        if (tokenizer.tokenType().equals("KEYWORD")) {
            type = tokenizer.keyWord();
        } else {
            type = tokenizer.identifier();
        }

        tokenizer.advance();
        String name = tokenizer.identifier();
        symbolTable.define(name, type, kind);
        tokenizer.advance();

        while (tokenizer.symbol() == ',') {
            tokenizer.advance();
            name = tokenizer.identifier();
            symbolTable.define(name, type, kind);
            tokenizer.advance();
        }
    }

    private void compileSubroutine() throws IOException {
        // compiles: (constructor | function | method) (void | type) subroutine name ( parameter list ) subroutinebody
        symbolTable.startSubroutine();
        String subroutineKind = tokenizer.keyWord();

        if (subroutineKind.equals("method")) {
            symbolTable.define("this", className, "ARG");
        }

        tokenizer.advance();
        tokenizer.advance();
        String subroutineName = tokenizer.identifier();

        tokenizer.advance();
        tokenizer.advance();
        compileParameterList();

        tokenizer.advance();
        tokenizer.advance();
        while (tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyWord().equals("var")) {
            compileVarDec();
            tokenizer.advance();
        }
        vmWriter.writeFunction(className + "." + subroutineName, symbolTable.varCount("VAR"));

        if (subroutineKind.equals("constructor")) {
            vmWriter.writePush("constant", symbolTable.varCount("FIELD"));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop("pointer", 0);
        } else if (subroutineKind.equals("method")) {
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0);
        }
        compileStatements();
        tokenizer.advance();
    }

    private void compileParameterList() throws IOException {
        // compiles: ( (type var name) (',' type var name) )
        if (tokenizer.symbol() != ')') {
            String type;

            if (tokenizer.tokenType().equals("KEYWORD")) {
                type = tokenizer.keyWord();
            } else {
                type = tokenizer.identifier();
            }
            tokenizer.advance();
            String name = tokenizer.identifier();
            symbolTable.define(name, type, "ARG");
            tokenizer.advance();
            while (tokenizer.symbol() == ',') {
                tokenizer.advance();
                if (tokenizer.tokenType().equals("KEYWORD")) {
                    type = tokenizer.keyWord();
                } else {
                    type = tokenizer.identifier();
                }
                tokenizer.advance();
                name = tokenizer.identifier();
                symbolTable.define(name, type, "ARG");
                tokenizer.advance();
            }
        }
    }

    private void compileVarDec() throws IOException {
        // compiles var type var name (',' var name) ;
        tokenizer.advance();
        String type;

        if (tokenizer.tokenType().equals("KEYWORD")) {
            type = tokenizer.keyWord();
        } else {
            type = tokenizer.identifier();
        }

        tokenizer.advance();
        String name = tokenizer.identifier();
        symbolTable.define(name, type, "VAR");
        tokenizer.advance();

        while (tokenizer.symbol() == ',') {
            tokenizer.advance();
            name = tokenizer.identifier();
            symbolTable.define(name, type, "VAR");
            tokenizer.advance();
        }
    }

    private void compileStatements() throws IOException {
        // compiles: let | if | while | do | return
        while (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyWord().equals("let")
                || tokenizer.keyWord().equals("if") || tokenizer.keyWord().equals("while")
                || tokenizer.keyWord().equals("do") || tokenizer.keyWord().equals("return"))) {

            if (tokenizer.keyWord().equals("let")) {
                compileLet();
            } else if (tokenizer.keyWord().equals("if")) {
                compileIf();
            } else if (tokenizer.keyWord().equals("while")) {
                compileWhile();
            } else if (tokenizer.keyWord().equals("do")) {
                compileDo();
            } else if (tokenizer.keyWord().equals("return")) {
                compileReturn();
            }
        }
    }

    private void compileDo() throws IOException {
        // compiles do subroutineCall ;
        tokenizer.advance();
        String name = tokenizer.identifier();

        tokenizer.advance();
        compileSubroutineCall(name);

        vmWriter.writePop("temp", 0);
        tokenizer.advance();
    }

    private void compileLet() throws IOException {
        // compiles let var name ('[' expression ']') = expression ;
        tokenizer.advance();
        String name = tokenizer.identifier();

        tokenizer.advance();
        if (tokenizer.symbol() == '[') {
            vmWriter.writePush(kindSegment(symbolTable.kindOf(name)), symbolTable.indexOf(name));
            tokenizer.advance();
            compileExpression();
            vmWriter.writeArithmetic("add");

            tokenizer.advance();
            tokenizer.advance();
            compileExpression();

            vmWriter.writePop("temp", 0);
            vmWriter.writePop("pointer", 1);
            vmWriter.writePush("temp", 0);
            vmWriter.writePop("that", 0);
        } else {
            tokenizer.advance();
            compileExpression();
            vmWriter.writePop(kindSegment(symbolTable.kindOf(name)), symbolTable.indexOf(name));
        }
        tokenizer.advance();
    }

    private void compileWhile() throws IOException {
        // compiles while '(' expression ')' '{' statements '}'
        int currLabel = labelCounter;
        labelCounter++;

        String expLabel = "WHILE_EXP" + currLabel;
        String endLabel = "WHILE_END" + currLabel;
        vmWriter.writeLabel(expLabel);

        tokenizer.advance();
        tokenizer.advance();
        compileExpression();

        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(endLabel);
        tokenizer.advance();
        tokenizer.advance();
        compileStatements();

        vmWriter.writeGoto(expLabel);
        vmWriter.writeLabel(endLabel);

        tokenizer.advance();
    }

    private void compileReturn() throws IOException {
        // compiles return expression ;
        tokenizer.advance();
        if (tokenizer.symbol() != ';') {
            compileExpression();
        } else {
            vmWriter.writePush("constant", 0);
        }
        vmWriter.writeReturn();
        tokenizer.advance();
    }

    private void compileIf() throws IOException {
        // compiles if '(' expression ')' '{' statements '}' (else { statements })
        int currLabel = labelCounter;
        labelCounter++;

        String trueLabel = "IF_TRUE" + currLabel;
        String falseLabel = "IF_FALSE" + currLabel;
        String endLabel = "IF_END" + currLabel;

        tokenizer.advance();
        tokenizer.advance();
        compileExpression();

        vmWriter.writeIf(trueLabel);
        vmWriter.writeGoto(falseLabel);
        vmWriter.writeLabel(trueLabel);

        tokenizer.advance();
        tokenizer.advance();
        compileStatements();
        tokenizer.advance();

        if (tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyWord().equals("else")) {
            vmWriter.writeGoto(endLabel);
            vmWriter.writeLabel(falseLabel);

            tokenizer.advance();
            tokenizer.advance();
            compileStatements();
            tokenizer.advance();

            vmWriter.writeLabel(endLabel);
        } else {
            vmWriter.writeLabel(falseLabel);
        }
    }

    private String kindSegment(String kind) {
        if (kind.equals("STATIC")) {
            return "static";
        } else if (kind.equals("FIELD")) {
            return "this";
        }  else if (kind.equals("ARG")) {
            return "argument";
        } else if (kind.equals("VAR")) {
            return "local";
        }
        return "";
    }

    private void compileExpression() throws IOException {
        // compiles term (op term)
        compileTerm();

        while (tokenizer.symbol() == '+' ||  tokenizer.symbol() == '-' || tokenizer.symbol() == '*'
                || tokenizer.symbol() == '/' ||  tokenizer.symbol() == '&' || tokenizer.symbol() == '|'
                || tokenizer.symbol() == '<' || tokenizer.symbol() == '>' || tokenizer.symbol() == '=') {
            char op = tokenizer.symbol();
            tokenizer.advance();
            compileTerm();

            if (op == '+') {
                vmWriter.writeArithmetic("add");
            } else if (op == '-') {
                vmWriter.writeArithmetic("sub");
            } else if (op == '*') {
                vmWriter.writeCall("Math.multiply", 2);
            } else if (op == '/') {
                vmWriter.writeCall("Math.divide", 2);
            } else if (op == '&') {
                vmWriter.writeArithmetic("and");
            } else if (op == '|') {
                vmWriter.writeArithmetic("or");
            } else if (op == '<') {
                vmWriter.writeArithmetic("lt");
            } else if (op == '>') {
                vmWriter.writeArithmetic("gt");
            } else if (op == '=') {
                vmWriter.writeArithmetic("eq");
            }
        }
    }

    private void compileTerm() throws IOException {
        // compiles int const | string const | keyboard const | var name | var name
        // [ expression ] | subroutineCall | ( expression ) | op term
        if (tokenizer.tokenType().equals("INT_CONST")) {
            vmWriter.writePush("constant", tokenizer.intVal());
            tokenizer.advance();

        } else if (tokenizer.tokenType().equals("STRING_CONST")) {
            String str = tokenizer.stringVal();
            vmWriter.writePush("constant", str.length());
            vmWriter.writeCall("String.new", 1);
            for (int i = 0; i < str.length(); i++) {
                vmWriter.writePush("constant", str.charAt(i));
                vmWriter.writeCall("String.appendChar", 2);
            }
            tokenizer.advance();

        } else if (tokenizer.tokenType().equals("KEYWORD")) {
            String keyword = tokenizer.keyWord();

            if (keyword.equals("true")) {
                vmWriter.writePush("constant", 0);
                vmWriter.writeArithmetic("not");
            } else if (keyword.equals("false") || keyword.equals("null")) {
                vmWriter.writePush("constant", 0);
            } else if (keyword.equals("this")) {
                vmWriter.writePush("pointer", 0);
            }
            tokenizer.advance();

        } else if (tokenizer.tokenType().equals("IDENTIFIER")) {
            String name = tokenizer.identifier();
            tokenizer.advance();

            if (tokenizer.symbol() == '[') {
                vmWriter.writePush(kindSegment(symbolTable.kindOf(name)), symbolTable.indexOf(name));
                tokenizer.advance();
                compileExpression();

                vmWriter.writeArithmetic("add");
                vmWriter.writePop("pointer", 1);
                vmWriter.writePush("that", 0);
                tokenizer.advance();

            } else if (tokenizer.symbol() == '(' || tokenizer.symbol() == '.') {
                compileSubroutineCall(name);
            } else {
                vmWriter.writePush(kindSegment(symbolTable.kindOf(name)), symbolTable.indexOf(name));
            }

        } else if (tokenizer.symbol() == '(') {
            tokenizer.advance();
            compileExpression();
            tokenizer.advance();

        } else if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
            char op = tokenizer.symbol();
            tokenizer.advance();
            compileTerm();

            if (op == '-') {
                vmWriter.writeArithmetic("neg");
            } else {
                vmWriter.writeArithmetic("not");
            }
        }
    }

    private void compileSubroutineCall(String name) throws IOException {
        // compiles subroutine name ( expressionList ) | (class name | var name) . subroutineName ( expressionList )
        int nArgs = 0;

        if (tokenizer.symbol() == '.') {
            tokenizer.advance();
            String subroutineName = tokenizer.identifier();
            String kind = symbolTable.kindOf(name);

            if (!kind.equals("NONE")) {
                vmWriter.writePush(kindSegment(kind), symbolTable.indexOf(name));
                name = symbolTable.typeOf(name) + "." + subroutineName;
                nArgs = 1;
            } else {
                name = name + "." + subroutineName;
            }
            tokenizer.advance();
        } else {
            vmWriter.writePush("pointer", 0);
            name = className + "." + name;
            nArgs = 1;
        }

        tokenizer.advance();
        nArgs += compileExpressionList();
        vmWriter.writeCall(name, nArgs);
        tokenizer.advance();
    }

    private int compileExpressionList() throws IOException {
        // compiles ( expression ( , expression) )
        int nArgs = 0;

        if (tokenizer.symbol() != ')') {
            compileExpression();
            nArgs = 1;

            while (tokenizer.symbol() == ',') {
                tokenizer.advance();
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;
    }
}

package Project11;

import java.io.*;

public class JackCompiler {

    public void compileCorrectPath(String inputFile) throws Exception {
        File input = new File(inputFile);

        if (input.isFile() && input.getName().endsWith(".jack")) {
            compile(input);
        } else if (input.isDirectory()) {
            System.out.println("\n" + input.getName());
            File[] files = input.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jack")) {
                        compile(file);
                    } else if (file.isDirectory()) {
                        compileCorrectPath(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static void compile(File file) throws Exception {
        String inputPath = file.getAbsolutePath();
        String outputPath = inputPath.replace(".jack", ".vm");
        System.out.println("Compiling " + file.getName() + " into " + new File(outputPath).getName());

        JackTokenizer tokenizer = new JackTokenizer(inputPath);
        CompilationEngine engine = new CompilationEngine(tokenizer, outputPath);
        engine.compileClass();
        engine.close();
    }
}

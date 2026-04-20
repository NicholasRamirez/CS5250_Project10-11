package Project10;

import java.io.*;

public class JackAnalyzer {

    public void analyzer(String inputFile) throws IOException {
        File input = new  File(inputFile);

        if (input.isFile() && input.getName().endsWith(".jack")) {
            process(input);
        } else if (input.isDirectory()) {
            System.out.println("\n" + input.getName());
            File[] files = input.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jack")) {
                        process(file);
                    } else if (file.isDirectory()) {
                        analyzer(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void process(File jackFile) throws IOException {
        String inputPath = jackFile.getAbsolutePath();
        String outputPath = inputPath.replace(".jack", ".xml");

        System.out.println("Processing " + jackFile.getName() + " into " + new File(outputPath).getName());

        JackTokenizer tokenizer = new JackTokenizer(inputPath);
        CompilationEngine engine = new CompilationEngine(tokenizer, outputPath);
        engine.compileClass();
        engine.close();
    }
}

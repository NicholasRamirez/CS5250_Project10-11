package Project10;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String input;

        if (args.length > 0) {
            input = args[0];
        } else {
            input = "Project10_Outputs/JacktoXML";
        }

        JackAnalyzer analyzer = new JackAnalyzer();
        analyzer.analyzer(input);
    }
}

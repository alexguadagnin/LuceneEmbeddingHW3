import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LuceneMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Inserisci la query: ");
        String queryInput = scanner.nextLine();

        ArrayList<LuceneResult> results = new ArrayList<>();

        try {
            results = LuceneQuery.vectorQuery(queryInput, LuceneConstants.INDEX_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LuceneResult.printResult(results);
    }
}

import java.io.IOException;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class LuceneEvaluate {

    public static void MRR() throws IOException {
        // Ad ogni query associo la relativa lista di risultati
        LinkedHashMap<String, ArrayList<LuceneResult>> queryResults = new LinkedHashMap<>();

        // Query di test
        ArrayList<String> query = new ArrayList<>();
        query.add(0, "metrics on cifar dataset");
        query.add(1, "f1 with SMCMC-Comp");
        query.add(2, "patient result");
        query.add(3, "frechet distance on amsterdamumcdb");
        query.add(4, "relationship between parameters and size in LLMs");

        // Definisco i risultati corretti per ogni query, dove la lista ha nella posizione
        // 0 il fileName ed in 1 il tableID
        LinkedHashMap<String, ArrayList<String>> trueResultByQuery = new LinkedHashMap<>();

        ArrayList<String> res0 = new ArrayList<>();
        res0.add(0, "2102.08921.json");
        res0.add(1, "S5.T1");

        ArrayList<String> res1 = new ArrayList<>();
        res1.add(0, "2307.07005.json");
        res1.add(1, "S5.T3.3");

        ArrayList<String> res2 = new ArrayList<>();
        res2.add(0, "2103.03705.json");
        res2.add(1, "S3.T1");

        ArrayList<String> res3 = new ArrayList<>();
        res3.add(0, "2102.08921.json");
        res3.add(1, "Ax4.T5");

        ArrayList<String> res4 = new ArrayList<>();
        res4.add(0, "2103.00148.json");
        res4.add(1, "S4.T1");

        trueResultByQuery.put(query.get(0), res0);
        trueResultByQuery.put(query.get(1), res1);
        trueResultByQuery.put(query.get(2), res2);
        trueResultByQuery.put(query.get(3), res3);
        trueResultByQuery.put(query.get(4), res4);

        queryResults.put(query.get(0), LuceneQuery.vectorQuery(query.get(0), LuceneConstants.INDEX_PATH_TEST));
        queryResults.put(query.get(1), LuceneQuery.vectorQuery(query.get(1), LuceneConstants.INDEX_PATH_TEST));
        queryResults.put(query.get(2), LuceneQuery.vectorQuery(query.get(2), LuceneConstants.INDEX_PATH_TEST));
        queryResults.put(query.get(3), LuceneQuery.vectorQuery(query.get(3), LuceneConstants.INDEX_PATH_TEST));
        queryResults.put(query.get(4), LuceneQuery.vectorQuery(query.get(4), LuceneConstants.INDEX_PATH_TEST));

        float rank = 0;
        for(int i = 0; i < 5; i++){
            ArrayList<LuceneResult> queryResult = queryResults.get(query.get(i));
            ArrayList<String> trueQueryResult = trueResultByQuery.get(query.get(i));
            //System.out.println("Query #" + i);
            int j = 1;
            for (LuceneResult lr : queryResult) {
                /*
                System.out.println("*" + lr.getFileName() + " == " + trueQueryResult.get(0));
                System.out.println("*" + lr.getTableID() + " == " + trueQueryResult.get(1));
                System.out.println("Score: " + lr.getScore());
                System.out.println("--------------------------");
                */
                // Confronto se il risultato corrente è uguale a quello "reale"
                if (lr.getFileName().equals(trueQueryResult.get(0)) && lr.getTableID().equals(trueQueryResult.get(1)) ) {
                    rank = rank + (1.0f / j);
                    System.out.println("Rank: " + rank);
                    break;
                }
                j++;
            }
            if(j != 10) {
                double DCG = 1/(Math.log(j+1) / Math.log(2));
                System.out.println("Query #" + (i + 1) + " DCG_10: " + DCG);
            } else {
                System.out.println("Query #" + (i + 1) + " DCG_10: 0");
            }
        }

        float MRR = rank/5;
        System.out.println("L'MRR è: " + MRR);
    }

}

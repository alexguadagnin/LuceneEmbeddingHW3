import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LuceneQuery {

    public static ArrayList<LuceneResult> vectorQuery(String queryText, String indexPathString) throws IOException {
        //Path indexPath = Paths.get(LuceneConstants.INDEX_PATH);
        Path indexPath = Paths.get(indexPathString);
        Directory directory = FSDirectory.open(indexPath);

        // Embedding Model
        //AllMiniLmL6V2EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

        // Apri il lettore dell'indice
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        // Genera l'embedding della query
        Embedding queryEmbedding = embeddingModel.embed(TextSegment.from(queryText)).content();
        float[] queryVector = queryEmbedding.vector();

        // Crea una query basata sull'embedding
        int topK = 15;
        KnnFloatVectorQuery query = new KnnFloatVectorQuery("embedding", queryVector, topK);

        // Esegui la query
        TopDocs topDocs = searcher.search(query, topK);

        ArrayList<LuceneResult> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.storedFields().document(scoreDoc.doc);
            LuceneResult result = new LuceneResult();
            result.setCaption(doc.get("caption"));
            result.setFileName(doc.get("fileName"));
            result.setTable(doc.get("table"));
            result.setTableID(doc.get("tableID"));
            result.setScore(scoreDoc.score);
            results.add(result);
        }

        /*
        // Mostra i risultati
        System.out.println("Risultati trovati: " + topDocs.totalHits.value());
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.storedFields().document(scoreDoc.doc);
            String caption = doc.get("caption");
            String table = doc.get("table");
            String filename = doc.get("fileName");
            System.out.println("Score: " + scoreDoc.score);
            System.out.println("Filename: " + filename);
            System.out.println("Caption: " + caption);
            //System.out.println("Table: " + table);
            System.out.println("-----------------------");
        }*/

        // Chiudi il lettore
        reader.close();
        directory.close();

        return results;
    }

}

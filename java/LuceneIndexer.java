import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class LuceneIndexer {

    public static void creaIndice(Path indexPath, Path filePath) throws IOException {
        //Path indexPath = Paths.get(LuceneConstants.INDEX_PATH);
        Directory directory = FSDirectory.open(indexPath);

        // Embedding Model
        //AllMiniLmL6V2EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

        // Lucene setup
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        ObjectMapper mapper = new ObjectMapper();

        //try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(LuceneConstants.FILE_PATH), "*.json")) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath, "*.json")) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();

                // Leggi il file JSON
                Map<String, Object> tables = mapper.readValue(file.toFile(),
                        new TypeReference<Map<String, Object>>() {});

                // Alcuni file contengono un campo extra non utile
                // Rimuovi il campo "PAPER'S NUMBER OF TABLES" se esiste
                if (tables.containsKey("PAPER'S NUMBER OF TABLES")) {
                    tables.remove("PAPER'S NUMBER OF TABLES");
                    //System.out.println("Campo 'PAPER'S NUMBER OF TABLES' ignorato nel file: " + currentFileName);
                }

                // Alcuni file contengono un campo "INFO" che specifica che quel file non ha tabelle, quindi skip
                if (tables.containsKey("INFO")) {
                    Object info = tables.get("INFO");
                    //System.out.println("File ignorato (nessuna tabella): " + currentFileName);
                    continue; // Salta l'elaborazione di questo file
                }

                for (Map.Entry<String, Object> entry : tables.entrySet()) {

                    String tableId = entry.getKey();
                    Object value = entry.getValue();

                    if(value instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tableData = (Map<String, Object>) value;


                        // Estraggo i campi necessari
                        String caption = "";
                        if (tableData.getOrDefault("caption", "") != null) {
                            caption = tableData.getOrDefault("caption", "").toString();
                        }
                        String footnotes = "";
                        if (tableData.getOrDefault("footnotes", "") != null) {
                            footnotes = tableData.getOrDefault("footnotes", "").toString();
                        }

                        String references = "";
                        if (tableData.getOrDefault("references", "") != null) {
                            references = tableData.getOrDefault("references", "").toString();
                        }

                        String table = "";
                        if (tableData.getOrDefault("table", "") != null) {
                            table = tableData.getOrDefault("table", "").toString();
                        }

                        // Racchiudo in un'unica stringa i campi che meglio descrivono la tabella
                        String tableDescription = caption + references;


                        // Creo un documento Lucene
                        Document doc = new Document();
                        doc.add(new TextField("caption", caption, Field.Store.YES));
                        doc.add(new TextField("table", table, Field.Store.YES));

                        // Memorizzo il filename per rappresentare meglio l'output delle query
                        doc.add(new StoredField("fileName", fileName)); // Nome del file
                        doc.add(new StoredField("tableID", tableId));

                        // Genero un embedding per la didascalia
                        Embedding embedding;
                        if (!tableDescription.isEmpty() && !tableDescription.isBlank()) {
                            embedding = embeddingModel.embed(TextSegment.from(tableDescription)).content();
                            doc.add(new KnnFloatVectorField("embedding", embedding.vector()));
                        }


                        writer.addDocument(doc);
                    } else {
                        System.out.println("SKIP");
                    }
                }
            }
        }

        writer.close();
        directory.close();
    }

}

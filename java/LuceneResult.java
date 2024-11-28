import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class LuceneResult {
    private String fileName;
    private String caption;
    private String table;
    private String tableID;
    private float score;


    public String getTableID() {
        return tableID;
    }

    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public static void printResult(@NotNull ArrayList<LuceneResult> results) {
        for(LuceneResult result : results) {
            System.out.println("Score: " + result.getScore());
            System.out.println("Filename: " + result.fileName);
            System.out.println("Caption: " + result.getCaption());
            //System.out.println("Table: " + result.getTable());
            System.out.println("-----------------------");
        }
    }
}

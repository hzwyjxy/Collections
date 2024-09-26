package localfile;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileUtils {
    public static synchronized void appendLine(String filePath, String line) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(line);
            bufferedWriter.newLine();

            bufferedWriter.close();
            fileWriter.close();
        } catch (Exception e) {
            System.out.println("添加失败：" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        appendLine("test.txt", "123");
        appendLine("test.txt", "123");
        appendLine("test.txt", "123");
    }
}

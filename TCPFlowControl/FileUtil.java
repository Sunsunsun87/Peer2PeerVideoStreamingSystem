import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {



    public static List<String> readFile(String file) {
        try {
           return Files.readAllLines(new File(file).toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static byte[] readFileBytes(String file) {
        try {
            return Files.readAllBytes(new File(file).toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }


    public static void writeFile(String file, String content, boolean append) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, append));
            bufferedWriter.write(content + System.lineSeparator());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(DataOutputStream writer, String Path) throws IOException {
        //200 ok
//            writer.writeBytes(response("200 OK"));
            FileInputStream fis = new FileInputStream(Path);//Add try-catch later
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = fis.read(buffer)) != -1){
                writer.write(buffer, 0, bytesRead);
            }
            fis.close();
    }


    public static void writeFile(String file, byte[] bytes) {
        try {
            File file1 = new File(file);
            if (!file1.getParentFile().exists()) {
                file1.getParentFile().mkdirs();
            }

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

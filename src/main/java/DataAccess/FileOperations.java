package DataAccess;

import java.io.*;

public class FileOperations {

    public static boolean CopyFile(String oldPath, String newPath) {
        try {
            FileInputStream fis = new FileInputStream(oldPath);
            BufferedInputStream bufis = new BufferedInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newPath);
            BufferedOutputStream bufos = new BufferedOutputStream(fos);

            int len = 0;
            while((len = bufis.read()) != -1){
                bufos.write(len);
            }

            bufis.close();
            bufos.close();
            fis.close();
            fos.close();
            return true;
        }
        catch(IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

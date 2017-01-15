package com.siyehua.klinegraph;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by huangxk on 2017/1/13.
 */
public class ABc {
    public static void main(String[] args) {
        try {
            String content = "";
            BufferedReader bf = new BufferedReader(new FileReader
                    ("C:\\Users\\monitor\\Desktop\\Fish-v318-1106\\kpdf\\001589.sql"));
            String a;
            while ((a = bf.readLine()) != null) {
                int start = a.indexOf("INSERT INTO");
                int edn = a.lastIndexOf(")");
                if (start >= 0 && edn >= 0) {
                    content += a.substring(start, edn + 1) + ";\n";
                }
            }
            InputStream inputStream = stringToInputStrem(content);
            OutputStream outputStream = new FileOutputStream("C:\\Users\\monitor\\Desktop\\Fish"
                    + "-v318-1106\\kpdf\\abc.txt");
            int len;
            byte[] buffer = new byte[1024 * 100];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
            System.out.print("finish");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static InputStream stringToInputStrem(String str) {
        return new ByteArrayInputStream(str.getBytes());
    }

    public static String inputStreamToString(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        String data = "";
        try {
            while ((i = inputStream.read()) != -1) {
                baos.write(i);
            }
            data = baos.toString();
            inputStream.close();
            baos.close();
        } catch (IOException e) {
        }
        return data;
    }
}

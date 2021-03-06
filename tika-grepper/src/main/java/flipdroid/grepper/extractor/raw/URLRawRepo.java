package flipdroid.grepper.extractor.raw;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

public class URLRawRepo {

    private static URLRawRepo instance;


    private URLRawRepo() {

    }

    public static URLRawRepo getInstance() {
        if (instance == null) {
            instance = new URLRawRepo();
        }
        return instance;
    }

    public byte[] fetch(String urlStr) throws URLRepoException, IOException {
        URL url = new URL(urlStr);
        ByteArrayOutputStream bos = null;
        int retryCount = 0;
        try {
            if (retryCount >= 3)
                return null;

            retryCount++;
            final URLConnection conn = url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");
            InputStream in = conn.getInputStream();

            final String encoding = conn.getContentEncoding();
            if (encoding != null) {
                if ("gzip".equalsIgnoreCase(encoding)) {
                    in = new GZIPInputStream(in);
                } else {
                    System.err.println("WARN: unsupported Content-Encoding: " + encoding);
                }
            }

            bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            try {
                while ((r = in.read(buf)) != -1) {
                    bos.write(buf, 0, r);
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            retryCount++;
        }
        return bos.toByteArray();
    }


}

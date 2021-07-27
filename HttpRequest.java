import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public final class HttpRequest implements Runnable {
    static class HttpCode {
        static String NOT_FOUND = "404 Not Found";
        static String OK = "200 OK";
    }
    final static String CRLF = "\r\n";
    private Socket socket;
    private String fileName;

    HttpRequest(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception error) {
            System.out.println(error);
        }
    }

    private FileInputStream openFile(String fileName) {
        try {
            if (fileName == null) {
                return null;
            }

            FileInputStream inStreamFile = new FileInputStream(fileName);
            return inStreamFile;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void parseRequestLine(BufferedReader bufReader) throws Exception {
        String requestLine = bufReader.readLine();
        System.out.println();
        System.out.println(requestLine);

        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // Método HTTP
        fileName = "." + tokens.nextToken();
    }

    private void parseHeaders(BufferedReader bufReader) throws Exception {
        for (String headerLine = null; headerLine != null && headerLine.length() != 0; headerLine = bufReader.readLine()) {
            System.out.println(headerLine);
        }
    }

    private void parseRequest() throws Exception {
        InputStream inStream = socket.getInputStream(); 
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        BufferedReader bufReader = new BufferedReader(inStreamReader);

        parseRequestLine(bufReader);
        parseHeaders(bufReader);
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (fileName.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return "image/jpeg";
        }

        return "application/octet-stream";
    }

    private static void sendFile(FileInputStream inStreamFile, OutputStream outStream) throws Exception {
        byte[] buffer = new byte[1024];

        for (int bytes = 0; bytes != -1; bytes = inStreamFile.read(buffer)) {
            outStream.write(buffer, 0, bytes);
        }
    }

    private void buildResponse() throws Exception {
        OutputStream outStream = socket.getOutputStream();
        DataOutputStream outStreamData = new DataOutputStream(outStream);

        String statusCode = null;
        String contentTypeLine = null;
        String entityBody = null;

        FileInputStream inStreamFile = openFile(fileName);
        boolean fileExists = inStreamFile != null;

        if (fileExists) {
            statusCode = HttpCode.OK;
            contentTypeLine = "Content-type: " + contentType(fileName);
        } else {
            statusCode = HttpCode.NOT_FOUND;
            contentTypeLine = "Content-type: text/html";
            entityBody = "<HTML>" +
            "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
            "<BODY>Not Found</BODY></HTML>";
        }

        // Headers
        outStreamData.writeBytes("HTTP/1.0 " + statusCode + CRLF);
        outStreamData.writeBytes(contentTypeLine + CRLF);
        outStreamData.writeBytes(CRLF);

        // Body
        if (fileExists) {
            sendFile(inStreamFile, outStream);
            inStreamFile.close();
        } else {
            outStreamData.writeBytes(entityBody + CRLF);
        }
        outStreamData.writeBytes(CRLF);

        outStreamData.close();
    }

    private void processRequest() throws Exception {

        parseRequest();
        buildResponse();

        socket.close();
    }
    
}

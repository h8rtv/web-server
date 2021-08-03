import java.io.*;
import java.net.*;
import java.util.*;

public final class HttpRequestHandler implements Runnable {
    final static String CRLF = "\r\n";
    final static String pathToResources = "./res/";
    final static String NOT_FOUND_HTML = "<html><head><title>Not Found</title></head><body>404 Not Found</body></html>";

    private Socket socket;
    private String fileName;

    public HttpRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception error) {
            System.out.println(error);
            error.printStackTrace();
        }
    }

    private FileInputStream openFile(String fileName) {
        try {
            if (fileName == null) {
                return null;
            }

            FileInputStream inStreamFile = new FileInputStream(pathToResources + fileName);
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
        String file = tokens.nextToken();
        if (file.equals("/")) {
            file = "/index.html";
        }
        fileName = "." + file;
    }

    private void parseHeaders(BufferedReader bufReader) throws Exception {
        System.out.println();
        for (String headerLine = bufReader.readLine(); headerLine.length() != 0; headerLine = bufReader.readLine()) {
            System.out.println(headerLine);
        }
    }

    private void parseRequest() throws Exception {
        InputStream inStream = socket.getInputStream(); 
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        BufferedReader bufReader = new BufferedReader(inStreamReader);

        parseRequestLine(bufReader);
        parseHeaders(bufReader);

        socket.shutdownInput();
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

        for (int bytes = inStreamFile.read(buffer); bytes != -1; bytes = inStreamFile.read(buffer)) {
            outStream.write(buffer, 0, bytes);
        }
    }

    private void buildResponse() throws Exception {
        OutputStream outStream = socket.getOutputStream();
        DataOutputStream outStreamData = new DataOutputStream(outStream);

        String statusCode = null;
        String contentTypeLine = null;

        FileInputStream inStreamFile = openFile(fileName);
        boolean fileExists = inStreamFile != null;

        if (fileExists) {
            statusCode = HttpCode.OK.toString();
            contentTypeLine = "Content-type: " + contentType(fileName);
        } else {
            statusCode = HttpCode.NOT_FOUND.toString();
            contentTypeLine = "Content-type: text/html";
            inStreamFile = openFile("./404.html");
        }

        // Headers
        outStreamData.writeBytes("HTTP/1.0 " + statusCode + CRLF);
        outStreamData.writeBytes(contentTypeLine + CRLF);
        outStreamData.writeBytes(CRLF);

        // Body
        if (inStreamFile != null) {
            sendFile(inStreamFile, outStream);
            inStreamFile.close();
        } else {
            outStreamData.writeBytes(NOT_FOUND_HTML + CRLF);
        }
        outStreamData.writeBytes(CRLF);

        socket.shutdownOutput();
    }

    private void processRequest() throws Exception {
        parseRequest();
        buildResponse();

        socket.close();
    }
}

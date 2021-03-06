import java.net.*;

public final class WebServer {
    public static void main(String argv[]) throws Exception {
        int port = 6789;
        ServerSocket server = new ServerSocket(port);
        try {
            while (true) {
                Socket client = server.accept();
                HttpRequestHandler request = new HttpRequestHandler(client);
                Thread thread = new Thread(request);
                thread.start();
            }
        } finally {
            server.close();
        }
    }
}
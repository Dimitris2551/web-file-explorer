import java.net.Socket;

public class connection {
    Socket connectionSocket;
    int listeningPort;
    
    public connection(Socket connectionSocket, int listeningPort) {
        this.connectionSocket = connectionSocket;
        this.listeningPort = listeningPort;
    }
}
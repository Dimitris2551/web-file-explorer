import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HttpServer extends Thread{
    Parser settings;
    LinkedBlockingQueue<connection> q;
    int listeningPort;
    public HttpServer(Parser settings, LinkedBlockingQueue<connection> q, int listeningPort) {
        this.settings = settings;
        this.q = q;
        this.listeningPort = listeningPort;
    }
    
    
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(listeningPort);
            
            while(true){
            Socket connectionSocket = serverSocket.accept();
            System.out.println("Http Server created a new Socket that is: "+connectionSocket);
            //rup the client socket in rupper class connetcion to let the worker know which ServerSocket it came from 
            try {
                q.put(new connection(connectionSocket, listeningPort));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        } catch (IOException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}

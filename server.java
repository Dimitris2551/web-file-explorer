import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;
import java.net.Socket;
import java.net.*;
import java.io.*;



public class server {

public static String getIpAddress() { 
    try {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                    String ipAddress=inetAddress.getHostAddress().toString();
                    System.out.println("IP address "+ipAddress);
                    return ipAddress;
                }
            }
        }
    } catch (SocketException ex) {
        System.out.println("Socket exception in GetIP Address of Utilities"+ex.toString());
    }
    return null; 
}

    public static void main(String[] args) {
        
        String myip = getIpAddress();
        //myip = "www.example.com";
        Parser settings = new Parser();
        settings.parse();
        LinkedBlockingQueue<connection> q = new LinkedBlockingQueue<connection>();
        (new worker(settings, q, myip)).start();
        (new worker(settings, q, myip)).start();
        (new HttpServer(settings, q, settings.listenPort)).start();
        (new HttpServer(settings, q, settings.statisticsPort)).start();
        
        /*File access = new File(settings.accessLog);
        File error = new File(settings.errorLog);
        try{
        PrintWriter aout = new PrintWriter(new FileWriter(access, true));
        PrintWriter eout = new PrintWriter(new FileWriter(error, true));
        } catch(IOException e) {
            e.printStackTrace();
        }*/
    }
}
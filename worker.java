import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;
import java.lang.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;

public class worker extends Thread {
    LinkedBlockingQueue<connection> q;
    Parser settings;
    Socket connectionSocket=null;
    String clientSentence="";
    String myip;
    Date startDate;
    long connectionCounter;
    connection c = null;
    public worker(Parser settings, LinkedBlockingQueue<connection> q, String myip) {
        this.q = q;
        this.settings = settings;
        this.myip = myip;
        Calendar calendar = Calendar.getInstance();
        startDate = calendar.getTime();
    }
    
    //Inner class
    private class fileInfo implements Comparable<fileInfo> {
        String subFileRelativePath;
        String fileName;
        public fileInfo(String subFileRelativePath, String fileName) {
            this.subFileRelativePath = subFileRelativePath;
            this.fileName = fileName;
        }
        
        public int compareTo(fileInfo other) {
            return this.fileName.compareTo(other.fileName);
        }
    }
    
    //Inner class
    private class fileType {
        String icon;
        String format;
        public fileType(String icon, String format) {
            this.icon = icon;
            this.format = format;
        }
    }
    
    public fileType getType(String fileName) {
    String icon = "/icons/";
    String format="text";
    int dot = fileName.lastIndexOf('.');
    String suffix = (dot == -1) ? "" : fileName.substring(dot);
    switch(suffix.toLowerCase()) {
            /* doc */
            case ".doc":
            case ".docx":
            case ".odt":
            icon += "doc.png";
            format = "application/msword";
            break;
            /* xls */
            case ".xls":
            case ".xlsx":
            case ".ods":
            icon += "xls.png";
            format = "application/vnd.ms-excel";
            break;
            /* ppt */
            case ".ppt":
            case ".pptx":
            case ".odp":
            icon += "ppt.png";
            format = "application/vnd.ms-powerpoint";
            break;
            /* pdf */
            case ".pdf":
            case ".ps":
            icon += "pdf.png";
            format = "application/pdf";
            break;
            /* images */
            case ".png":
            case ".jpg":
            case ".jpeg":
            case ".bmp":
            case ".tiff":
            case ".svg":
            case ".pgm":
            case ".ppm":
            case ".pbm":
            icon += "img.png";
            format = "image/jpeg";
            break;
            /* video */  
            case ".mp4":
            case ".flv":
            case ".mkv":
            case ".ogv":
            case ".avi":
            case ".mov":
            case ".qt":
            icon += "video.png";
            format = "video/mp4";
            break;
            /* audio */  
            case ".wav":
            case ".mp3":
            case ".ogg":
            case ".cda":
            case ".flac":
            case ".snd":
            case ".aa":
            case ".mka":
            case ".wma":
            case ".m4p":
            case ".mp4a":
            case ".mpa":
            icon += "audio.png";
            format = "audio/mpeg";
            break;
            /* html */
            case ".html":
            case ".htm":
            icon += "html.png";
            format = "text/html";
            break;
            /* xml */
            case ".xml":
            icon += "xml.png";
            format = "text/plain";
            break;
            /* rss */
            case ".rss":
            icon += "rss.png";
            break;
            default:
            icon += "noicon";
            format = "text/plain";
            break;
        }
        return (new fileType(icon, format));
    }
    
    public synchronized void writeError(Exception e) {
        
        try{
        File error = new File(settings.errorLog);
        PrintWriter eout = new PrintWriter(new FileWriter(error, true));
        String inlog = connectionSocket.getRemoteSocketAddress().toString();
        inlog = inlog+" - ["+TheTime()+"] "+clientSentence;
        inlog = inlog+" "+e.toString();
        eout.write(inlog+"\n");
        eout.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        //better write ip date time and header
    }
    
    public synchronized void writeAccess() {
        
        try{
        File access = new File(settings.accessLog);
        PrintWriter aout = new PrintWriter(new FileWriter(access, true));
        String inlog = connectionSocket.getRemoteSocketAddress().toString();
        inlog = inlog.substring(0, inlog.lastIndexOf(':'));
        inlog = inlog+" - ["+TheTime()+"] "+clientSentence;
        aout.write(inlog+"\n");
        aout.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public String TheTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss yyyy");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now); //2016/11/16 12:08:43
    }
    
    public static String clean(String clientSentence){
        int start = clientSentence.indexOf('/');
        int finish = clientSentence.lastIndexOf('H');
        clientSentence = clientSentence.substring(start+1, finish-1);
        System.out.println(clientSentence);
        int len = clientSentence.length();
        System.out.println("length: "+len); 
        if(len == 2)
        {
            System.out.println("They asked for the rootDir");
            return "";
        }
        return clientSentence;
    }
    
    public void showStatistics(DataOutputStream outToClient) {
        try{
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        long sumDate = endDate.getTime() - startDate.getTime();
        long hours = sumDate/3600000;
        sumDate = sumDate % 3600000;
        long minutes = sumDate/60000;
        sumDate = sumDate % 60000;
        long sec = sumDate/1000;
        String content = "\n<!DOCTYPE html>\n<html>\n<head>\n<title>\nmyServer\n</title>\n</head>\n<body>\n<h1>ce325Server Statistics</h1> \n";
        content = content+"<p>The server has been running for "+hours+" hours "+minutes+" minutes and "+sec+" seconds<p>";
        content = content+"\n</body>\n</html>\n";
        int contentLength = content.length();
        
        String header = "HTTP/1.1 200 OK \nDate: "+TheTime()+"\nServer: CE325 (Java based server)\nLast-Modified: "+TheTime()+"\nContent-Length: "+contentLength+"\nConnection: close\nContent-Type: text/html\n";
        outToClient.writeBytes(header+content);
        } catch(Exception e) {
            writeError(e);
        }
    }
    
    public String responseBuilder(String filepath, DataOutputStream outToClient) {
        String path = settings.rootDir+filepath;
        System.out.println(path);
        File file = new File(path);
        if(!file.exists())
        {
            String htmlResponce = "\n<!DOCTYPE html>\n<html>\n<head>\n<title>\nNot_Found\n</title>\n</head>\n<body>\n<h1>404 The file you requested was not Found</h1>\n</body>\n</html>\n";
            int contentLength = htmlResponce.length();
            String serverAnswer = "HTTP/1.1 404 Not Found \nDate: "+TheTime()+"\nServer: CE325 (Java based server)\nLast-Modified: Mon, 23 Mar 2015 15:04:54 GMT\nContent-Length: "+contentLength+"\nConnection: close\nContent-Type: text/html\n"+htmlResponce;
            return serverAnswer;
        }
        else
        {
            if(file.isDirectory())
            {
                String htmlResponce = "\n<!DOCTYPE html>\n<html>\n<head>\n<title>\nmyServer\n</title>\n</head>\n<body>\n<h1>"+file.getName()+"</h1> \n";
                
                if((path.substring(0, path.lastIndexOf('/')) != settings.rootDir) && (path != settings.rootDir))
                {
                    File superFile = new File(path+"/..");
                    String superName = superFile.getName();
                    
                    Path pathAbsolute = Paths.get(path+"/..");
                    Path pathBase = Paths.get(settings.rootDir);
                    Path pathRelative = pathBase.relativize(pathAbsolute);
                    String subFileRelativePath = pathRelative.toString();
                    System.out.println(subFileRelativePath); 
                    
                    htmlResponce = htmlResponce+"\n\n<a href=\"http://"+myip+":"+c.listeningPort+"/"+pathRelative+"\">\n"+superName+"</a>\n\n";
                }
                String[] fileNames = file.list();
                int len = fileNames.length;
                ArrayList<fileInfo> dNames = new ArrayList<fileInfo>();
                ArrayList<fileInfo> fNames = new ArrayList<fileInfo>();
                String subFileRelativePath;
                
                
                for(int i=0 ; i<len ; i++)
                {
                    if(fileNames[i].charAt(0) != '.')
                    {
                        Path pathAbsolute = Paths.get(path+"/"+fileNames[i]);
                        Path pathBase = Paths.get(settings.rootDir);
                        Path pathRelative = pathBase.relativize(pathAbsolute);
                        subFileRelativePath = pathRelative.toString();
                        File currentFile = new File(pathAbsolute.toString());
                        if(currentFile.isDirectory())
                        {
                            dNames.add(new fileInfo(subFileRelativePath, fileNames[i]));
                        }
                        else
                        {
                            fNames.add(new fileInfo(subFileRelativePath, fileNames[i]));
                        }
                    }
                }
                
                Collections.sort(dNames);
                Collections.sort(fNames);
                
                ListIterator<fileInfo> d = dNames.listIterator();
                while(d.hasNext()) {
                    fileInfo dInfo = d.next();
                    fileType kind = getType(dInfo.fileName);
                    System.out.println(dInfo.subFileRelativePath);
                    htmlResponce = htmlResponce+"\n\n<p><img src=\"http://"+myip+":"+c.listeningPort+"/icons/dir.png\"> <a href=\"http://"+myip+":"+c.listeningPort+"/"+dInfo.subFileRelativePath+"\">\n"+dInfo.fileName+"</a></p>\n";
                    
                }
                ListIterator<fileInfo> f = fNames.listIterator();
                while(f.hasNext()) {
                    fileInfo fInfo = f.next();
                    fileType kind = getType(fInfo.fileName);
                    System.out.println(fInfo.subFileRelativePath);
                    System.out.println(kind.icon);
                    htmlResponce = htmlResponce+"\n\n<p><img src=\"http://"+myip+":"+c.listeningPort+""+kind.icon+"\"><a href=\"http://"+myip+":"+c.listeningPort+"/"+fInfo.subFileRelativePath+"\">\n"+fInfo.fileName+"</a></p>\n";
                    
                }
                
                htmlResponce = htmlResponce+"\n</body>\n</html>\n";
                int contentLength = htmlResponce.length();
                SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
                String last = sdf.format(file.lastModified());
                String serverAnswer = "HTTP/1.1 200 OK \nDate: "+TheTime()+"\nServer: CE325 (Java based server)\nLast-Modified: "+last+"\nContent-Length: "+contentLength+"\nConnection: close\nContent-Type: text/html\n"+htmlResponce;
                return serverAnswer;
                
            }
            else
            {
                long contentLength = file.length();
                fileType kind = getType(file.getName());
                String serverAnswer = "HTTP/1.1 200 OK \nDate: Mon, 23 Mar 2015 16:55:25 GMT\nServer: CE325 (Java based server)\nLast-Modified: Mon, 23 Mar 2015 15:04:54 GMT\nContent-Length: "+contentLength+"\nConnection: close\nContent-Type: "+kind.format+"\n\n";
                
                try{
                outToClient.writeBytes(serverAnswer);
                Files.copy(file.toPath(), outToClient);
                } catch(IOException e) {
                    e.printStackTrace();
                    writeError(e);
                }
                return "";
            }
        }
        //return "the method reached its end";
    }

    public void run() {
        String previousSentence = "/ HTTP 1.1";
        while(true){
            
            try {
                c = q.take();
                
                connectionSocket = c.connectionSocket;
                System.out.println("Hello from a thread!");
                String clientRequest;
                String htmlResponce = "\n<!DOCTYPE html>\n<html>\n<head>\n<title>\nmyServer\n</title>\n</head>\n<body>\n<h1>this text is for you</h1>\n<a href=\"http://facebook.com\">\nfacebook</a>\n\n</body>\n</html>\n";
                int contentLength = htmlResponce.length();
                String serverAnswer = "HTTP/1.1 200 OK \nDate: Mon, 23 Mar 2015 16:55:25 GMT\nServer: CE325 (Java based server)\nLast-Modified: Mon, 23 Mar 2015 15:04:54 GMT\nContent-Length: "+contentLength+"\nConnection: close\nContent-Type: text/html\n"+htmlResponce;
                //System.out.println(connectionSocket.getKeepAlive());
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                //System.out.println(inFromClient);
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                clientSentence = inFromClient.readLine();
                System.out.println(clientSentence);
                if(clientSentence == null)
                {
                    clientSentence = previousSentence;
                }
                previousSentence = clientSentence;
                writeAccess();
                clientSentence = clean(clientSentence);
                System.out.println(clientSentence);
                
                if(c.listeningPort == 8001)
                {
                    showStatistics(outToClient);
                    outToClient.close();
                    continue;
                }
                
                serverAnswer = responseBuilder(clientSentence, outToClient);
                
                outToClient.writeBytes(serverAnswer);
                outToClient.close();
            }catch (Exception e) {
                writeError(e);
            }
        }
    }
}
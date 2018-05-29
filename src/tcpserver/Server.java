/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axelb
 */
public class Server implements Runnable {
    public Server()
    {
        try {
            serverSocket = new ServerSocket(1500);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            Socket connection = serverSocket.accept();
            
            OutputStream out = connection.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            String request = "";
            String data;
            do
            {
                data = in.readLine();
                request += data;
            }
            while(!data.equals(""));
            
            if(request.startsWith("GET"))
            {
                Date date = new Date();
                String response = "HTTP/1.1 ";
                String fileContent = "";
                String filename = request.substring(4, request.length() - 9);
                String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
                
                if(fileExtension.matches("(jpg|jpeg|png|gif|bmp)"))
                {
                    File file = new File(filename);
                    try
                    {
                        FileInputStream fileStream = new FileInputStream(file);
                        byte imageData[] = new byte[(int) file.length()];
                        fileStream.read(imageData);
                        
                        fileContent = new String(Base64.getEncoder().encode(imageData));
                    }
                    catch(IOException ex)
                    {
                        response += "404 Not Found" + System.lineSeparator();
                    }
                }
                else
                {
                    try(BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(filename)))
                    {
                        int c;
                        do
                        {
                            c = fileStream.read();
                            if(c != -1)
                                fileContent += (char) c;
                        }
                        while(c != -1);

                        response += "200 OK" + System.lineSeparator();
                    }
                    catch(IOException ex)
                    {
                        response += "404 Not Found" + System.lineSeparator();
                    }
                }
                //Date lastModified = new Date();
                
                response += "Date: " + date + System.lineSeparator();
                response += "Server: TP" + System.lineSeparator();
                response += "Content-Length: " + fileContent.getBytes().length + System.lineSeparator();
                //response += "Connection: close" + System.lineSeparator();
                
                switch(fileExtension)
                {
                    case "txt" :
                        response += "Content-Type: text/plain" + System.lineSeparator();
                        break;
                    case "html" :
                    case "htm" :
                        response += "Content-Type: text/html" + System.lineSeparator();
                        break;
                    case "jpg" :
                    case "jpeg" :
                        response += "Content-Type: image/jpeg" + System.lineSeparator();
                        break;
                    case "png" :
                        response += "Content-Type: image/png" + System.lineSeparator();
                        break;
                    case "gif" :
                        response += "Content-Type: image/gif" + System.lineSeparator();
                        break;
                    case "bmp" :
                        response += "Content-Type: image/bmp" + System.lineSeparator();
                        break;
                }
                
                response += System.lineSeparator();
                response += fileContent;
                
                out.write(response.getBytes());
                out.flush();
            }
            else if(request.startsWith("PUT"))
            {
                BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(request.substring(4, request.length() - 9)));
                String fileContent = "";
                int c;
                do
                {
                    c = fileStream.read();
                    if(c != -1)
                        fileContent += (char) c;
                }
                while(c != -1);
                
                fileContent += System.lineSeparator() + System.lineSeparator();
                out.write(fileContent.getBytes());
                out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    ServerSocket serverSocket;
}

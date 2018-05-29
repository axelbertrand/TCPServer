/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template filename, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axelb
 */
public class Client {
    public Client()
    {
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 1500);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getRequest(String filename)
    {
        try {
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String request = "GET " + filename + " HTTP/1.1" + System.lineSeparator() + System.lineSeparator();
            out.write(request.getBytes());
            out.flush();
            
            String response = "";
            char data;
            do
            {
                data = (char) in.read();
                response += data;
            }
            while(in.ready());
            
            int contentTypeIndex = response.indexOf("Content-Type");
            String contentType = response.substring(contentTypeIndex, response.indexOf('\n', contentTypeIndex));
            contentType = contentType.split(":")[1];
            
            if(contentType.startsWith(" text"))
            {
                return response;
            }
            else if(contentType.startsWith(" image"))
            {
                File file = new File(filename);
                String outputFilename = file.getParentFile().getParent() + "\\client" + filename.substring(filename.lastIndexOf("\\"));
                String fileContent = response.substring(response.indexOf(System.lineSeparator() + System.lineSeparator()) + 4);
                
                try (FileOutputStream outputFile = new FileOutputStream(outputFilename)) {
                    outputFile.write(Base64.getDecoder().decode(fileContent));
                    outputFile.flush();
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public String putRequest(String filename)
    {
        
        
        return null;
    }
    
    private Socket socket;
}

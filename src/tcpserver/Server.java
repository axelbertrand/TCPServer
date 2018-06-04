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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        while(true)
        {
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
                        
                        try
                        {
                            File file = new File(getClass().getResource("server/" + filename).toURI());
                            BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(file));
                            byte imageData[] = new byte[(int) file.length()];
                            fileStream.read(imageData);

                            fileContent = new String(Base64.getEncoder().encode(imageData));
                            
                            response += "200 OK" + System.lineSeparator();
                        }
                        catch(Exception ex)
                        {
                            response += "404 Not Found" + System.lineSeparator();
                        }
                    }
                    else
                    {
                        try
                        {
                            File file = new File(getClass().getResource("server/" + filename).toURI());
                            BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(file));
                            byte fileData[] = new byte[(int) file.length()];
                            fileStream.read(fileData);
                            
                            fileContent = new String(fileData);

                            response += "200 OK" + System.lineSeparator();
                        }
                        catch(Exception ex)
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
                    String reponse="";
                
                    int indHttp = request.indexOf(" HTTP/1.");
                    String http = request.substring(indHttp+1, indHttp+9);

                    String cheminFichier = request.substring(4, indHttp);
                    String nomFichier = cheminFichier.substring(cheminFichier.lastIndexOf("\\") + 1);
                    reponse += http;
                    
                    String ext = nomFichier.substring(nomFichier.lastIndexOf(".") + 1);
                    String type = "";
                    
                    String fichier = "";
                    char c;
                    do
                    {
                        c = (char) in.read();
                        fichier += c;
                    }
                    while(in.ready());
                    
                    
                    try{
                        String path = "build\\classes\\tcpserver\\server\\" + nomFichier;
                        FileInputStream f = new FileInputStream(path);
                        byte[] contenu = Files.readAllBytes(Paths.get(path));
                    
                        String compare = "";
                        if(ext.equals("txt")) {
                            compare = new String(contenu);
                            type = "text";
                        }
                        else {
                            compare = new String(Base64.getEncoder().encode(contenu));
                            type = "image";
                        }
                    
                    
                        if(fichier.equals(compare)) {
                            reponse += " 204 No Content"+System.lineSeparator();
                        }
                        else{
                            writeToFile(nomFichier,fichier,type);
                            reponse += " 200 OK"+System.lineSeparator();
                        }
                    }		
                    catch (FileNotFoundException e){
                        if(ext.equals("txt")) {
                            type = "text";
                        }
                        else {
                            type = "image";
                        }
                        writeToFile(nomFichier, fichier, type);
                        reponse = http+" 201 Created"+System.lineSeparator();
                    }
                     
                    reponse += "Content-Location " + nomFichier + System.lineSeparator();
                    reponse += System.lineSeparator() + System.lineSeparator();
                    out.write(reponse.getBytes());
                    out.flush();
                }
            
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void writeToFile(String nomFichier, String fichier, String type) {
        
        try {
            File clientFolder = new File(getClass().getResource("server").toURI());
            File file = new File(clientFolder, nomFichier);
            FileOutputStream outputFile = new FileOutputStream(file);
            
            if(type.equals("text"))
            {
                outputFile.write(fichier.getBytes());
            }
            else 
            {
                outputFile.write(Base64.getDecoder().decode(fichier));
            }

            outputFile.flush();
        } catch (URISyntaxException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args)
    {
        (new Thread(new Server())).start();
    }
    
    private ServerSocket serverSocket;
}

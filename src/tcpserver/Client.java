/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template filename, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axelb
 */
public class Client implements Runnable {
    public Client()
    {
        try {
            System.out.println("Ouverture de connexion");
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 1500);
            System.out.println("Connexion etablie");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choisissez le nom du fichier :");
        String filename = scanner.next();
        System.out.println("Choisissez le type de requête (GET/PUT) :");
        String requestMode = scanner.next();
        
        switch(requestMode)
        {
            case "GET" :
            case "get" :
                getRequest(filename);
                break;
            case "PUT" :
            case "put" :
                putRequest(filename);
            default :
                System.out.println("Ce type de requête n'est pas reconnu");
        }
    }
    
    public void getRequest(String filename)
    {
        try {
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String request = "GET " + filename + " HTTP/1.1" + System.lineSeparator() + System.lineSeparator();
            out.write(request.getBytes());
            out.flush();
            System.out.println("Requête GET envoyée au serveur");
            
            String response = "";
            char data;
            do
            {
                data = (char) in.read();
                response += data;
            }
            while(in.ready());
            
            System.out.println("Réponse du serveur : ");
            System.out.println(response);
            
            int contentTypeIndex = response.indexOf("Content-Type");
            if(contentTypeIndex < 0)
                return;
            
            String contentType = response.substring(contentTypeIndex, response.indexOf('\n', contentTypeIndex));
            contentType = contentType.split(":")[1];
            
            String fileContent = response.substring(response.indexOf(System.lineSeparator() + System.lineSeparator()) + 4);
            
            File clientFolder = new File(getClass().getResource("client").toURI());
            File file = new File(clientFolder, filename);
            FileOutputStream outputFile = new FileOutputStream(file);
            
            if(contentType.startsWith(" text"))
            {
                outputFile.write(fileContent.getBytes());
            }
            else if(contentType.startsWith(" image"))
            {
                outputFile.write(Base64.getDecoder().decode(fileContent));
            }

            outputFile.flush();

        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void putRequest(String filename)
    {
        String fileContent;
        try {
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            File file = new File(getClass().getResource("client/" + filename).toURI());
            BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(file));
            byte data[] = new byte[(int) file.length()];
            fileStream.read(data);

            String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
            if(fileExtension.matches("(jpg|jpeg|png|gif|bmp)")) {
                fileContent = new String(Base64.getEncoder().encode(data));
            }
            else {
                fileContent = new String(data);
            }
                        
            String request = "PUT " + filename + " HTTP/1.1" + System.lineSeparator();
            request += "Host: client" + System.lineSeparator();
            
            switch(fileExtension)
            {
                case "txt" :
                    request += "Content-Type: text/plain" + System.lineSeparator();
                    break;
                case "html" :
                case "htm" :
                    request += "Content-Type: text/html" + System.lineSeparator();
                    break;
                case "jpg" :
                case "jpeg" :
                    request += "Content-Type: image/jpeg" + System.lineSeparator();
                    break;
                case "png" :
                    request += "Content-Type: image/png" + System.lineSeparator();
                    break;
                case "gif" :
                    request += "Content-Type: image/gif" + System.lineSeparator();
                    break;
                case "bmp" :
                    request += "Content-Type: image/bmp" + System.lineSeparator();
                    break;
            }
            
            request += "Content-Length: " + fileContent.getBytes().length + System.lineSeparator();
            
            request += System.lineSeparator();
            request += fileContent;
                        
            out.write(request.getBytes());
            out.flush();
            System.out.println("Requête PUT envoyée au serveur");
            
            String response = "";
            char rep;
            do
            {
                rep = (char) in.read();
                response += rep;
            }
            while(in.ready());
            
            System.out.println("Réponse du serveur : ");
            System.out.println(response);
            
            } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args)
    {
        (new Thread(new Client())).start();
    }
    
    private Socket socket;
}

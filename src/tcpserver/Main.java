/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpserver;

/**
 *
 * @author axelb
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread server = new Thread(new Server());
        server.start();
        
        Client client = new Client();
        String response = client.getRequest("C:\\Users\\axelb\\Documents\\Polytech\\ARAR\\ARARTCP\\src\\arartcp\\server\\image_test.jpg");
        
        System.out.println("réponse :\n" + response);
    }
    
}

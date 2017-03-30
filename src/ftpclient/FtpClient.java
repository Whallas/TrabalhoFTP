/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpclient;

import java.net.*;
import java.io.*;

/**
 *
 * @author kernel
 */
public class FtpClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here
        Socket soc = new Socket("192.168.56.1", 2121);
        transfereComando t = new transfereComando(soc);
        t.displayMenu();
    }
}

class transfereComando {

    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;
    String kuser, kpass;
    private boolean userConectado;

    transfereComando(Socket soc) {
        try {
            ClientSoc = soc;
            din = new DataInputStream(ClientSoc.getInputStream());
            dout = new DataOutputStream(ClientSoc.getOutputStream());
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception ex) {
        }
    }

    void verifica() throws Exception {

    }

    void cmd_user() throws Exception {
        if (userConectado) {
            System.out.print("Usuario já conectado.");
        } else {
            login();
        }
    }

    void login() throws Exception {
        String nomeUsuario, senha, codigo_retorno;
        System.out.print("Usuario: ");
        nomeUsuario = br.readLine();
        dout.writeUTF("USER");
        dout.writeUTF(nomeUsuario);
        kuser = nomeUsuario;
        //codigo_retorno = din.readUTF();
        dout.writeUTF("PASS");
        System.out.print("Senha: ");
        senha = br.readLine();
        dout.writeUTF(senha);
        codigo_retorno = din.readUTF();
        if (codigo_retorno.compareTo("531") == 0) {
            System.out.print("Usuário ou senha inválido");
        } else {
            System.out.print("Welcome to jftp 0.01");
            userConectado = true;
        }
    }

    void lista() throws Exception {
        String msgFromServer;
        if (userConectado) {
            dout.writeUTF("ls");
            while ((msgFromServer = din.readUTF()).compareTo("-1") != 0) {
                System.out.println(msgFromServer);
            }
        } else {
            System.out.print("Voce precisa logar primeiro.");
        }
    }

    void SendFile() throws Exception {

        String filename;
        System.out.print("Enter File Name :");
        filename = br.readLine();

        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("File not Exists...");
            dout.writeUTF("File not found");
            return;
        }

        dout.writeUTF(filename);

        String msgFromServer = din.readUTF();
        if (msgFromServer.compareTo("File Already Exists") == 0) {
            String Option;
            System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
            Option = br.readLine();
            if (Option == "Y") {
                dout.writeUTF("Y");
            } else {
                dout.writeUTF("N");
                return;
            }
        }

        System.out.println("Sending File ...");
        FileInputStream fin = new FileInputStream(f);
        int ch;
        do {
            ch = fin.read();
            dout.writeUTF(String.valueOf(ch));
        } while (ch != -1);
        fin.close();
        System.out.println(din.readUTF());

    }

    void ReceiveFile() throws Exception {
        String fileName;
        System.out.print("Enter File Name :");
        fileName = br.readLine();
        dout.writeUTF(fileName);
        String msgFromServer = din.readUTF();

        if (msgFromServer.compareTo("File Not Found") == 0) {
            System.out.println("File not found on Server ...");
            return;
        } else if (msgFromServer.compareTo("READY") == 0) {
            System.out.println("Receiving File ...");
            File f = new File(fileName);
            if (f.exists()) {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option = br.readLine();
                if (Option == "N") {
                    dout.flush();
                    return;
                }
            }
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            do {
                temp = din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fout.write(ch);
                }
            } while (ch != -1);
            fout.close();
            System.out.println(din.readUTF());

        }

    }

    public void displayMenu() throws Exception {
        while (true) {
            //System.out.println("Welcome to jftp client 0.01");            
            System.out.print("\nftp :");
            String[] comando = br.readLine().trim().split("\\s+");

            //System.out.println("count is = "+(comando.length));
            if (comando[0].compareTo("user") == 0) {
                cmd_user();
            } else if (comando[0].compareTo("pass") == 0) {
                verifica();
                //cmd_pass();
            } else if (comando[0].compareTo("ls") == 0) {
                lista();
            } else if (comando[0].compareTo("send") == 0) {
                verifica();
                dout.writeUTF("SEND");
                SendFile();
            } else if (comando[0].compareTo("get") == 0) {
                verifica();
                dout.writeUTF("GET");
                ReceiveFile();
            } else if (comando[0].compareTo("quit") == 0) {
                dout.writeUTF("DISCONNECT");
                System.exit(1);

            } else {
                //dout.writeUTF("DISCONNECT");
                System.out.print("\nAjuda?");
            }
        }
    }
}

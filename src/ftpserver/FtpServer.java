/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpserver;

import java.net.*;
import java.io.*;

/**
 *
 * @author kernel
 */
public class FtpServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception
    {
        ServerSocket soct=new ServerSocket(2020);
        ServerSocket soc=new ServerSocket(2121);
        System.out.println("FTP Server Started on Port Number 5217");
        while(true)
        {
            System.out.println("Waiting for Connection ...");
            transferfile t=new transferfile(soc.accept());
            
        }
    }
}
    
class transferfile extends Thread {
    private static final String[][] usuarios = {{"whallas", "111"}, {"analberto", "222"}, {"tiago", "333"}};
    //private static ArrayList<String> users = new ArrayList<>();    
    private boolean userConectado;
    
    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;
    String kuser, kpass;
    
    transferfile(Socket soc)
    {
        try
        {
            ClientSoc=soc;                        
            din=new DataInputStream(ClientSoc.getInputStream());
            dout=new DataOutputStream(ClientSoc.getOutputStream());
            System.out.println("FTP Client Connected ...");
            start();
            
        }
        catch(Exception ex)
        {
        }        
    }
    
    void verifica() throws Exception {
        //530 dont logged
    }
    
    void cmd_user() throws Exception {
        String usuario = din.readUTF();
        String result;
        String codigo_retorno;
        kuser = usuario;
        /*
        for (String[] chave : usuarios) {
            if (chave[0].equals(usuario)) {
                result = "Usuário ok";
                codigo_retorno = "331";
                dout.writeUTF(codigo_retorno);
            } else {
                result = "Usuário ok";
                codigo_retorno = "531";
                dout.writeUTF(codigo_retorno);
            }
        }
        */
    }
    
    void cmd_pass() throws Exception {
        String senha = din.readUTF();
        //String login = din.readUTF();
        String result;
        String codigo_retorno;
        System.out.println("verificando senha para " + kuser + " e senha " + senha);
        for (String[] chave : usuarios) {
            System.out.println("testando " + chave[0] + " senha " + chave[1]);
            if (chave[0].equals(kuser) && chave[1].equals(senha)) {
                result = "Senha ok";
                codigo_retorno = "230";
                dout.writeUTF(codigo_retorno);
                this.userConectado = true;
            } 
        }
        if (!userConectado) {
            result = "Usuario ou senha inválido";
            codigo_retorno = "531";
            dout.writeUTF(codigo_retorno);
        }
        
    }
    
    void lista() throws Exception {
        File dir = new File(".");
        File[] filesList = dir.listFiles();
        //dout.writeUTF("debug...");
        for (File file : filesList) {
            if (file.isFile()) {
                //System.out.println(file.getName());
                //dout.writeUTF("listando arquivos...");
                dout.writeUTF(file.getName());
            }
        }
        dout.writeUTF("-1");
    }
    
    void SendFile() throws Exception
    {        
        String filename=din.readUTF();
        File f=new File(filename);
        if(!f.exists())
        {
            dout.writeUTF("File Not Found");
            return;
        }
        else
        {
            dout.writeUTF("READY");
            FileInputStream fin=new FileInputStream(f);
            int ch;
            do
            {
                ch=fin.read();
                dout.writeUTF(String.valueOf(ch));
            }
            while(ch!=-1);    
            fin.close();    
            dout.writeUTF("File Receive Successfully");                            
        }
    }
    
    void ReceiveFile() throws Exception
    {
        String filename=din.readUTF();
        if(filename.compareTo("File not found")==0)
        {
            return;
        }
        File f=new File(filename);
        String option;
        
        if(f.exists())
        {
            dout.writeUTF("File Already Exists");
            option=din.readUTF();
        }
        else
        {
            dout.writeUTF("SendFile");
            option="Y";
        }
            
            if(option.compareTo("Y")==0)
            {
                FileOutputStream fout=new FileOutputStream(f);
                int ch;
                String temp;
                do
                {
                    temp=din.readUTF();
                    ch=Integer.parseInt(temp);
                    if(ch!=-1)
                    {
                        fout.write(ch);                    
                    }
                }while(ch!=-1);
                fout.close();
                dout.writeUTF("File Send Successfully");
            }
            else
            {
                return;
            }
            
    }


    public void run()
    {
        while(true)
        {
            try
            {
                System.out.println("Waiting for Command ...");
                String Command=din.readUTF();
                if(Command.compareTo("USER")==0)
                {
                    System.out.println("\tUSER Command Received ...");
                    cmd_user();
                    continue;
                }
                else if(Command.compareTo("PASS")==0)
                {
                    System.out.println("\tPASS Command Receiced ...");                
                    cmd_pass();
                    continue;
                }
                else if(Command.compareTo("GET")==0)
                {
                    System.out.println("\tGET Command Received ...");
                    ReceiveFile();
                    continue;
                }
                else if(Command.compareTo("SEND")==0)
                {
                    System.out.println("\tSEND Command Receiced ...");                
                    ReceiveFile();
                    continue;
                }
                else if(Command.compareTo("ls")==0)
                {
                    System.out.println("\tls Command Receiced ...");
                    lista();
                    continue;
                }
                else if(Command.compareTo("DISCONNECT")==0)
                {
                    System.out.println("\tDisconnect Command Received ...");
                    System.exit(1);
                }
            }
            catch(Exception ex)
            {
            }
        }
    }
   
}
package FTP;

/**
 * @author kernel
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FTPServer {
    public static void main(String args[]) throws Exception {
        ServerSocket soc = new ServerSocket(5217);
        System.out.println("Servidor FTP Iniciado na porta 5217");
        while (true) {
            System.out.println("Aguardando Conexão ...");
            //transferfile t = new transferfile(soc.accept());
            new transferfile(soc.accept());
        }
    }
}

class transferfile extends Thread {
    private static final String[][] usersAndKeys = {{"whallas", "111"}, {"analberto", "222"}, {"tiago", "333"}};
    private static ArrayList<String> users = new ArrayList<>();
    private DataInputStream din;
    private DataOutputStream dout;

    transferfile(Socket soc) {
        try {
            din = new DataInputStream(soc.getInputStream());
            dout = new DataOutputStream(soc.getOutputStream());
            System.out.println("FTP Cliente Conectado ...");
            start();
        } catch (Exception ignored) {
        }
    }

    private void SendFile() throws Exception {
        String filename = din.readUTF();
        File f = new File(filename);
        if (!f.exists()) {
            dout.writeUTF("Arquivo não encontrado.");
        } else {
            dout.writeUTF("READY");
            FileInputStream fin = new FileInputStream(f);
            int ch;
            do {
                ch = fin.read();
                dout.writeUTF(String.valueOf(ch));
            } while (ch != -1);
            fin.close();
            dout.writeUTF("Arquivo Recebido com Sucesso");
        }
    }

    private void ReceiveFile() throws Exception {
        String filename = din.readUTF();
        if (filename.compareTo("Arquivo não Encontrado") == 0) {
            return;
        }
        File f = new File(filename);
        String option;

        if (f.exists()) {
            dout.writeUTF("Arquivo já Existe");
            option = din.readUTF();
        } else {
            dout.writeUTF("Envia arquivo");
            option = "Y";
        }

        if (option.compareTo("Y") == 0) {
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            do {
                temp = din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) fout.write(ch);
            } while (ch != -1);
            fout.close();
            dout.writeUTF("Arquivo enviado com Sucesso");
        }
    }

    private void login() throws IOException {
        String usuario = din.readUTF();
        String senha = din.readUTF();
        String result = null;

        System.out.println(usuario + " : " + senha);

        if (!users.contains(usuario)) {
            for (String[] log : usersAndKeys) {
                if (log[0].equals(usuario) && log[1].equals(senha)) {
                    result = "login efetuado com Sucesso";
                    users.add(usuario);
                    break;
                }
            }
            if (result == null) result = "Error de login";
        } else result = "Usuário Registrado!";

        dout.writeUTF(result);
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Aguardando por um comando ...");
                String Command = din.readUTF();
                if (Command.compareTo("GET") == 0) {
                    System.out.println("\tGET Comando Recebido ...");
                    SendFile();
                } else if (Command.compareTo("SEND") == 0) {
                    System.out.println("\tSEND Comando Recebido ...");
                    ReceiveFile();
                } else if (Command.compareTo("DISCONNECT") == 0) {
                    System.out.println("\tDisconnect Comando Recebido ...");
                    System.exit(1);
                } else if (Command.compareTo("LOGIN") == 0) {
                    System.out.println("\tLOGIN Comando Recebido ...");
                    login();
                }
                /*else if (Command.compareTo("USER") == 0) {
                    System.out.println("\tUSER Comando Recebido ...");
                } else if (Command.compareTo("PASS") == 0) {
                    System.out.println("\tPASS Comando Recebido ...");
                    senha();
                }*/
            } catch (Exception ignored) {
            }
        }
    }
}
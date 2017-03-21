package FTP;

import java.io.*;
import java.net.Socket;

class FTPClient {
    public static void main(String args[]) throws Exception {
        Socket soc = new Socket("127.0.0.1", 5217);
        transferfileClient t = new transferfileClient(soc);
        t.displayMenu();
    }
}

class transferfileClient {
    private DataInputStream din;
    private DataOutputStream dout;
    private BufferedReader br;
    private boolean userConectado;
    private String nomeUsuario;

    transferfileClient(Socket soc) {
        try {
            din = new DataInputStream(soc.getInputStream());
            dout = new DataOutputStream(soc.getOutputStream());
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception ignored) {
        }
    }

    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (final Exception ignored) {
        }
    }

    private void SendFile() throws Exception {
        String filename;
        System.out.print("Enter File Name: ");
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
            if ("Y".equals(Option)) {
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
        }
        while (ch != -1);
        fin.close();
        System.out.println(din.readUTF());
    }

    private void ReceiveFile() throws Exception {
        String fileName;
        System.out.print("Enter File Name :");
        fileName = br.readLine();
        dout.writeUTF(fileName);
        String msgFromServer = din.readUTF();

        if (msgFromServer.compareTo("File Not Found") == 0) {
            System.out.println("File not found on Server ...");
        } else if (msgFromServer.compareTo("READY") == 0) {
            System.out.println("Receiving File ...");
            File f = new File(fileName);
            if (f.exists()) {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option = br.readLine();
                if ("N".equals(Option)) {
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

    private void login() throws Exception {
        System.out.print("Usuario: ");
        nomeUsuario = br.readLine();
        dout.writeUTF(nomeUsuario);

        System.out.print("Senha: ");
        String senha = br.readLine();
        dout.writeUTF(senha);

        System.out.println("Aguardando resposta...");

        String result = din.readUTF();
        if (result.compareTo("Sucess Login") == 0) {
            userConectado = true;
            System.out.println("Conexão com sucesso!");
        } else if (result.compareTo("Login Error") == 0) {
            System.out.println("Conexão falhou");
            //System.exit(1);
        } else if (result.compareTo("User Logged") == 0) {
            System.out.println("Usuário já está logado");
        }
    }

    void displayMenu() throws Exception {
        while (true) {
            clearConsole();
            if (userConectado) {
                System.out.println("Olá, " + nomeUsuario + ".");
                System.out.println("[ MENU ]");
                System.out.println("1. USER");
                System.out.println("2. PASS");
                System.out.println("3. Send File");
                System.out.println("4. Receive File");
                System.out.println("5. Exit");
                System.out.print("\nEnter Choice :");
                int choice;
                choice = Integer.parseInt(br.readLine());
                if (choice == 1) {
                    dout.writeUTF("USER");
                    SendFile();
                } else if (choice == 2) {
                    dout.writeUTF("PASS");
                    ReceiveFile();
                } else if (choice == 3) {
                    dout.writeUTF("SEND");
                    ReceiveFile();
                } else if (choice == 4) {
                    dout.writeUTF("GET");
                    ReceiveFile();
                } else if (choice == 5) {
                    dout.writeUTF("DISCONNECT");
                    System.exit(0);
                }
            } else {
                dout.writeUTF("LOGIN");
                login();
            }
        }
    }
}

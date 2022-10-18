/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package es21p89server;

/**
 *
 * @author FEDERICOCOPES
 */
import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Es21p89Server extends Thread {

    ServerSocket server;

    public Es21p89Server() throws IOException {
        server = new ServerSocket(13);
        server.setSoTimeout(1000); // 1000ms = 1s
    }

    public static void saveStats(int first, int second, int third) { //Scrivere dati su file CSV
        try {
            BufferedReader br = new BufferedReader(new FileReader("datiJAVACaffe.csv"));
            FileWriter txt = new FileWriter("datiJAVACaffe.csv", true);
            txt.write("0");
            txt.write(";");
            txt.write((String.valueOf(first)));
            txt.write(";");
            txt.write(String.valueOf(second));
            txt.write(";");
            txt.write((String.valueOf(third)));
            txt.write("\n");
            txt.close();
        } catch (IOException ex) {
        }
    }

    public static String[] leggiFile(int code) { //Leggere una colonna (decisa da input)
        String data[];
        int media = 0;
        String currentLine;
        ArrayList<String> colData = new ArrayList();
        try {
            FileReader fr = new FileReader("datiJAVACaffe.csv");
            BufferedReader br = new BufferedReader(fr);
            while ((currentLine = br.readLine()) != null) {
                data = currentLine.split(";");
                colData.add(data[code]);

            }
        } catch (Exception e) {
            return null;
        }
        return colData.toArray(new String[0]);
    }

    public int leggiLinea() throws FileNotFoundException, IOException  {
        int result = 0;
        FileReader file = new FileReader("datiJAVACaffe.csv");
        BufferedReader buffer = new BufferedReader(file);
        //read the 1st line
        String line = buffer.readLine();
        //display the 1st line
        line = line.substring(2);
        line = line.replace(";", "");
        System.out.println(line);
        result = Integer.valueOf(line);
        return result;
    }
    
    public static void eliminaContenuto() {
        try {
            FileChannel.open(Path.of("datiJAVACaffe.csv"), StandardOpenOption.WRITE)
                    .truncate(0).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Socket connection = null;
        while (true) {
            try {
                ArrayList answer = new ArrayList();
                ArrayList richiesta = new ArrayList();
                connection = server.accept();
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                richiesta = (ArrayList) in.readObject();
                System.out.println("SCELTA EFFETTUATA: " + (int) richiesta.get(0));
                int num = 0;
                switch ((int) richiesta.get(0)) {
                    case 1: //votare un caffe
                        saveStats((int) richiesta.get(1), (int) richiesta.get(2), (int) richiesta.get(3));
                        System.out.print("I voti sono stati salvati correttamente");
                        num = 1; //corretta scrittura dei voti
                        answer.add(num);
                        out.writeObject(answer);
                        out.flush();
                        break;
                    case 2://visualizzare il voto medio di un caffe
                        String data[] = leggiFile((int) richiesta.get(1));
                        int media = 0;
                        int count = 0;
                        for (int i = 0; i < data.length; i++) {
                            count++;
                            media = media + Integer.parseInt(data[i]);
                        }
                        int val = media / data.length;
                        answer.add(val);
                        out.writeObject(answer);
                        out.flush();
                        break;
                    case 3: //visualizzare i codici di un caffe
                        int res = leggiLinea();
                        System.out.println("La linea vale: "+res);
                        answer.add(res);
                        out.writeObject(answer);
                        out.flush();
                        eliminaContenuto();
                        break;
                }
            } catch (SocketTimeoutException exception) {
            } catch (IOException exception) {
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Es21p89Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.shutdownOutput();
                        connection.close();
                    } catch (IOException exception) {
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        int c;
        try {
            Es21p89Server daytime_server = new Es21p89Server();
            daytime_server.start();
            c = System.in.read();
            daytime_server.interrupt();
            daytime_server.join();
        } catch (IOException exception) {
            System.err.println("Errore!");
        } catch (InterruptedException exception) {
            System.err.println("Fine!");
        }
    }
}

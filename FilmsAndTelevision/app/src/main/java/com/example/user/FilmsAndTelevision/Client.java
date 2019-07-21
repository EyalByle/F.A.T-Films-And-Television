package com.example.user.FilmsAndTelevision;

import java.io.*;
import java.net.*;

public class Client
{
    public static boolean is_initiated = false;  // Is the client initiated
    private static boolean waitDone = false;     // Is the send/receive command done
    private static final int SERVER_PORT = 2000; // Port of the server
    private static final String SERVER_IP = "172.29.231.117";  // IP of the server
    public static String sent_text = "EMPTY";  // Text that the client sends to the server
    public static String received_text = "EMPTY";  // Text that the client receives from the server
    private static final int BUFFER_SIZE = 4096;  // Buffer size for receiving big messages
    private final static Object syncObj = new Object();  // Sync object to sync between the server communications threads to the GUI thread
    private static final int MESSAGE_LENGTH = 8;  // Length of the int length at the beginning of a message: "00000011Hello World" for example

    public static Socket socket;  // Client-Server socket

    // Returns the IP address of the server
    public static String getServerIp()
    {
        return Client.SERVER_IP;
    }

    // Creates the client and initiates the communication socket
    public static class ClientThread implements Runnable
    {

        @Override
        public void run()
        {
            if (Client.is_initiated)
            {
                return;
            }
            while (true)
            {
                try
                {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    Client.socket = new Socket(serverAddr, SERVER_PORT);
                    Client.is_initiated = true;
                    break;
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                    System.out.println("GOING TO CRASH BECAUSE REASONS");
                }
            }
            System.out.println("Success!");
            Client.notifyDone();

        }
    }

    // Waits for the socket to finish the action it is doing
    // Waits until notifyDone is being called
    public static void waitForSock()
    {
        try
        {
            synchronized (syncObj)
            {
                waitDone = false;
                while (!waitDone)
                {
                    syncObj.wait(100);
                }
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    // Notifies that the socket has finished the action it was doing for the code to continue running
    public static synchronized void notifyDone()
    {
        synchronized (syncObj)
        {
            waitDone = true;
            syncObj.notify();
        }
    }

    // Sends a message to the server
    public static class SendToServer implements Runnable
    {

        @Override
        public void run()
        {
            if (!Client.is_initiated)
            {
                // The program should never enter this place
                System.out.println("Somehow, it happened");
            }
            Client.sent_text = Methods.encrypt(Client.sent_text);
            System.out.println("Sending something: " + Client.sent_text);
            try
            {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                String length = String.format("%08d", Client.sent_text.getBytes().length);
                out.print(length + Client.sent_text);
                out.flush();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            Client.notifyDone();
        }
    }

    // Receives a message from the server
    public static class ReceiveFromServer implements Runnable
    {
        @Override
        public void run()
        {
            if (!Client.is_initiated)
            {
                System.out.println("Client not initiated: Going to crash");
            }
            System.out.println("RECEIVING SOMETHING");
            try {
                char[] buff = new char[BUFFER_SIZE];
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int numOfBytes = input.read(buff, 0, MESSAGE_LENGTH);
                int length = Integer.parseInt(String.valueOf(buff, 0, MESSAGE_LENGTH));
                System.out.println(length);
                numOfBytes = input.read(buff, 0, Math.min(BUFFER_SIZE, length));
                Client.received_text = String.valueOf(buff, 0, numOfBytes);
                int lengthDiff = length - numOfBytes;
                while (lengthDiff > 0)
                {
                    numOfBytes = input.read(buff, 0, Math.min(lengthDiff, BUFFER_SIZE));
                    Client.received_text += String.valueOf(buff, 0, numOfBytes);
                    lengthDiff -= numOfBytes;
                }
                Client.received_text = Methods.decrypt(Client.received_text);
                System.out.println("DONE RECV" + Client.received_text.length());
                System.out.println(Client.received_text);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            Client.notifyDone();
        }
    }
}
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ClienteChatUDP {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            Scanner scanner = new Scanner(System.in);
            System.out.print("Ingrese su nombre: ");
            String nombreCliente = scanner.nextLine();
            //Envia el nombre al servidor
            enviarMensaje(socket, nombreCliente, serverAddress, SERVER_PORT);
            //Recibe mensajes del servidor
            Thread recibirMensajes = new Thread(() -> {
                try {
                    while (true) {
                        byte[] receiveBuffer = new byte[BUFFER_SIZE];
                        DatagramPacket packetRecibido = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packetRecibido);
                        String mensajeRecibido = new String(packetRecibido.getData(), 0, packetRecibido.getLength());
                        System.out.println(mensajeRecibido);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();
            //Envía mensajes al servidor
            while (true) {
                String message = scanner.nextLine();
                if (message.startsWith("!file ->")) {
                    // Enviar el comando "!file ->" seguido de la ruta del archivo
                    enviarMensaje(socket, message, serverAddress, SERVER_PORT);
                } else {
                    // Recoje la hora y fecha actual
                    LocalDateTime fechaHoraActual = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String fechaHoraFormateada = fechaHoraActual.format(formatter);
                    // Envía el mensaje con colorines
                    enviarMensaje(socket, "[" + fechaHoraFormateada + "] " + nombreCliente + ": " + message, serverAddress, SERVER_PORT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Envia un mensaje al servidor
    private static void enviarMensaje(DatagramSocket socket, String message, InetAddress address, int port) throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
    }
}

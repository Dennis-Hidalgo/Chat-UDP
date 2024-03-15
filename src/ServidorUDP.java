import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorUDP {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static List<InetAddress> direccionesConectadas = new ArrayList<>();
    private static List<Integer> puertosConectados = new ArrayList<>();

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            System.out.println("Servidor en espera");
            // Se queda a la espera de clientes
            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                handleClientMessage(socket, packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Maneja los mensajes de los usuarios
    private static void handleClientMessage(DatagramSocket socket, DatagramPacket packet) throws IOException {
        // Recoge la IP, el puerto y el mensaje
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        String message = new String(packet.getData(), 0, packet.getLength());

        // Verificar si el cliente ya está registrado
        boolean isClientKnown = direccionesConectadas.contains(address) && puertosConectados.contains(port);
        String[] parts = message.split(":");
        String nombreCliente = parts[0];
        if (!isClientKnown) {
            direccionesConectadas.add(address);
            puertosConectados.add(port);
            System.out.println("Nuevo cliente registrado: " + nombreCliente + " - " + address.toString() + ":" + port);
            // Informa al grupo de que el cliente ha entrado
            for (int i = 0; i < direccionesConectadas.size(); i++) {
                enviarMensaje(socket, "¡" + nombreCliente + " ha entrado al chat!", direccionesConectadas.get(i), puertosConectados.get(i));
            }
        } else {
            // Enviar el mensaje a todos los clientes conectados
            for (int i = 0; i < direccionesConectadas.size(); i++) {
                enviarMensaje(socket, message, direccionesConectadas.get(i), puertosConectados.get(i));
            }
        }
    }

    // Envía un mensaje a un cliente
    private static void enviarMensaje(DatagramSocket socket, String message, InetAddress address, int port) throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
    }
}

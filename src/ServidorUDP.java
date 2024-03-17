import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorUDP {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static List<InetAddress> direccionesConectadas = new ArrayList<>();

    private static List<String> nombreClientes = new ArrayList<>();
    private static List<Integer> puertosConectados = new ArrayList<>();
    private static final String RUTA = System.getProperty("user.home") + "/Desktop/users/";

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
            nombreClientes.add(nombreCliente);
            System.out.println("Nuevo cliente registrado: " + nombreCliente + " - " + address.toString() + ":" + port);
            // Informa al grupo de que el cliente ha entrado
            for (int i = 0; i < direccionesConectadas.size(); i++) {
                enviarMensaje(socket, "¡" + nombreCliente + " ha entrado al chat!", direccionesConectadas.get(i), puertosConectados.get(i));
            }

            // Crear carpeta para el nuevo cliente
            File carpetaCliente = new File(RUTA + nombreCliente);
            if (!carpetaCliente.exists()) {
                carpetaCliente.mkdir();
                System.out.println("Carpeta creada para el cliente: " + nombreCliente);
            }
        } else {
            // Si el mensaje es para enviar un archivo
            if (message.startsWith("!file ->")) {
                String filePath = message.substring("!file ->".length()).trim();
                File fileToSend = new File(filePath);
                if (fileToSend.exists() && fileToSend.isFile()) {
                    // Copiar el archivo a la carpeta repository
                    File repositoryFolder = new File(RUTA + "/repository");
                    if (!repositoryFolder.exists()) {
                        repositoryFolder.mkdirs();
                    }
                    File copiedFile = new File(repositoryFolder, fileToSend.getName());
                    copyFile(fileToSend, copiedFile);
                    // Enviar el archivo a cada carpeta de cliente conectado
                    for (int i = 0; i < direccionesConectadas.size(); i++) {
                        File clientFolder = new File(RUTA + nombreClientes.get(i));
                        System.out.println("direcciones: #" + nombreClientes + "#");
                        if (!clientFolder.exists()) {
                            clientFolder.mkdirs();
                        }
                        File destinationFile = new File(clientFolder, copiedFile.getName());
                        copyFile(copiedFile, destinationFile);
                        enviarMensaje(socket, "has recibido el fichero '" + copiedFile.getName() + "'.", direccionesConectadas.get(i), puertosConectados.get(i));
                    }
                } else {
                    enviarMensaje(socket, "El archivo especificado no existe o no es un archivo válido.", address, port);
                }
            } else {
                // Enviar el mensaje a todos los clientes conectados
                for (int i = 0; i < direccionesConectadas.size(); i++) {
                    enviarMensaje(socket, message, direccionesConectadas.get(i), puertosConectados.get(i));
                }
            }
        }
    }

    // Envía un mensaje a un cliente
    private static void enviarMensaje(DatagramSocket socket, String message, InetAddress address, int port) throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
    }

    // Copia un archivo a otro destino
    private static void copyFile(File source, File destination) throws IOException {
        try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}

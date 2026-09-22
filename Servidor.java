import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {
    // Puerto por el que se conectarán tus compañeros (puedes cambiarlo si deseas)
    private static final int PUERTO = 5000;

    // "Libreta" donde guardamos a los usuarios conectados: <NombreUsuario, Socket>
    // ConcurrentHashMap permite que varios hilos la lean/modifiquen de forma segura.
    public static ConcurrentHashMap<String, Socket> clientesConectados = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor iniciado");
        System.out.println("Esperando conexiones en el puerto " + PUERTO + "...");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                // El programa se queda "pausado" aquí hasta que un cliente se conecta
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nueva conexión entrante desde: " + socketCliente.getInetAddress().getHostAddress());

                // Creamos y arrancamos un hilo exclusivo para atender a este cliente
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                Thread hilo = new Thread(manejador);
                hilo.start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}

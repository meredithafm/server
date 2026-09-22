import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        // Cambia "localhost" por tu IP (ej. "192.168.1.96") cuando tus compañeros se vayan a conectar desde otras computadoras.
        String host = "localhost"; 
        int puerto = 5000;

        try (Socket socket = new Socket(host, puerto)) {
            
            // Hilo secundario: Se encarga de recibir e imprimir los mensajes del servidor en todo momento
            // Es el equivalente exacto a tu función "recibir_mensajes" en Python
            Thread hiloRecepcion = new Thread(() -> {
                try {
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    String mensaje;
                    while ((mensaje = entrada.readLine()) != null) {
                        System.out.println("\n" + mensaje);
                    }
                } catch (IOException e) {
                    System.out.println("Conexión con el servidor cerrada.");
                }
            });
            hiloRecepcion.start();

            // Hilo principal: Se encarga de leer lo que escribes en consola y enviarlo
            PrintWriter salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                String mensaje = scanner.nextLine();
                salida.println(mensaje);
            }

        } catch (IOException e) {
            System.out.println("No se pudo conectar al servidor: " + e.getMessage());
        }
    }
}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private String nombreUsuario;
    private BufferedReader entrada;
    private PrintWriter salida;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Canal para LEER lo que envía el cliente (UTF-8)
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            // Canal para ESCRIBIR respuestas hacia el cliente (UTF-8, con auto-flush activado)
            salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // PASO 1: Pedir y registrar el nombre de usuario
            salida.println("SERVIDOR: Bienvenido. Ingrese su nombre de usuario:");
            this.nombreUsuario = entrada.readLine();

            // Si se desconectó antes de mandar nombre
            if (this.nombreUsuario == null || this.nombreUsuario.trim().isEmpty()) {
                desconectar();
                return;
            }

            this.nombreUsuario = this.nombreUsuario.trim();
            ServidorChat.clientesConectados.put(this.nombreUsuario, this.socket);
            System.out.println("Usuario registrado exitosamente: [" + this.nombreUsuario + "]");
            salida.println("SERVIDOR: Registrado como '" + this.nombreUsuario + "'. Puedes enviar mensajes o usar /LISTA.");

            String lineaRecibida;
            // PASO 2: Leer continuamente los mensajes del cliente
            while ((lineaRecibida = entrada.readLine()) != null) {
                lineaRecibida = lineaRecibida.trim();

                // Caso A: Solicitar la lista de usuarios activos
                if (lineaRecibida.equalsIgnoreCase("/LISTA")) {
                    String usuarios = String.join(", ", ServidorChat.clientesConectados.keySet());
                    salida.println("SERVIDOR (Usuarios activos): " + usuarios);
                } 
                // Caso B: Formato de la pizarra -> /Destinatario } mensaje
                else if (lineaRecibida.startsWith("/")) {
                    int posCierre = lineaRecibida.indexOf('}');
                    if (posCierre != -1) {
                        String destinatario = lineaRecibida.substring(1, posCierre).trim();
                        String mensaje = lineaRecibida.substring(posCierre + 1).trim();
                        
                        enviarMensajePrivado(destinatario, mensaje);
                    } else {
                        salida.println("SERVIDOR (Error): Formato no válido. Debe ser: /Destinatario } mensaje");
                    }
                } 
                // Caso C: Texto normal sin formato correcto
                else {
                    salida.println("SERVIDOR: Comando no reconocido. Usa /LISTA o /Destinatario } mensaje");
                }
            }

        } catch (IOException e) {
            System.out.println("Conexión perdida con: " + (nombreUsuario != null ? nombreUsuario : "Cliente desconocido"));
        } finally {
            desconectar();
        }
    }

    // Función para buscar al destinatario en la "libreta" y enviarle el texto
    private void enviarMensajePrivado(String destinatario, String mensaje) {
        Socket socketDestino = ServidorChat.clientesConectados.get(destinatario);

        if (socketDestino != null && !socketDestino.isClosed()) {
            try {
                PrintWriter salidaDestino = new PrintWriter(
                    new OutputStreamWriter(socketDestino.getOutputStream(), StandardCharsets.UTF_8), true
                );
                // Envía el mensaje al destinatario indicando quién lo manda
                salidaDestino.println("[" + this.nombreUsuario + "]: " + mensaje);
                // Confirmación para el remitente
                salida.println("SERVIDOR: Mensaje entregado a " + destinatario);
            } catch (IOException e) {
                salida.println("SERVIDOR (Error): Fallo al enviar mensaje a " + destinatario);
            }
        } else {
            salida.println("SERVIDOR (Error): El usuario '" + destinatario + "' no se encuentra conectado.");
        }
    }

    // Limpia el registro cuando el usuario se desconecta
    private void desconectar() {
        if (nombreUsuario != null) {
            ServidorChat.clientesConectados.remove(nombreUsuario);
            System.out.println("Usuario desconectado: [" + nombreUsuario + "]");
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

import socket
import threading

def recibir_mensajes(cliente_socket):
    while True:
        try:
            mensaje = cliente_socket.recv(1024).decode('utf-8')
            if mensaje:
                print(f"\n{mensaje}")
            else:
                break
        except Exception as e:
            print("Conexión con el servidor cerrada.")
            cliente_socket.close()
            break

def iniciar_cliente():
    host = '192.168.1.96' 
    puerto = 5000

    cliente = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    cliente.connect((host, puerto))

    hilo_recepcion = threading.Thread(target=recibir_mensajes, args=(cliente,))
    hilo_recepcion.start()

    while True:
        mensaje = input()
        cliente.send(f"{mensaje}\n".encode('utf-8')) 

if __name__ == "__main__":
    iniciar_cliente()
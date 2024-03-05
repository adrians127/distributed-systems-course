import socket

serverIP = "127.0.0.1"
serverPort = 9008
msg = (300).to_bytes(4, byteorder='little')

print('PYTHON UDP CLIENT')
print('Sending:', int.from_bytes(msg, byteorder='little'))
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.sendto(msg, (serverIP, serverPort))

data, addr = client.recvfrom(1024)
print('Received response:', int.from_bytes(data, byteorder='little'))
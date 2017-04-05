import gps
import os
import time
import requests
import json
import urllib2
import sys
import thread
import signal

from classes import Evento
from classes import Servidor
from classes import Acelerometro
from classes import GPS
from classes import Gateway

if __name__ == "__main__":
    print("Street Analyzer!")


def monitorar(a,b):
    while True:
        gps.fazerLeitura()
        #gps.imprimir()
        time.sleep(1)
    #sys.exit()

# Listen on port 2947 (gpsd) of localhost

def signal_handler(signal, frame):
        print('Saindo!')
        sys.exit(0)
#signal.signal(signal.SIGINT, signal_handler)
#print('Press Ctrl+C para sair')

#verifica se ja nao esta rodando

import socket
import sys
 
HOST = ''   # Symbolic name, meaning all available interfaces
PORT = 8888 # Arbitrary non-privileged port
 
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print 'Socket created'
 
#Bind socket to local host and port
try:
    s.bind((HOST, PORT))
except socket.error as msg:
    print 'Sistema ja esta rodando'
    sys.exit()
     
print 'Sistema online'
 
#Start listening on socket
s.listen(10)
print 'Socket now listening'




session = gps.gps("localhost", "2947")
session.stream(gps.WATCH_ENABLE | gps.WATCH_NEWSTYLE)
#os.system('cls||clear')

#server = Servidor("http://api.carriots.com", 'api.carriots.com', '8f49f29c268e6a267ab6c52fc160a02971895716add28b2ea602aad09c6db65f', 'davidsonbhz@gmail.com', 'galileo@davidsonbhz.davidsonbhz')

#acel = Acelerometro('virtual')
acel = Acelerometro('hw')
gps = GPS()
gw = Gateway()

gw.check(1);

#sys.exit()

try:
    thread.start_new_thread(monitorar, (True,True))

except Exception, err:
    print "Error: unable to start thread"
    print Exception, err
    sys.exit()

while True:
    #if(acel.fazerLeitura()>0):
        #print "Impacto!"
        #acel.imprimir()
        #gps.imprimir()
    acel.fazerLeitura()
    #print gw.leituras[2]
    gw.updateUDP(gps, acel)
    #time.sleep(0.05)
    pass


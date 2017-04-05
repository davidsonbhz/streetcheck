import gps
import json
from random import randrange
import math
import socket
import smbus
import math
import smbus
import math
import timeit



class Gateway:
    gw = ""
    online = False
    sockTCP = None
    sockUDP = None
    leituras = [0 for i in range(1000)]
    index = 0
    indexn = 0
    tempo = 0
    ftime = 0
    ftempo = False
    fclock = False
    arquivo = open("/home/pi/dados.txt", "a")
    
    def __init__(self):
        print "Iniciando gateway"
        #arquivo = open("/home/pi/dados.txt", "a")
    
    def gravalista(self, info):
        #processa a lista de dados e grava em arquivo
        self.indexn = 0
        #while(True) :
        #    k = self.leituras[index]
        #print "Gravando: %s" % (info)
        try:
            #self.arquivo.write(info)   
	    b = 1
        except:
            print "Erro ao gravar arquivo"
            
    def envialista(self):
        #envia a lista para o gateway
        index = 0
        while(True) :
            k = self.leituras[index]
        
    
    # verifica se o gateway esta online
    def check(self, tipo):
        f = open("/tmp/gateway.txt","r") 
        gw = f.read().strip() 
        #print "Gateway: %s" % (gw)
        try:
            self.sockTCP = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.sockUDP = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                
            dest = (gw, 10101)
            dest2 = (gw, 10102)
            self.sockTCP.connect(dest)
            self.sockUDP.connect(dest2)
            
            self.online = True
        except:
            #print "Gateway nao disponivel no momento"
            self.online = False

    def clock(self):
        if not self.fclock:
            self.fclock = True
            self.ftime = datetime.now()
        else:
            datetime.now()
            t = datetime.now() - self.ftime
            print "t=%f " % (t)
        

    def updateTCP(self, gps, acel):
        if not self.online:
            self.check(1)
            
        if not self.ftempo:
            self.tempo = timeit.default_timer()
            self.ftempo = True
        
        #guardas os eixos xyz
        data = [(acel.ax, acel.ay, acel.az, gps.latitude, gps.longitude, gps.velocidade, 0, 0)]
        #print "Index=%d" % (self.index)
        self.leituras[self.index] = data
        self.index = self.index + 1
        if self.index >= len(self.leituras):
            self.index = 0
            self.ftempo = False
            # code you want to evaluate
            elapsed = timeit.default_timer() - self.tempo

            print "ciclo em %d segundos" % elapsed
        #print {'ax:':acel.ax, 'ay:':acel.ay, 'az:':acel.az, 'lat:':gps.latitude, 'lon':gps.longitude, 'vel:':gps.velocidade}


      
    def monitorar(delay):
        print "monitorando\n"

    def updateUDP(self, gps, acel):
        
        if self.online:
            try:
                #print "Enviando por UDP"
                d = "data "+ gps.getinfo() +" "+acel.getinfo()
                #return str(self.latitude) + " " + str(self.longitude)+ " " + str(self.velocidade)
                
                self.gravalista(d)
                self.sockUDP.send(d)
                #print "sending: " + d
                
            except Exception as e:
                self.online = False
                print str(e) 
        else:
            self.check(2)
            
        if not self.ftempo:
            self.tempo = timeit.default_timer()
            self.ftempo = True
        
        #guardas os eixos xyz
        data = [(acel.ax, acel.ay, acel.az, gps.latitude, gps.longitude, gps.velocidade, 0, 0)]
        #print "Index=%d" % (self.index)
        self.leituras[self.index] = data
        self.index = self.index + 1
        if self.index >= len(self.leituras):
            self.index = 0
            self.ftempo = False
            # code you want to evaluate
            elapsed = timeit.default_timer() - self.tempo

            print "ciclo em %d segundos" % elapsed
        #print {'ax:':acel.ax, 'ay:':acel.ay, 'az:':acel.az, 'lat:':gps.latitude, 'lon':gps.longitude, 'vel:':gps.velocidade}

    def monitorar(delay):
        print "monitorando\n"


##############################################    
class GPS:

    session = ''
    latitude = 0
    longitude = 0
    altitude = 0
    velocidade = 0
    
    def __init__(self):

        # Listen on port 2947 (gpsd) of localhost
        print "Inciando servico de gps\n"
        self.session = gps.gps("localhost", "2947")
        self.session.stream(gps.WATCH_ENABLE | gps.WATCH_NEWSTYLE)
        
    def getinfo(self):
        return str(self.latitude) + " " + str(self.longitude)+ " " + str(self.velocidade)

    def imprimir(self):
        print "Latitude %.10f Longitude %.10f " % (self.latitude, self.longitude)
    

    def monitorar(self, atraso):
        while 1>0 :
            self.fazerLeitura()
            time.sleep(3)
        

    def fazerLeitura(self):
        #print "Fazendo leitura do gps"
        try:
            report = self.session.next()

            if report['class'] == 'TPV':
                if hasattr(report, 'lat'):
                    self.latitude = report.lat
                    self.longitude = report.lon
                if hasattr(report, 'speed'):
                    self.velocidade = report.speed * gps.MPS_TO_KPH
                    print "Speed: %f " % (self.velocidade)
                #os.system('cls||clear')
            
        except KeyError:
            pass
        except KeyboardInterrupt:
            quit()
        except StopIteration:
            session = None
            print "GPSD has terminated"
        
        


class Acelerometro:
    
    ax = ay = az =0
    arx = ary = arz = 0
    gx = gy = gz = 0
    grx = gry = grz = 0
    impacto = 0
    modo = 'virtual'
    tipotrecho = '?'
    power_mgmt_1 = 0x6b
    power_mgmt_2 = 0x6c
    bus = smbus.SMBus(1) # or bus = smbus.SMBus(1) for Revision 2 boards
    address = 0x68       # This is the address value read via the i2cdetect command
    # Power management registers
    power_mgmt_1 = 0x6b
    power_mgmt_2 = 0x6c

    
    def __init__(self, tipo='virtual'):
        self.modo = tipo

    def imprimir(self):
        print "(%d,%d,%d) %d " % (self.x, self.y, self.z, self.impacto)
    
    def getinfo(self):
        #return str(self.x) + " " + str(self.y) + " " + str(self.z) + " " + str(self.impacto)
        return str(self.ax) + " " + str(self.ay) + " " + str(self.az)

    def fazerLeitura(self):
        if self.modo == 'virtual':
            self.leituraAleatoria()
        else:
            self.fazerLeituraGPIO()
            
        media = (self.ax+self.ay+self.az)/3
        difquad = (self.ax-media)**2 + (self.ay-media)**2 + (self.az-media)**2
        desvio = math.sqrt(difquad/3)
        self.impacto = desvio
        
        return self.impacto
    
    def fazerLeituraGPIO(self):
        
        self.x = randrange(0, 9)

        bus = smbus.SMBus(1) # or bus = smbus.SMBus(1) for Revision 2 boards
        address = 0x68       # This is the address value read via the i2cdetect command

        # Now wake the 6050 up as it starts in sleep mode
        bus.write_byte_data(self.address, self.power_mgmt_1, 0)

        #print "gyro data"
        #print "---------"

        self.grx = self.read_word_2c(0x43)
        self.gry = self.read_word_2c(0x45)
        self.grz = self.read_word_2c(0x47)
        
        self.gx = (self.grx / 131)
        self.gy = (self.grx / 131)
        self.gz = (self.grx / 131)
        

        #print "gyro_xout: ", self.grx, " scaled: ", self.gx
        #print "gyro_yout: ", self.gry, " scaled: ", self.gy
        #print "gyro_zout: ", self.grz, " scaled: ", self.gz

        #print
        #print "accelerometer data"
        #print "------------------"

        accel_xout = self.read_word_2c(0x3b)
        accel_yout = self.read_word_2c(0x3d)
        accel_zout = self.read_word_2c(0x3f)

        accel_xout_scaled = accel_xout / 16384.0
        accel_yout_scaled = accel_yout / 16384.0
        accel_zout_scaled = accel_zout / 16384.0
        
        self.arx = accel_xout
        self.ary = accel_yout
        self.arz = accel_zout
        
        self.ax = accel_xout_scaled;
        self.ay = accel_yout_scaled;
        self.az = accel_zout_scaled;
        

        #print "accel_xout: ", accel_xout, " scaled: ", accel_xout_scaled
        #print "accel_yout: ", accel_yout, " scaled: ", accel_yout_scaled
        #print "accel_zout: ", accel_zout, " scaled: ", accel_zout_scaled

        #print "x rotation: " , self.get_x_rotation(accel_xout_scaled, accel_yout_scaled, accel_zout_scaled)
        #print "y rotation: " , self.get_y_rotation(accel_xout_scaled, accel_yout_scaled, accel_zout_scaled)

        temp = self.read_word_2c(0x41)
        temp = temp/340.00+36.53
        #print "temp: " , temp;    

        
    def read_byte(self, adr):
        return bus.read_byte_data(self.address, adr)

    def read_word(self, adr):
        high = self.bus.read_byte_data(self.address, adr)
        low = self.bus.read_byte_data(self.address, adr+1)
        val = (high << 8) + low
        return val

    def read_word_2c(self, adr):
        val = self.read_word(adr)
        if (val >= 0x8000):
            return -((65535 - val) + 1)
        else:
            return val

    def dist(self,a,b):
        return math.sqrt((a*a)+(b*b))

    def get_y_rotation(self,x,y,z):
        radians = math.atan2(x, self.dist(y,z))
        return -math.degrees(radians)

    def get_x_rotation(self,x,y,z):
        radians = math.atan2(y, self.dist(x,z))
        return math.degrees(radians)

    def leituraAleatoria(self):
        trecho = 'bom'
        qmax = 20
        fx = 6
        low = 3
        hi = 5
        try:
            f = open("/tmp/tipotrecho.txt","r") 
            trecho = f.read().strip() 
            if trecho != self.tipotrecho:
                self.tipotrecho = trecho
                print trecho
        except:
            trecho = 'bom'
        
        if trecho=='bom':
            qmax = 10
            fx = 7 
            
        elif trecho=='regular':
            qmax = 30
            fx = 5
        elif trecho == 'pessimo':
            qmax = 120
            fx = 4
        
        
        q = randrange(0, 10)
        
        if q > fx:            
            self.ax = randrange(0, qmax)
            self.ay = randrange(0, qmax)
            self.az = randrange(0, qmax)
        else:
            self.ax = randrange(low, hi)
            self.ay = randrange(low, hi)
            self.az = randrange(low, hi)
    
        #print "Random: %d %d %d " % (self.ax, self.ay, self.az)




class Evento:
            
    latitude = .0
    longitude = .0
    luminosidade = 0
    xaxis = 0
    yaxis = 0
    zaxis = 0
    velocidade = 0
    tempo = ''
    impacto = 0
            
    def __init__(self, lat, lon):
        #print "construtor %d %d " % (lat, lon)
        self.latitude = lat
        self.longitude = lon
        
    def setX(self, x):
        self.xaxis = x
        
    def setY(self, y):
        self.yaxis = y
    
    def setZ(self, z):
        self.zaxis = z
    
    def setLum(self, v):
        self.luninosidade = v
    
    def setSpeed(self, v):
        self.velocidade = v
    
    def setTime(self, v):
        self.tempo = v
    
    def imprime(self):
        print "Latitude %.10f Longitude %.10f : Impacto (%d,%d,%d) : Lum %d : Velocidade %d : Tempo %s" % (self.latitude, self.longitude, self.xaxis, self.yaxis, self.zaxis, self.luminosidade, self.velocidade, self.tempo)
    

class Servidor:
    porta = '80'
    chave = ''
    url = ''
    usuario = ''
    device = ''
    host = ''
    
    def __init__(self, url, host, chave, usuario, dispositivo):
        #print "construtor %d %d " % (lat, lon)
        self.chave = chave
        self.url = url
        self.usuario = usuario
        self.device = dispositivo
        self.host = host
        
    def enviaDados(self, evento):
        
        postdata = {
        "protocol": "v2",
        "checksum": "", 
        "device": self.device,
        "at": "now",
        "data": {
            "Carriots": {
                "velocidade": evento.velocidade,
                "lat": evento.latitude,
                "lon": evento.longitude,
                "x": evento.xaxis,
                "y": evento.yaxis,
                "z": evento.zaxis,
                "impacto": evento.impacto,
                "persist": "true"
            }
            }
        }

        
        with open('/tmp/payload.txt', 'w') as outfile:
            json.dump(postdata, outfile, sort_keys=True, indent=4,
                      ensure_ascii=False)

        
        


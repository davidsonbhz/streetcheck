package iot.ufop.br.streetcheck;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by davidson on 02/03/17.
 */

public class ServerUDP extends Thread {

    public static int pacotes = 0;
    public static long ultimopacote = 0;
    DatagramSocket srv;
    private boolean rodando;
    private DeviceData devdata;
    private Funcao callback;
    private boolean hasdata;

    public ServerUDP(DeviceData devd) throws Exception {
        this.srv = new DatagramSocket(10102);
        rodando = true;
        devdata = devd;

        //this.callback = fcall;
    }

    public DeviceData getData() {
        return devdata;
    }

    @Override
    public void run() {


        String str;
        byte[] lmessage = new byte[100];
        DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);
        while(rodando) {

            try {
                srv.receive(packet);

                //Log.e("server", "Recebendo pacote UDP");
                str = new String(lmessage, 0, packet.getLength());
                ultimopacote = System.currentTimeMillis();
                devdata.readLine(str);
                hasdata = true;

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

        }


    }

    public void parar() {
        this.rodando = false;
    }

    public boolean isHasdata() {
        return hasdata;
    }

    public void setHasdata(boolean hasdata) {
        this.hasdata = hasdata;
    }
}

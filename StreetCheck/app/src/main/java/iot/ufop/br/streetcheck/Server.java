package iot.ufop.br.streetcheck;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by davidson on 02/03/17.
 */

public class Server extends Thread {

    ServerSocket srv;
    private boolean rodando;
    private DeviceData devdata;
    private Funcao callback;
    private boolean hasdata;

    public Server(DeviceData devd) throws Exception {
        this.srv = new ServerSocket(10101);
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

        while(rodando) {

            try {

                Socket s = srv.accept();
                Log.e("server", "Iniciando thread de servidor");

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                PrintStream out = new PrintStream(s.getOutputStream(), true);
                System.out.println("ip: " + s.getInetAddress());

                while((str = in.readLine())!=null) {
                    System.out.println("readline: " + str);

                    if(str.startsWith("quit")) {
                        s.close();
                        break;
                    }
                    //devdata.readLine(str);
                    hasdata = true;
                    //if(callback!=null) callback.atualizaTela(devdata);
                    out.println("COM: "+str);
                    //out.write(("COM: "+str).getBytes());

                }

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

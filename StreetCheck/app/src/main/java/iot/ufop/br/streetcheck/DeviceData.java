package iot.ufop.br.streetcheck;

import android.os.Environment;
import android.util.Log;

import com.androidplot.xy.XYSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Observable;
import java.util.Observer;

import static android.R.attr.data;
import static android.content.Context.MODE_APPEND;
import static android.os.ParcelFileDescriptor.MODE_WORLD_READABLE;

/**
 * Created by davidson on 03/03/17.
 */

public class DeviceData implements XYSeries {
    public static float currentSpeed;
    private double x, y, z;
    private double k, kmax, velocidade;
    public static double latitude, longitude;
    private Number[] leiturasK = new Number[100];
    private Number[] leiturasX = new Number[100];
    private Number[] leiturasY = new Number[100];
    private Number[] leiturasZ = new Number[100];
    private Number[] leiturasS = new Number[100];

    private FileOutputStream fout;
    private OutputStreamWriter writer;
    private int index, eixo;
    public String tag = "X";

    private MyObservable notifier;

    {
        notifier = new MyObservable();
    }

    public DeviceData() {
        eixo = 4;  //analise do impacto
        leiturasK = new Number[100];
        leiturasX = new Number[100];
        leiturasY = new Number[100];
        leiturasZ = new Number[100];
        leiturasS = new Number[100];

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/IOT");
        dir.mkdirs();
        File file = new File(dir+"/dados"+System.currentTimeMillis()+".txt");


        try {
            fout  = new FileOutputStream(file, true);

            writer = new OutputStreamWriter(fout);

            System.out.println("Gravando no arquivo "+file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }



    }

    /**
     * eixo é 1 para x, 2 para y, 3 para z, 4 (default) para desvio padrao
     * @param d
     * @param eixo
     */
    public DeviceData(DeviceData d, int eixo) {
        this.eixo = eixo; //um dos eixos:
        this.leiturasK = d.leiturasK;
        this.leiturasX = d.leiturasX;
        this.leiturasY = d.leiturasY;
        this.leiturasZ = d.leiturasZ;
        this.leiturasS = d.leiturasS;
    }


    /**
     * Le a linha de dados vinda do socket
     * @param l =data lat lon x y z k
     */
    public void readLine(String l) {
        if(!l.startsWith("data")) {
            return;
        }
        try {
            String a[] = l.split(" ");
            //System.out.println("Decoding..." + l);

            //latitude = Double.valueOf(a[1]);
            //longitude = Double.valueOf(a[2]);


            velocidade = DeviceData.currentSpeed;// Double.valueOf(a[3]);

            x = Double.valueOf(a[4])*100;
            y = Double.valueOf(a[5])*100;
            z = Double.valueOf(a[6])*100;
            //k = Double.valueOf(a[6]);

            double media = (x+y+z)/3;
            double difquad = Math.pow(x-media, 2) + Math.pow(y-media, 2) + Math.pow((z-media),2);
            double desvio = Math.sqrt(difquad/3);
            k = desvio;

            if (k > kmax) {
                kmax = k;
            }
            synchronized (this) {
                leiturasK[index] = k;
                leiturasX[index] = x;
                leiturasY[index] = y;
                leiturasS[index] = velocidade;
                leiturasZ[index++] = z;
            }
            if(index>leiturasK.length-1) {
                index=0;
            }
            notifier.notifyObservers();

            if(fout!=null) {
                //if(velocidade>0) writer.append(l+" "+tag+ " " + DeviceData.currentSpeed + " " + String.valueOf(System.currentTimeMillis())+ "\n");
                String ss = l+" "+tag+ " " + String.valueOf(velocidade * 0.4).replace(".", ",") + " " + String.valueOf(System.currentTimeMillis())+ " " + latitude + " " + longitude + " enddata\n";

                if(velocidade>0) writer.append(ss);
            }


        } catch(Exception ee) {
            ee.printStackTrace();
            Log.e("devicedata", ee.getMessage());
        }

    }

    public void set(Double v) {
        Number vetor[];
        switch (eixo) {
            case 1:
                vetor = leiturasX;
                break;
            case 2:
                vetor = leiturasY;
                break;
            case 3:
                vetor = leiturasZ;
                break;
            case 4:
                vetor = leiturasS;
                break;
            default:
                vetor = leiturasK;
                break;
        }

        vetor[index++] = v;
        if(index>vetor.length-1) {
            index=0;
        }

    }


    public void set(Float v) {
        Number vetor[];
        switch (eixo) {
            case 1:
                vetor = leiturasX;
                break;
            case 2:
                vetor = leiturasY;
                break;
            case 3:
                vetor = leiturasZ;
                break;
            case 4:
                vetor = leiturasS;
                break;
            default:
                vetor = leiturasK;
                break;
        }

        vetor[index++] = v;
        if(index>vetor.length-1) {
            index=0;
        }

    }


    public String getAccelString() {
        return x+ "," + y + "," + z + " : "+k;
    }

    public double getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public double getKmax() {
        return kmax;
    }

    public void setKmax(double kmax) {
        this.kmax = kmax;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Number[] getLeiturasK() {
        return leiturasK;
    }

    public Number[] getLeiturasX() {
        return leiturasX;
    }

    public Number[] getLeiturasY() {
        return leiturasY;
    }

    public Number[] getLeiturasZ() {
        return leiturasZ;
    }

    public int getIndex() {
        return index;
    }

    public double getVelocidade() {
        return velocidade;
    }

    // encapsulates management of the observers watching this datasource for update events:
    class MyObservable extends Observable {
        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }

    public void addObserver(Observer observer) {
        notifier.addObserver(observer);
    }

    @Override
    public int size() {
        return leiturasK.length;
    }

    @Override
    public Number getX(int index) {
        if(index>=leiturasK.length) {
            throw new IllegalArgumentException();
        }
        return index;
    }

    @Override
    public Number getY(int index) {
        //System.out.println(index+" - "+y);
        switch (eixo) {
            case 1:
                return leiturasX[index];
            case 2:
                return leiturasY[index];
            case 3:
                return leiturasZ[index];
            case 4:
                return leiturasS[index];
            default:
                return leiturasK[index];
        }
    }

    @Override
    public String getTitle() {
        switch (eixo) {
            case 1:
                return "e.X";
            case 2:
                return "e.Y";
            case 3:
                return "e.Z";
            case 4:
                return "SP";
            default:
                return "DP";
        }
    }
}

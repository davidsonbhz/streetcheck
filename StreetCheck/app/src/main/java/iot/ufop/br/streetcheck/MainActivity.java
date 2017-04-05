package iot.ufop.br.streetcheck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.PlotListener;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IBaseGpsListener {

    TextView tlatitude, tlongitude, tacelerometro, tvelocidade, timpactomax;
    DeviceData devdata = new DeviceData();
    DeviceData devdataX, devdataY, devdataZ, devdataS;
    Handler uiHandler;
    Server server;
    ServerUDP server2;
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    double tempo;
    MediaPlayer alarme1;


    private XYPlot dynamicPlot;
    private MyPlotUpdater plotUpdater;
    private Thread myThread;


    private void preparaGrafico() {
        dynamicPlot = (XYPlot) findViewById(R.id.dynamicXYPlot);

        plotUpdater = new MyPlotUpdater(dynamicPlot);

        // only display whole numbers in domain labels
        dynamicPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("0"));

        // getInstance and position datasets:
        //SampleDynamicSeries serieK = new SampleDynamicSeries(devdata, 0, "Sine 1");

        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 200, 0), null, null, null);

        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(1);
        dynamicPlot.addSeries(devdata, formatter1);
        dynamicPlot.addSeries(devdataX, new LineAndPointFormatter(Color.BLUE, null, null, null));
        dynamicPlot.addSeries(devdataY, new LineAndPointFormatter(Color.RED, null, null, null));
        dynamicPlot.addSeries(devdataZ, new LineAndPointFormatter(Color.YELLOW, null, null, null));
        dynamicPlot.addSeries(devdataS, new LineAndPointFormatter(Color.GREEN, null, null, null));

        LineAndPointFormatter formatter2 =
                new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
        formatter2.getLinePaint().setStrokeWidth(10);
        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);


        // hook up the plotUpdater to the data model:
        devdata.addObserver(plotUpdater);

        // thin out domain tick labels so they dont overlap each other:
        dynamicPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(5);

        dynamicPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        dynamicPlot.setRangeStepValue(10);

        dynamicPlot.getGraph().getLineLabelStyle(
                XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("###.#"));

        // uncomment this line to freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[]{PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraph().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraph().getRangeGridLinePaint().setPathEffect(dashFx);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devdataX = new DeviceData(devdata, 1);
        devdataY = new DeviceData(devdata, 2);
        devdataZ = new DeviceData(devdata, 3);
        devdataS = new DeviceData(devdata, 4);

        alarme1 = MediaPlayer.create(this, R.raw.alert2);

        uiHandler = new Handler(Looper.getMainLooper());

        tlatitude = (TextView) findViewById(R.id.latitude);
        tlongitude = (TextView) findViewById(R.id.longitude);
        tacelerometro = (TextView) findViewById(R.id.acelerometro);
        tvelocidade = (TextView) findViewById(R.id.velocidade);
        timpactomax = (TextView) findViewById(R.id.impactomax);

        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);

        preparaGrafico();

        TimerTask tk = new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        atualizaTela();
                    }
                });


            }
        };
        Timer t = new Timer();
        t.schedule(tk, 100, 500);

        TimerTask tpac = new TimerTask() {
            @Override
            public void run() {
                if(ServerUDP.ultimopacote == 0 || (System.currentTimeMillis() - ServerUDP.ultimopacote)>10000) {
                    alarme1.start();
                }
            }
        } ;

        Timer t2 = new Timer();
        t.schedule(tpac, 100, 10000);


        TextView text = (TextView) findViewById(R.id.ipdevice);
        text.setText("IP: " + ipAddress);

        Button btbom = (Button) findViewById(R.id.btbom);
        Button btregular = (Button) findViewById(R.id.btruim);
        Button btpessimo = (Button) findViewById(R.id.btpessimo);
        Button btnaosei = (Button) findViewById(R.id.btnaosei);


        btbom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devdata.tag = "BOM";
                //System.out.println("CLASSIFICACAO: "+devdata.tag);
            }
        });
        btpessimo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devdata.tag = "RUIM";
                //System.out.println("CLASSIFICACAO: "+devdata.tag);
            }
        });
        btregular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devdata.tag = "REGULAR";
                //System.out.println("CLASSIFICACAO: "+devdata.tag);
            }
        });

        btnaosei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devdata.tag = "X";
                //System.out.println("CLASSIFICACAO: "+devdata.tag);
            }
        });


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            server = new Server(devdata);
            server2 = new ServerUDP(devdata);

            new Thread(server).start();
            new Thread(server2).start();


        } catch (Exception e) {
            text.setText("Erro: " + e.getMessage());
        }



        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException se) {}
        this.updateSpeed(null);

    }

    public void atualizaTela() {


        tacelerometro.setText(devdata.getAccelString());
        //timpacto.setText(String.valueOf(devdata.getK()));
        //timpacto.setText(String.valueOf(devdata.getVelocidade()));
        timpactomax.setText(String.valueOf(devdata.getKmax()));
        tvelocidade.setText(String.valueOf(DeviceData.currentSpeed * 0.4));
        //if(server.isHasdata()) {

        if(devdata.getLatitude()!=0) {
            //caso nao se receba os dados do gps nao atualiza

            tlatitude.setText(String.valueOf(devdata.getLatitude()));
            tlongitude.setText(String.valueOf(devdata.getLongitude()));

        }

        tempo++;
        //}


        //Log.e("main", "lat: "+devdata.getLatitude()+" - Lon: "+ devdata.getLongitude());

    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("CHANGE LOCATION!!! " +location.getSpeed());
        DeviceData.currentSpeed = location.getSpeed() * 10;
        DeviceData.latitude = location.getLatitude();
        DeviceData.longitude = location.getLongitude();
        //devdataS.set(location.getSpeed()*10);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

        if(location != null)
        {
            location.setUseMetricunits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }

        //Formatter fmt = new Formatter();

        //fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = String.valueOf(nCurrentSpeed); //   fmt.toString();
        //strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "miles/hour";
        if(this.useMetricUnits())
        {
            strUnits = "meters/second";
        }

        //TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.t);
        //timpacto.setText(strCurrentSpeed + " " + strUnits);
        tvelocidade.setText(strCurrentSpeed);
        //txtCurrentSpeed.setText(strCurrentSpeed + " " + strUnits);
    }

    private boolean useMetricUnits() {
        // TODO Auto-generated method stub
        return true;
        //CheckBox chkUseMetricUnits = (CheckBox) this.findViewById(R.id.chkMetricUnits);
        //return chkUseMetricUnits.isChecked();
    }

    // redraws a plot whenever an update is received:
    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }

}

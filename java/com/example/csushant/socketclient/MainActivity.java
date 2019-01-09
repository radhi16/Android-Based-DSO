package com.example.csushant.socketclient;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends Activity {

    private GraphicalView mChart;

    private XYSeries visitsSeries ;
    private XYMultipleSeriesDataset dataset;

    private XYSeriesRenderer visitsRenderer;
    private XYMultipleSeriesRenderer multiRenderer;
    //private LinearLayout layout;
    LineAndPointFormatter series1Format;
    SimpleXYSeries series1;
    //boolean firstTime = true;
    //int frameCount =0;
    //long startTime = 0;

    //PlotMyData plotter;
    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear;
    //private XYPlot dynamicPlot;
    //int count =0;
    //private Thread myThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        layout = (LinearLayout) findViewById(R.id.chart_container);

        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        textResponse = (TextView)findViewById(R.id.response);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});
        // Setting up chart
        setupChart();
    }

    private void setupChart(){

        // Creating an  XYSeries for Visits
        visitsSeries = new XYSeries("Channel 1 Data");

        // Creating a dataset to hold each series
        dataset = new XYMultipleSeriesDataset();
        // Adding Visits Series to the dataset
        dataset.addSeries(visitsSeries);

        // Creating XYSeriesRenderer to customize visitsSeries
        visitsRenderer = new XYSeriesRenderer();
        visitsRenderer.setColor(Color.RED);
        visitsRenderer.setPointStyle(PointStyle.CIRCLE);
        visitsRenderer.setFillPoints(true);
        visitsRenderer.setLineWidth(4);
        visitsRenderer.setDisplayChartValues(true);

        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        multiRenderer = new XYMultipleSeriesRenderer();

        multiRenderer.setChartTitle("Digital Oscilloscope");
        multiRenderer.setXTitle("Seconds");
        multiRenderer.setYTitle("Amplitude");
        multiRenderer.setZoomButtonsVisible(true);

        //multiRenderer.setXAxisMin(0);
        //multiRenderer.setXAxisMax(10);

        multiRenderer.setYAxisMin(-128);
        multiRenderer.setYAxisMax(127);

        //multiRenderer.setBarSpacing(2);

        // Adding visitsRenderer to multipleRenderer
        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
        // should be same
        multiRenderer.addSeriesRenderer(visitsRenderer);

        // Getting a reference to LinearLayout of the MainActivity Layout
        LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_container);

        mChart = (GraphicalView) ChartFactory.getLineChartView(getBaseContext(), dataset, multiRenderer);

        // Adding the Line Chart to the LinearLayout
        chartContainer.addView(mChart);

    }


    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    // Start plotting chart
                    MyClientTask myClientTask = new MyClientTask(
                            editTextAddress.getText().toString(),
                            Integer.parseInt(editTextPort.getText().toString()));

                    myClientTask.execute();
                }};

    public class MyClientTask extends AsyncTask<Void, String, Void> {

        //private Thread myThread;
        String dstAddress;
        int dstPort;
        String response = "";
        //byte[] responseByte = new byte[1024];
        //int responseSize;

        //int responseInt;

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
               // if(firstTime){
                socket = new Socket(dstAddress, dstPort);

            //    ByteArrayOutputStream byteArrayOutputStream =
            //            new ByteArrayOutputStream(1024);
            //    byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                //}

    /*
     * notice:
     * inputStream.read() will block if no data return
     */
                //while(frameCount<6) {
                int i = 0;
                while (((bytesRead = inputStream.read()) != -1) && i < 1000){
                    String [] values = new String[2];
                    //byteArrayOutputStream.write(buffer, 0, bytesRead);
                    values[0] = Integer.toString(i);
                    values[1] = Integer.toString(bytesRead);
                    if(bytesRead != 13 || bytesRead != 10) {
                        publishProgress(values);
                        Thread.sleep(10);
                        i++;
                    }

                    //byteArrayOutputStream.write(buffer, 0, bytesRead);
                    //response += byteArrayOutputStream.toString("UTF-8");
                }
            //    responseByte = byteArrayOutputStream.toByteArray();
            //    publishProgress();
            //    frameCount++;
            //    }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;

        }

        // Plotting generated data in the graph
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            visitsSeries.add(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
            mChart.repaint();
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);



        }
    }






}

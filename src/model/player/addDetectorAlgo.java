package model.player;

import javafx.scene.canvas.Canvas;
import model.algorithems.TimeSeriesAnomalyDetector;
import model.Util.AnomalyReport;
import model.Util.CorrelatedFeatures;
import model.Util.TimeSeries;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class addDetectorAlgo implements TimeSeriesAnomalyDetector {

    TimeSeriesAnomalyDetector newAlgo;
    public addDetectorAlgo(String path,String name) {
        try {
            URL[] url = new URL[1];
            url[0]=new URL("file://" + path);
            URLClassLoader classLoader = new URLClassLoader(url);
            Class<?> classInstance = classLoader.loadClass(name);
            newAlgo=(TimeSeriesAnomalyDetector)classInstance.newInstance();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void learnNormal(TimeSeries ts) {
        newAlgo.learnNormal(ts);
    }

    @Override
    public List<AnomalyReport> detect(TimeSeries ts) {
        return newAlgo.detect(ts);
    }


    public Runnable draw(Canvas canvas, CorrelatedFeatures correlatedFeatures, int timeStamp) {
        return null;
    }
}

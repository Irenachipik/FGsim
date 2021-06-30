package model;

import model.algorithems.*;
import model.Util.AnomalyReport;
import model.Util.CorrelatedFeatures;
import model.Util.TimeSeries;
import model.player.Player;
import model.settings.Property;
import model.settings.Settings;
import view.*;
import viewmodel.ViewModel;

//import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyModel extends Observable {
    public static final String Rudder = "Rudder";
    public static final String Throttle = "Throttle";
    public static final String AirSpeed = "AirSpeed";
    public static final String Altimeter = "Altimeter";
    public static final String Aileron = "Aileron";
    public static final String Elevator = "Elevator";
    public static final String Roll = "Roll";
    public static final String Pitch = "Pitch";
    public static final String Yaw = "Yaw";


    private Settings mSettings = null;
    private TimeSeries mTimeSeris = null;
    private Player mPlayer = new Player();
    private SocketWriter mFlightGearConnection = null;


    public List<AnomalyReport> getAnomlies() {
        return anomlies;
    }

    private List<AnomalyReport> anomlies = null;

    public void setmCorleatedToTheSelected(String value) {
        mCorleatedToTheSelected = ToolKit.getMaxColumCorrelatedFeature(value, mCorleatedFeatureResult);
    }

    private CorrelatedFeatures mCorleatedToTheSelected = null;

    private final ArrayList<TimeSeriesAnomalyDetector> mDetectors = new ArrayList<>();



    private final List<CorrelatedFeatures> mCorleatedFeatureResult = new ArrayList();

    private TimeSeriesAnomalyDetector mSelectedAlgo = null;
    Future longRunningTaskFuture = null;
    ExecutorService executorService = Executors.newSingleThreadExecutor();


    public void setSettings(Settings settings) {
        this.mSettings = settings;
        mPlayer.setSettings(settings);
        mFlightGearConnection = new SocketWriter(settings.getFlightGearPort());
        addChangeDataListener(mFlightGearConnection);
    }


    public void setTimeSeris(TimeSeries timeSeris) {
        this.mTimeSeris = timeSeris;
        mPlayer.setTimeSeris(timeSeris);


        new Thread(() -> {
            SimpleAnomalyDetector simpleAnomalyDetector = new SimpleAnomalyDetector();
            simpleAnomalyDetector.learnNormal(timeSeris);
            mCorleatedFeatureResult.addAll(simpleAnomalyDetector.getNormalModel());
            addDetector(simpleAnomalyDetector);
            ZscoreDetector zscoreDetector = new ZscoreDetector();
            anomlies = zscoreDetector.detect(timeSeris);
            zscoreDetector.learnNormal(timeSeris);
            addDetector(zscoreDetector);
            HybridDetector hybridDetector = new HybridDetector(timeSeris,zscoreDetector,simpleAnomalyDetector);
            addDetector(hybridDetector);
        }).start();
   }

    public void addObservers(ViewModel viewModel) {
        this.addObserver(viewModel);

    }

    public void addDetector(TimeSeriesAnomalyDetector timeSeriesAnomalyDetector) {
        mDetectors.add(timeSeriesAnomalyDetector);

    }

    public boolean isSettingSet() {
        return mSettings != null;
    }

    public void play() {
        mPlayer.play();
    }

    public void play(double rate) {
        mPlayer.play(rate);
    }

    public void pause() {
        mPlayer.pause();
    }

    public void stop() {
        mPlayer.stop();
        mFlightGearConnection.CloseSocket();
    }

    public Property getProperty(String name) {
        for (Property property : mSettings.getPropertyList()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    public int getPropertyColumNumber(String name) {
        for (Property property : mSettings.getPropertyList()) {
            if (property.getName().equals(name)) {
                return property.getColumNumber();
            }
        }
        return 0;
    }

    public void addChangeDataListener(DataChanged dataChanged) {
        mPlayer.addChangeDataListener(dataChanged);
    }



    public double getLength() {
        return mPlayer.getLength();
    }

    public void changeTimeStamp(int value) {
        mPlayer.injectTimeStamp(value);
    }



    public int getColumNumberFromSelectedColumn(String selectedItem) {
        return mTimeSeris.getIndexOfFeature(selectedItem);
    }


    public CorrelatedFeatures getCorletedFeature() {
        return mCorleatedToTheSelected;
    }

    public String getCorrelatedFeature(String value) {
        mCorleatedToTheSelected = ToolKit.getMaxColumCorrelatedFeature(value, mCorleatedFeatureResult);
        return mCorleatedToTheSelected.feature2;
    }

    public void setSelectedAlgo(String selectedAlgo) {
        for (TimeSeriesAnomalyDetector detector : mDetectors) {
            if (detector.getClass().getSimpleName().equals(selectedAlgo)) {
                mSelectedAlgo = detector;
                break;
            }
        }
    }


}
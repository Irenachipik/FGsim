package model.player;

import model.Util.TimeSeries;
import model.settings.Settings;
import view.DataChanged;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Player {

    private int ratio;
    private int mLength;
    private TimeSeries mTimeSeries;
    private Settings mSettings;
    private final static int MILISECONDS = 1000;

    private int timestepNow = 0;
    private int NowMseconds = 0;

    private final List<DataChanged> mDataChangeds = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> scheduledFuture = null;


    public void setTimeSeris(TimeSeries timeSeris) {
        mTimeSeries = timeSeris;
        mLength = timeSeris.getSizeOfAllFeatures() / ratio;
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
        ratio = MILISECONDS / settings.getDataSamplingRate();
    }

    public void addChangeDataListener(DataChanged dataChanged) {
        mDataChangeds.add(dataChanged);
    }



    public void play() {
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {

            setTimeStamp(timestepNow + 1);
            if (timestepNow % ratio == 0) {
                NowMseconds += 1;
            }

        }, 0, mSettings.getDataSamplingRate(), TimeUnit.MILLISECONDS);


    }
    public void play(double rate) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        long speed;
        if(rate==1.0)
            speed = mSettings.getDataSamplingRate();
        else
             speed = ((long)(100/rate));

        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {

            setTimeStamp(timestepNow + 1);
            if (timestepNow % ratio == 0) {
                NowMseconds += 1;
            }

        }, 0, speed, TimeUnit.MILLISECONDS);


    }

    public void pause() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    public void setTimeStamp(int timeStamp) {
        timestepNow = timeStamp;
        float[] data = mTimeSeries.getLineOfFloat(timestepNow);
        for (DataChanged dataChanged : mDataChangeds) {
            dataChanged.onChangedData(timestepNow, data);
        }
    }

    public void setClock(int seconds) {
        NowMseconds = seconds;

    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        NowMseconds =0;
        setTimeStamp(0);

    }

    public int getLength() {
        return mTimeSeries.getSizeOfAllFeatures();
    }

    public void injectTimeStamp(int value) {
        setTimeStamp(value);
        NowMseconds =(value / ratio);
    }

    public int getCurrentTimeStamp() {
        return timestepNow;
    }
}

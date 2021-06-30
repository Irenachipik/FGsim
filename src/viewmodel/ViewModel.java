package viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import model.MyModel;
import model.Util.AnomalyReport;
import model.Util.TimeSeries;
import model.settings.Property;
import model.settings.Settings;
import view.DataChanged;
import view.ViewController;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static model.MyModel.*;

public class ViewModel implements DataChanged,Observer {
    private ViewController c;
    private final MyModel mModel = new MyModel();
    private String sF,cF="";
    public ArrayList<String> mCsvColums = new ArrayList<>();
    //public ListView mCsvColums;
    public StringProperty selectedColumnFromList=new SimpleStringProperty();
    public StringProperty rollProperty=new SimpleStringProperty(),pitchProperty=new SimpleStringProperty(),yawProperty=new SimpleStringProperty();
    public DoubleProperty speedProperty=new SimpleDoubleProperty();
    public DoubleProperty altimeterProperty=new SimpleDoubleProperty();
    public DoubleProperty aileron=new SimpleDoubleProperty();
    public DoubleProperty elevator=new SimpleDoubleProperty();
    public DoubleProperty rudder=new SimpleDoubleProperty();
    public DoubleProperty throttle=new SimpleDoubleProperty();
    public IntegerProperty timeStep = new SimpleIntegerProperty();
    public ArrayList<Float> selectedFeatures = new ArrayList<>();
    public ArrayList<Float> correlatedFeatures = new ArrayList<>();
    public XYChart.Series selectedFeature = new XYChart.Series<>(), correlatedFeature = new XYChart.Series<>();
    public List<AnomalyReport> anomalies = new ArrayList<>();


    public LineChart mColumnSelectedChart;
    public LineChart mCorleatedChart;
    //public LineChart sFeature = new LineChart(),cFeature = new LineChart();
    private TimeSeries timeSeris;

    public ViewModel(MyModel model){
        //this.mModel=model;
        //mModel.addO
        this.mModel.addObservers(this);
        mModel.addChangeDataListener(this);

        this.selectedColumnFromList.addListener((observableValue, o, nv) -> {
            System.out.println(selectedColumnFromList);
            this.onSelectedColumnFromList((String) observableValue.getValue());
        });

    }



    public void setSettings(Settings settings){ mModel.setSettings(settings);}

    public void loadCsv(String csvPath) {
        if (!mModel.isSettingSet()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Xml Error");
            a.setContentText("please upload fixed xml before upload csv flight");
            a.showAndWait();
        } else {
            if (csvPath != null) {
                try {
                     timeSeris = new TimeSeries(csvPath);
                    mModel.setTimeSeris(timeSeris);
                    this.mCsvColums.clear();
                    mCsvColums.addAll(timeSeris.getAllFeathresNames());

                } catch (Exception e) {

                }
            }
        }
    }
    public void onSelectedColumnFromList(String value) {

        Platform.runLater(()->{
            selectedFeature = new XYChart.Series();
            correlatedFeature = new XYChart.Series();
            mModel.setmCorleatedToTheSelected(value);
            float[] selectedData = this.timeSeris.getArrayFromStringUntilTimeStamp(value, this.timeStep.intValue());
            sF = mModel.getCorletedFeature() != null ? mModel.getCorletedFeature().feature1 :"";
            //String mostCorrelative = mModel.getCorletedFeature().feature2 != null ? mModel.getCorletedFeature().feature2 :"";//mModel.getCorrelatedFeature(value);
            cF = mModel.getCorletedFeature().feature2 != null ? mModel.getCorletedFeature().feature2 :"";
            float[] corelteadData = this.timeSeris.getArrayFromStringUntilTimeStamp(cF, this.timeStep.intValue());
            for (int i = 0; i < selectedData.length; i += 50) {
                selectedFeature.getData().add(new XYChart.Data(String.valueOf(i), selectedData[i]));
                //c.mColumnSelectedChart.getData().add(new XYChart.Data(String.valueOf(i), selectedData[i]));
                //selectedFeatures.add(selectedData[i]);

            }
            for (int i = 0; i < corelteadData.length; i += 50) {
                correlatedFeature.getData().add(new XYChart.Data(String.valueOf(i), corelteadData[i]));
                //c.mCorleatedChart.getData().add(new XYChart.Data(String.valueOf(i), corelteadData[i]));

                //correlatedFeatures.add(corelteadData[i]);
            }
            c.selectedChart.getData().clear();
            c.selectedChart.getData().addAll(selectedFeature);
            c.correlatedChart.getData().clear();
            c.correlatedChart.getData().addAll(correlatedFeature);
            anomalies = mModel.getAnomlies();
            anomalies.forEach(anomalyReport -> {c.anomalyChart.getData().add(anomalyReport.timeStep);});
            //mColumnSelectedChart.getData().clear();
            //mColumnSelectedChart.getData().addAll(selectedFeature);

            //mCorleatedChart.getData().clear();
            //mCorleatedChart.getData().addAll(correlatedFeature);

        });
        //selectedFeature.getData().clear();
        //correlatedFeature.getData().clear();


    }

    public ArrayList<String> getColTitels(){
        return mCsvColums;
    }

    public Property getProperty(String name){

        return mModel.getProperty(name);
    }

    public void play(double rate) {
        mModel.play(rate);
    }
    public void pause() {mModel.pause();}
    public void stop() {mModel.stop(); }
    public void forward(){mModel.changeTimeStamp(timeStep.getValue()+10);}
    public void backward(){mModel.changeTimeStamp(timeStep.getValue()-10);}
    public void bb(){mModel.changeTimeStamp(0);}

    @Override
    public void update (Observable o, Object arg){

    }


    public double getLength() {return mModel.getLength(); }

    public void changeTimeStamp(int value) {mModel.changeTimeStamp(value); }

    @Override
    public void onChangedData(int timeStamp, float[] values) {
        Platform.runLater(() -> {
            //speedProperty.setValue(String.format("%.2f", values[mModel.getPropertyColumNumber(AirSpeed)]));
           //altimeterProperty.setValue(String.format("%.2f", values[mModel.getPropertyColumNumber(Altimeter)]));
            speedProperty.setValue( values[mModel.getPropertyColumNumber(AirSpeed)]);
            altimeterProperty.setValue( values[mModel.getPropertyColumNumber(Altimeter)]);
            rollProperty.setValue(String.format("%.2f", values[mModel.getPropertyColumNumber(Roll)]));
            pitchProperty.setValue(String.format("%.2f", values[mModel.getPropertyColumNumber(Pitch)]));
            yawProperty.setValue(String.format("%.2f", values[mModel.getPropertyColumNumber(Yaw)]));
            throttle.setValue(values[mModel.getPropertyColumNumber(Throttle)]);
            rudder.setValue(values[mModel.getPropertyColumNumber(Rudder)]);
            aileron.setValue(values[mModel.getPropertyColumNumber(Aileron)]);
            elevator.setValue(values[mModel.getPropertyColumNumber(Elevator)]);
            timeStep.setValue(timeStamp);


            if (timeStamp % 50 == 0) {
                if (selectedFeature != null) {
                    selectedFeature.getData().add(new XYChart.Data(String.valueOf(timeStamp), values[mModel.getColumNumberFromSelectedColumn(selectedColumnFromList.getValue())]));
                }
                String correlatedFeatures = cF; //mModel.getCorletedFeature().feature2 != null ? mModel.getCorletedFeature().feature2 :"";//mModel.getCorrelatedFeature(selectedColumnFromList.getValue());;
                if (!correlatedFeatures.isEmpty()) {
                    correlatedFeature.getData().add(new XYChart.Data(String.valueOf(timeStamp), values[mModel.getColumNumberFromSelectedColumn(correlatedFeatures)]));

                }
            }
        });
    }

    public void setController(ViewController controller) {
        this.c = controller;
        this.selectedColumnFromList.bind(c.selectedColumn);
    }

    //public void addAnomalyDetector(String parent, String name) { mModel.addDetector(new addDetectorAlgo(parent, name));}
}


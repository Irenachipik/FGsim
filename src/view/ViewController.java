package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.settings.Interpreter;
import model.settings.Settings;
import viewmodel.ViewModel;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static model.MyModel.Rudder;
import static model.MyModel.Throttle;

public class ViewController implements Initializable {

    @FXML
    public Pane welcomepane;
    @FXML  public Pane dashpane;
    public Button toBeginButton;
    public Button toEndButton;
    public Button ffButton;
    public Button pauseButton;
    public Button stopButton;
    public Button playButton;
    public Button fbButton;
    public ScatterChart anomalyChart;
    ViewModel viewModel;
    private Stage mMainStage;
    //Welcome scene
    @FXML   public Button Start;
    @FXML  public TextField CsvPath;
    @FXML  public TextField xmlPath;
    //Dashboard scene
    @FXML  public Circle OuterCircle;
    @FXML  public Circle InnerCircle;
    @FXML  public Slider throttleVal;
    @FXML  public Slider rudderVal;
    @FXML  public ListView csvParam;
    @FXML  public Label airSpeedVal;
    @FXML  public Label altVal;
    @FXML  public Label rollVal;
    @FXML  public Label pitchVal;
    @FXML  public Label yawVal;
    @FXML  public Arc speedGauge;
    @FXML  public Arc altitudeGauge;
    @FXML  public Label cTime;
    @FXML  public ChoiceBox flightRate;
    @FXML  public Slider playerSlider;
    @FXML  public LineChart selectedChart;
    @FXML  public LineChart correlatedChart;
    @FXML  public Button addAlgoBTN;
    @FXML  public Canvas mCanvas;
    @FXML  public XYChart.Series selectedFeature= new XYChart.Series(), correlatedFeature= new XYChart.Series();
    //public ArrayList<FloatProperty> selectedFeatures = new ArrayList<>();
    //public ArrayList<FloatProperty> correlatedFeatures= new ArrayList<>();
    public StringProperty selectedColumn=new SimpleStringProperty();


    public ViewController() {
    }

    public void init(ViewModel vm){
        this.viewModel = vm;

        csvParam.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) ->  selectedColumn.setValue((String)observableValue.getValue()));
        selectedChart.setCreateSymbols(false);
        correlatedChart.setCreateSymbols(false);
        airSpeedVal.textProperty().bind(this.viewModel.speedProperty.asString());
        altVal.textProperty().bind(this.viewModel.altimeterProperty.asString());
        rollVal.textProperty().bind(this.viewModel.rollProperty);
        pitchVal.textProperty().bind(this.viewModel.pitchProperty);
        yawVal.textProperty().bind(this.viewModel.yawProperty);
        InnerCircle.centerXProperty().bind(this.viewModel.aileron.multiply(50));
        InnerCircle.centerYProperty().bind(this.viewModel.elevator.multiply(50));
        playerSlider.valueProperty().bind(this.viewModel.timeStep);
        speedGauge.lengthProperty().bind(this.viewModel.speedProperty.multiply(-1).add(365));
        altitudeGauge.lengthProperty().bind(this.viewModel.altimeterProperty.divide(-5).add(365));
        addAlgoBTN.setOnMouseClicked(mouseEvent ->{
            FileChooser fc=new FileChooser();
            fc.setTitle("open anomaly detector class file");
            fc.setInitialDirectory(new File("/"));
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter
                            ("class file", "*.class")
            );
            File chooser=fc.showOpenDialog(null);
            if(chooser!=null) {
               // vm.addAnomalyDetector(chooser.getParent(),chooser.getName());
            }
            else {
                Alert message=new Alert(Alert.AlertType.ERROR);
                message.setContentText("oops!"
                        + " \n please choose a class file");
                message.show();
            }

        });



        selectedFeature.dataProperty().bindBidirectional(this.viewModel.selectedFeature.dataProperty());
        correlatedFeature.dataProperty().bindBidirectional(this.viewModel.correlatedFeature.dataProperty());
        selectedFeature.dataProperty().addListener((o,ov,nv)->{
            selectedChart.getData().addAll(selectedFeature);});

    }
    public void setMainStage(Stage primaryStage) {
        mMainStage = primaryStage;
    }

    public void onOpenCsvClicked(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("File Choose");
        fc.setInitialDirectory(new File("/"));
        FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("CSV Files (*.csv)","*.csv");
        fc.getExtensionFilters().add(ef);
        File chosen = fc.showOpenDialog(null);
        if(chosen!=null)
        {
            CsvPath.setText(chosen.getAbsolutePath());
            this.viewModel.loadCsv(chosen.getAbsolutePath());
           // mCsvColums.getItems().removeAll();
            csvParam.getItems().addAll(this.viewModel.getColTitels());
            configureSliders();
            configurePlayer();

        }
        if(!xmlPath.getText().isEmpty()&&!CsvPath.getText().isEmpty())
        {
            Start.setDisable(false);
        }

    }
    private void configureSliders() {
        rudderVal.setMin(viewModel.getProperty(Rudder).getMinRange());
        rudderVal.setMax(viewModel.getProperty(Rudder).getMaxRange());
        throttleVal.setMin(viewModel.getProperty(Throttle).getMinRange());
        throttleVal.setMax(viewModel.getProperty(Throttle).getMaxRange());
    }

    private void configurePlayer() {

        toBeginButton.setOnMouseClicked(mouseEvent -> {
            viewModel.bb();
            playerSlider.setDisable(false);
        });
        fbButton.setOnMouseClicked(mouseEvent -> {
            viewModel.backward();
            playerSlider.setDisable(true);
        });
        playButton.setOnMouseClicked(mouseEvent -> {
            viewModel.play((Double) flightRate.getValue());
            playerSlider.setDisable(true);
        });
        pauseButton.setOnMouseClicked(mouseEvent -> {
            viewModel.pause();
            playerSlider.setDisable(false);
        });
        stopButton.setOnMouseClicked(mouseEvent -> {
            viewModel.stop();
            correlatedFeature.getData().clear();
            selectedFeature.getData().clear();
            playerSlider.setDisable(false);
        });
        ffButton.setOnMouseClicked(mouseEvent -> {
            viewModel.forward();
            playerSlider.setDisable(true);
        });
        toEndButton.setOnMouseClicked(mouseEvent -> {
            viewModel.play((Double) flightRate.getValue());
            playerSlider.setDisable(false);
        });

        playerSlider.valueChangingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                viewModel.changeTimeStamp((int) playerSlider.getValue());
            }
        });
        ObservableList<Number> speedList  = FXCollections.observableArrayList(0.5,1.0,1.5,2.0);
        flightRate.setItems(speedList);
        flightRate.setValue(1.0);
        flightRate.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.play( speedList.get(newVal.intValue()).doubleValue());
        });
       /* flightRate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                mModel.changeTimeStamp((int) mPlayerSlider.getValue());
            }
        });*/
        playerSlider.setMin(0);
        playerSlider.setMax(viewModel.getLength());
        playerSlider.setDisable(false);
    }


    public void onOpenSettingsClicked(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("File Choose");
        fc.setInitialDirectory(new File("/"));
        FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("XML Files (*.xml)","*.xml");
        fc.getExtensionFilters().add(ef);
        File chosen = fc.showOpenDialog(null);
        Settings settings;
        try {
            settings = Interpreter.getUserSettings(chosen.getAbsolutePath());
        } catch (Exception e) {
            settings = Interpreter.getCachedUserSettings();
        }
        if(chosen!=null)
        {
            //vm.loadXml("resources/"+chosen.getName());
            xmlPath.setText(chosen.getAbsolutePath());
            viewModel.setSettings(settings);

        }
        if(!xmlPath.getText().isEmpty()&&!CsvPath.getText().isEmpty())
        {
            Start.setDisable(false);
        }
    }


    public void begin(ActionEvent event) {
      welcomepane.setVisible(false);
      dashpane.setVisible(true);

       /* FXMLLoader root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //mCsvColums.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> ViewModel.onSelectedColumnFromList((String) observableValue.getValue()));

    }
}

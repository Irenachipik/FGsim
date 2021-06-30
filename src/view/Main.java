package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.MyModel;
import viewmodel.ViewModel;

import java.net.URL;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = getClass().getClassLoader().getResource("dashPane.fxml");
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("dashpane.fxml"));
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        ViewController controller = loader.getController();
        controller.setMainStage(primaryStage);
        MyModel m = new MyModel();
        ViewModel vm = new ViewModel(m);
        controller.init(vm);
        primaryStage.setTitle("Simulator review");
        vm.setController(controller);
        primaryStage.setScene(new Scene(root, 990, 650));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

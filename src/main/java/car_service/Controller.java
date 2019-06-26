package car_service;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

public class Controller implements Initializable {
    static Controller controller;

    int maxElevator = 3, maxGarage = 2, maxMechanic = 5, maxWrench = 10, maxHammer = 5, maxBalancer = 1;
    Semaphore freeElevatorSem = new Semaphore(maxElevator, true);
    Semaphore freeGarageSem = new Semaphore(maxGarage, true);
    Semaphore freeMechanicSem = new Semaphore(maxMechanic, true);
    Semaphore freeWrenchSem = new Semaphore(maxWrench, true);
    Semaphore freeHammerSem = new Semaphore(maxHammer, true);
    Semaphore freeBalancerSem = new Semaphore(maxBalancer, true);
    //private int time = 10000;
    public Label actualElevator, actualGarage, actualMechanic, actualWrench, actualHammer, actualBalancer, update;

    public TextField brand, model, engine, year, vinNumber, time;
    public CheckBox ifElevator, ifGarage, ifMechanic, ifWrench, ifHammer, ifBalancer;

    //String url = "jdbc:sqlserver://KRZYSZTOFPC\\CARSERVICESQL;database=CarServiceDatabase;integratedSecurity=true";
    String url = "jdbc:sqlserver://DESKTOP-C4ACI6E\\MSSQLSERVER;database=CarServiceDatabase;integratedSecurity=true";

    @FXML
    public ComboBox<String> carMalfunction;

    @FXML
    public TreeView<String> treeView;
    TreeItem<String> treeRoot = new TreeItem<String>("Samochody w warsztacie");
    private ArrayList<Car> carList = new ArrayList<>();

    @FXML
    private ImageView logo;
/*
    private Timeline timeline;

    public void showSemStatus() {
        timeline = new Timeline();
        KeyFrame semUpdate = new KeyFrame(Duration.millis(200),
                event1 -> {
                    actualElevator.setText(freeElevatorSem.availablePermits() + "/" + maxElevator);
                    actualGarage.setText(freeGarageSem.availablePermits() + "/" + maxGarage);
                    actualMechanic.setText(freeMechanicSem.availablePermits() + "/" + maxMechanic);
                    actualWrench.setText(freeWrenchSem.availablePermits() + "/" + maxWrench);
                    actualHammer.setText(freeHammerSem.availablePermits() + "/" + maxHammer);
                    actualBalancer.setText(freeBalancerSem.availablePermits() + "/" + maxBalancer);
                });
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(semUpdate);
        timeline.play();
    }
*/

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = this;
        ObservableList<String> carMalfunctionsList = FXCollections.observableArrayList();
        try {
            FileReader fileReader = new FileReader("src/main/resources/malfunctionsDatabase.txt");
            BufferedReader bufferReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferReader.readLine()) != null) {
                carMalfunctionsList.add(line);
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        carMalfunction.setItems(carMalfunctionsList);

        File file = new File("src/main/resources/logo.png");
        Image image = new Image(file.toURI().toString());
        logo.setImage(image);
        treeRoot.setExpanded(true);
        treeView.setRoot(treeRoot);
        treeView.setShowRoot(false);

        try {
            DriverManager.registerDriver(new SQLServerDriver());
            Connection conn = DriverManager.getConnection(url);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //showSemStatus();
    }


    public void newCar() {
        Car car = new Car(brand.getText(), model.getText(), engine.getText(), year.getText(), vinNumber.getText(), carMalfunction.getSelectionModel().getSelectedItem(),
                ifElevator.isSelected(), ifGarage.isSelected(), ifMechanic.isSelected(), ifWrench.isSelected(), ifHammer.isSelected(), ifBalancer.isSelected(), freeElevatorSem,
                freeGarageSem, freeMechanicSem, freeWrenchSem, freeHammerSem, freeBalancerSem, Integer.valueOf(time.getText()));
        carList.add(car);
        car.start();

        brand.clear();
        model.clear();
        engine.clear();
        year.clear();
        vinNumber.clear();
        ifElevator.setSelected(false);
        ifGarage.setSelected(false);
        ifMechanic.setSelected(false);
        ifWrench.setSelected(false);
        ifHammer.setSelected(false);
        ifBalancer.setSelected(false);
    }

    public void removeTreeItem() {
        TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
        item.getParent().getChildren().remove(item);
    }


    public void test() {
        addCar("Ford", "Mondeo", "3.0 V6 226KM", "2003", "2HGFB2F53EH512407", "Układ hamulcowy",
                true, false, true, true, false, false, 10000);
        addCar("Alfa Romeo", "159", "1.9 JTD 150KM", "2007", "WDBUF56J36A768098", "Diesel, układ wtryskowy",
                false, true, true, true, false, false, 10000);
        addCar("Audi", "A4", "1.8 TSI 190KM", "2002", "4S4BRBHC8B3324884", "Napęd, skrzynia biegów",
                true, false, true, true, true, false, 10000);
        addCar("Skoda", "Fabia", "1.2 MPI 60KM", "2015", "3VWSK69M01M144266", "Koła",
                true, false, true, true, false, true, 10000);
        addCar("Skoda", "Octavia", "2.0 TDI 140KM", "2009", "1G2NF52E14M527526", "Koła",
                true, false, true, true, false, true, 10000);
        addCar("Ford", "Focus", "2.5 T 225KM", "2005", "JTHBK1GG4E2134001", "Układ paliwowy",
                false, true, true, false, false, false, 10000);
        addCar("Jeep", "Grand Cherokee", "3.0 CRD 240KM", "2012", "2C3AA43R25H544686", "Elektryka",
                false, true, true, false, false, false, 10000);
        addCar("Mitsubishi", "Pajero", "3.2 DI-DC 170KM", "2007", "1YVHZ8DH9C5M37357", "Akumulator",
                false, false, true, false, false, false, 10000);
/*
        addCar("Ford", "Mondeo", "3.0 V6 226KM", "2003", "2HGFB2F53EH512407", "Układ hamulcowy",
                true, false, true, true, false, false, 10000);
        addCar("Alfa Romeo", "159", "1.9 JTD 150KM", "2007", "WDBUF56J36A768098", "Diesel, układ wtryskowy",
                false, true, true, true, false, false, 7000);
        addCar("Audi", "A4", "1.8 TSI 190KM", "2002", "4S4BRBHC8B3324884", "Napęd, skrzynia biegów",
                true, false, true, true, true, false, 13000);
        addCar("Skoda", "Fabia", "1.2 MPI 60KM", "2015", "3VWSK69M01M144266", "Koła",
                true, false, true, true, false, true, 5000);
        addCar("Skoda", "Octavia", "2.0 TDI 140KM", "2009", "1G2NF52E14M527526", "Koła",
                true, false, true, true, false, true, 6000);
        addCar("Ford", "Focus", "2.5 T 225KM", "2005", "JTHBK1GG4E2134001", "Układ paliwowy",
                false, true, true, false, false, false, 10000);
        addCar("Jeep", "Grand Cherokee", "3.0 CRD 240KM", "2012", "2C3AA43R25H544686", "Elektryka",
                false, true, true, false, false, false, 6000);
        addCar("Mitsubishi", "Pajero", "3.2 DI-DC 170KM", "2007", "1YVHZ8DH9C5M37357", "Akumulator",
                false, false, true, false, false, false, 5000);
*/
    }

    private void addCar(String brand, String model, String engine, String year, String vinNumber, String malfunction,
                        boolean ifElevator, boolean ifGarage, boolean ifMechanic, boolean ifWrench, boolean ifHammer, boolean ifBalancer,
                        int time){
        Car car = new Car(brand, model, engine, year, vinNumber, malfunction,
                ifElevator, ifGarage, ifMechanic, ifWrench, ifHammer, ifBalancer, freeElevatorSem,
                freeGarageSem, freeMechanicSem, freeWrenchSem, freeHammerSem, freeBalancerSem, time);
        carList.add(car);
        car.start();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int changeToolAmount(Semaphore freeToolSem, int maxTool, String name, Label toolLabel) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(maxTool));
        dialog.setTitle("Zmień ilość " + name);
        dialog.setHeaderText("Ile potrzeba " + name + "?");
        dialog.setContentText("Ilość " + name + " :");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            maxTool = Integer.valueOf(result.get());
            freeToolSem.drainPermits();
            freeToolSem.release(maxTool);
            toolLabel.setText(freeToolSem.availablePermits() + "/" + maxTool);
        }
        return maxTool;
    }

    public void changeElevator() { maxElevator = changeToolAmount(freeElevatorSem, maxElevator, "podnośników", actualElevator); }

    public void changeGarage() {
        maxGarage = changeToolAmount(freeGarageSem, maxGarage, "garaży", actualGarage);
    }

    public void changeMechanic() { maxMechanic = changeToolAmount(freeMechanicSem, maxMechanic, "mechaników", actualMechanic); }

    public void changeWrench() {
        maxWrench = changeToolAmount(freeWrenchSem, maxWrench, "kluczy", actualWrench);
    }

    public void changeHammer() {
        maxHammer = changeToolAmount(freeHammerSem, maxHammer, "młotków", actualHammer);
    }

    public void changeBalancer() { maxBalancer = changeToolAmount(freeBalancerSem, maxBalancer, "wyważarek", actualBalancer); }
}


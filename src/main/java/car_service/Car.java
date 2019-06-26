package car_service;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

import static car_service.Controller.*;

class Car extends Thread {

    private String brand, model, engine, year, vinNumber, malfunction;
    private boolean ifElevator, ifGarage, ifMechanic, ifWrench, ifHammer, ifBalancer;
    private Semaphore freeElevatorSem, freeGarageSem, freeMechanicSem, freeWrenchSem, freeHammerSem, freeBalancerSem;
    private int time;

    Car(String brand, String model, String engine, String year, String vinNumber, String malfunction,
        boolean ifElevator, boolean ifGarage, boolean ifMechanic, boolean ifWrench, boolean ifHammer, boolean ifBalancer,
        Semaphore freeElevatorSem, Semaphore freeGarageSem, Semaphore freeMechanicSem, Semaphore freeWrenchSem,
        Semaphore freeHammerSem, Semaphore freeBalancerSem, int time) {
        this.brand = brand;
        this.model = model;
        this.engine = engine;
        this.year = year;
        this.vinNumber = vinNumber;
        this.malfunction = malfunction;
        this.ifElevator = ifElevator;
        this.ifGarage = ifGarage;
        this.ifMechanic = ifMechanic;
        this.ifWrench = ifWrench;
        this.ifHammer = ifHammer;
        this.ifBalancer = ifBalancer;
        this.freeElevatorSem = freeElevatorSem;
        this.freeGarageSem = freeGarageSem;
        this.freeMechanicSem = freeMechanicSem;
        this.freeWrenchSem = freeWrenchSem;
        this.freeHammerSem = freeHammerSem;
        this.freeBalancerSem = freeBalancerSem;
        this.time = time;
    }

    public void run() {
        TreeItem<String> rootChild = new TreeItem<String>(brand + " " + model);
        controller.treeRoot.getChildren().add(rootChild);
        rootChild.getChildren().add(new TreeItem<>("Silnik: " + engine));
        rootChild.getChildren().add(new TreeItem<>("Rok: " + year));
        rootChild.getChildren().add(new TreeItem<>("Vin: " + vinNumber));
        rootChild.getChildren().add(new TreeItem<>("Usterka: " + malfunction));
        rootChild.getChildren().add(new TreeItem<>("Podnosnik: " + controller.ifElevator.isSelected()));
        rootChild.getChildren().add(new TreeItem<>("Garaż: " + controller.ifGarage.isSelected()));
        rootChild.getChildren().add(new TreeItem<>("Mechanik: " + controller.ifMechanic.isSelected()));
        rootChild.getChildren().add(new TreeItem<>("Klucz: " + controller.ifWrench.isSelected()));
        rootChild.getChildren().add(new TreeItem<>("Młotek: " + controller.ifHammer.isSelected()));
        rootChild.getChildren().add(new TreeItem<>("Wyważarka: " + controller.ifBalancer.isSelected()));

        try {
            addToDatabase(brand, model, engine, year, vinNumber, malfunction, ifElevator, ifGarage, ifMechanic, ifWrench, ifHammer, ifBalancer, time);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        acquireSem(ifElevator, freeElevatorSem);
        acquireSem(ifGarage, freeGarageSem);
        acquireSem(ifMechanic, freeMechanicSem);
        acquireSem(ifWrench, freeWrenchSem);
        acquireSem(ifHammer, freeHammerSem);
        acquireSem(ifBalancer, freeBalancerSem);

        updateSems();

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        releaseSem(ifElevator, freeElevatorSem);
        releaseSem(ifGarage, freeGarageSem);
        releaseSem(ifMechanic, freeMechanicSem);
        releaseSem(ifWrench, freeWrenchSem);
        releaseSem(ifHammer, freeHammerSem);
        releaseSem(ifBalancer, freeBalancerSem);

        updateSems();
        rootChild.setValue(brand + " " + model + "   |     DO ODBIORU");
    }


    private void addToDatabase(String brand, String model, String engine, String year, String vinNumber, String malfunction,
                               boolean ifElevator, boolean ifGarage, boolean ifMechanic, boolean ifWrench, boolean ifHammer, boolean ifBalancer,
                               int time) throws SQLException {
        DriverManager.registerDriver(new SQLServerDriver());
        Connection conn = DriverManager.getConnection(controller.url);
        String query = "insert into Cars(Brand, Model, Engine, Year, vinNumber, Malfunction, ifElevator, ifGarage, ifMechanic, ifWrench, ifHammer, ifBalancer, time, Date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Calendar calendar = Calendar.getInstance();
        java.sql.Date actualDate = new java.sql.Date(calendar.getTime().getTime());

        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, brand);
        preparedStmt.setString(2, model);
        preparedStmt.setString(3, engine);
        preparedStmt.setString(4, year);
        preparedStmt.setString(5, vinNumber);
        preparedStmt.setString(6, malfunction);
        preparedStmt.setBoolean(7, ifElevator);
        preparedStmt.setBoolean(8, ifGarage);
        preparedStmt.setBoolean(9, ifMechanic);
        preparedStmt.setBoolean(10, ifWrench);
        preparedStmt.setBoolean(11, ifHammer);
        preparedStmt.setBoolean(12, ifBalancer);
        preparedStmt.setInt(13, time);
        preparedStmt.setDate(14, actualDate);

        preparedStmt.execute();
        conn.close();
    }

    private static void acquireSem(boolean ifTool, Semaphore freeToolSem){
        if (ifTool) {
            try {
                freeToolSem.acquire(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void releaseSem(boolean ifTool, Semaphore freeToolSem){
        if (ifTool) {
            freeToolSem.release(1);
        }
    }

    private void updateSems() {
        Platform.runLater(
                () -> {
                    controller.actualElevator.setText(controller.freeElevatorSem.availablePermits() + "/" + controller.maxElevator);
                    controller.actualGarage.setText(controller.freeGarageSem.availablePermits() + "/" + controller.maxGarage);
                    controller.actualMechanic.setText(controller.freeMechanicSem.availablePermits() + "/" + controller.maxMechanic);
                    controller.actualWrench.setText(controller.freeWrenchSem.availablePermits() + "/" + controller.maxWrench);
                    controller.actualHammer.setText(controller.freeHammerSem.availablePermits() + "/" + controller.maxHammer);
                    controller.actualBalancer.setText(controller.freeBalancerSem.availablePermits() + "/" + controller.maxBalancer);
                }
        );
    }
}

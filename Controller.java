import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.ResourceBundle;

public class Controller {
    private Stage stage;

    @FXML
    private TextField proteinName;
    @FXML
    private TextField geneName;
    @FXML
    private TextField sequenceVer;
    @FXML
    private TextField proteinExistence;
    @FXML
    private TextField entryName;
    @FXML
    private TextField organismName;
    @FXML
    private TextField identifier;
    @FXML
    private Button searchButton;
    @FXML
    private TableView resultsTable;
    @FXML
    private Button loadButton;
    @FXML
    private TextField filePath;
    @FXML
    private Button saveButton;
    @FXML
    private TableView<TimeRecord> measurementsTable;
    @FXML
    private TextField savePath;

    private String fastaPath = "";
    private long fileLength = 0;
    private final ObservableList<TimeRecord> timeRecords = FXCollections.observableArrayList();
    private final ObservableList<ProteinRecord> proteinRecords = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // measurments table setup
        measurementsTable.setEditable(true);

        TableColumn<TimeRecord, String> fileNameCol = new TableColumn<TimeRecord, String>("Nazwa");
        fileNameCol.setPrefWidth(100);
        fileNameCol.setCellValueFactory(new PropertyValueFactory<TimeRecord, String>("filename"));

        TableColumn<TimeRecord, String> fileSizeCol = new TableColumn<TimeRecord, String>("Rozmiar");
        fileSizeCol.setPrefWidth(100);
        fileSizeCol.setCellValueFactory(new PropertyValueFactory<TimeRecord, String>("filesize"));

        TableColumn<TimeRecord, String> timeCol = new TableColumn<TimeRecord, String>("Czas");
        timeCol.setPrefWidth(100);
        timeCol.setCellValueFactory(new PropertyValueFactory<TimeRecord, String>("time"));

        measurementsTable.getColumns().addAll(fileNameCol, fileSizeCol, timeCol);
        measurementsTable.setItems(timeRecords);

        // results table setup
        resultsTable.setEditable(true);
        TableColumn<ProteinRecord, String> proteinNameCol = new TableColumn<ProteinRecord, String>("Nazwa");
        proteinNameCol.setPrefWidth(100);
        proteinNameCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("name"));

        TableColumn<ProteinRecord, String> identifierCol = new TableColumn<ProteinRecord, String>("ID");
        identifierCol.setPrefWidth(50);
        identifierCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("id"));

        TableColumn<ProteinRecord, String> entryNameCol = new TableColumn<ProteinRecord, String>("Wpis");
        entryNameCol.setPrefWidth(100);
        entryNameCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("entry"));

        TableColumn<ProteinRecord, String> organismNameCol = new TableColumn<ProteinRecord, String>("Organizm");
        organismNameCol.setPrefWidth(100);
        organismNameCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("organism"));

        TableColumn<ProteinRecord, String> geneNameCol = new TableColumn<ProteinRecord, String>("Gen");
        geneNameCol.setPrefWidth(100);
        geneNameCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("gene"));

        TableColumn<ProteinRecord, String> proteinExisitenceCol = new TableColumn<ProteinRecord, String>("PE");
        proteinExisitenceCol.setPrefWidth(50);
        proteinExisitenceCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("pe"));

        TableColumn<ProteinRecord, String> sequenceVerCol = new TableColumn<ProteinRecord, String>("SV");
        sequenceVerCol.setPrefWidth(50);
        sequenceVerCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("sv"));

        TableColumn<ProteinRecord, String> sequenceCol = new TableColumn<ProteinRecord, String>("Sekwencja");
        sequenceCol.setPrefWidth(150);
        sequenceCol.setCellValueFactory(new PropertyValueFactory<ProteinRecord, String>("sequence"));

        resultsTable.getColumns().addAll(proteinNameCol, identifierCol, entryNameCol, organismNameCol, geneNameCol, proteinExisitenceCol, sequenceVerCol, sequenceCol);
        resultsTable.setItems(proteinRecords);
    }

    @FXML
    private void search(ActionEvent actionEvent) {
        // check if fasta file is specified
        if (fastaPath.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Brak pliku!");
            alert.setHeaderText(null);
            alert.setContentText("Nie wybrano pliku FASTA do przeszukiwania!");
            alert.showAndWait();
            return;
        }

        // clear table with search results
        proteinRecords.clear();
        resultsTable.getItems().clear();

        // find sequences and add them to search results table
        proteinRecords.add(new ProteinRecord(proteinName.getText(), identifier.getText(), entryName.getText(), organismName.getText(), geneName.getText(), proteinExistence.getText(), sequenceVer.getText(), "qwertyuiolkmnb2vfdfghjklkmnbvc312h3jk"));

        // add new row to table with file size and time
        long searchTime = 0;
        timeRecords.add(new TimeRecord(fastaPath, Long.toString(fileLength), Long.toString(searchTime)));
    }

    @FXML
    private void loadFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik:");
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        String path = selectedFile.getAbsolutePath();
        if (check_fasta_extension(path)) {
            fastaPath = path;
            fileLength = selectedFile.length();
            filePath.setText(path);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Nieprawidłowy format!");
            alert.setHeaderText(null);
            alert.setContentText("Wybrany plik nie jest plikiem FASTA!");
            alert.showAndWait();
            fastaPath = "";
            fileLength = 0;
            filePath.setText("");
        }
    }

    @FXML
    private void save(ActionEvent actionEvent) {
        String path = savePath.getText();
        saveTimeRecords(path);
    }

    private boolean check_fasta_extension(String path) {
        int lastIndexOf = path.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return false;
        }
        return path.substring(lastIndexOf).equals(".FASTA");
    }

    private void saveTimeRecords(String path){
        final char CSV_SEPARATOR = ';';
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(path))) {
            for (TimeRecord timeRecord : timeRecords) {
                csvWriter.append(timeRecord.getFilename()).append(CSV_SEPARATOR)
                        .append(timeRecord.getFilesize()).append(CSV_SEPARATOR)
                        .append(timeRecord.getTime()).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TimeRecord {
        private final SimpleStringProperty filename;
        private final SimpleStringProperty filesize;
        private final SimpleStringProperty time;

        private TimeRecord(String fName, String fSize, String time) {
            this.filename = new SimpleStringProperty(fName);
            this.filesize = new SimpleStringProperty(fSize);
            this.time = new SimpleStringProperty(time);
        }

        public String getFilename() {
            return filename.get();
        }

        public void setFilename(String newFilename) {
            filename.set(newFilename);
        }

        public String getFilesize() {
            return filesize.get();
        }

        public void setFilesize(String newFilesize) {
            filesize.set(newFilesize);
        }

        public String getTime() {
            return time.get();
        }

        public void setTime(String newTime) {
            time.set(newTime);
        }
    }

    public static class ProteinRecord {
        private final SimpleStringProperty name;
        private final SimpleStringProperty id;
        private final SimpleStringProperty entry;
        private final SimpleStringProperty organism;
        private final SimpleStringProperty gene;
        private final SimpleStringProperty pe;
        private final SimpleStringProperty sv;
        private final SimpleStringProperty sequence;

        private ProteinRecord(String proteinName, String identifier, String entryName, String organismName, String geneName, String PE, String SV, String proteinSequence) {
            this.name = new SimpleStringProperty(proteinName);
            this.id = new SimpleStringProperty(identifier);
            this.entry = new SimpleStringProperty(entryName);
            this.organism = new SimpleStringProperty(organismName);
            this.gene = new SimpleStringProperty(geneName);
            this.pe = new SimpleStringProperty(PE);
            this.sv = new SimpleStringProperty(SV);
            this.sequence = new SimpleStringProperty(proteinSequence);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String newName) {
            name.set(newName);
        }

        public String getId() {
            return id.get();
        }

        public void setId(String newId) {
            id.set(newId);
        }

        public String getEntry() {
            return entry.get();
        }

        public void setEntry(String newEntry) {
            entry.set(newEntry);
        }

        public String getOrganism() {
            return organism.get();
        }

        public void setOrganism(String newOrganism) {
            name.set(newOrganism);
        }

        public String getGene() {
            return gene.get();
        }

        public void setGene(String newGene) {
            gene.set(newGene);
        }

        public String getPe() {
            return pe.get();
        }

        public void setPe(String newPe) {
            pe.set(newPe);
        }

        public String getSv() {
            return sv.get();
        }

        public void setSv(String newSv) {
            sv.set(newSv);
        }

        public String getSequence() {
            return sequence.get();
        }

        public void setSequence(String newSequence) {
            sequence.set(newSequence);
        }
    }
}

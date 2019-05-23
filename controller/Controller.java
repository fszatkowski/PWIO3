package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FastaRecord;
import model.FileUtils;

public class Controller {
	private Stage stage;

	@FXML
	private TextField protein;
	@FXML
	private TextField gene;
	@FXML
	private TextArea sequence;
	@FXML
	private TextField identifier;
	@FXML
	private TextField entry;
	@FXML
	private TextField organism;
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
	private TableView<Result> measurementsTable;
	@FXML
	private TextField savePath;

	private String fastaPath = "";
	private Long fileLength = null;
	private final ObservableList<Result> resultTimes = FXCollections.observableArrayList();
	private final ObservableList<FastaRecord> fastaRecords = FXCollections.observableArrayList();
	private HashMap<String, String> params;
	private FileUtils fileUtils;
	ArrayList<Result> resultFiles = new ArrayList<Result>();

	private int repeatNum = 100;

	@FXML
	public void initialize() {
		// measurments table setup
		measurementsTable.setEditable(true);

		TableColumn<Result, String> fileNameCol = new TableColumn<Result, String>("Nazwa");
		fileNameCol.setPrefWidth(100);
		fileNameCol.setCellValueFactory(new PropertyValueFactory<Result, String>("name"));

		TableColumn<Result, String> fileMethodCol = new TableColumn<Result, String>("Metoda");
		fileMethodCol.setPrefWidth(100);
		fileMethodCol.setCellValueFactory(new PropertyValueFactory<Result, String>("method"));

		TableColumn<Result, String> timeCol = new TableColumn<Result, String>("Czas");
		timeCol.setPrefWidth(100);
		timeCol.setCellValueFactory(new PropertyValueFactory<Result, String>("time"));

		measurementsTable.getColumns().addAll(fileNameCol, fileMethodCol, timeCol);
		measurementsTable.setItems(resultTimes);

		// results table setup
		resultsTable.setEditable(true);

		TableColumn<FastaRecord, String> identifierCol = new TableColumn<FastaRecord, String>("ID");
		identifierCol.setPrefWidth(50);
		identifierCol.setCellValueFactory(new PropertyValueFactory<FastaRecord, String>("id"));

		TableColumn<FastaRecord, String> entryNameCol = new TableColumn<FastaRecord, String>("Nazwa");
		entryNameCol.setPrefWidth(100);
		entryNameCol.setCellValueFactory(new PropertyValueFactory<FastaRecord, String>("entry"));

		TableColumn<FastaRecord, String> proteinNameCol = new TableColumn<FastaRecord, String>("Bia³ko");
		proteinNameCol.setPrefWidth(100);
		proteinNameCol.setCellValueFactory(new PropertyValueFactory<FastaRecord, String>("protein"));

		TableColumn<FastaRecord, String> organismNameCol = new TableColumn<FastaRecord, String>("Organizm");
		organismNameCol.setPrefWidth(100);
		organismNameCol.setCellValueFactory(new PropertyValueFactory<FastaRecord, String>("organism"));

		TableColumn<FastaRecord, String> geneNameCol = new TableColumn<FastaRecord, String>("Gen");
		geneNameCol.setPrefWidth(100);
		geneNameCol.setCellValueFactory(new PropertyValueFactory<FastaRecord, String>("gene"));

		TableColumn<FastaRecord, String> sequenceCol = new TableColumn<FastaRecord, String>("Sekwencja");
		sequenceCol.setPrefWidth(150);
		sequenceCol.setCellValueFactory(new PropertyValueFactory<FastaRecord, String>("sequence"));

		resultsTable.getColumns().addAll(identifierCol, entryNameCol, proteinNameCol, organismNameCol, geneNameCol,
				sequenceCol);
		resultsTable.setItems(fastaRecords);
	}

	@FXML
	private void search(ActionEvent actionEvent) throws IOException {

		// check if fasta file is specified
		if (fileUtils == null) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Brak pliku!");
			alert.setHeaderText(null);
			alert.setContentText("Nie wybrano pliku FASTA do przeszukiwania!");
			alert.showAndWait();
			return;
		} else {
			clearTables();
			setParams();
			fileUtils.loadFile();

			int l;
			Result result;
			FileUtils file;
			ArrayList<FastaRecord> resultRecords;
			// stream method
			fileUtils.clearResultTimes();
			for (l = 0; l < repeatNum; l++) {
				fileUtils.searchForRecordsWithStream(params);
			}
			System.out.println("Avarge time stream(): " + getAverage());
			resultFiles.add(new Result(fileUtils.getFilePath(), "stream()", getAverage().toString()));

			// classic method with for loop
			fileUtils.clearResultTimes();
			for (l = 0; l < repeatNum; l++) {
				fileUtils.searchForRecords(params);
			}
			System.out.println("Avarge time for loop: " + getAverage());
			resultFiles.add(new Result(fileUtils.getFilePath(), "for loop", getAverage().toString()));

			// parallelStream method
			fileUtils.clearResultTimes();
			for (l = 0; l < repeatNum; l++) {
				fileUtils.searchForRecordsWithParallelStrem(params);
			}
			System.out.println("Avarge time parallelStream(): " + getAverage());
			resultFiles.add(new Result(fileUtils.getFilePath(), "parallelStream()", getAverage().toString()));

			resultRecords = fileUtils.getResults();
			updateFilesTable(resultFiles);
			updateRecordsTable(resultRecords);

		}
	}

	public Long getAverage() {

		Long avg;
		long sum = 0;
		for (Long time : fileUtils.getResultTimes()) {
			sum += time;
		}
		avg = sum / repeatNum;

		return avg;
	}

	@FXML
	private void loadFile(ActionEvent actionEvent) throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Wybierz plik:");
		java.io.File selectedFile = fileChooser.showOpenDialog(stage);
		fastaPath = selectedFile.getAbsolutePath();
		fileLength = selectedFile.length();
		fileUtils = new FileUtils(fastaPath);

		if (!fileUtils.ifFastaExtension()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("NieprawidÅ‚owy format!");
			alert.setHeaderText(null);
			alert.setContentText("Wybrany plik nie jest plikiem FASTA!");
			alert.showAndWait();
			fastaPath = "";
			fileLength = null;
			filePath.setText("");
		}
		filePath.setText(fastaPath);
	}

	private void updateRecordsTable(ArrayList<FastaRecord> resultRecords) {

		for (FastaRecord record : resultRecords) {

			fastaRecords.add(record);
		}
		resultsTable.setItems(fastaRecords);
	}

	private void updateFilesTable(ArrayList<Result> resultFiles) {

		for (Result file : resultFiles) {

			resultTimes.add(file);
		}
		measurementsTable.setItems(resultTimes);
	}

	private void setParams() {

		params = new HashMap<String, String>();
		params.put("identifier", identifier.getText());
		params.put("entry", entry.getText());
		params.put("protein", protein.getText());
		params.put("organism", organism.getText());
		params.put("gene", gene.getText());
		params.put("sequence", sequence.getText());
	}

	private HashMap<String, String> getParams() {
		return params;
	}

	private void clearTables() {
		fastaRecords.clear();
		resultsTable.getItems().clear();
		resultTimes.clear();
	}

	public class Result {

		private SimpleStringProperty time;
		private SimpleStringProperty method;
		private SimpleStringProperty name;

		public Result(String name, String method, String time) {
			this.name = new SimpleStringProperty(name);
			this.method = new SimpleStringProperty(method);
			this.time = new SimpleStringProperty(time);
		}

		public String getName() {
			return name.get();
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public String getTime() {
			return time.get();
		}

		public void setTime(String time) {
			this.time.set(time);
		}

		public String getMethod() {
			return method.get();
		}

		public void setMethod(String method) {
			this.method.set(method);
		}
	}

}

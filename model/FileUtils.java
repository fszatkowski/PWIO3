package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileUtils {

	private SimpleStringProperty filePath;
	private SimpleLongProperty fileSize;
	private SimpleLongProperty fileTime;
	private SimpleStringProperty fileMethod;

	private List fastaRecords;
	private RandomAccessFile raf = null;
	private File fastaFile;
	private BufferedReader reader;
	private ArrayList results = new ArrayList<FastaRecord>();

	public ArrayList getResults() {
		return results;
	}

	public FileUtils(String filePath, String fileMethod, Long fileSize, Long fileTime) throws FileNotFoundException {
		this.filePath = new SimpleStringProperty(filePath);
		this.fileMethod = new SimpleStringProperty(fileMethod);
		if (fileSize != null)
			this.fileSize = new SimpleLongProperty(fileSize);
		if (fileTime != null)
			this.fileTime = new SimpleLongProperty(fileTime);

		raf = new RandomAccessFile(filePath, "rw");
	}

	public FileUtils(FileUtils fileUtils) throws FileNotFoundException {
		this.filePath = new SimpleStringProperty(fileUtils.getFilePath());
		this.fileMethod = new SimpleStringProperty(fileUtils.getFileMethod());
		this.fileSize = new SimpleLongProperty(fileUtils.getFileSize());
		this.fileTime = new SimpleLongProperty(fileUtils.getFileTime());
	}

	public String getFileMethod() {
		return fileMethod.get();
	}

	public String getFilePath() {
		return filePath.get();
	}

	public void setFilePath(String filePath) {
		this.filePath.set(filePath);
	}

	public Long getFileSize() {
		return fileSize.get();
	}

	public void setFileSize(Long fileSize) {
		this.fileSize.set(fileSize);
	}

	public Long getFileTime() {
		return fileTime.get();
	}

	public void setFileTime(Long fileTime) {
		this.fileTime.set(fileTime);
	}

	public boolean ifFastaExtension() {
		int lastIndexOf = filePath.get().lastIndexOf(".");
		if (lastIndexOf == -1) {
			return false;
		}
		return filePath.get().substring(lastIndexOf).equals(".fasta");
	}

	public ArrayList<FastaRecord> loadFile() throws IOException {

		fastaRecords = new ArrayList<FastaRecord>();
		FastaRecord record = new FastaRecord();

		String line = null;
		String lineSeparator = null;
		StringBuilder recordStr = new StringBuilder();
		lineSeparator = getLineSeparator(this.filePath.get());

		this.openFile();

		while ((line = this.reader.readLine()) != null) {
			if (line.length() > 0 && line.charAt(0) == '>') {
				if (recordStr.length() != 0) {
					record = record.parse(recordStr.toString());
					this.fastaRecords.add(record);
				}
				recordStr = new StringBuilder();
			}

			recordStr.append(line);
			recordStr.append("\n\r");
		}
		this.closeInputFile();
		return (ArrayList<FastaRecord>) fastaRecords;
	}

	public void searchForRecordsWithParallelStrem(HashMap<String, String> params) {

		results.clear();
		long startTime = System.currentTimeMillis();
		fastaRecords.parallelStream().forEach(i -> ifMatch(i, params));
		long endTime = System.currentTimeMillis();
		this.fileTime = new SimpleLongProperty(endTime - startTime);
		System.out.println("parallelStream() time: " + fileTime);

		this.fileMethod = new SimpleStringProperty("parallelStream()");
	}

	public void searchForRecordsWithStream(HashMap<String, String> params) {

		results.clear();
		long startTime = System.currentTimeMillis();
		fastaRecords.stream().forEach(i -> ifMatch(i, params));
		long endTime = System.currentTimeMillis();
		this.fileTime = new SimpleLongProperty(endTime - startTime);
		System.out.println("stream() time: " + fileTime);

		this.fileMethod = new SimpleStringProperty("stream()");
	}

	public void searchForRecords(HashMap<String, String> params) {

		results.clear();
		long startTime = System.currentTimeMillis();
		for (Object record : fastaRecords) {
			ifMatch(record, params);
		}
		long endTime = System.currentTimeMillis();
		this.fileTime = new SimpleLongProperty(endTime - startTime);
		System.out.println("for loop time: " + fileTime);

		this.fileMethod = new SimpleStringProperty("for loop");
	}

	public void ifMatch(Object record, HashMap<String, String> params) {

		if (!((FastaRecord) record).getId().contains(params.get("identifier")))
			return;
		if (!((FastaRecord) record).getEntry().contains(params.get("entry")))
			return;
		if (!((FastaRecord) record).getProtein().contains(params.get("protein")))
			return;
		if (!((FastaRecord) record).getOrganism().contains(params.get("organism")))
			return;
		if (!((FastaRecord) record).getGene().contains(params.get("gene")))
			return;
		this.results.add(record);
	}

	public static String getLineSeparator(String srcPath) throws IOException {
		File file = new File(srcPath);
		char current;
		String lineSeparator = "";

		FileInputStream fis = new FileInputStream(file);
		try {
			while (fis.available() > 0) {
				current = (char) fis.read();
				if ((current == '\n') || (current == '\r')) {
					lineSeparator += current;
					if (fis.available() > 0) {
						char next = (char) fis.read();
						if ((next != current) && ((next == '\r') || (next == '\n'))) {
							lineSeparator += next;
						}
					}
					return lineSeparator;
				}
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return null;
	}

	public void openFile() throws FileNotFoundException {

		this.reader = new BufferedReader(new FileReader(filePath.get()));
	}

	private void closeInputFile() throws IOException {
		this.reader.close();
	}
}

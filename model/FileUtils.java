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

import javafx.beans.property.SimpleStringProperty;

public class FileUtils {

	private SimpleStringProperty filePath;

	private ArrayList<FastaRecord> fastaRecords;
	private RandomAccessFile raf = null;
	private File fastaFile;
	private BufferedReader reader;
	private ArrayList<FastaRecord> results = new ArrayList<FastaRecord>();

	private ArrayList resultsList = new ArrayList<ArrayList>();

	private ArrayList<Long> resultTimes = new ArrayList<Long>();

	public ArrayList<Long> getResultTimes() {
		return resultTimes;
	}

	public void clearResultTimes() {
		resultTimes.clear();
	}

	public void setResultTimes(ArrayList<Long> resultTimes) {
		this.resultTimes = resultTimes;
	}

	public ArrayList<FastaRecord> getResults() {
		return fastaRecords;
	}

	public FileUtils(String filePath) throws FileNotFoundException {
		this.filePath = new SimpleStringProperty(filePath);
		raf = new RandomAccessFile(filePath, "rw");
	}

	public FileUtils(FileUtils fileUtils) throws FileNotFoundException {
		this.filePath = new SimpleStringProperty(fileUtils.getFilePath());
	}

	public String getFilePath() {
		return filePath.get();
	}

	public void setFilePath(String filePath) {
		this.filePath.set(filePath);
	}

	public boolean ifFastaExtension() {
		int lastIndexOf = filePath.get().lastIndexOf(".");
		if (lastIndexOf == -1) {
			return false;
		}
		return filePath.get().substring(lastIndexOf).equals(".fasta");
	}

	public ArrayList<ArrayList> loadFile() throws IOException {

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

		// dziele liste na polowe
		int num = fastaRecords.size();
		List<FastaRecord> head = fastaRecords.subList(0, num / 2);
		List<FastaRecord> tail = fastaRecords.subList(num / 2, num);

		resultsList.add(head);
		resultsList.add(tail);

		System.out.println("Number of records: " + num);

		return (ArrayList<ArrayList>) resultsList;
	}

	public void searchForRecordsWithParallelStrem(HashMap<String, String> params) {

		results.clear();
		long startTime = System.currentTimeMillis();
		resultsList.parallelStream().forEach(i -> ifMatch(i, params));
		long endTime = System.currentTimeMillis();
		Long fileTime = new Long(endTime - startTime);

		resultTimes.add(fileTime);
	}

	public void searchForRecordsWithStream(HashMap<String, String> params) {

		results.clear();
		long startTime = System.currentTimeMillis();
		resultsList.stream().forEach(i -> ifMatch(i, params));
		long endTime = System.currentTimeMillis();
		Long fileTime = new Long(endTime - startTime);

		resultTimes.add(fileTime);
	}

	public void searchForRecords(HashMap<String, String> params) {

		results.clear();
		long startTime = System.currentTimeMillis();
		ifMatch(fastaRecords, params);
		long endTime = System.currentTimeMillis();
		Long fileTime = new Long(endTime - startTime);

		resultTimes.add(fileTime);
	}

	public void ifMatch(Object i, HashMap<String, String> params) {

		// // convert Object to ArrayList<FastarRecord>
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("records", i);
		List<FastaRecord> records = (List<FastaRecord>) hashMap.get("records");
		FastaRecord[] recordsList = records.toArray(new FastaRecord[records.size()]);

		for (FastaRecord record : recordsList) {
			if (!((FastaRecord) record).getId().contains(params.get("identifier")))
				break;
			if (!((FastaRecord) record).getEntry().contains(params.get("entry")))
				break;
			if (!((FastaRecord) record).getProtein().contains(params.get("protein")))
				break;
			if (!((FastaRecord) record).getOrganism().contains(params.get("organism")))
				break;
			if (!((FastaRecord) record).getGene().contains(params.get("gene")))
				break;
			this.results.add(record);
		}
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

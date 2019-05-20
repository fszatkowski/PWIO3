package model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleStringProperty;

public class FastaRecord {
	private final SimpleStringProperty id;
	private final SimpleStringProperty entry;
	private final SimpleStringProperty protein;
	private final SimpleStringProperty organism;
	private final SimpleStringProperty gene;
	private final SimpleStringProperty sequence;

	private Map<String, String> paramsMap = null;
	private static final String REGEXP = "\\>([a-zA-Z]*)\\|(.*?)\\|(.*?) (.*?)(?:OS=(.*?))?(?:GN=(.*?))?(?:PE=(.*?))?(?:SV=([1-9]*))?(?:[\n\r](.*))?";
	private static final String[] KEYS = { "identifier", "entryName", "proteinName", "organismName", "geneName",
			"proteinExistence", "sequenceVersion", "sequence" };

	public FastaRecord() {
		this.id = null;
		this.entry = null;
		this.protein = null;
		this.organism = null;
		this.gene = null;
		this.sequence = null;
	}

	public FastaRecord(String identifier, String entry, String protein, String organism, String gene, String sequence) {
		this.id = new SimpleStringProperty(identifier);
		this.entry = new SimpleStringProperty(entry);
		this.protein = new SimpleStringProperty(protein);
		this.organism = new SimpleStringProperty(organism);
		this.gene = new SimpleStringProperty(gene);
		this.sequence = new SimpleStringProperty(sequence);
	}

	public String getId() {
		return id.get();
	}

	public void setId(String id) {
		this.id.set(id);
	}

	public String getEntry() {
		return entry.get();
	}

	public void setEntry(String entry) {
		this.entry.set(entry);
	}

	public String getProtein() {
		return protein.get();
	}

	public void setProtein(String protein) {
		this.protein.set(protein);
	}

	public String getOrganism() {
		return organism.get();
	}

	public void setOrganism(String organism) {
		this.organism.set(organism);
	}

	public String getGene() {
		return gene.get();
	}

	public void setGene(String gene) {
		this.gene.set(gene);
	}

	public String getSequence() {
		return sequence.get();
	}

	public void setSequence(String sequence) {
		this.sequence.set(sequence);
	}

	public FastaRecord parse(String recordStr) {
		FastaRecord fastaRecord = null;

		paramsMap = new HashMap<String, String>();
		Pattern pattern = Pattern.compile(REGEXP, Pattern.DOTALL);
		Matcher m = pattern.matcher(recordStr);
		paramsMap.clear();

		if (m.matches()) {
			for (int i = 0; i < m.groupCount() - 1; i++) {
				paramsMap.put(KEYS[i], m.group(i + 2));
				if (paramsMap.get(KEYS[i]) == null)
					paramsMap.put(KEYS[i], "");
			}
		} else
			return null;

		fastaRecord = new FastaRecord(paramsMap.get("identifier"), paramsMap.get("entryName"),
				paramsMap.get("proteinName"), paramsMap.get("organismName"), paramsMap.get("geneName"),
				paramsMap.get("sequence").replaceAll("\\s", ""));

		return (fastaRecord);
	}
}

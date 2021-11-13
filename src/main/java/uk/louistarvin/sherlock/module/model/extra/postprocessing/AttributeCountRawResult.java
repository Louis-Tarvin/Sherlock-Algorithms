package uk.louistarvin.sherlock.module.model.extra.postprocessing;

import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.AbstractModelTaskRawResult;
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import java.io.Serializable;


public class AttributeCountRawResult extends AbstractModelTaskRawResult {

	private long file1id;
	private long file2id;

	private int file1TotalOperators;
	private int file2TotalOperators;

	private int file1TotalOperands;
	private int file2TotalOperands;

	private int file1UniqueOperators;
	private int file2UniqueOperators;

	private int file1UniqueOperands;
	private int file2UniqueOperands;

	private int file1Lines;
	private int file2Lines;

	private int file1Variables;
	private int file2Variables;

	private int file1ControlStatements;
	private int file2ControlStatements;

	public AttributeCountRawResult(ISourceFile file1, ISourceFile file2) {
		this.file1id = file1.getPersistentId();
		this.file2id = file2.getPersistentId();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean testType(AbstractModelTaskRawResult baseline) {
		// TODO Auto-generated method stub
		return baseline instanceof AttributeCountRawResult;
	}	

	public long getFile1id() {
		return file1id;
	}

	public long getFile2id() {
		return file2id;
	}

	public int getFile1TotalOperators() {
		return file1TotalOperators;
	}

	public void setFile1TotalOperators(int file1TotalOperators) {
		this.file1TotalOperators = file1TotalOperators;
	}

	public int getFile2TotalOperators() {
		return file2TotalOperators;
	}

	public void setFile2TotalOperators(int file2TotalOperators) {
		this.file2TotalOperators = file2TotalOperators;
	}

	public int getFile1TotalOperands() {
		return file1TotalOperands;
	}

	public void setFile1TotalOperands(int file1TotalOperands) {
		this.file1TotalOperands = file1TotalOperands;
	}

	public int getFile2TotalOperands() {
		return file2TotalOperands;
	}

	public void setFile2TotalOperands(int file2TotalOperands) {
		this.file2TotalOperands = file2TotalOperands;
	}

	public int getFile1UniqueOperators() {
		return file1UniqueOperators;
	}

	public void setFile1UniqueOperators(int file1UniqueOperators) {
		this.file1UniqueOperators = file1UniqueOperators;
	}

	public int getFile2UniqueOperators() {
		return file2UniqueOperators;
	}

	public void setFile2UniqueOperators(int file2UniqueOperators) {
		this.file2UniqueOperators = file2UniqueOperators;
	}

	public int getFile1UniqueOperands() {
		return file1UniqueOperands;
	}

	public void setFile1UniqueOperands(int file1UniqueOperands) {
		this.file1UniqueOperands = file1UniqueOperands;
	}

	public int getFile2UniqueOperands() {
		return file2UniqueOperands;
	}

	public void setFile2UniqueOperands(int file2UniqueOperands) {
		this.file2UniqueOperands = file2UniqueOperands;
	}

	public int getFile1Lines() {
		return file1Lines;
	}

	public void setFile1Lines(int file1Lines) {
		this.file1Lines = file1Lines;
	}

	public int getFile2Lines() {
		return file2Lines;
	}

	public void setFile2Lines(int file2Lines) {
		this.file2Lines = file2Lines;
	}

	public int getFile1Variables() {
		return file1Variables;
	}

	public void setFile1Variables(int file1Variables) {
		this.file1Variables = file1Variables;
	}

	public int getFile2Variables() {
		return file2Variables;
	}

	public void setFile2Variables(int file2Variables) {
		this.file2Variables = file2Variables;
	}

	public int getFile1ControlStatements() {
		return file1ControlStatements;
	}

	public void setFile1ControlStatements(int file1ControlStatements) {
		this.file1ControlStatements = file1ControlStatements;
	}

	public int getFile2ControlStatements() {
		return file2ControlStatements;
	}

	public void setFile2ControlStatements(int file2ControlStatements) {
		this.file2ControlStatements = file2ControlStatements;
	}
}

package com.moto.common.util.excelWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;



// TODO: Auto-generated Javadoc
/**
 * The Class ExcelFromObject.
 */
public class ExcelObject extends ExcelWriter {

	/**
	 * Instantiates a new excel from object.
	 *
	 * @param path the path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ExcelObject(String path) throws IOException {
		super(path);
	}

	/**
	 * Instantiates a new excel from object.
	 *
	 * @param path the path
	 * @param overrideOldFile the override old file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ExcelObject(String path, boolean overrideOldFile)
			throws IOException {
		super(path, overrideOldFile);
	}

	/**
	 * Instantiates a new excel from object.
	 *
	 * @param path the path
	 * @param sheetLimit the sheet limit
	 * @param overrideOldFile the override old file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ExcelObject(String path, int sheetLimit, boolean overrideOldFile)
			throws IOException {
		super(path, sheetLimit, overrideOldFile);
	}

	/**
	 * Instantiates a new excel from object.
	 *
	 * @param path the path
	 * @param sheetLimit the sheet limit
	 * @param sheetName the sheet name
	 * @param overrideOldFile the override old file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ExcelObject(String path, int sheetLimit, String sheetName,
			boolean overrideOldFile) throws IOException {
		super(path, sheetLimit, sheetName, overrideOldFile);
	}

	/**
	 * Adds the rows.
	 *
	 * @param objList the obj list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addRows(List<Object> objList) throws IOException {
		if (null == objList || objList.size() <= 0)
			throw new NullPointerException("Invalid input");
		for (Object obj : objList) {
			writeToExcel(obj);
		}
	}

	/**
	 * Adds the row.
	 *
	 * @param obj the obj
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addRow(Object obj) throws IOException {
		if (null == obj)
			throw new NullPointerException("Invalid input");
		writeToExcel(obj);
	}

	/**
	 * Adds the rows.
	 *
	 * @param objList the obj list
	 * @param sheetName the sheet name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addRows(List<Object> objList, String sheetName)
			throws IOException {
		if (null == objList || objList.size() <= 0)
			throw new NullPointerException("Invalid input");
		this.sheetName = sheetName;
		for (Object obj : objList) {
			writeToExcel(obj);
		}
	}

	/**
	 * Adds the row.
	 *
	 * @param obj the object
	 * @param sheetName the sheet name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addRow(Object obj, String sheetName) throws IOException {
		if (null == obj)
			throw new NullPointerException("Invalid input");
		this.sheetName = sheetName;
		writeToExcel(obj);
	}

	/**
	 * Write to excel.
	 *
	 * @param obj the object
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeToExcel(Object obj) throws IOException {

		int rowNum = getRowCount(this.sheetName);
		if (this.sheetLimit <= rowNum)
			throw new IOException("Sheet limt exceeded");
		if (rowNum == 0) {
			createHeader(obj, rowNum);
			rowNum++;
		}
		writeDataToSheet(obj, rowNum);
	}

	/**
	 * Creates the header.
	 *
	 * @param obj the object
	 * @param rowNum the row number
	 */
	private void createHeader(Object obj, int rowNum) {
		Row rowhead = this.sheet.createRow(rowNum);
		int cellnum = 0;
		try {
			for (Field f : obj.getClass().getDeclaredFields()) {
				rowhead.createCell(cellnum++).setCellValue(
						f.getName().toUpperCase());

			}
		} catch (Exception e) {
			//Logger.errorMessageStep(e.getMessage());
		}
	}

	/**
	 * Write data to sheet.
	 *
	 * @param obj the object
	 * @param rowNum the row number
	 */
	private void writeDataToSheet(Object obj, int rowNum) {
		Row row = this.sheet.createRow(rowNum++);
		int cellnum = 0;
		try {
			for (Field field : obj.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				Cell cell = row.createCell(cellnum++);
				cell.setCellValue(field.get(obj).toString());
			}
		} catch (Exception exp) {
			//Logger.errorMessageStep(exp.getMessage());
		}
	}
	
	
	/**
	 * Update row.
	 *
	 * @param obj the object
	 * @param keyColumn the key column
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void updateRow(Object obj, String keyColumn) throws IOException {
		Row header = this.sheet.getRow(0);
		Object colValue = "";

		int colIndex = -1;
		for (Cell cell : header) {
			if (cell.getStringCellValue().equalsIgnoreCase(keyColumn)) {
				colIndex = cell.getColumnIndex();
			}
		}

		Class<?> c = obj.getClass();
		Field field;
		try {
			field = c.getDeclaredField(keyColumn);
			field.setAccessible(true);
			colValue = field.get(obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int rowNum = -1;

		for (Row row : this.sheet) {
			Cell cell = row.getCell(colIndex);

			if (cell.getStringCellValue().equalsIgnoreCase(colValue.toString())) {
				rowNum = row.getRowNum();
	
			}
		
			if (rowNum > -1)
				writeDataToSheet(obj, rowNum);
			//Logger.log("The Row updated");
		}

	}

	/**
	 * Update row.
	 *
	 * @param obj the object
	 * @param keyColumn the key column
	 * @param sheetName the sheet name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void updateRow(Object obj, String keyColumn, String sheetName)
			throws IOException {

		this.sheetName = sheetName;
		updateRow(obj, keyColumn);
		
	}
}

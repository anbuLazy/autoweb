package com.moto.common.util.excelWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

// TODO: Auto-generated Javadoc
/**
 * The Class ExcelFromMap.
 */
public class ExcelMap extends ExcelWriter {

	/** The headermap. */
	private Map<String, String> headermap = new HashMap<String, String>();

	/** The coloumn num. */
	private int coloumnNum = 0;

	/**
	 * Instantiates a new excel from map.
	 * 
	 * @param path
	 *            the path
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ExcelMap(String path) throws IOException {
		super(path);
	}

	/**
	 * Instantiates a new excel from map.
	 * 
	 * @param path
	 *            the path
	 * @param overrideOldFile
	 *            the override old file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ExcelMap(String path, boolean overrideOldFile) throws IOException {
		super(path, overrideOldFile);
	}

	/**
	 * Instantiates a new excel from map.
	 * 
	 * @param path
	 *            the path
	 * @param sheetLimit
	 *            the sheet limit
	 * @param overrideOldFile
	 *            the override old file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ExcelMap(String path, int sheetLimit, boolean overrideOldFile)
			throws IOException {
		super(path, sheetLimit, overrideOldFile);
	}

	/**
	 * Instantiates a new excel from map.
	 * 
	 * @param path
	 *            the path
	 * @param sheetLimit
	 *            the sheet limit
	 * @param sheetName
	 *            the sheet name
	 * @param overrideOldFile
	 *            the override old file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ExcelMap(String path, int sheetLimit, String sheetName,
			boolean overrideOldFile) throws IOException {
		super(path, sheetLimit, sheetName, overrideOldFile);
	}

	/**
	 * Adds the rows to excel.
	 * 
	 * @param rowsList
	 *            the rows list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addRowsToExcel(List<Map<String,String>> rowsList) throws IOException {
		if (null == rowsList || rowsList.size() <= 0)
			throw new NullPointerException("Invalid input");
		for (Map<String, String> rowMap : rowsList) {
			writeToExcel(rowMap, 0, false);
		}
		createHeaderForMap();

	}

	/**
	 * Adds the row.
	 * 
	 * @param rowMap
	 *            the row map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addRow(Map<String, String> rowMap) throws IOException {
		if (null == rowMap)
			throw new NullPointerException("Invalid input");
		writeToExcel(rowMap, 0, false);
		createHeaderForMap();
	}

	/**
	 * Adds the rows.
	 * 
	 * @param rowsList
	 *            the rows list
	 * @param sheetName
	 *            the sheet name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addRows(List<Map<String,String>> rowsList, String sheetName)
			throws IOException {
		if (null == rowsList || rowsList.size() <= 0)
			throw new NullPointerException("Invalid input");
		this.sheetName = sheetName;
		for (Map<String, String> rowMap : rowsList) {
			writeToExcel(rowMap, 0, false);
		}
		createHeaderForMap();
	}

	/**
	 * Adds the row.
	 * 
	 * @param rowMap
	 *            the row map
	 * @param sheetName
	 *            the sheet name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addRow(Map<String,String> rowMap, String sheetName) throws IOException {
		if (null == rowMap)
			throw new NullPointerException("Invalid input");
		this.sheetName = sheetName;
		writeToExcel(rowMap, 0, false);
		createHeaderForMap();
	}

	/**
	 * Write to excel.
	 * 
	 * @param detailsMap
	 *            the details map
	 * @param rowNumber
	 *            the row number
	 * @param update
	 *            the update
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeToExcel(Map<String, String> detailsMap, int rowNumber,
			boolean update) throws IOException {

		int rowNum = getRowCount(this.sheetName);
		Row rowData;
		if (sheetLimit <= rowNum && !update)
			throw new IOException("Sheet limt exceeded");

		if (rowNum == 0 && !update) {
			rowNum++;
			coloumnNum = 0;
			headermap = new HashMap<String, String>();
		}
		if (!update)
			rowData = sheet.createRow(rowNum);
		else
			rowData = sheet.getRow(rowNumber);
		for (String key : detailsMap.keySet()) {
			Cell cell = null;
			if (!headermap.containsKey(key)) {

				headermap.put(key, coloumnNum + "");
				cell = rowData.createCell(coloumnNum);
				coloumnNum++;
			} else {
				cell = rowData.createCell(Integer.parseInt(headermap.get(key)));

			}
			Object value = detailsMap.get(key);
			cell.setCellValue(value.toString());

		}
	}

	/**
	 * Creates the header for map.
	 */
	private void createHeaderForMap() {
		Row rowhead = this.sheet.createRow(0);
		try {
			// writing header
			for (String f : headermap.keySet()) {
				rowhead.createCell(Integer.parseInt(headermap.get(f)))
						.setCellValue(f);
			}

		} catch (Exception e) {
			// Log Exception
		}
	}

	/**
	 * Update row.
	 * 
	 * @param detailsMap
	 *            the details map
	 * @param keyColumn
	 *            the key column
	 * @param sheetName
	 *            the sheet name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void updateRow(Map<String, String> detailsMap, String keyColumn,
			String sheetName) throws IOException {

		this.sheetName = sheetName;
		this.sheet = this.workbook.getSheet(sheetName);
		updateRow(detailsMap, keyColumn);
	}

	/**
	 * Update row.
	 * 
	 * @param detailsMap
	 *            the details map
	 * @param keyColumn
	 *            the key column
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void updateRow(Map<String, String> detailsMap, String keyColumn)
			throws IOException {

		int colIndex = -1;
		String value = detailsMap.get(keyColumn);
		// Get the column number
		Row rowHead = sheet.getRow(0);
		for (Cell cell : rowHead) {
			if (cell.getStringCellValue().equalsIgnoreCase(keyColumn)) {
				colIndex = cell.getColumnIndex();
				break;
			}
		}
		int totalrows = getRowCount(sheetName);
		int rowNum = -1;
		getHeaderMap(sheetName);
		if (colIndex > -1) {
			for (int i = 1; i < totalrows; i++) {
				Row row = this.sheet.getRow(i);
				Cell cell = row.getCell(colIndex);
				if (cell.getStringCellValue().equalsIgnoreCase(value)) {
					rowNum = row.getRowNum();
					if (rowNum > -1) {
						writeToExcel(detailsMap, rowNum, true);
						break;
					}
				}

			}
		}
		createHeaderForMap();
	}

	/**
	 * Gets the header map.
	 * 
	 * @param sheetname
	 *            the sheet name
	 * @return the header map
	 */
	private void getHeaderMap(String sheetname) {
		int totalRows = getRowCount(sheetName);

		if (totalRows > 0) {
			headermap = new HashMap<String, String>();
			Row rowhead = this.sheet.getRow(0);

			for (Cell cell : rowhead) {

				if (!headermap.containsKey(cell.getStringCellValue())) {
					headermap.put(cell.getStringCellValue(),
							cell.getColumnIndex() + "");
				}
			}
		}
	}
}

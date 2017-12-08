package com.moto.common.util.excelWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * The Class ExcelWriter.
 * 
 * */
public abstract class ExcelWriter {

	protected int sheetLimit;
	protected String sheetName;
	protected String path;
	protected String fileName;
	protected Workbook workbook;
	protected Sheet sheet;
	protected boolean overrideFlag;
	protected String srcFolder;

	/**
	 * Instantiates a new excel writer.
	 * 
	 * @param path
	 *            the path
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ExcelWriter(String path) throws IOException {
		this.sheetName = "Sheet";
		this.overrideFlag = true;
		getfileNameWithTimeStamp(path, 0);
		createFile();
	}

	/**
	 * Instantiates a new excel writer.
	 * 
	 * @param path
	 *            the path
	 * @param overrideOldFile
	 *            the override old file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ExcelWriter(String path, boolean overrideOldFile) throws IOException {
		this.sheetName = "Sheet";
		this.overrideFlag = overrideOldFile;
		getfileNameWithTimeStamp(path, 0);
		createFile();
	}

	/**
	 * Instantiates a new excel writer.
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
	public ExcelWriter(String path, int sheetLimit, boolean overrideOldFile)
			throws IOException {
		this.sheetName = "Sheet";
		this.overrideFlag = overrideOldFile;
		getfileNameWithTimeStamp(path, sheetLimit);
		createFile();
	}

	/**
	 * Instantiates a new excel writer.
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
	public ExcelWriter(String path, int sheetLimit, String sheetName,
			boolean overrideOldFile) throws IOException {
		this.sheetName = sheetName;
		this.overrideFlag = overrideOldFile;
		getfileNameWithTimeStamp(path, sheetLimit);
		createFile();
	}

	public ExcelWriter(String srcFolder, String destFile)
			throws IOException {
		this.srcFolder = srcFolder;
		this.overrideFlag = true;
		getfileNameWithTimeStamp(destFile, 0);
		createFile();
	}

	/**
	 * Save workbook.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void saveWorkbook() throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(new File(this.path));
		this.workbook.write(out);
		out.close();
	}

	/**
	 * Gets the row count.
	 * 
	 * @param sheetName
	 *            the sheet name
	 * @return the row count
	 */
	public int getRowCount(String sheetName) {
		int index = workbook.getSheetIndex(sheetName);
		if (index == -1) {
			this.sheet = this.workbook.createSheet(sheetName);
			return 0;
		} else {
			this.sheet = workbook.getSheetAt(index);
			int number = this.sheet.getLastRowNum() + 1;
			return number;
		}
	}

	/**
	 * Gets the file name with time stamp.
	 * 
	 * @param path
	 *            the path
	 * @param sheetLimit
	 *            the sheet limit
	 * @return the file name with time stamp
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void getfileNameWithTimeStamp(String path, int sheetLimit)
			throws IOException {
		File file = new File(path);
		String fileName = file.getName();

		if (!fileName.endsWith(".xls") && !fileName.endsWith(".XLS")
				&& !fileName.endsWith(".xlsx") && !fileName.endsWith(".XLSX")) {
			fileName = fileName + ".xls";
		}

		if ((fileName.endsWith(".xls") || fileName.endsWith(".XLS"))
				&& sheetLimit <= 0) {
			sheetLimit = 65500;
		} else if ((fileName.endsWith(".xlsx") || fileName.endsWith(".XLSX"))
				&& sheetLimit <= 0) {
			sheetLimit = 1000000;
		}
		if (!overrideFlag) {
			String timestamp = new Timestamp(System.currentTimeMillis())
					.toString();
			timestamp = timestamp.substring(0, timestamp.length() - 6)
					.replaceAll(":", "");
			fileName = fileName.replace(".", timestamp + ".");
		}
		this.fileName = fileName;
		this.path = path.replace(file.getName(), fileName);
		this.sheetLimit = sheetLimit;
	}

	/**
	 * Creates the file.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void createFile() throws FileNotFoundException, IOException {
		if (this.fileName.endsWith(".xls") || this.fileName.endsWith(".XLS")) {
			this.workbook = new HSSFWorkbook();

		} else if (this.fileName.endsWith(".xlsx")
				|| this.fileName.endsWith(".XLSX")) {
			this.workbook = new XSSFWorkbook();
		}
		saveWorkbook();
	}

}

package com.moto.common.util.excelWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMapToExcel {
	private static final Logger logger = LogManager.getLogger(TestMapToExcel.class);

	ExcelMap excelWriter = null;

	@Test
	void init() {

		try {
			logger.info("Calling ExcelWriter");
			excelWriter = new ExcelMap("./output/TestMap.xlsx", 10,
					"TestSheet", true);
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods = "init")
	public void TestAddRows() {
		logger.info("Calling AddRows");
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("STR", "Lek");
		map.put("BOO", String.valueOf(true));
		map.put("Int", String.valueOf(100));
		map.put("Char", String.valueOf('a'));
		list.add(map);

		map = new LinkedHashMap<String,String>();
		map.put("STR", "AAA");
		map.put("BOO", String.valueOf(false));
		map.put("Int", String.valueOf(15));
		list.add(map);

		map = new LinkedHashMap<String,String>();
		map.put("STR", "BBB");
		map.put("BOO", String.valueOf(new Boolean(true)));
		map.put("Int", String.valueOf(124));
		map.put("Double", String.valueOf(new Double(222.12)));
		map.put("double", String.valueOf(333.33));
		map.put("Integer", String.valueOf(new Integer(12)));
		list.add(map);

		try {
			excelWriter.addRowsToExcel(list);
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods = "init")
	public void TestAddRowsToNewSheet() {
		logger.info("Calling AddRows with Sheet Name");
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("STR", "CCC");
		map.put("BOO", String.valueOf(true));
		map.put("Int", String.valueOf(222));
		map.put("Char", String.valueOf('p'));
		list.add(map);

		map = new LinkedHashMap<String,String>();
		map.put("STR", "DDD");
		map.put("BOO", String.valueOf(false));
		map.put("Int", String.valueOf(10));
		map.put("Char", String.valueOf('s'));
		list.add(map);

		map = new LinkedHashMap<String,String>();
		map.put("STR", "EEE");
		map.put("BOO", String.valueOf(new Boolean(true)));
		map.put("Int", String.valueOf(125));
		map.put("Double", String.valueOf(new Double(222.22)));
		map.put("double", String.valueOf(333.33));
		map.put("Integer", String.valueOf(new Integer(12)));
		list.add(map);

		try {
			excelWriter.addRows(list, "MyTestSheet");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods = { "TestAddRowsToNewSheet" })
	public void TestAddrow() {

		logger.info("Calling AddRows");
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("STR", "FFF");
		map.put("BOO", String.valueOf(true));
		map.put("Int", String.valueOf(222));
		map.put("Char", String.valueOf('p'));

		try {
			excelWriter.addRow(map);
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods = { "TestAddrow" })
	public void TestAddRowWithSheetName() {

		logger.info("Calling AddRows with sheet Name");
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("STR", "GGG");
		map.put("BOO", String.valueOf(false));
		map.put("Int", String.valueOf(100));
		map.put("Double", String.valueOf(19.80));

		try {
			excelWriter.addRow(map, "MyTestSheet2");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods = { "TestAddRowWithSheetName" })
	public void TestUpdateRowWithSheetName() {

		logger.info("Calling UpdateRows with sheet Name");
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("STR", "HHH");
		map.put("BOO", String.valueOf(new Boolean(false)));
		map.put("Int", String.valueOf(388));
		map.put("Double", String.valueOf(new Double(222.12)));

		try {
			excelWriter.updateRow(map, "STR", "MyTestSheet");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods = { "TestUpdateRowWithSheetName" })
	public void TestUpdateRow() {

		logger.info("Calling UpdateRows with sheet Name");
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("STR", "III");
		map.put("BOO", String.valueOf(new Boolean(true)));
		map.put("Int", String.valueOf(388));
		map.put("Double", String.valueOf(new Double(77.22)));

		try {
			excelWriter.updateRow(map, "STR");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods = { "TestUpdateRow" })
	public void TestSaveWorkbook() {

		logger.info("Calling Save workbook");
		try {
			excelWriter.saveWorkbook();
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}
}
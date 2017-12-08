package com.moto.common.util.excelWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;



public class TestObjectToExcelWriter {
	private static final Logger logger = LogManager.getLogger(TestObjectToExcelWriter.class);

	ExcelObject excelWriter = null;
	MyObject obj1, obj2, obj3, obj4 = null;

	@Test
	void init() {
		obj1 = new MyObject();
		obj1.setBooObj(true);
		obj1.setBooPri(false);
		obj1.setCharPri('G');
		obj1.setDate(new Date());
		obj1.setDouObj(222.12);
		obj1.setDouPri(333.23);
		obj1.setIntObj(123);
		obj1.setLongPri(1232233);
		obj1.setIntPri(213123);
		obj1.setShortPri(new Integer(12).shortValue());
		obj1.setStr("Gopal");
		obj2 = new MyObject();
		obj2.setBooObj(true);
		obj2.setBooPri(false);
		obj2.setCharPri('K');
		obj2.setDate(new Date());
		obj2.setDouObj(222.12);
		obj2.setDouPri(333.23);
		obj2.setIntObj(12334);
		obj2.setLongPri(1232233);
		obj2.setIntPri(213123);
		obj2.setShortPri(new Integer(12).shortValue());
		obj2.setStr("Krishna");
		obj3 = new MyObject();
		obj3.setBooObj(true);
		obj3.setBooPri(false);
		obj3.setCharPri('R');
		obj3.setDate(new Date());
		obj3.setDouObj(222.12);
		obj3.setDouPri(333.23);
		obj3.setIntObj(1234);
		obj3.setLongPri(1232233);
		obj3.setIntPri(213123);
		obj3.setShortPri(new Integer(12).shortValue());
		obj3.setStr("Raju");
		obj4 = new MyObject();
		obj4.setBooObj(true);
		obj4.setBooPri(false);
		obj4.setCharPri('V');
		obj4.setDate(new Date());
		obj4.setDouObj(222.12);
		obj4.setDouPri(333.23);
		obj4.setIntObj(12334);
		obj4.setLongPri(1232233);
		obj4.setIntPri(213123);
		obj4.setShortPri(new Integer(12).shortValue());
		obj4.setStr("Vegesna");
		try {
			logger.info("Calling ExcelWriter");
			excelWriter = new ExcelObject("./output/TestObj.xlsx", 30,
					"TestSheet", true);
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods={"init"})
	public void TestAddRows() {
		logger.info("Calling AddRows");
		List<Object> list = new ArrayList<Object>();
		list.add(obj1);
		list.add(obj2);
		list.add(obj3);
		list.add(obj4);
		try {
			excelWriter.addRows(list);
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods={"init"})
	public void TestAddRowsToNewSheet() {
		logger.info("Calling AddRows with Sheet Name");
		List<Object> list = new ArrayList<Object>();
		list.add(obj1);
		list.add(obj2);
		try {
			excelWriter.addRows(list, "MyTestSheet");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods={"init"})
	public void TestAddrow() {

		logger.info("Calling AddRows");
		try {
			excelWriter.addRow(obj3);
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods={"init"})
	public void TestAddrowWithSheetName() {

		logger.info("Calling AddRows with sheet Name");
		try {
			excelWriter.addRow(obj4, "MyTestSheet");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	@Test(dependsOnMethods={"TestAddrowWithSheetName"})
	public void TestUpdateRowValue() {

		try {
			MyObject obj2 = new MyObject();
			obj2.setBooObj(true);
			obj2.setBooPri(false);
			obj2.setCharPri('P');
			obj2.setDate(new Date());
			obj2.setDouObj(777.12);
			obj2.setDouPri(888.23);
			obj2.setIntObj(123);
			obj2.setLongPri(1232233);
			obj2.setIntPri(213123);
			obj2.setShortPri(new Integer(12).shortValue());
			obj2.setStr("Gopal");
			excelWriter.updateRow(obj2,"intObj","MyTestSheet");
			
			MyObject obj3 = new MyObject();
			obj3.setBooObj(true);
			obj3.setBooPri(false);
			obj3.setCharPri('A');
			obj3.setDate(new Date());
			obj3.setDouObj(111.12);
			obj3.setDouPri(222.23);
			obj3.setIntObj(33333);
			obj3.setLongPri(1232233);
			obj3.setIntPri(213123);
			obj3.setShortPri(new Integer(12).shortValue());
			obj3.setStr("Raju");
			excelWriter.updateRow(obj3,"str");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}
	
	@Test(dependsOnMethods={"TestUpdateRowValue"})
	public void TestSaveWorkbook() {

		logger.info("Calling Save workbook");
		try {
			excelWriter.saveWorkbook();
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		}
	}
}

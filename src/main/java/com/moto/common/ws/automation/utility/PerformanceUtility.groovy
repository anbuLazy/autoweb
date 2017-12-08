package com.moto.common.ws.automation.utility;

import java.awt.Color
import java.awt.Shape
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.axis.NumberTickUnit
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.ValueMarker
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.Day
import org.jfree.data.time.RegularTimePeriod
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.ui.ApplicationFrame
import org.jfree.util.ShapeUtilities

class PerformanceUtility extends ApplicationFrame{
	
	private static XYSeries aboveThreshold = new XYSeries("Above Threshold")
	
	private static generatePerformanceGraphs(String reportTitle, List<List> outputData, double threshold) {
		String perfInfo = ""
		for (int i = 0; i < outputData.size(); i+=5) {
			String scenarioNumber = outputData[i][0]; String url = outputData[i][1]; String responseTime = outputData[i+3][0] + "s"; String expectedStatus = outputData[i+4][0]
			String image = generatePassFailGraphic(outputData[i+3][0], threshold, scenarioNumber)
			if (outputData[i][2] == "POST" && image.contains("red")) {
				perfInfo = perfInfo + """<p class="row text-center"><img src="$image" alt="Above Threshold" width="10" height="10"><b>&emsp; Scenario $scenarioNumber</b> &emsp;&emsp;<a href="../inputJson/${scenarioNumber}.json" target="_blank">POST ($responseTime)</a>&emsp;&emsp;  $url &emsp;&emsp; Expected Status: $expectedStatus</p>"""
			} else if (outputData[i][2] == "PUT" && image.contains("red")) {
				perfInfo = perfInfo + """<p class="row text-center"><img src="$image" alt="Above Threshold" width="10" height="10"><b>&emsp; Scenario $scenarioNumber</b> &emsp;&emsp;<a href="../inputJson/${scenarioNumber}.json" target="_blank">POST ($responseTime)</a>&emsp;&emsp;  $url &emsp;&emsp; Expected Status: $expectedStatus</p>"""
			} else if (image.contains("red")){
				perfInfo = perfInfo + """<p class="row text-center"><img src="$image" alt="Below Threshold" width="10" height="10"><b>&emsp; Scenario $scenarioNumber</b> &emsp;&emsp; GET ($responseTime)</a>&emsp;&emsp;  $url &emsp;&emsp; Expected Status: $expectedStatus</p>"""
			} else {
				perfInfo = perfInfo + ""
			}
		}
		createAllDataChart(outputData, reportTitle, threshold)
		createThresholdGraph(aboveThreshold, reportTitle, threshold)
		createAllTimeGraph(reportTitle, threshold)
		createAllTimeAverageGraph(reportTitle, threshold)
		return perfInfo
	}
	
	private static String generatePassFailGraphic (double responseTime, double threshold, String scenarioNumber) {
		if (responseTime > threshold) {
//			if (scenarioNumber.contains("a") || scenarioNumber.contains("b") || scenarioNumber.contains("c") || scenarioNumber.contains("d")) {
//				scenarioNumber = scenarioNumber.substring(0, scenarioNumber.length()-1)
//			}
			scenarioNumber = scenarioNumber.substring(0, scenarioNumber.indexOf("("))
			aboveThreshold.add(Integer.parseInt(scenarioNumber), responseTime)
			return "images/reddot.jpg"
		} else {
			return "images/greendot.jpg"
		}
	}
	
	private static createAllDataChart(List<List> outputData, String reportTitle, double threshold) {
		XYSeriesCollection result = new XYSeriesCollection()
		XYSeries series = new XYSeries("All Data")
		for (int i = 0; i < outputData.size(); i+=5) {
			String scenario = outputData[i][0]
//			if (scenario.contains("a") || scenario.contains("b") || scenario.contains("c") || scenario.contains("d")) {
//				scenario = scenario.substring(0, scenario.length()-1)
//			}
			int scenarioNumber = Integer.parseInt(scenario.substring(0, scenario.indexOf("(")))
			double responseTime = outputData[i+3][0]
			series.add(scenarioNumber, responseTime)
		}
		generateAllResponseTimeFile(series)
		result.addSeries(series)
		JFreeChart chart = ChartFactory.createScatterPlot( "OPS : " + reportTitle, "Scenario Number", "Response Time (sec)", result, PlotOrientation.VERTICAL, false, true, false)
		
		XYPlot xyPlot = (XYPlot) chart.getPlot()
		if (result.getSeriesCount() == 1) {
			NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis()
			domain.setRange(0, result.getDomainUpperBound(false) + 1)
			domain.setTickUnit(new NumberTickUnit(1))
			NumberAxis range = (NumberAxis) xyPlot.getRangeAxis()
			range.setRange(0.0, result.getRangeUpperBound(false) + 0.3)
		}
		xyPlot.setBackgroundPaint(Color.WHITE)
		xyPlot.setDomainGridlinePaint(Color.black)
		xyPlot.setRangeGridlinePaint(Color.black)
		Shape diamond = ShapeUtilities.createDiamond(3)
		XYItemRenderer renderer = xyPlot.getRenderer()
		renderer.setSeriesShape(0, diamond)
		renderer.setSeriesPaint(0, Color.blue)
		ValueMarker marker = new ValueMarker(threshold)
		marker.setPaint(Color.red)
		xyPlot.addRangeMarker(marker)
		File file = new File("results/images/all" + reportTitle + ".jpg")
		ChartUtilities.saveChartAsJPEG(file, chart, 600, 400)
	}
	
	private static createThresholdGraph(XYSeries aboveThreshold, String reportTitle, double threshold) {
		XYSeriesCollection result = new XYSeriesCollection()
		result.addSeries(aboveThreshold)
		JFreeChart chart = ChartFactory.createScatterPlot( "OPS Threshold (" + threshold + "s) Failures", "Scenario Number", "Response Time (sec)", result, PlotOrientation.VERTICAL, false, true, false)
	
		XYPlot xyPlot = (XYPlot) chart.getPlot()
		if (result.getSeriesCount() == 1) {
			NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis()
			domain.setRange(0, result.getDomainUpperBound(false) + 1)
			domain.setTickUnit(new NumberTickUnit(1))
			NumberAxis range = (NumberAxis) xyPlot.getRangeAxis()
			range.setRange(0.0, result.getRangeUpperBound(false) + 0.3)
		}
		xyPlot.setBackgroundPaint(Color.WHITE)
		xyPlot.setDomainGridlinePaint(Color.black)
		xyPlot.setRangeGridlinePaint(Color.black)
		Shape diamond = ShapeUtilities.createDiamond(3)
		XYItemRenderer renderer = xyPlot.getRenderer()
		renderer.setSeriesShape(0, diamond)
		renderer.setSeriesPaint(0, Color.red)
		ValueMarker marker = new ValueMarker(threshold)
		marker.setPaint(Color.red)
		xyPlot.addRangeMarker(marker)
		File file = new File("results/images/threshold" + reportTitle + ".jpg")
		ChartUtilities.saveChartAsJPEG(file, chart, 600, 400)
	}
	
	private static createAllTimeGraph(String reportTitle, double threshold) {
		XYSeriesCollection result = new XYSeriesCollection()
		XYSeries xySeries = generateXYCollectionAllTime()
		result.addSeries(xySeries)
		JFreeChart chart = ChartFactory.createScatterPlot("OPS All-Time Response Times", "Date", "Response Time (sec)", result, PlotOrientation.VERTICAL, false, true, false)
	
		XYPlot xyPlot = (XYPlot) chart.getPlot()
		if (xySeries.getItemCount() == 1) {
			DateAxis xAxis = new DateAxis("Date")
			xAxis.setRange(0, result.getDomainUpperBound(false) + 1)
			xAxis.setTickUnit(new NumberTickUnit(1))
			NumberAxis range = (NumberAxis) xyPlot.getRangeAxis()
			range.setRange(0.0, result.getRangeUpperBound(false) + 0.3)
			xyPlot.setDomainAxis(xAxis)
		}
		DateAxis xAxis = new DateAxis("Date")
		xyPlot.setDomainAxis(xAxis)
		xyPlot.setBackgroundPaint(Color.WHITE)
		xyPlot.setDomainGridlinePaint(Color.black)
		xyPlot.setRangeGridlinePaint(Color.black)
		Shape diamond = ShapeUtilities.createDiamond(3)
		XYItemRenderer renderer = xyPlot.getRenderer()
		renderer.setSeriesShape(0, diamond)
		renderer.setSeriesPaint(0, Color.blue)
		ValueMarker marker = new ValueMarker(threshold)
		marker.setPaint(Color.red)
		xyPlot.addRangeMarker(marker)
		File file = new File("results/images/alltime" + reportTitle + ".jpg")
		ChartUtilities.saveChartAsJPEG(file, chart, 600, 400)
	}
	
	private static createAllTimeAverageGraph(String reportTitle, double threshold) {
		XYSeriesCollection result = new XYSeriesCollection()
		XYSeries xySeries = generateXYCollectionAverageAllTime()
		result.addSeries(xySeries)
		JFreeChart chart = ChartFactory.createXYLineChart("OPS All-Time Average Response Times", "Date", "Response Time (sec)", result, PlotOrientation.VERTICAL, false, true, false)
	
		XYPlot xyPlot = (XYPlot) chart.getPlot()
		if (xySeries.getItemCount() == 1) {
			DateAxis xAxis = new DateAxis("Date")
			xAxis.setRange(0, result.getDomainUpperBound(false) + 1)
			xAxis.setTickUnit(new NumberTickUnit(1))
			NumberAxis range = (NumberAxis) xyPlot.getRangeAxis()
			range.setRange(0.0, result.getRangeUpperBound(false) + 0.3)
			xyPlot.setDomainAxis(xAxis)
		}
		DateAxis xAxis = new DateAxis("Date")
		xyPlot.setDomainAxis(xAxis)
		xyPlot.setBackgroundPaint(Color.WHITE)
		xyPlot.setDomainGridlinePaint(Color.black)
		xyPlot.setRangeGridlinePaint(Color.black)
		Shape diamond = ShapeUtilities.createDiamond(3)
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer()
		renderer.setSeriesShape(0, diamond)
		renderer.setSeriesPaint(0, Color.blue)
		ValueMarker marker = new ValueMarker(threshold)
		marker.setPaint(Color.red)
		xyPlot.addRangeMarker(marker)
		xyPlot.setRenderer(renderer)
		File file = new File("results/images/alltimeaverage" + reportTitle + ".jpg")
		ChartUtilities.saveChartAsJPEG(file, chart, 600, 400)
	}
	
	private static generateAllResponseTimeFile (XYSeries result) {
		File masterFile = new File("results/MasterResponseTime.txt")
		List allData = masterFile.readLines()
		List allNewData = []
		String newData = prepareAllResponseTimes(result)
		for (int i = 0; i < allData.size(); i++) {
			if (allData[i].toString().contains(getCurrentDate())) {
				String updatedData = allData[i].toString() + newData
				allNewData.add(updatedData)
			} else if (i == allData.size() - 1) {
				allNewData.add(allData[i].toString())
				String newDateData = getCurrentDate() + "|" + newData
				allNewData.add(newDateData)
			} else {
				allNewData.add(allData[i].toString())
			}
		}
		regenerateAllData(allNewData)
	}
	
	private static String prepareAllResponseTimes (XYSeries result) {
		String resultString = ""
		for (int i = 0; i < result.getItemCount(); i++) {
			resultString = resultString + result.getY(i) + "|"
		}
		return resultString
	}

	private static regenerateAllData (List allNewData) {
		File masterFile = new File("results/MasterResponseTime.txt")
		def output = new PrintWriter(masterFile, StandardCharsets.UTF_8.name())
		for (int i = 0; i < allNewData.size(); i++) {
			if (i == allNewData.size() - 1) {
				output.print(allNewData[i])	
			} else {
				output.println(allNewData[i])
			}
		}
		output.close()
	}
	
	private static String getCurrentDate() {
		Calendar currDate = Calendar.getInstance()
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy")
		String currentDay = sdf.format(currDate.getTime())
		return currentDay
	}
	
	private static XYSeries generateXYCollectionAllTime () {
		
		XYSeries series = new XYSeries("All Time Data")
		File masterFile = new File("results/MasterResponseTime.txt")
		List allData = masterFile.readLines()
		for (int i = 1; i < allData.size(); i++) {
			List dateSpecificData = allData[i].toString().split("\\|")
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy")
			Date dateFormatted = sdf.parse(dateSpecificData[0].toString())
			long epoch = dateFormatted.getTime()
			for (int j = 1; j < dateSpecificData.size() - 1; j++) {
				double responseTime = Double.parseDouble(dateSpecificData[j])
				series.add(epoch, responseTime)
			}
		}
		return series
	}
	
	private static XYSeries generateXYCollectionAverageAllTime () {
		
		XYSeries series = new XYSeries("All Time Data")
		File masterFile = new File("results/MasterResponseTime.txt")
		List allData = masterFile.readLines()
		for (int i = 1; i < allData.size(); i++) {
			List dateSpecificData = allData[i].toString().split("\\|")
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy")
			Date dateFormatted = sdf.parse(dateSpecificData[0].toString())
			long epoch = dateFormatted.getTime()
			double totalTime = 0
			for (int j = 1; j < dateSpecificData.size(); j++) {
				double responseTime = Double.parseDouble(dateSpecificData[j])
				totalTime = totalTime + responseTime
			}
			series.add(epoch, totalTime/(dateSpecificData.size() - 1))
		}
		return series
	}
}

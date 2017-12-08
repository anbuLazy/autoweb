package com.moto.common.ws.automation.utility;

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ReportUtility {
	private static final Logger logger = LogManager.getLogger(ReportUtility.class);
	
	private static int failNumber = 0
	private static double threshold
	private static String jsonResponse = ""
	private static String linkToJIRA = ""
	
	private static generate (properties, List<List> outputData, double totalTime) {
		threshold = Double.parseDouble(properties.performanceThreshold)
//		jsonResponse = WebserviceUtility.CreateJIRATicket(properties)
//		println "JIRA TICKET CREATED: " + jsonResponse
//		JSONObject actualJSON = new JSONObject(jsonResponse)
//		linkToJIRA = "http://idart.mot.com/browse/" + actualJSON.getString("key")
		String title = createReportName()
		//[Scenario Number, URL, POST(Y/N)] | Failure Count | Errors[Field, Expected, Actual] | Response Time | Expected Status Code |
		int totalTestData = outputData.size()
		int totalTests = totalTestData/5
		int failCountOverall = 0
		String errorData = ""
		for (int i = 0; i < totalTestData; i+=5) {
			if (outputData[i+1][0] != 0) {
				failCountOverall++
			}
		}
		
		if (failCountOverall > 0) {
			errorData = generateErrorDetails (outputData, failCountOverall)
		} else {
			errorData = """<div class="text-center"><img src="images/greendot.jpg" alt="Above Threshold" width="10" height="10">&emsp;No errors to report.</div>"""
		}
		
		double testAvg = (totalTime/totalTests).round(3)
		String total = totalTime + "s for " + totalTests + " tests. Avg: " + testAvg + "s/test."
		generateHTMLReport (title, outputData, errorData, totalTests, failCountOverall, total)
	}
	
	static generateReport (double thresholdLimit, List<List> outputData, double totalTime) {
		threshold = thresholdLimit;
//		jsonResponse = WebserviceUtility.CreateJIRATicket(properties)
//		println "JIRA TICKET CREATED: " + jsonResponse
//		JSONObject actualJSON = new JSONObject(jsonResponse)
//		linkToJIRA = "http://idart.mot.com/browse/" + actualJSON.getString("key")
		String title = createReportName()
		//[Scenario Number, URL, POST(Y/N)] | Failure Count | Errors[Field, Expected, Actual] | Response Time | Expected Status Code |
		int totalTestData = outputData.size()
		int totalTests = totalTestData/5
		int failCountOverall = 0
		String errorData = ""
		for (int i = 0; i < totalTestData; i+=5) {
			if (outputData[i+1][0] != 0) {
				failCountOverall++
			}
		}
		
		if (failCountOverall > 0) {
			errorData = generateErrorDetails (outputData, failCountOverall)
		} else {
			errorData = """<div class="text-center"><img src="images/greendot.jpg" alt="Above Threshold" width="10" height="10">&emsp;No errors to report.</div>"""
		}
		
		double testAvg = (totalTime/totalTests).round(3)
		String total = totalTime + "s for " + totalTests + " tests. Avg: " + testAvg + "s/test."
		generateHTMLReport (title, outputData, errorData, totalTests, failCountOverall, total)
	}
	
	private static String createReportName() {
		Calendar currDate = Calendar.getInstance()
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmmss")
		String filename = sdf.format(currDate.getTime())
		return filename
	}
	
	private static generateErrorDetails (List<List> outputData, int totalFailCount) {
		String failData = ""
		int totalTests = outputData.size()
		for (int i = 0; i < totalTests; i+=5) {
			if (outputData[i+1][0] != 0) {
				failNumber++
				failData = failData + processIndividualFailure(outputData, i, totalFailCount)
			}
		}
		
		return failData
	}
	
	private static String processIndividualFailure(List<List> outputData, int i, int totalFailCount) {
		String errors = ""; String testInfo = ""
		String scenarioNumber = outputData[i][0]; String url = outputData[i][1]; String responseTime = outputData[i+3][0] + "s"; String expectedStatus = outputData[i+4][0]
		
		if (outputData[i][2] == "POST") {
			testInfo = """<br><h4><b>Failure $failNumber of $totalFailCount </b></h4><p><b>Scenario $scenarioNumber &emsp;&emsp;<a href="../inputJson/${scenarioNumber}.json" target="_blank">POST ($responseTime)</a>&emsp;&emsp;  $url &emsp;&emsp; Expected Status: $expectedStatus</b></p>"""
		} else if (outputData[i][2] == "PUT") {
			testInfo = """<br><h4><b>Failure $failNumber of $totalFailCount </b></h4><p><b>Scenario $scenarioNumber &emsp;&emsp;<a href="../inputJson/${scenarioNumber}.json" target="_blank">PUT ($responseTime)</a>&emsp;&emsp;  $url &emsp;&emsp; Expected Status: $expectedStatus</b></p>"""
		} else {
			testInfo = """<br><h4><b>Failure $failNumber of $totalFailCount </b></h4><p><b>Scenario $scenarioNumber &emsp;&emsp; GET ($responseTime)&emsp;&emsp;  $url &emsp;&emsp; Expected Status: $expectedStatus</b></p>"""
			//testInfo = """<br><h1><b>Failure $failNumber of $totalFailCount </b></h1><p><h4><b>Scenario $scenarioNumber</b> &emsp;&emsp; <a href="$linkToJIRA" target="_blank">GET ($responseTime)</a>&emsp;&emsp;  &emsp;&emsp; Expected Status: $expectedStatus</h4></p>"""
		}
		
		int failCount = outputData[i+1][0]
		for (int j = 0; j < failCount; j++) {
			String field = outputData[i+2][(j*3)]
			String expected = outputData[i+2][(j*3)+1]
			String actual = outputData[i+2][(j*3)+2]
			
			// replace < and > in order to avoid html display problems
			if (actual != null) {
				actual = actual.replaceAll('\\<','&lt;');
				actual = actual.replaceAll('\\>','&gt;');
			}
						
			errors = errors + """<p><b><font color="red"><br>&emsp;Field: $field</p><p>&emsp;Expected: $expected &emsp;Actual: $actual</font></b></p>"""
			//errors = errors + """<p><font color="red"><br><h4>&emsp;Field: $field</p><p>&emsp;Expected: $expected &emsp;Actual: $actual</h4></font></p>"""
		}
		
		return testInfo + errors	
	}
		
	private static generateHTMLReport (String reportTitle, List<List> outputData, String errorData, int totalTests, int failCountOverall, String totalTime) {
		int totalPassed = totalTests - failCountOverall
		double passSliderAmount = (totalPassed*400)/totalTests
		int passSlider = (int) Math.round(passSliderAmount)
		int failSlider = 400 - passSlider
		String passFill = "#D1E0FF"; String failFill = "#D1E0FF";
		
		if (totalPassed == 0) {
			passFill = "#FF0000"
		}
		if (failCountOverall == 0) {
			failFill = "#008000"
		}
		
		String title = """<p class="txt_slogan"><i><b>Test Scenarios Executed: $totalTests</i><pass>  $totalPassed</pass><fail>  $failCountOverall</fail></b></p>"""
		String barScript = """
		<script>var c=document.getElementById("1"); var ctx1=c.getContext("2d");
			ctx1.fillStyle="#008000"; ctx1.fillRect(0,0,$passSlider,30);
			ctx1.fillStyle="#FF0000"; ctx1.fillRect($passSlider,0,$failSlider,30); 
			ctx1.font='20px Times'; 
			ctx1.fillStyle= '$passFill'; ctx1.fillText($totalPassed, ${passSlider/2}, 20);
			ctx1.fillStyle= '$failFill'; ctx1.fillText($failCountOverall, ${passSlider + failSlider/2 - 10}, 20); 
		</script>"""
		String performance = PerformanceUtility.generatePerformanceGraphs(reportTitle, outputData, threshold)
		publishHTMLReport (reportTitle, title, barScript, errorData, performance, totalTime)
	}
	
	private static publishHTMLReport(String title, String titleScript, String barScript, String errorData, String performanceData, String totalTime) {
		String html = 
		"""
		<!DOCTYPE html>
	<html lang="en">
    <head>
        <title>Webservice Results $title</title>
        <meta name="keywords" content="" />
		<meta name="description" content="" />

        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <!--<link rel="shortcut icon" href="PUT YOUR FAVICON HERE">-->
        
        <!-- Google Web Font Embed -->
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400,300,300italic,400italic,600,600italic,700,700italic,800,800italic' rel='stylesheet' type='text/css'>
        
        <!-- Bootstrap core CSS -->
        <link href="css/bootstrap.css" rel='stylesheet' type='text/css'>

        <!-- Custom styles for this template -->
        <link href="js/colorbox/colorbox.css"  rel='stylesheet' type='text/css'>
        <link href="css/templatemo_style.css"  rel='stylesheet' type='text/css'>

        <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!--[if lt IE 9]>
          <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
          <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
        <![endif]-->
    </head>
    
    <body>
        <div class="templatemo-top-bar" id="templatemo-top">
            <div class="container">
                <div class="subheader">
                    <p>Web Service Automation Report $title</p>
                </div>
            </div>
        </div>
        <div class="templatemo-top-menu">
            <div class="container">
                <!-- Static navbar -->
                <div class="navbar navbar-default" role="navigation">
                    <div class="container">
                        <div class="navbar-header">
                                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                                <span class="sr-only">Toggle navigation</span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                                </button>
                                
								<div class="navbar-collapse collapse" id="templatemo-nav-bar">
									<ul class="nav navbar-nav navbar-right" style="margin-top: 40px;">
										<li class="active"><a href="#templatemo-carousel">SUMMARY</a></li>
										<li><a href="#templatemo-report">RESULTS</a></li>
										<li><a href="#templatemo-performance">PERFORMANCE</a></li>
										<li><a href="#templatemo-links">LINKS</a></li>
									</ul>
								</div><!--/.nav-collapse -->
                        </div>
                    </div><!--/.container-fluid -->
                </div><!--/.navbar -->
            </div> <!-- /container -->
        </div>
		
		<div>
            <!-- Background -->
            <div id="templatemo-carousel">
                <!-- Indicators -->
                <div class="carousel-inner">
                    <div class="item active">
                        <div class="container">
                            <div class="carousel-caption">
                                <h1 class="txt_darkgrey">Webservice <em>Test Automation</em></h1>
                                <p class="txt_slogan"><i></i></p>
								$titleScript
								<p><i>$totalTime</p>
								<div>
								<canvas id="1" width="400" height="30" style="border:1px solid #D1E0FF;">Your browser does not support the canvas tag.</canvas>
								</div>
								
                            </div>
                        </div>
                    </div>
            </div><!-- /#templatemo-carousel -->
        </div>

       $barScript

        <div class="templatemo-report" id="templatemo-report">
            <div class="container">
                <div class="row">
                    <div class="templatemo-line-header">
                        <div class="text-center">
                            <hr class="team_hr team_hr_left hr_gray"/><span class="txt_darkgrey">&nbsp; &nbsp; &nbsp; &nbsp; RESULTS &nbsp; &nbsp;</span>
                            <hr class="team_hr team_hr_right hr_gray"/>
                        </div>
                    </div>
                </div>
                <div class="clearfix"> </div>
                <div class="text-left">
				$errorData
				</div>
            </div>
        </div><!-- /.templatemo-team -->

        <div id="templatemo-performance" >
            <div class="container">
                <div class="row">
                    <div class="templatemo-line-header" >
                        <div class="text-center">
                            <hr class="team_hr team_hr_left hr_gray"/><span class="txt_darkgrey">PERFORMANCE</span>
                            <hr class="team_hr team_hr_right hr_gray"/>
                        </div>
                    </div>
                    <div class="clearfix"></div>
					<div class="templatemo-gallery-category" style="font-size:16px; margin-top:80px;">
                        <div class="text-center">
                            <a class="active" href=".gallery">All</a> | <a href=".gallery-responseTimes">Overall</a> | <a href=".gallery-resources">API</a> | <a href=".gallery-threshold">Threshold</a> | <a href=".gallery-lifetime">Lifetime</a>							
                        </div>
                    </div>
                </div> <!-- /.row -->


                <div class="clearfix"></div>
                <div class="text-center">
                    <ul class="templatemo-project-gallery" >
                        <li class="col-lg-2 col-md-2 col-sm-2  gallery gallery-responseTimes" >
                            <a class="colorbox" href="images/all${title}.jpg" data-group="gallery-responseTimes">
                                <div class="templatemo-project-box">
                                    <img src="images/all${title}.jpg" class="img-responsive" alt="gallery" />
                                    <div class="project-overlay">
                                        <h5>Automation Report</h5>
                                        <hr />
										<h5>Response Times</h5>
                                    </div>
                                </div>
                            </a>
                        </li>
  
                        
                        <li class="col-lg-2 col-md-2 col-sm-2  gallery gallery-resources" >
                            <a class="colorbox" href="images/progress.jpg" data-group="gallery-resources">
                                <div class="templatemo-project-box">
                                    <img src="images/progress.jpg" class="img-responsive" alt="gallery" />
                                    <div class="project-overlay">
                                        <h5>Average API</h5>
                                        <hr />
                                        <h5>Response Times</h5>
                                    </div>
                                </div>
                            </a>
                        </li>
                        
                        <li class="col-lg-2 col-md-2 col-sm-2 gallery gallery-threshold" >
                            <a class="colorbox" href="images/threshold${title}.jpg" data-group="gallery-threshold">
                                <div class="templatemo-project-box">
                                    <img src="images/threshold${title}.jpg" class="img-responsive" alt="gallery" />
                                    <div class="project-overlay">
                                        <h5>Response Times</h5>
                                        <hr />
                                        <h5>Above Threshold</h5>
                                    </div>
                                </div>
                            </a>
                        </li>
                        
                        <li class="col-lg-2 col-md-2 col-sm-2 gallery gallery-lifetime" >
                            <a class="colorbox" href="images/alltimeaverage${title}.jpg" data-group="gallery-lifetime">
                                <div class="templatemo-project-box">
                                    <img src="images/alltimeaverage${title}.jpg" class="img-responsive" alt="gallery" />
                                    <div class="project-overlay">
                                        <h5>Lifetime Average</h5>
                                        <hr />
                                        <h5>Response Times</h5>
                                    </div>
                                </div>
                            </a>
                        </li>
                        
                        <li class="col-lg-2 col-md-2 col-sm-2 gallery gallery-lifetime" >
                            <a class="colorbox" href="images/alltime${title}.jpg" data-group="gallery-lifetime">
                                <div class="templatemo-project-box">
                                    <img src="images/alltime${title}.jpg" class="img-responsive" alt="gallery" />
                                    <div class="project-overlay">
                                        <h5>Lifetime All</h5>
                                        <hr />
                                        <h5>Response Times</h5>
                                    </div>
                                </div>
                            </a>
                        </li>

                    </ul><!-- /.gallery -->
				<div class="clearfix"></div>
				<div class="clearfix"></div>
                <div class="row text-center">
                    <br><h4><b>Threshold: $threshold s </b></h4><br>
					
					$performanceData
				
				</div>
            </div><!-- /.container -->
        </div> <!-- /.templatemo-portfolio -->

        <div class="templatemo-links" id="templatemo-links">
            <div class="container">
                <div class="row">
                    <div class="templatemo-line-header" >
                        <div class="text-center">
                            <hr class="team_hr team_hr_left hr_gray"/><span class="txt_darkgrey">&nbsp; &nbsp; &nbsp; &nbsp; LINKS &nbsp; &nbsp; &nbsp; &nbsp; </span>
                            <hr class="team_hr team_hr_right hr_gray" />
                        </div>
                    </div>
                    <div class="clearfix"></div>


                    <div class="text-center">

                        <div style="margin-top:60px;">
                            <ul class="list-inline">
                                <li>
                                    <a href="http://sam-jenkins.mot.com:8080/jenkins/view/PDBc/" target="_blank"><img src="images/jenkins-menu-icon.png" class="img-responsive" alt="Jenkins Server" /></a>
                                </li>
								<li>
                                    <a href="chrome-extension://mkhojklkhkdaghjjfdnphfphiaiohkef/index.html" target="_blank" ><img src="images/postman.png" class="img-responsive" alt="Postman" /></a>
                                </li>
                            </ul>

                        </div>

                    </div>
                </div>
            </div>
        </div>


        <div class="templatemo-footer" >
            <div class="container">
                <div class="row">
                    <div class="text-center">

                        <div class="footer_container">
                            
                            <div class="height30"></div>
                            <a class="btn btn-lg btn-orange" href="#" role="button" id="btn-back-to-top">Back To Top</a>
                            <div class="height30"></div>
                        </div>
                        <div class="footer_bottom_content">
                        	Copyright © 2016 <a href="#">Motorola</a>
                        </div>
                        
                    </div>
                </div>
            </div>
        </div>

        <script src="js/jquery.min.js" type="text/javascript"></script>
        <script src="js/bootstrap.min.js"  type="text/javascript"></script>
        <script src="js/stickUp.min.js"  type="text/javascript"></script>
        <script src="js/colorbox/jquery.colorbox-min.js"  type="text/javascript"></script>
        <script src="js/templatemo_script.js"  type="text/javascript"></script>
    </body>
	</html>
		"""
		
		File file = new File("results/" + title + ".html")
		def outputWriter = new PrintWriter(file, StandardCharsets.UTF_8.name())
		outputWriter.println(html)
		outputWriter.close()
		logger.info "Test execution complete. Please refer to " + title + ".html for results."
	}
}
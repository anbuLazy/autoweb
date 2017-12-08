package com.moto.common.ws.automation.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is REST client utility class for auto moto services to call REST
 * services using Java or programming language who support or use Java as
 * native.
 * 
 * @author sksharma
 * 
 */
public class RestClientUtility {

	private final String baseUri;

	/**
	 * Parameterized constructor for RestClientUtility.
	 * 
	 * @param baseUri
	 */
	public RestClientUtility(String baseUri) {
		this.baseUri = baseUri;
	}

	/**
	 * This method is client for REST test service of auto moto services
	 * 
	 * @return <code>String</code>
	 */
	public String test() {
		return getDataFromService("/testdata/test", "GET", null);
	}
	
	/**
	 * This method is client for REST to get test scenario names
	 * 
	 * @param jsonInput
	 *            An object of <code>String</code> giving the input json as
	 *            string
	 * @return The json response as object of <code>String</code>
	 */
	public String getTestScenarioNames(final String jsonInput) {
		return getDataFromService("/testdata/getTestScenarioName", "POST", jsonInput);
	}
	
	public String getServiceHealthCheckStatus(final String jsonInput) {
		return getDataFromService("/healthcheck/getServiceHealthCheckStatus", "POST", jsonInput);
	}
	
	/**
	 * This method is client for REST get test scenario name service of auto moto
	 * services
	 * 
	 * @param jsonInput
	 *            An object of <code>String</code> giving the input json as
	 *            string
	 * @return The json response as object of <code>String</code>
	 */
	public String getTestScenarioName(final String jsonInput) {
		return getDataFromService("/testdata/getTestScenarioName", "POST", jsonInput);
	}

	/**
	 * This method is client for REST get test data service of auto moto
	 * services
	 * 
	 * @param jsonInput
	 *            An object of <code>String</code> giving the input json as
	 *            string
	 * @return The json response as object of <code>String</code>
	 */
	public String getTestData(final String jsonInput) {
		return getDataFromService("/testdata/getTestData", "POST", jsonInput);
	}

	/**
	 * This method is client for REST set test data service of auto moto
	 * services
	 * 
	 * @param jsonInput
	 *            An object of <code>String</code> giving the input json as
	 *            string
	 * @return The json response as object of <code>String</code>
	 */
	public String setTestData(final String jsonInput) {
		return setDataToService("/testdata/setTestData", "POST", jsonInput);
	}
	
	/**
	 * This method is client for REST set test data service of auto moto
	 * services
	 * 
	 * @param jsonInput
	 *            An object of <code>String</code> giving the input json as
	 *            string
	 * @return The json response as object of <code>String</code>
	 */
	public String setTestResultData(final String jsonInput) {
		return setDataToService("/result/setResultData", "POST", jsonInput);
	}
	
	public String setHealthCheckResultData(final String jsonInput) {
		return setDataToService("/healthcheck/setServiceHealthCheck", "POST", jsonInput);
	}
	
	private String setDataToService(final String strURL, final String serviceMethod, final String jsonInput) {
		String output = null;
		
		httpsSetup(this.baseUri);
		
		try {
			final URL url = new URL(this.baseUri + strURL);
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(serviceMethod);
			conn.setRequestProperty("Content-Type", "application/json");

			try (final OutputStream os = conn.getOutputStream()) {
				os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
				os.flush();
			}

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			try (final InputStream is = conn.getInputStream()) {
				output = IOUtils.toString(is, StandardCharsets.UTF_8);
			}

			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	private String getDataFromService(final String strURL, final String serviceMethod, final String jsonInput) {
		String output = null;

		httpsSetup(this.baseUri);

		try {
			final URL url = new URL(this.baseUri + strURL);
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(serviceMethod);
			conn.setDoOutput(true);
			//conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			if (StringUtils.isNotEmpty(jsonInput)) {
				try (final OutputStream os = conn.getOutputStream()) {
					os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
					os.flush();
				}
			}

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			try (final InputStream is = conn.getInputStream()) {
				output = IOUtils.toString(is, StandardCharsets.UTF_8);
			}

			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

	public static void httpsSetup(final String url) {
		if (!StringUtils.startsWithIgnoreCase(url, "https://")) {
			return;
		}
		final TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String t) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String t) {
			}
		} };
		try {
			final SSLContext ssl_ctx = SSLContext.getInstance("TLS");
			ssl_ctx.init(null, certs, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(ssl_ctx.getSocketFactory());

			final HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
	}
}
package com.moto.common.ws.automation.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is REST client utility class for auto moto services to call REST
 * services using Java or programming language who support or use Java as
 * native.
 * 
 * @author sksharma
 *
 */
public class RestFileUploadClientUtility {
	private String baseUri;

	/**
	 * Parameterized constructor for RestClientUtility.
	 * 
	 * @param baseUri
	 */
	public RestFileUploadClientUtility(String baseUri) {
		this.baseUri = baseUri;
	}

	/**
	 * @param fileSufix
	 *            the value to be file sufix
	 * @param uploadFile
	 *            the value to be file object
	 * @return string response
	 */
	public String uploadFile(String fileSufix, File uploadFile) {
		String output = null;
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "***232404jkg4220957934FW**";

		int bytesRead, bytesAvailable, bufferSize;

		byte[] buffer;

		int maxBufferSize = 1 * 1024 * 1024;

		try {
			URL url = new URL(this.baseUri + "/upload/uploadFiles");
			conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);

			FileInputStream fileInputStream = new FileInputStream(uploadFile);

			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"files\";"
					+ " filename=\"" + fileSufix + "-" + uploadFile.getName()
					+ "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			fileInputStream.close();
			dos.flush();
			dos.close();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				output = "Failed : HTTP error code : " + conn.getResponseCode();
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			output = this.convertToString(conn.getInputStream());

			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * This is private method to convert <code>InputStream</code> to
	 * <code>String</code>
	 * 
	 * @param is
	 *            An object of <code>InputStream</code>
	 * @return <code>String</code>
	 */
	private String convertToString(InputStream is) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader((is)));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
	}
}

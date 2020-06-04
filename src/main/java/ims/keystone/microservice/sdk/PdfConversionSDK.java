package ims.keystone.microservice.pdfconversion.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
public class PdfConversionSDK {

	private String pdfConversionURL;
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private static final String charset = "UTF-8";
    private OutputStream outputStream;
    private PrintWriter writer;
    private static final int BUFFER_SIZE = 4096;

    /**
     * This constructor initializes the pdfConversion URL
     */
    public PdfConversionSDK(String pdfConversionURL) {
 
		// the pdfConversion URL
		this.pdfConversionURL = pdfConversionURL;

        // creates a unique boundary based on time stamp
        this.boundary = "===" + System.currentTimeMillis() + "===";
    }
 
    /**
     * Add an upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
	public void addFilePart(String fieldName, File uploadFile)
		throws IOException {

		String fileName = uploadFile.getName();
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append(
			"Content-Disposition: form-data; name=\"" + fieldName
			+ "\"; filename=\"" + fileName + "\"")
			.append(LINE_FEED);
		writer.append(
			"Content-Type: "
			+ URLConnection.guessContentTypeFromName(fileName))
			.append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();
 
		FileInputStream inputStream = new FileInputStream(uploadFile);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		inputStream.close();

		writer.append(LINE_FEED);
		writer.flush();    
	}
 
    /**
     *  Upload the file to be converted to PDF
     */
    public int uploadFile(String fileName)
		throws IOException {

		// configure uploadFileURL
		String uploadFileURL = String.format("%s%suploadFile", this.pdfConversionURL,
			File.separator);

		//log.debug("uploadFileURL is : {}", uploadFileURL);
        System.out.println("uploadFileURL is " + uploadFileURL);

		String result = null;

		// configure and establish http connection
        URL url = new URL(uploadFileURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setUseCaches(false);
        con.setDoOutput(true); // indicates POST method
        con.setDoInput(true);
        con.setRequestProperty("Content-Type",
        	"multipart/form-data; boundary=" + boundary);
        con.setRequestProperty("User-Agent", "PdfConversionSDK");
        outputStream = con.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);

		// add the upload file to the request
        File uploadFile1 = new File(fileName);
        addFilePart("file", uploadFile1);

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		int responseCode = con.getResponseCode();

		//log.debug("uploadFile Response Code : {}", responseCode);
		System.out.println("uploadFile Response Code is " + responseCode);

		// get the response
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			result = response.toString();

			//log.debug("uploadFile Response String : {}", result);
			System.out.println("uploadFile Response String is " + result);
        
		} else {
			//log.debug("uploadFile request didn't work");
            System.out.println("uploadFile request didn't work" );
		}
             
		con.disconnect();

		if (result != null) {
			return(Integer.parseInt(result));
		} else {
			return(-1);
		}
    }

    /**
     *  Get the status of the job/file being converted to PDF
     */
	public String getJobStatus (int jobId) 
		throws IOException {

		// configure getJobStatusURL
		String getJobStatusURL = String.format("%s%sgetStatus%s%d", this.pdfConversionURL,
			File.separator, File.separator, jobId);

		//log.debug("getURL is : {}", getJobStatusURL);
        System.out.println("getJobStatusURL is " + getJobStatusURL);

		String result = null;

		// configure and establish http connection
		URL obj = new URL(getJobStatusURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "PdfConversionSDK");

		int responseCode = con.getResponseCode();

		//log.debug("getJobStatus Response Code : {}", responseCode);
        System.out.println("getJobStatus Response Code is " + responseCode);

		// get the response
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			result = response.toString();

			//log.debug("getJobStatus Response String : {}", result);
            System.out.println("getJobStatus Response String is " + result);
		} else {
			//log.debug("getJobStatus request didn't work");
            System.out.println("getJobStatus request didn't work");
		}
		
        con.disconnect();
		return(result);
	}

    /**
     *  Download the job's converted PDF file
     */
	public void downloadFile (int jobId, String downloadDir) 
       	throws IOException {

		// configure downloadFileURL
		String downloadFileURL = String.format("%s%sdownloadFile%s%d", this.pdfConversionURL,
			File.separator, File.separator, jobId);

		//log.debug("getDownloadURL is : {}", downloadFileURL);
        System.out.println("downloadFileURL is " + downloadFileURL);

		String result = null;

		// configure and establish http connection
		URL obj = new URL(downloadFileURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		int responseCode = con.getResponseCode();

		//log.debug("Download Response Code : {}", responseCode);
        System.out.println("downloadFile Response Code is " + responseCode);

		// get the response
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
            System.out.println("Success so far on download");

            String fileName = "";
            String disposition = con.getHeaderField("Content-Disposition");
            String contentType = con.getContentType();
            int contentLength = con.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                    	disposition.length() - 1);
                }
            } else {
                // extracts file name from URL ???
                fileName = "somefile.pdf";
            }
 
            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // open input stream from the HTTP connection
            InputStream inputStream = con.getInputStream();
			// remove the jobId from the filename
            String saveFilePath = downloadDir + File.separator + fileName.substring(6);
             
            // open an output stream to save into file
            FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
 
            fileOutputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");

        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }

        con.disconnect();
	}
}

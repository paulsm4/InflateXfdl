package com.rgb;

import java.io.*;
import java.util.zip.*; // GZIP etc

import org.w3c.dom.*; // XML DOM
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XfdlFile {

	private String fname;
	FileInputStream fis = null;
	Base64.InputStream bis = null;
	GZIPInputStream gis = null;
	MimeHeader mimeHeader = null;
	
	public XfdlFile (String fname) throws Exception {
		// Verify the file exists
		File f = new File(fname);
		if (!f.exists ())
			throw new Exception (fname + " does not exist");
		this.fname = fname;
	}
	
	public void close () throws IOException {
		if (gis != null)
			gis.close ();
		if (bis != null)
			bis.close ();
		if (fis != null)
			fis.close ();		
	}
	
	public InputStream open () throws Exception {
		
		// Check MIME/Content-type header
		if (parseBase64GZipFileHeader (fname)) {

			// Read base-64 text into new InputStream
			fis = new FileInputStream (fname);
			fis.skip(mimeHeader.header_len);
			Base64.InputStream bis = new Base64.InputStream (fis, Base64.DECODE);

			// Unzip from decoded stream
			GZIPInputStream gis = new GZIPInputStream (bis);
			return gis;
		}
		else if (parseXmlFile (fname)) {
			return fis = new FileInputStream (fname);
		}
		else
			throw new Exception (fname + " does not appear to be a valid IBM Forms file.");
	}
	
	/*
	 * IBM Forms 4.x: application/vnd.xfdl; content-encoding="base64-gzip"
	 * IBM Forms 8.x: application/vnd.xfdl;content-encoding=base64-gzip
	 */
	private boolean parseBase64GZipFileHeader (String fname) {
		FileInputStream fis = null;
		mimeHeader = new MimeHeader ();
		try {
			byte bytes[] = new byte[80];
			fis = new FileInputStream(fname);
			fis.read (bytes, 0, bytes.length);
			String header = new String (bytes);
			int i= header.lastIndexOf (';');
			if (i > 0) {
				mimeHeader.mime_type = header.substring (0, i-1).trim();
				String s = header.substring(i+1);
				int j = s.lastIndexOf("base64-gzip");
				if (j > 0) {
					if (s.lastIndexOf("\"base64-gzip\"") > 0) {
						mimeHeader.content_type = s.substring(0, j+13).trim();
						mimeHeader.header_len = header.lastIndexOf ("gzip\"") + 5;
					}
					else {
						mimeHeader.content_type = s.substring(0, j+11).trim();
						mimeHeader.header_len = header.lastIndexOf ("gzip") + 4;
					}					
				}
			}
			fis.close ();
		}
		catch (Exception e) {
			; // No-op
		}
		
		// If header_len > 0, probably a valid base64-gzip file
		return (mimeHeader.header_len > 0);
	}

	private boolean parseXmlFile (String fname) throws Exception {
		FileInputStream fis = new FileInputStream (new File(fname));
		DocumentBuilder db = 
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = db.parse (fis);
		fis.close ();
		String rootNodeName = document.getDocumentElement().getNodeName();
		return rootNodeName.equals("XFDL");
	}
}

class MimeHeader {
	public String mime_type;
	public String content_type;
	public int header_len;
	public MimeHeader () {
		header_len = -1;  // Default: MIME/Content header not found
	}
}


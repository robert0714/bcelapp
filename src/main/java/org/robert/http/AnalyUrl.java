package org.robert.http;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class AnalyUrl {
	private static String baseUrl = "http://192.1.0.83:5180/projfile/RC/data/ExceptionLog/00000000/";
	private static String outputFolderName ="D:/userDatas/robert/Desktop/2014_0215Exception_log/";
	public static List<String> extractUrls(String input) {
		List<String> result = new ArrayList<String>();
		String regex = "href=\"[-a-zA-Z0-9+&@#/%_.?]*\""; // matches
															// <http://google.com>

		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			result.add(matcher.group());
		}

		return result;
	}

	public static void main01(String[] args) {
		List<String> lines = extractUrls(baseUrl);
	}
	public static String getHtmlContent(final String baseUrl){

		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(baseUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false)); 
		byte[] responseBody=null;
		try {
			// 返回狀態值.
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
			}

			// 取得回傳資訊.
			 responseBody = method.getResponseBody();
			
		} catch (HttpException httpexc) {
			System.err.println("Fatal protocol violation: "
					+ httpexc.getMessage());
			httpexc.printStackTrace();
		} catch (IOException ioexc) {
			System.err.println("Fatal transport error: " + ioexc.getMessage());
			ioexc.printStackTrace();
		} finally {

			// ** 無論如何都必須釋放連接.
			method.releaseConnection();
		}

		String pageContent = new String(responseBody);
		return pageContent;
	}
	public static void main(String[] args) {
		final List<String> urls = new ArrayList<String>();
		
		String pageContent = getHtmlContent(baseUrl);
		List<String> tmp = extractUrls(pageContent);
		urls.addAll(tmp);
		
		Set<String> mqDataSet =getPartNameSet() ;

		List<String> ulrList = new ArrayList<String>();
		for (String tmp01 : urls) {
			boolean yes =false;
			for(String line :mqDataSet){ 
				if(StringUtils.contains(tmp01, line)){
					 yes =true;
					break;
				}
			}
			System.out.println(tmp01);
			if (yes) {
//				UlrFile urlFile = new UlrFile();
				String url = baseUrl
						+ StringUtils.replace(
								StringUtils.replace(tmp01, "href=\"", ""),
								"\"", "");
				String[] strArray = StringUtils.split(url, "/");
				String fileName = strArray[strArray.length - 1];
				// System.out.println(fileName);
				String pageUnitContent = getHtmlContent(url);
				File file = new File(outputFolderName + fileName);

				try {
					FileUtils.write(file, pageUnitContent, "UTF-8");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		for (String url : ulrList) {

			System.out.println(url);
		}
	}
	public static Set<String> getPartNameSet() {
		File src = new File("D:/userDatas/robert/Desktop/RCDF001mrl_20140215");
		Set<String> result = new HashSet<String>();
		try {
			result.addAll(FileUtils.readLines(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
  class UlrFile{
	private String baseUrl; 
	private String name;
	private String url = baseUrl   + StringUtils.replace(StringUtils.replace(name, "href=\"", ""), "\"", "");
	private String content;
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String getName() {
		
		return name;
	}
	public void setName(String name) {
		this.name =StringUtils.replace(StringUtils.replace(name, "href=\"", ""), "\"", ""); 
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	} 
	
	
	
}
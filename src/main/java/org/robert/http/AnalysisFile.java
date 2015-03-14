package org.robert.http;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class AnalysisFile {
	private static String folderName = "D:/userDatas/robert/Desktop/2014_0215Exception_log";

	final static String regexPersonIdTag = "<tw.gov.moi.rs.dto.PairEntry>\n?\r?.*<key>personId</key>\n?\r?.*<value class=\"string\">[a-zA-Z0-9]{1}\\d{9}</value>\n?\r?.*</tw.gov.moi.rs.dto.PairEntry>";
	final static String regexSiteIdTag = "<tw.gov.moi.rs.dto.PairEntry>\n?\r?.*<key>siteId</key>\n?\r?.*<value class=\"string\">\\d{8}</value>\n?\r?.*</tw.gov.moi.rs.dto.PairEntry>";
	final static String regexTrModeTag = "<tw.gov.moi.rs.dto.PairEntry>\n?\r?.*<key>trMode</key>\n?\r?.*<value class=\"string\">\\d{2}</value>\n?\r?.*</tw.gov.moi.rs.dto.PairEntry>";
	final static String regexApplyHhmmssTag = "<tw.gov.moi.rs.dto.PairEntry>\n?\r?.*<key>applyHhmmss</key>\n?\r?.*<value class=\"string\">\\d{6}</value>\n?\r?.*</tw.gov.moi.rs.dto.PairEntry>";
	final static String regexApplyYyymmddTag  = "<tw.gov.moi.rs.dto.PairEntry>\n?\r?.*<key>applyYyymmdd</key>\n?\r?.*<value class=\"string\">\\d{7}</value>\n?\r?.*</tw.gov.moi.rs.dto.PairEntry>";
	
	
	final static String regexPersonId = "[a-zA-Z0-9]{1}\\d{9}";
	final static String regexSiteId = "\\d{8}";
	final static String regexTrMode = "\\d{2}";
	final static String regexApplyHhmmss = "\\d{6}";
	final static String regexApplyYyymmdd = "\\d{7}"; 
	
	
	final static String rlSql4MTemplate = "select move_in_yyymmdd from rldf004m where person_id='%s' and site_id='%s' and personal_mark='0';";
	final static String rlSql1MTemplate = "select village,neighbor,street_doorplate from rldf001m where household_head_id in (select household_Head_id from rldf004m where person_id='%s'  and site_id='%s' and personal_mark='0');";
	final static String rcSqlrcdf001mTemplate = "select move_in_yyymmdd,site_id,village,neighbor,street_doorplate from rcdf001m where person_id='%s' ;";

	protected static Set<String> extractData(final String expr, final String src) {
		final Set<String> init = new HashSet<String>();

		final Pattern pattern = Pattern.compile(expr);
		final Matcher matcher = pattern.matcher(src);
		while (matcher.find()) {
			String extraData = matcher.group();
			init.add(extraData);
		}
		return init;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AnalysisFile analysis = new AnalysisFile();
		 
		analysis.diplayMQidByFiles();
	}

	public static void diplayMQidByFiles() {
		File dir = new File(folderName);
		String[] listNames = dir.list();
		for(String name : listNames){
			final	String[] strArray = StringUtils.splitByWholeSeparator(name, "_");
			String mqId = StringUtils.remove(strArray[2], ".log");
			System.out.println(mqId);
		}
	}
	/**
	 * @param args
	 */
	public static void main02(String[] args) {
		AnalysisFile analysis = new AnalysisFile();
		Map<String, List<UnitNeedProcess>> initiation = analysis.testRCDF001mrl();
		StringBuffer sbf = new StringBuffer();
		for (String siteId : initiation.keySet()) {
			sbf.append("site id : " + siteId).append("\n\r");
			System.out.println("site id : " + siteId);
			final List<UnitNeedProcess> list = initiation.get(siteId);

			for (UnitNeedProcess unit : list) {
				sbf.append(unit.getFileName()).append("\n\r");
				sbf.append(unit.getRlSql4M()).append("\n\r");
				sbf.append(unit.getRlSql1M()).append("\n\r");
				sbf.append(unit.getRcSqlrcdf001m()).append("\n\r");
				sbf.append("==========================").append("\n\r");

				System.out.println(unit.getFileName());
				System.out.println(unit.getRlSql4M());
				System.out.println(unit.getRlSql1M());
				System.out.println(unit.getRcSqlrcdf001m());
				System.out.println("==========================");
			}
			System.out.println("---------------------------------------");
			sbf.append("---------------------------------------")
					.append("\n\r");
		}
		try {
			FileUtils.write(new File("/home/weblogic/Desktop/excel_log"),
					sbf.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String extractRcdfm06mInfo(final File srcFile) {
		String result= StringUtils.EMPTY;
		try { 
			
			final String fileContent = FileUtils.readFileToString(srcFile);
			final Set<String> personIdSet = new HashSet<String>();
			final Set<String> siteIdSet = new HashSet<String>();
			final Set<String> trModeSet = new HashSet<String>();
			final Set<String> applyHhmmssSet = new HashSet<String>();
			final Set<String> applyYyymmddSet = new HashSet<String>();
			

			final Set<String> siteIdDataSet = extractData(regexSiteIdTag,
					fileContent);
			final Set<String> personIdDataSet = extractData(regexPersonIdTag,
					fileContent);			
			final Set<String> trModeDataSet = extractData(regexTrModeTag,
					fileContent);
			final Set<String> applyHhmmssDataSet = extractData(regexApplyHhmmssTag,
					fileContent);
			final Set<String> applyYyymmddDataSet = extractData(regexApplyYyymmddTag,
					fileContent);
			
			
			for (String personIdPartData : personIdDataSet) {
				personIdSet
						.addAll(extractData(regexPersonId, personIdPartData));
			}
			for (String siteIdPartData : siteIdDataSet) {
				siteIdSet.addAll(extractData(regexSiteId, siteIdPartData));
			}
			
			for (String trModePartData : trModeDataSet) {
				trModeSet
						.addAll(extractData(regexTrMode, trModePartData));
			}
			for (String applyHhmmssPartData : applyHhmmssDataSet) {
				applyHhmmssSet.addAll(extractData(regexApplyHhmmss, applyHhmmssPartData));
			}
			for (String applyYyymmddPartData : applyYyymmddDataSet) {
				applyYyymmddSet
						.addAll(extractData(regexApplyYyymmdd, applyYyymmddPartData));
			}
			
			
			String personId = StringUtils.EMPTY;
			String siteId = StringUtils.EMPTY;
			
			String trMode = StringUtils.EMPTY;
			String applyHhmmss = StringUtils.EMPTY;
			String applyYyymmdd = StringUtils.EMPTY;
			
			if (CollectionUtils.isNotEmpty(personIdSet)) {
				personId = personIdSet.toArray(new String[] {})[0];
			}
			if (CollectionUtils.isNotEmpty(siteIdSet)) {
				siteId = siteIdSet.toArray(new String[] {})[0];
			}
			if (CollectionUtils.isNotEmpty(personIdSet)) {
				trMode = trModeSet.toArray(new String[] {})[0];
			}
			if (CollectionUtils.isNotEmpty(siteIdSet)) {
				applyHhmmss = applyHhmmssSet.toArray(new String[] {})[0];
			}
			if (CollectionUtils.isNotEmpty(personIdSet)) {
				applyYyymmdd = applyYyymmddSet.toArray(new String[] {})[0];
			}
			
			result=String.format(" (  tr_mode ='%s' and   person_id ='%s'  and site_id = '%s' and apply_yyymmdd ='%s'  and   apply_hhmmss ='%s'  )", trMode,personId,siteId,applyYyymmdd,applyHhmmss);
			if(StringUtils.equals(personId, "G220312859")  || StringUtils.equals(personId, "N224777253")){
				System.out.println(srcFile.getName());
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return result;
	}
	public UnitNeedProcess extractRCDF001mrlInfo(final File srcFile) {
		UnitNeedProcess newUnit = new UnitNeedProcess();
		try {
			newUnit.setFileName(srcFile.getName());
			final String fileContent = FileUtils.readFileToString(srcFile);
			final Set<String> personIdSet = new HashSet<String>();
			final Set<String> siteIdSet = new HashSet<String>();

			final Set<String> siteIdDataSet = extractData(regexSiteIdTag,
					fileContent);
			final Set<String> personIdDataSet = extractData(regexPersonIdTag,
					fileContent);
			for (String personIdPartData : personIdDataSet) {
				personIdSet
						.addAll(extractData(regexPersonId, personIdPartData));
			}
			for (String siteIdPartData : siteIdDataSet) {
				siteIdSet.addAll(extractData(regexSiteId, siteIdPartData));
			}
			String personId = StringUtils.EMPTY;
			String siteId = StringUtils.EMPTY;
			if (CollectionUtils.isNotEmpty(personIdSet)) {
				personId = personIdSet.toArray(new String[] {})[0];
			}
			if (CollectionUtils.isNotEmpty(siteIdSet)) {
				siteId = siteIdSet.toArray(new String[] {})[0];
			}
			newUnit.setPersonId(personId);
			newUnit.setSiteId(siteId);
			String rlSql4M = String.format(rlSql4MTemplate, personId, siteId);
			String rlSql1M = String.format(rlSql1MTemplate, personId, siteId);
			String rcSqlrcdf001m = String.format(rcSqlrcdf001mTemplate,
					personId);
			newUnit.setRlSql1M(rlSql1M);
			newUnit.setRlSql4M(rlSql4M);
			newUnit.setRcSqlrcdf001m(rcSqlrcdf001m);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newUnit;
	}

	class UnitNeedProcess {
		private String fileName;
		private String personId;
		private String siteId;
		private String rlSql4M;
		private String rlSql1M;
		private String rcSqlrcdf001m;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getRlSql4M() {
			return rlSql4M;
		}

		public void setRlSql4M(String rlSql4M) {
			this.rlSql4M = rlSql4M;
		}

		public String getRlSql1M() {
			return rlSql1M;
		}

		public void setRlSql1M(String rlSql1M) {
			this.rlSql1M = rlSql1M;
		}

		public String getRcSqlrcdf001m() {
			return rcSqlrcdf001m;
		}

		public void setRcSqlrcdf001m(String rcSqlrcdf001m) {
			this.rcSqlrcdf001m = rcSqlrcdf001m;
		}

		public String getPersonId() {
			return personId;
		}

		public void setPersonId(String personId) {
			this.personId = personId;
		}

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

	}
	public void displayRcdfm06mSQL(){
		final Set<String> nameSet = getPartNameSet();
		nameSet.remove("");
		File dir = new File(folderName);
		final File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				for (String nameunit : nameSet) {
					if (StringUtils.contains(name, nameunit)) {
						return true;
					}
				}
				return false;
			}
		});
		
		final Set<String> sqlSet =new HashSet<String>();
		
		List<String> params =new ArrayList<String>();
		for (File file : files) {
			String sql = extractRcdfm06mInfo(file);
			params.add(sql);
			sqlSet.add(sql);
		}  
		String sql = "select count(*) from Rcdfm06m where "+StringUtils.join(params," or ");
		System.out.println(sql);
		System.out.println(sqlSet.size());
	}
	public Map<String, List<UnitNeedProcess>> testRCDF001mrl() {
		final Set<String> nameSet = getPartNameSet();
		nameSet.remove("");
		File dir = new File(folderName);
		final File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				for (String nameunit : nameSet) {
					if (StringUtils.contains(name, nameunit)) {
						return true;
					}
				}
				return false;
			}
		});
		Map<String, List<UnitNeedProcess>> result = new HashMap<String, List<AnalysisFile.UnitNeedProcess>>();
		for (File file : files) {
			final UnitNeedProcess unit = extractRCDF001mrlInfo(file);
			List<UnitNeedProcess> list = result.get(unit.getSiteId());
			if (list == null) {
				list = new ArrayList<AnalysisFile.UnitNeedProcess>();
			}
			list.add(unit);
			result.put(unit.getSiteId(), list);
		}
		return result;
	}

	public Set<String> getPartNameSet() {
		File src = new File("D:/userDatas/robert/Desktop/RCDF001mrl_20140215_for_AW.txt");
		Set<String> result = new HashSet<String>();
		try {
			result.addAll(FileUtils.readLines(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}

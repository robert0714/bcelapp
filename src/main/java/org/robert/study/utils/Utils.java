package org.robert.study.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class Utils {
    private static final String fixedNumber = "1987654321";
//    public static void main(String[] args){
//    	boolean symbol = isEnglishCharacters("Z05");
//    	System.out.println("symbol: "+symbol);
//    	
//    	System.out.println((int)'A');
//    	System.out.println((int)'Z');
//    	System.out.println((int)'0');
//    	System.out.println((int)'5');
//    }
    
    /**
     * example: F212345674
     * **/
    public static String getCheckNum(final String originalString) {
        String firstSymbol = originalString.substring(0, 1);
        String otherSymbol = originalString.substring(1, originalString.length());
        String beforeCaculation = getSymbolCode().get(firstSymbol) + otherSymbol;
        String result = logicalCalulation(beforeCaculation, fixedNumber);
        return result;
    }

    public static boolean isEnglishCharacters(String sample) {
        char[] charArray = sample.toCharArray();
        boolean result = charArray.length > 0 ? true : false;
        Set<Integer> colleaction = getNumerCharacterUnicodeSymbolCode();
        for (char tmpcahr : charArray) {
            int intTmp = (int) tmpcahr;
            if (!colleaction.contains(Integer.valueOf(intTmp))) {
                result = false;
            }
        }

        return result;
    }

    private static final Set<Integer> getNumerCharacterUnicodeSymbolCode() {
        Set<Integer> result = new HashSet<Integer>();
        for (int i = 97; i < 124; i++) {
            result.add(Integer.valueOf(i));
        }
        for (int i = 65; i < 91; i++) {
            result.add(Integer.valueOf(i));
        }
        for (int i = 48; i < 61; i++) {
            result.add(Integer.valueOf(i));
        }
        result.add(95);// '_'
        return result;
    }

    public static void outputFile(final String fileName, List<String> data) {
        BufferedWriter bWriter = null;
        try {
            // bWriter = new BufferedWriter(new FileWriter(new File(fileName)));
            bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), "UTF8"));
            for (int i = 0; i < data.size(); ++i) {
                String ss = data.get(i);
                bWriter.write(ss);
                if (i == (data.size() - 1)) {
                    // 最後一行不下分行符號
                } else {
                    bWriter.write("\r\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bWriter != null) {
                try {
                    bWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void outputFile(final File file, String data) throws IOException {
        BufferedWriter bWriter = null;
        try {
            bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
            bWriter.write(data);
        } finally {
            if (bWriter != null) {
                try {
                    bWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static <T> void serialization(T aObject, File outFile) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(outFile));
            // oos.writeInt(12345);
            // oos.writeObject("Today");
            oos.writeObject(aObject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String filter(String sample, String requirementString) {
        StringTokenizer st = new StringTokenizer(sample, " ");
        String result = null;
        while (st.hasMoreElements()) {
            String tmp = (String) st.nextElement();
            if (tmp.contains(requirementString)) {
                result = tmp;
            }
        }
        return result;
    }

    public static String trimData(final String src) {
        String result = src.substring(src.indexOf("-") + 1);
        return result;
    }

    public static <T> T deserialization(InputStream inputStream) {
        T result = null;
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(inputStream);
            // int i = ois.readInt();
            // String today = (String) ois.readObject();
            result = (T) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public static <T> T deserialization(File inputFile) {
        T result = null;
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(inputFile));
            // int i = ois.readInt();
            // String today = (String) ois.readObject();
            result = (T) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public static List<String> inputReadFile(final File file) {
        List<String> result = new ArrayList<String>();
        BufferedReader br = null;
        try {
            // br = new BufferedReader(new FileReader(new File(fileName)));
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

            String line = br.readLine();
            do {
                result.add(line);
                line = br.readLine();
            } while (line != null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static String logicalCalulation(final String parameter, final String fixedNumber) {
        String result = "";
        String imediateStr;
        try {
            StringBuffer sbf = new StringBuffer();
            char[] parameterAaary1 = parameter.toCharArray();
            char[] parameterAaary2 = fixedNumber.toCharArray();
            if (parameterAaary1.length != parameterAaary2.length) {
                throw new RuntimeException("統號位數必須相等於特定數位數");
            } else {
                for (int i = 0; i < parameterAaary1.length; i++) {
                    int initData = (Integer.valueOf(String.valueOf(parameterAaary1[i])).intValue())
                            * (Integer.valueOf(String.valueOf(parameterAaary2[i])).intValue());
                    String tmpString = String.valueOf(initData);
                    sbf.append(tmpString.charAt(tmpString.length() - 1));
                }
            }
            char[] parameterAaary3 = sbf.toString().toCharArray();
            int imediateNum = 0;
            for (char character : parameterAaary3) {
                imediateNum += Integer.valueOf(String.valueOf(character));
            }
            imediateStr = Integer.valueOf(imediateNum).toString();
            Integer numm = Integer.valueOf(String.valueOf(imediateStr.charAt(imediateStr.length() - 1)));
            if (numm.equals(0)) {
                result = "0";
            } else {
                result = String.valueOf(10 - numm);
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("parameter: " + parameter + " ; fixedNumber: " + fixedNumber);
        }
        return result;
    }

    // private final static String retrieveLastChar(String )
    private static Map<String, String> getSymbolCode() {
        Map<String, String> result = new HashMap<String, String>();
        // 由於邏輯有不連號情形發生，故使用此法
        result.put("A", "10");
        result.put("B", "11");
        result.put("C", "12");
        result.put("D", "13");
        result.put("E", "14");
        result.put("F", "15");
        result.put("G", "16");
        result.put("H", "17");// 不連號發生
        result.put("J", "18");
        result.put("K", "19");
        result.put("L", "20");
        result.put("M", "21");
        result.put("N", "22");
        result.put("P", "23");
        result.put("Q", "24");
        result.put("R", "25");
        result.put("S", "26");
        result.put("T", "27");
        result.put("U", "28");
        result.put("V", "29");
        result.put("X", "30");
        result.put("Y", "31");
        result.put("W", "32");
        result.put("Z", "33");
        result.put("I", "34");
        result.put("O", "35");
        return result;
    }
}

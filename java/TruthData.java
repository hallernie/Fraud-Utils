import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.StringReader;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Complile file as follows:
//      $ javac -classpath C:\\tools\\java\\libraries\\opencsv-3.4.jar TruthData.java
//

public class TruthData{
    static int CREATED_DATE = 0;
    static int ORGANIZATION_ID = 1;
    static int MERCHANT_ID = 2;
    static int SESSION_ID = 3;
    static int AUTH_PAYMENT_ID = 4;
    static int DEPOSIT_PAYMENT_ID = 5;
    static int RESPONSE = 6;
    static int STATUS = 7;
    static int DEVICE_REPUTATION_SCORE = 8;
    static int DEVICE_REVIEW_STATUS = 9;
    static int CBK_OPEN_DATE = 10;
    static int CBK_CURRENT_CYCLE = 11;
    static int CBK_REASON_CODE = 12;
    static int VI_ALERT_CREATE_DATE = 13;
    static int MC_ALERT_CREATE_DATE = 14;

	public static void main(String[] args){
        if(args.length < 2) {
            System.out.println("Usage:");
            System.out.println("  java -classpath \"opencsv-3.4.jar;\" TruthData pidw_file reason_code_mapping_file");
            System.exit(1);
        }

		String lookup_table_file = args[1];
		String pidw_input_file = args[0];
		//String lookup_table_file = "C:\\vantiv\\docs\\tasks\\fraud\\tmx_rules_optimization\\automation\\reason_code_mapping.csv";
		//String pidw_input_file = "C:\\vantiv\\docs\\tasks\\fraud\\tmx_rules_optimization\\automation\\aft_detail_with_truth_81300_20141215_20150315.csv";
		Map<String, String> reason_codes = null;

		TruthData td = new TruthData();

		reason_codes = td.loadReasonCodeLookup(lookup_table_file);
		td.createTruthData(pidw_input_file, reason_codes);
	}

	private Map<String,String> loadReasonCodeLookup(String lookup_table_file){
		Map<String,String> reason_codes = new HashMap<String,String>();
		try{
			CSVReader reader = new CSVReader(new FileReader(lookup_table_file));
     		String [] nextLine;
     		while ((nextLine = reader.readNext()) != null) {
				reason_codes.put(nextLine[0],nextLine[1]);
			}
		}
		catch(FileNotFoundException ex){System.out.println("FileNotFoundException");}
		catch(IOException ex){System.out.println("IOException");}

		return reason_codes;
	}

	private void createTruthData(String pidw_input_file, Map<String,String> reason_codes){
		try{
		    CSVReader reader = null;
            String[] aNextline = null;
            FileReader iFR = new FileReader (pidw_input_file);
		    BufferedReader iBR = new BufferedReader(iFR);
            String nextLine = null;
            String responseValue = null;
            String bankDeclined = null;
            String deviceReviewStatus = null;
            String truth = null;
            String cbkReasonCode = null;
            String viAlertCreateDate = null;
            String mcAlertCreateDate = null;

            // Output the header row
            nextLine = iBR.readLine();
            System.out.println(nextLine + ",TRUTH,Bank Declined");
            
            while((nextLine = iBR.readLine()) != null){
		        reader = new CSVReader(new StringReader(nextLine));
                aNextline = reader.readNext();

                cbkReasonCode = aNextline[CBK_REASON_CODE];
                viAlertCreateDate = aNextline[VI_ALERT_CREATE_DATE];
                mcAlertCreateDate = aNextline[MC_ALERT_CREATE_DATE];

                responseValue = reason_codes.get(aNextline[RESPONSE]);
                if(responseValue == null){
                    System.out.println(nextLine + ",CODE_NOT_FOUND,CODE_NOT_FOUND");
                    continue;
                }
                else {
                    responseValue = responseValue.toUpperCase();
                    if(responseValue.equals("DECLINED/ISSUER")){
                        bankDeclined = "YES";
                    }
                    else{
                        bankDeclined = "NO";
                    }
                }

                deviceReviewStatus = aNextline[DEVICE_REVIEW_STATUS].toUpperCase();
                if(deviceReviewStatus.equals("UNAVAILABLE")){
                    System.out.println(nextLine + ",NOT_CHECKED" + "," + bankDeclined);
                    continue;
                }

                if(responseValue.equals("DECLINED/VANTIV")){
                    System.out.println(nextLine + ",UNKNOWN" + "," + bankDeclined);
                    continue;
                }

                if(responseValue.equals("DECLINED/ISSUER")){
                    System.out.println(nextLine + ",FRAUD" + "," + bankDeclined);
                    continue;
                }

                if(responseValue.equals("APPROVED")){
                    if(!cbkReasonCode.equals("--") || !viAlertCreateDate.equals("--") || !mcAlertCreateDate.equals("--")){
                        System.out.println(nextLine + ",FRAUD" + "," + bankDeclined);
                        //System.out.println(nextLine + ",TEST_FRAUD");
                        continue;
                    }
                    else {
                        System.out.println(nextLine + ",NOT_FRAUD" + "," + bankDeclined);
                        //System.out.println(nextLine + ",TEST_NOT_FRAUD");
                        continue;
                    }
                }
            }
		}
		catch(FileNotFoundException ex){System.out.println("FileNotFoundException");}
		catch(IOException ex){System.out.println("IOException");}
	}
}

import java.io.FileReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.io.FileNotFoundException;
import java.lang.Comparable;
import com.opencsv.CSVReader;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

//
// Utility class to join TMX event file data to PIDW truth data.
//
// Input:
//
//      PIDW Truth Data File (args[0])
//          Note: This truth data file should have been processed by the TruthData script. Meaning
//                that there should be column called "Bank Declined"
//
//
//      WIDENED ThreatMetrix data file (args[1])
//
// Output (To the terminal. So redirect if you want to save):
//
//      The original widened file with 5 extra columns extracted from the PIDW Truth data file.
//      The join is on Events."Custom Attribute 9" and PIDW."AUTH_PAYMENT_ID"
//          ChargeBack, VIAlert, MCAlert, BankDeclined, Response
//
//
// Sample command line (Windows/Cygwin):
//
//      java -classpath "P:\\Advanced Fraud Working Folder\\automation\\java_libraries\\opencsv-3.4.jar;." MergeWidenedToPidw pidw_processed_file_name threat_metrix_export_file
//
public class MergeWidenedToPidw{
    public static void main(String[] args){
        if(args.length < 2){
            System.out.println("Usage:");
            System.out.println("  % MergeWidenedToPidw pidw_filename events_filename");
            System.exit(0);
        }

        MergeWidenedToPidw value = new MergeWidenedToPidw();
        Map<String,FraudTags> lookup_fraud_map = value.createFraudMap(args[0]);

        value.widenWithPidwFraudData(args[1], "Custom Attribute 9", lookup_fraud_map);

    } // END: main


    //
    // Load lookup table with fraud column data from PIDW truth data report.
    //
    // Input:
    //
    //      PIDW truth data filename
    //
    // Output:
    //
    //      Map<String,FraudTags> where key is auth payment id, and value is object containing fraud column data.
    //      
    private Map<String,FraudTags> createFraudMap(String pidw_file_name){
        FileReader iFR = null;
        String current_line = null;
        Map<String,FraudTags> lookup_fraud_map = new HashMap<String,FraudTags>();
        CSVReader reader = null;
        String[] values = null;

        VantivUtils util = new VantivUtils();

        int auth_payment_id_column = util.findColumn(pidw_file_name,"AUTH_PAYMENT_ID");
        int cbk_open_date_column = util.findColumn(pidw_file_name,"CBK_OPEN_DATE");
        int cbk_current_cycle_column = util.findColumn(pidw_file_name,"CBK_CURRENT_CYCLE");
        int cbk_reason_code_column = util.findColumn(pidw_file_name,"CBK_REASON_CODE");
        int vi_alert_create_date_column = util.findColumn(pidw_file_name,"VI_ALERT_CREATE_DATE");
        int mc_alert_create_date_column = util.findColumn(pidw_file_name,"MC_ALERT_CREATE_DATE");
        int bank_declined_column = util.findColumn(pidw_file_name,"Bank Declined");
        int response_column = util.findColumn(pidw_file_name,"RESPONSE");

        String auth_payment_id = null;
        String cbk_open_date = null;
        String cbk_current_cycle = null;
        String cbk_reason_code = null;
        String vi_alert_create_date = null;
        String mc_alert_create_date = null;
        String bank_declined = null;
        String response = null;

        try{
            iFR = new FileReader (pidw_file_name);
            BufferedReader iBR = new BufferedReader(iFR);

            // Skip the header row
            iBR.readLine();
            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                values = reader.readNext();

                auth_payment_id = values[auth_payment_id_column].substring(1);
                cbk_open_date = values[cbk_open_date_column];
                cbk_current_cycle = values[cbk_current_cycle_column];
                cbk_reason_code = values[cbk_reason_code_column];
                vi_alert_create_date = values[vi_alert_create_date_column];
                mc_alert_create_date = values[mc_alert_create_date_column];
                bank_declined = values[bank_declined_column];
                response  = "\"" + values[response_column] + "\"";

                lookup_fraud_map.put(auth_payment_id,
                        new FraudTags(auth_payment_id,cbk_open_date,cbk_current_cycle,cbk_reason_code,
                            vi_alert_create_date,mc_alert_create_date,bank_declined,response));
            }
        }
        catch(FileNotFoundException ex){
            System.out.printf("File not found in main %n");
        }
        catch(IOException ex){
            System.out.println("IO Exception.");
        }

        return lookup_fraud_map;
    }



    //
    // Load lookup table. Assumes the lookup file data is comma delimited, with the key in the first column,
    // and the value in the second column.
    //
    // Input:
    //
    //      ThreatMetrix policy file name
    //
    // Output:
    //
    //      
    private void widenWithPidwFraudData(String widened_export_file_name, String auth_payment_id_column_name,
            Map<String,FraudTags> lookup_fraud_map){
        FileReader iFR = null;
        String current_line = null;
        CSVReader reader = null;
        String[] values = null;
        String str_out = null;
        String cbk_out = null;
        String vi_alert_out = null;
        String mc_alert_out = null;
        String bank_declined_out = null;
        String response_out = null;
        FraudTags fraud_tags = null;

        int auth_payment_id_column = 0;

        try{
            iFR = new FileReader (widened_export_file_name);
            BufferedReader iBR = new BufferedReader(iFR);

            // Find auth payment id column and output the header row
            str_out = iBR.readLine();
            
            auth_payment_id_column = new VantivUtils().findColumn(widened_export_file_name,
                    auth_payment_id_column_name);

            str_out += ",ChargeBack,VIAlert,MCAlert,BankDeclined,Response";
            System.out.println(str_out);

            // Process the remainder of the file.
            while( (current_line = iBR.readLine()) != null){
                cbk_out = "0";
                vi_alert_out = "0";
                mc_alert_out = "0";
                bank_declined_out = "0";
                response_out = "0";

                reader = new CSVReader(new StringReader(current_line));
                values = reader.readNext();

                // Assumes that the data is in the form "request_id:825521483440620316"
                try{
                    fraud_tags = lookup_fraud_map.get(values[auth_payment_id_column]);
                }
                catch(ArrayIndexOutOfBoundsException ex){
                    //System.out.println("Caught");
                    fraud_tags = null;
                }

                try{
                    // Check if payment had a charge back
                    if( (!fraud_tags.getCbkOpenDate().equals("--") ||
                                (!fraud_tags.getCbkCurrentCycle().equals("--")) ||
                                (!fraud_tags.getCbkReasonCode().equals("--"))) ){
                        cbk_out = "1";
                    }
                    // Check if payment had a visa fraud alert
                    if( (!fraud_tags.getViAlertCreateDate().equals("--")) ){ 
                        vi_alert_out = "1";
                    }
                    // Check if payment had a mc fraud alert
                    if( (!fraud_tags.getMcAlertCreateDate().equals("--")) ){ 
                        mc_alert_out = "1";
                    }
                    // Check if transaction was declined by the bank
                    if( (fraud_tags.getBankDeclined().equals("YES")) ){ 
                        bank_declined_out = "1";
                    }
                    // Output the response column
                    response_out = fraud_tags.getResponse();

                    System.out.printf("%s,%s,%s,%s,%s,%s%n",current_line, cbk_out, vi_alert_out, mc_alert_out, bank_declined_out,response_out);
                }
                catch(NullPointerException ex){
                    // Could not find the auth payment id, so output the line with "0" for the
                    // three additional fraud columns
                    System.out.println(current_line + ",NF,NF,NF,NF,NF");
                }
            }
        }
        catch(FileNotFoundException ex){
            System.out.printf("File not found in main %n");
        }
        catch(IOException ex){
            System.out.println("IO Exception.");
        }

    } // END: widenWithPidwFraudData
}


class FraudTags{
    private String auth_payment_id = null;
    private String cbk_open_date = null;
    private String cbk_current_cycle = null;
    private String cbk_reason_code = null;
    private String vi_alert_create_date = null;
    private String mc_alert_create_date = null;
    private String bank_declined = null;
    private String response = null;

    public FraudTags(String auth_payment_id, String cbk_open_date, String cbk_current_cycle, String cbk_reason_code,
                        String vi_alert_create_date, String mc_alert_create_date, String bank_declined,
                        String response){
        this.auth_payment_id = auth_payment_id;
        this.cbk_open_date = cbk_open_date;
        this.cbk_open_date = cbk_open_date;
        this.cbk_current_cycle = cbk_current_cycle;
        this.cbk_reason_code = cbk_reason_code;
        this.vi_alert_create_date = vi_alert_create_date;
        this.mc_alert_create_date = mc_alert_create_date;
        this.bank_declined = bank_declined;
        this.response = response;
    }

    public String getAuthPaymentId(){
        return this.auth_payment_id;
    }
    
    public String getCbkOpenDate(){
        return this.cbk_open_date;
    }

    public String getCbkCurrentCycle(){
        return this.cbk_current_cycle;
    }

    public String getCbkReasonCode(){
        return this.cbk_reason_code;
    }

    public String getViAlertCreateDate(){
        return this.vi_alert_create_date;
    }

    public String getMcAlertCreateDate(){
        return this.mc_alert_create_date;
    }

    public String getBankDeclined(){
        return this.bank_declined;
    }

    public String getResponse(){
        return this.response;
    }
}

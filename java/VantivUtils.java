import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Iterator;
import java.lang.Comparable;
import com.opencsv.CSVReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.regex.*;


public class VantivUtils{

    public static void main(String[] args){
        VantivUtils vu = new VantivUtils();
        vu.fixThreatMetrixEventExportFileNew("events.csv");
        //vu.fixThreatMetrixEventExportFile("ThreatMetrixEvents_20170104.csv");
    }
    
    //
    // For the given input file, returns the zero based column index of the "column_name" column.
    //
    public int findColumn(String results_file, String column_name){
        String current_line = null;
        CSVReader reader = null;
        String[] aNextLine = null;
        int reasons_column = 0;

        try{
            FileReader iFR = new FileReader (results_file);
	        BufferedReader iBR = new BufferedReader(iFR);

            // Find the header row by reading the file until finding the string "column_name"
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains(column_name)){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(found){
                StringBuffer tmp_sb = new StringBuffer(column_name);
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                for(String val: aNextLine){
                    if(val.contentEquals(tmp_sb)){
                        return reasons_column;
                    }
                    else{
                        reasons_column++;
                    }
                }
            }
        }
        catch(IOException ex){
            System.out.println("findColumn: IOException");
        }

        return -1;
    } // END: findColumn

    //
    // For the given csv header string, returns the zero based column index
    // of the "column_name" column. Or returns -1 if the column is not present.
    //
    public int findColumnGivenHeader(String header, String column_name){
        CSVReader reader = null;
        String[] aNextLine = null;
        int column_position = 0;

        StringBuffer tmp_sb = new StringBuffer(column_name);
        reader = new CSVReader(new StringReader(header));
        try{
            aNextLine = reader.readNext();
        }
        catch(IOException ex){
            System.out.println("IOException in findColumnGivenHeader");
        }
        for(String val: aNextLine){
            if(val.contentEquals(tmp_sb)){
                return column_position;
            }
            else{
                column_position++;
            }
        }
        
        return -1;
    } // END: findColumnGivenHeader



    //
    // Preprocess the results_file and exact all rules into a Set. The results_file is based off the
    // ThreatMetrix events export file.
    //
    // Input:
    //
    //      results_file: This file must contain a column called "rules_column_name" that contains a comma delimited
    //                    list of rules in the format below:
    //
    //      "{TrueIPGeoOnLocalBlacklist}"
    //      "{5PaymentsOnDeviceLocalDay,5PaymentsOnFuzzyDeviceLocalDay,5PaymentsOnTrueIPLocalDay}"
    //
    // Output:
    //
    //      Ordered set containing the list of rules.
    //
    public NavigableSet<String> generateRulesList(String events_file, NavigableSet<String> rule_set, String rules_column_name){
        String currentLine = null;
        CSVReader reader = null;
        String[] aNextLine = null;
        String[] current_rules = null;

        int reasons_column = findColumn(events_file,rules_column_name);
        if(reasons_column == -1){
            System.out.printf("generateRulesList: Could not find column header \"%s\"%n",rules_column_name);
            System.exit(0);
        }
        
        try{
            FileReader iFR = new FileReader (events_file);
	        BufferedReader iBR = new BufferedReader(iFR);

            while((currentLine = iBR.readLine()) != null) {
                reader = new CSVReader(new StringReader(currentLine));
                aNextLine = reader.readNext();
                if(aNextLine != null){
		            if( (! aNextLine[reasons_column].equals("")) && (! aNextLine[reasons_column].equals(rules_column_name)) ){
                        current_rules = extractRuleNames(aNextLine[reasons_column]);
                        for(String val: current_rules){
                            rule_set.add(val);
                        }
                    }				
                }
            }
        }
        catch(FileNotFoundException ex){System.out.println("File not found.");}
        catch(IOException ex){System.out.println("IO Exception.");}

	    return rule_set;
    } // END: generateRulesList


    //
    // Preprocess the results_file and exact all rules into a Sorted Map. The results_file is based off the
    // ThreatMetrix events export file.
    //
    // Input:
    //
    //      results_file: This file must contain a column called "rules_column_name" that contains a comma delimited
    //                    list of rules in the format below:
    //
    //      "{TrueIPGeoOnLocalBlacklist}"
    //      "{5PaymentsOnDeviceLocalDay,5PaymentsOnFuzzyDeviceLocalDay,5PaymentsOnTrueIPLocalDay}"
    //
    // Output:
    //
    //      TreeMap containing the list of rules.
    //
    public TreeMap<String,String> generateRulesList(String events_file, TreeMap<String,String> rule_map, String rules_column_name){
        String currentLine = null;
        CSVReader reader = null;
        String[] aNextLine = null;
        String[] current_rules = null;

        int reasons_column = findColumn(events_file,rules_column_name);
        if(reasons_column == -1){
            System.out.printf("generateRulesList: Could not find column header \"%s\"%n",rules_column_name);
            System.exit(0);
        }

        try{
            FileReader iFR = new FileReader (events_file);
	        BufferedReader iBR = new BufferedReader(iFR);

            while((currentLine = iBR.readLine()) != null) {
                reader = new CSVReader(new StringReader(currentLine));
                aNextLine = reader.readNext();
                if(aNextLine != null){
		            if( (! aNextLine[reasons_column].equals("")) && (! aNextLine[reasons_column].equals(rules_column_name)) ){
                        current_rules = extractRuleNames(aNextLine[reasons_column]);
                        for(String val: current_rules){
                            rule_map.put(val,"");
                        }
                    }				
                }
            }
        }
        catch(FileNotFoundException ex){System.out.println("File not found.");}
        catch(IOException ex){System.out.println("IO Exception.");}

	    return rule_map;
    } // END: generateRulesList


    // Input is string in the following format:
    //
    //      "{TrueIPGeoOnLocalBlacklist}"
    //      "{5PaymentsOnDeviceLocalDay,5PaymentsOnFuzzyDeviceLocalDay,5PaymentsOnTrueIPLocalDay}"
    //
    // Returns a String[] containing the rule names.
    //
    public String[] extractRuleNames(String commaDelimitedRuleNames){
        if(commaDelimitedRuleNames.equals("")){
            return null;
        }

        commaDelimitedRuleNames = commaDelimitedRuleNames.substring(1,commaDelimitedRuleNames.length()-1);
        String[] ruleNames = commaDelimitedRuleNames.split(",");
        return ruleNames;
    } // END: extractRuleNames


    // Input is string in the following format:
    //
    //      "{TrueIPGeoOnLocalBlacklist}"
    //      "{5PaymentsOnDeviceLocalDay,5PaymentsOnFuzzyDeviceLocalDay,5PaymentsOnTrueIPLocalDay}"
    //
    // Returns a String[] containing the rule names.
    //
    public String[] extractRuleNames2(String commaDelimitedRuleNames){
        if(commaDelimitedRuleNames.equals("")){
            return null;
        }

        //commaDelimitedRuleNames = commaDelimitedRuleNames.substring(1,commaDelimitedRuleNames.length()-1);
        String[] ruleNames = commaDelimitedRuleNames.split(",");
        return ruleNames;
    } // END: extractRuleNames2


    //
    //Return a copy of the input map.
    //
    public Map<String,Integer> copyMap(Map<String,Integer> rule_map){
        Map<String,Integer> copy_of_rule_map = new HashMap<String,Integer>();

        for (Entry<String, Integer> entry : rule_map.entrySet()) {
            String key = entry.getKey().toString();;
            Integer value = entry.getValue();
            copy_of_rule_map.put(key,value);
        }

        return copy_of_rule_map;
    } // END: copyMap


    // ****
    // ****
    // Input is the ThreatMetrix policy xml export file.
    // Returns the rule_name/rule_weight lookup table.
    // ****
    // ****
    public Map<String,Integer> generateRulesLookupFromPolicy(String policy_file){
        String currentLine = null;
        Map<String,Integer> rulesLookup = new HashMap<String,Integer>();
        String riskWeight = null;
        String ruleName = null;

        try{
            FileReader iFR = new FileReader (policy_file);
	        BufferedReader iBR = new BufferedReader(iFR);

            // Process the remainder of the file.
            while((currentLine = iBR.readLine()) != null) {
                if( (currentLine.contains("<rule ")) || (currentLine.contains("<ifRule "))){
                    // Get the rule weight
                    riskWeight = currentLine.split("riskWeight=\"")[1].split("\"")[0];

                    // Get the rule name
                    currentLine = iBR.readLine();
                    ruleName = currentLine.split("<name>")[1].split("</name>")[0];

                    // Store the ruleName/riskWeight in the lookup
                    rulesLookup.put(ruleName,new Integer(riskWeight));
                }
            }
        }
        catch(FileNotFoundException ex){
            System.out.println("generateRulesLookupFromPolicy:  File not found.");
            System.exit(0);
        }
        catch(IOException ex){
            System.out.println("generateRulesLookupFromPolicy:  IO Exception.");
            System.exit(0);
        }

        return rulesLookup;
    } // END: generateRulesLookupFromPolicy


    // ****
    // ****
    // Input is a csv file with "rule_name, rule_weight" value pairs.
    // Returns the rule_name/rule_weight lookup table.
    // ****
    // ****
    public Map<String,Integer> generateRulesLookupFromCsv(String rule_file){
        String currentLine = null;
        Map<String,Integer> rulesLookup = new HashMap<String,Integer>();
        String riskWeight = null;
        String ruleName = null;

        try{
            FileReader iFR = new FileReader (rule_file);
	        BufferedReader iBR = new BufferedReader(iFR);

            //Skip the first line, which is assumed to be the header row.
            iBR.readLine();

            // Process the remainder of the file.
            while((currentLine = iBR.readLine()) != null) {
                // Get the rule name
                ruleName = currentLine.split(",")[0];
                
                // Get the rule weight
                riskWeight = currentLine.split(",")[1];

                // Store the ruleName/riskWeight in the lookup
                rulesLookup.put(ruleName,new Integer(riskWeight));
            }
        }
        catch(FileNotFoundException ex){
            System.out.println("generateRulesLookupFromCsv:  File not found.");
            System.exit(0);
        }
        catch(IOException ex){
            System.out.println("generateRulesLookupFromCsv:  IO Exception.");
            System.exit(0);
        }

        return rulesLookup;
    } // END: generateRulesLookupFromCsv


    //
    // Takes as input a Map<String,Integer> and outputs the values as a csv.
    //
    public void outputMapAsCsv(Map<String,Integer> lookup){
        for(Map.Entry<String, Integer> entry : lookup.entrySet()){
            System.out.printf("%s,%d%n", entry.getKey(), entry.getValue());
        }
    } // END: outputMapAsCsv

    //
    // Takes as input a csv file and a List<String> of column names.
    // Returns a csv file containing just the specified column data.
    //
    public void extractColumnsFromCsv(String csv_file_name, List<String> column_names){
        try{
            FileReader iFR = new FileReader (csv_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = "";

            // Find the header row by reading the file until finding the string column_names[0]
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains(column_names.get(0))){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(!found){
                System.out.println("extractColumnsFromCsv: Header column not found.");
            }

            // Store the header
            String header = current_line;

            // Process the remainder of the file.
            CSVReader reader = null;
            String[] aNextLine = null;
            String out_str = "";
            while(current_line != null){
                out_str = "";
                try{ // If error with csv reader occurs skip this interation.
                    for(String val: column_names){
                        reader = new CSVReader(new StringReader(current_line));
                        aNextLine = reader.readNext();
                        out_str += "\"" + aNextLine[findColumnGivenHeader(header, val)] + "\"" + ",";
                    }
                    // Remove the training ","
                    out_str = out_str.substring(0,out_str.length()-1);
                    System.out.println(out_str);
                }
                catch(NoClassDefFoundError ex){
                    System.out.println("extractColumnsFromCsv: NoClassDefFoundError");
                }

                current_line = iBR.readLine();
            }

        }
        catch(IOException ex){
            System.out.println("extractColumnsFromCsv: IOException");
        }
    } // END: extractColumnsFromCsv


    //
    // Takes as input a TMX events export and:
    //      1. Removes the blank first row of the file (if first row is blank)
    //      2. Removes trailing "," from data rows.
    //      3. Fix "Event Time" format.
    //          "2015-11-02 22:56:36.039 UTC" to "2015/11/02 22:56:36"
    //      4. Fix "Transaction Amount"
    //          "500" to "5.00"
    //      5. Fix "Custom Attribute 9"
    //          "request_id:819893079477290099" to "819893079477290099"
    //      6. Does not include "Reasons", "TMX Reasons", or "Event ID" columns in the input file.
    //
    // Output is to standard out, so redirect to save.
    //
    public void fixThreatMetrixEventExportFile(String events_file_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            // Assumpion is that the first line blank
            current_line = iBR.readLine();
            if(current_line.equals("")){
                current_line = iBR.readLine();
            }


            // Process the remainder of the file
            //
            // Find the "Event Time", "Transaction Amount", "Custom Attribute 9" columns
            int event_time_column = findColumnGivenHeader(current_line, "Event Time");
            int transaction_amount_column = findColumnGivenHeader(current_line, "Transaction Amount");
            int custom_attr9_column = findColumnGivenHeader(current_line, "Custom Attribute 9");

            // Find the "Reasons", and "TMX Reason Code" columns
            int reasons_column = findColumnGivenHeader(current_line, "Reasons");
            int tmx_reasons_column = findColumnGivenHeader(current_line, "TMX Reasons");
            int event_id_column = findColumnGivenHeader(current_line, "Event ID");

            CSVReader reader = null;
            String[] aNextLine = null;
            String str_out = "";
            int cnt = -1;
            
            // Print the header row. Exclude "Reasons" and "TMX Reasons" columns.
            // Note: the header row does not have the extra "," (ARGH!!!)
            reader = new CSVReader(new StringReader(current_line));
            aNextLine = reader.readNext();
            for(String val: aNextLine){
                cnt++;
                if( (cnt != reasons_column) && (cnt != tmx_reasons_column) &&
                        (cnt != event_id_column) ){
                    str_out += val + ",";
                }
            }
            System.out.println(str_out.substring(0,str_out.length()-1));

            while( ((current_line = iBR.readLine()) != null) ) {
                cnt = -1;
                str_out = "";
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();

                if(event_time_column != -1){
                    aNextLine[event_time_column] = fixEventTime(aNextLine[event_time_column]);
                }
                if(transaction_amount_column != -1){
                    aNextLine[transaction_amount_column] = fixTransactionAmount(aNextLine[transaction_amount_column]);
                }
                if(custom_attr9_column != -1){
                    aNextLine[custom_attr9_column] = fixCustomAttribute9(aNextLine[custom_attr9_column]);
                }

                for(String val: aNextLine){
                    cnt++;
                    if( (cnt != reasons_column) && (cnt != tmx_reasons_column) &&
                            (cnt != event_id_column) ){
                        // Keeping the following check in the code, just in case column is included
                        // that contains "," (comma) in the data.
                        if(val.contains(",")){
                            // Change the separator char from "," to "%"
                            val = val.replace(",","%");
                        }
                        str_out += val + ",";
                    }
                }
                System.out.println(str_out.substring(0,str_out.length()-2));
            }
            
        }
        catch(IOException ex){
            System.out.println("IOException in fixThreatMetrixEventExportFile");
        }
    } // END: fixThreatMetrixEventExportFile
    
    


    //
    // Takes as input a TMX events export and:
    //      1. Removes the blank first row of the file (if first row is blank)
    //      2. Removes trailing "," from data rows.
    //      3. Fix "DATETIME" format.
    //          "2015-11-02 22:56:36.039 UTC" to "2015/11/02 22:56:36"
    //      4. Fix "TRANSACTION_AMOUNT"
    //          "500" to "5.00"
    //      5. Fix "LOCAL_ATTRIB_9"
    //          "request_id:819893079477290099" to "819893079477290099"
    //      6. Does not include "REASON_CODE", "TMX_REASON_CODE" columns.
    //
    // Output is to standard out, so redirect to save.
    //
    public void fixThreatMetrixEventExportFileNew(String events_file_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            current_line = iBR.readLine();

            // Process the remainder of the file
            //
            // Find the "DATETIME", "TRANSACTION_AMOUNT", "LOCAL_ATTRIB_9" columns
            int event_time_column = findColumnGivenHeader(current_line, "DATETIME");
            int transaction_amount_column = findColumnGivenHeader(current_line, "TRANSACTION_AMOUNT");
            int custom_attr9_column = findColumnGivenHeader(current_line, "LOCAL_ATTRIB_9");

            // Find the "REASON_CODE", and "TMX_REASON_CODE" columns
            int reasons_column = findColumnGivenHeader(current_line, "REASON_CODE");
            int tmx_reasons_column = findColumnGivenHeader(current_line, "TMX_REASON_CODE");

            CSVReader reader = null;
            String[] aNextLine = null;
            String str_out = "";
            int cnt = -1;
            
            // Print the header row. Exclude "Reasons" and "TMX Reason Code" columns.
            // Note: the header row does not have the extra "," (ARGH!!!)
            reader = new CSVReader(new StringReader(current_line));
            aNextLine = reader.readNext();
            for(String val: aNextLine){
                cnt++;
                if( (cnt != reasons_column) && (cnt != tmx_reasons_column) ){
                    str_out += val + ",";
                }
            }
            System.out.println(str_out.substring(0,str_out.length()-1));

            while( ((current_line = iBR.readLine()) != null) ) {
                cnt = -1;
                str_out = "";
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();

                if(event_time_column != -1){
                    aNextLine[event_time_column] = fixEventTime(aNextLine[event_time_column]);
                }
                if(transaction_amount_column != -1){
                    aNextLine[transaction_amount_column] = fixTransactionAmountNew(aNextLine[transaction_amount_column]);
                }
                if(custom_attr9_column != -1){
                    aNextLine[custom_attr9_column] = fixCustomAttribute9(aNextLine[custom_attr9_column]);
                }

                for(String val: aNextLine){
                    cnt++;
                    if( (cnt != reasons_column) && (cnt != tmx_reasons_column) ){
                        // Keeping the following check in the code, just in case column is included
                        // that contains "," (comma) in the data.
                        if(val.contains(",")){
                            // Change the separator char from "," to "%"
                            val = val.replace(",","%");
                        }
                        str_out += val + ",";
                    }
                }
                System.out.println(str_out.substring(0,str_out.length()-1));
            }
            
        }
        catch(IOException ex){
            System.out.println("IOException in fixThreatMetrixEventExportFile");
        }
    } // END: fixThreatMetrixEventExportFileNew
    
    
    
    //
    // Takes as input a TMX events export and:
    //      1. Removes the blank first row of the file (if first row is blank)
    //      2. Removes trailing "," from data rows.
    //      3. Fix "DATETIME" format.
    //          "2015-11-02 22:56:36.039 UTC" to "2015/11/02 22:56:36"
    //      4. Fix "TRANSACTION_AMOUNT"
    //          "500" to "5.00"
    //      5. Fix "LOCAL_ATTRIB_9"
    //          "request_id:819893079477290099" to "819893079477290099"
    //      6. Does not include "REASON_CODE", "TMX_REASON_CODE" columns.
    //
    // Output is to standard out, so redirect to save.
    //
    public void fixThreatMetrixEventExportFileNew2(String events_file_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            current_line = iBR.readLine();

            // Process the remainder of the file
            //
            // Find the "Event Time", "Transaction Amount", "Custom Attribute 9" columns
            int event_time_column = findColumnGivenHeader(current_line, "Event Time");
            int transaction_amount_column = findColumnGivenHeader(current_line, "Transaction Amount");
            int custom_attr9_column = findColumnGivenHeader(current_line, "Custom Attribute 9");

            // Find the "Reasons", and "TMX_REASON_CODE" columns
            int reasons_column = findColumnGivenHeader(current_line, "Reasons");
            int tmx_reasons_column = findColumnGivenHeader(current_line, "TMX Reason Code");

            CSVReader reader = null;
            String[] aNextLine = null;
            String str_out = "";
            int cnt = -1;
            
            // Print the header row. Exclude "Reasons" and "TMX Reason Code" columns.
            // Note: the header row does not have the extra "," (ARGH!!!)
            reader = new CSVReader(new StringReader(current_line));
            aNextLine = reader.readNext();
            for(String val: aNextLine){
                cnt++;
                if( (cnt != reasons_column) && (cnt != tmx_reasons_column) ){
                    str_out += val + ",";
                }
            }
            System.out.println(str_out.substring(0,str_out.length()-1));

            while( ((current_line = iBR.readLine()) != null) ) {
                cnt = -1;
                str_out = "";
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();

                if(event_time_column != -1){
                    aNextLine[event_time_column] = fixEventTime(aNextLine[event_time_column]);
                }
                if(transaction_amount_column != -1){
                    aNextLine[transaction_amount_column] = fixTransactionAmountNew(aNextLine[transaction_amount_column]);
                }
                if(custom_attr9_column != -1){
                    aNextLine[custom_attr9_column] = fixCustomAttribute9(aNextLine[custom_attr9_column]);
                }

                for(String val: aNextLine){
                    cnt++;
                    if( (cnt != reasons_column) && (cnt != tmx_reasons_column) ){
                        // Keeping the following check in the code, just in case column is included
                        // that contains "," (comma) in the data.
                        if(val.contains(",")){
                            // Change the separator char from "," to "%"
                            val = val.replace(",","%");
                        }
                        str_out += val + ",";
                    }
                }
                System.out.println(str_out.substring(0,str_out.length()-1));
            }
            
        }
        catch(IOException ex){
            System.out.println("IOException in fixThreatMetrixEventExportFile");
        }
    } // END: fixThreatMetrixEventExportFileNew
    
    
    // Takes as input:
    //      1. TMX events export file
    //      2. Name of "Join" column
    //      3. Name of "Rules" column
    //      4. Name of output "Join" column
    //      5. Name of output "Rules" column
    //
    // Output is two columns csv file. First column is join id column value. Second column is
    // rule associated to this join id. For example for input of:
    //
    //      1234,{rule1,rule2}
    //      4321,{rule1}
    //
    // Output would be:
    //
    //      1234,rule1
    //      1234,rule2
    //      4321,rule1
    //
    // Output is to standard out, so redirect to save.
    //
    public void extractRulesBasedOnJoinId(String events_file_name, String join_column_name,
                                        String rules_column_name, String output_join_column_name,
                                        String output_rules_column_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            // Find the header row by reading the file until finding the string column_names[0]
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains(join_column_name)){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(!found){
                System.out.println("extractRulesBasedOnJoinId: Header column not found.");
                System.exit(0);
            }

            // Check that the rules column exists
            if(! current_line.contains(rules_column_name)){
                System.out.println("extractRulesBasedOnJoinId: Rules column not found.");
                System.exit(0);
            }
            
            int rules_column = findColumnGivenHeader(current_line, rules_column_name);
            int join_column = findColumnGivenHeader(current_line, join_column_name);

            // Print the header row
            System.out.println(output_join_column_name + "," + output_rules_column_name);

            // Process the remainder of the file
            CSVReader reader = null;
            String[] aNextLine = null;

            // Process the remainder of the file.
            String[] stra_rules = null;
            String request_id = null;
            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                stra_rules = extractRuleNames(aNextLine[rules_column]);
                request_id = aNextLine[join_column];

                if(stra_rules != null){
                    for(String val: stra_rules){
                        System.out.println(request_id + "," + val);
                    }
                }
                else{
                    System.out.println(request_id + ",no_rules");
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in extractRulesBasedOnJoinId");
        }
    } // END: extractRulesBasedOnJoinId



    // Takes as input:
    //      1. TMX events export file
    //
    // Output is two columns csv file. First column is join id column value. Second column is
    // rule associated to this join id. For example for input of:
    //
    //      1234,{rule1,rule2}
    //      4321,{rule1}
    //
    // Output would be:
    //
    //      1234,rule1
    //      1234,rule2
    //      4321,rule1
    //
    // Output is to standard out, so redirect to save.
    //
    public void extractRulesOld(String events_file_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            // Find the header row by reading the file until finding the string "Request ID"
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains("Request ID")){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(!found){
                System.out.println("extractRulesOld: Header column not found.");
                System.exit(0);
            }

            // Check that the rules column exists
            if(! current_line.contains("Reasons")){
                System.out.println("extractRulesOld: Rules column not found.");
                System.exit(0);
            }
            
            int rules_column = findColumnGivenHeader(current_line, "Reasons");
            int join_column = findColumnGivenHeader(current_line, "Request ID");

            // Print the header row
            System.out.println("Request ID,Rules");

            // Process the remainder of the file
            CSVReader reader = null;
            String[] aNextLine = null;

            // Process the remainder of the file.
            String[] stra_rules = null;
            String request_id = null;
            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                stra_rules = extractRuleNames(aNextLine[rules_column]);
                request_id = aNextLine[join_column];

                if(stra_rules != null){
                    for(String val: stra_rules){
                        System.out.println(request_id + "," + val);
                    }
                }
                else{
                    System.out.println(request_id + ",no_rules");
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in extractRulesOld");
        }
    } // END: extractRulesOld
    
    
    // Takes as input:
    //      1. TMX events export file
    //
    // Output is two columns csv file. First column is join id column value. Second column is
    // rule associated to this join id. For example for input of:
    //
    //      1234,{rule1,rule2}
    //      4321,{rule1}
    //
    // Output would be:
    //
    //      1234,rule1
    //      1234,rule2
    //      4321,rule1
    //
    // Output is to standard out, so redirect to save.
    //
    public void extractRulesNew(String events_file_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            // Find the header row by reading the file until finding the string "Request ID"
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains("REQUEST_ID")){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(!found){
                System.out.println("extractRulesNew: Header column not found.");
                System.exit(0);
            }

            // Check that the rules column exists
            if(! current_line.contains("REASON_CODE")){
                System.out.println("extractRulesNew: Rules column not found.");
                System.exit(0);
            }
            
            int rules_column = findColumnGivenHeader(current_line, "REASON_CODE");
            int join_column = findColumnGivenHeader(current_line, "REQUEST_ID");

            // Print the header row
            System.out.println("REQUEST_ID,Rules");

            // Process the remainder of the file
            CSVReader reader = null;
            String[] aNextLine = null;

            // Process the remainder of the file.
            String[] stra_rules = null;
            String request_id = null;
            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                stra_rules = extractRuleNames2(aNextLine[rules_column]);
                request_id = aNextLine[join_column];

                if(stra_rules != null){
                    for(String val: stra_rules){
                        System.out.println(request_id + "," + val);
                    }
                }
                else{
                    System.out.println(request_id + ",no_rules");
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in extractRulesNew");
        }
    } // END: extractRulesNew
    
    
    
    // Takes as input:
    //      1. TMX events export file
    //      2. Name of "Join" column
    //      3. Name of "Rules" column
    //      4. Name of output "Join" column
    //      5. Name of output "Rules" column
    //
    // Output is two columns csv file. First column is join id column value. Second column is
    // rule associated to this join id. For example for input of:
    //
    //      1234,{rule1,rule2}
    //      4321,{rule1}
    //
    // Output would be:
    //
    //      1234,rule1
    //      1234,rule2
    //      4321,rule1
    //
    // Output is to standard out, so redirect to save.
    //
    public void extractTmxRulesBasedOnJoinId(String events_file_name, String join_column_name,
                                        String rules_column_name, String output_join_column_name,
                                        String output_rules_column_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            // Find the header row by reading the file until finding the string column_names[0]
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains(join_column_name)){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(!found){
                System.out.println("extractRulesBasedOnJoinId: Header column not found.");
                System.exit(0);
            }

            // Check that the rules column exists
            if(! current_line.contains(rules_column_name)){
                System.out.println("extractRulesBasedOnJoinId: Rules column not found.");
                System.exit(0);
            }
            
            int rules_column = findColumnGivenHeader(current_line, rules_column_name);
            int join_column = findColumnGivenHeader(current_line, join_column_name);

            // Print the header row
            System.out.printf("%1$s,%2$s,TmxCoreRule,IsVelocity,VelocityExpression,VHr,VDay,VWeek,VMonth%n",
                                output_join_column_name,output_rules_column_name);

            // Process the remainder of the file
            CSVReader reader = null;
            String[] aNextLine = null;

            String[] stra_rules = null;
            String request_id = null;
            String is_velocity = null;
            String tmx_rule_name = null;
            String core_rule_name = null;
            String velocity_expr = null;
            String vhr = null;
            String vday = null;
            String vweek = null;
            String vmonth = null;
            String[] stra_vel_expr = null;
            String vel_regx = "_\\d+_\\d+_\\d+_\\d+";
            Pattern vel_pattern = Pattern.compile(vel_regx);
            Matcher vel_matcher = null;

            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                stra_rules = extractRuleNames(aNextLine[rules_column]);
                request_id = aNextLine[join_column];

                if(stra_rules != null){
                    for(String val: stra_rules){
                        is_velocity = "0";
                        tmx_rule_name = val;
                        core_rule_name = val;
                        velocity_expr = "NA";
                        vhr = "NA";
                        vday = "NA";
                        vweek = "NA";
                        vmonth = "NA";

                        vel_matcher = vel_pattern.matcher(val);

                        if(vel_matcher.find()){
                            core_rule_name = val.substring(0,vel_matcher.start());
                            is_velocity = "1";
                            velocity_expr = vel_matcher.group().substring(1,vel_matcher.group().length());
                            stra_vel_expr = vel_matcher.group().split("_");
                            vhr = stra_vel_expr[1];
                            vday = stra_vel_expr[2];
                            vweek = stra_vel_expr[3];
                            vmonth = stra_vel_expr[4];
                        }
                        System.out.printf("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s%n",
                                            request_id,tmx_rule_name,core_rule_name,is_velocity,velocity_expr,
                                            vhr,vday,vweek,vmonth);
                    }
                }
                else{
                    System.out.printf("%1$s,no_rules,no_rules,0,NA,NA,NA,NA,NA%n",
                            request_id);
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in extractRulesBasedOnJoinId");
        }
    } // END: extractTmxRulesBasedOnJoinId




    // Takes as input:
    //      1. TMX events export file
    //
    // Output is two columns csv file. First column is join id column value. Second column is
    // rule associated to this join id. For example for input of:
    //
    //      1234,{rule1,rule2}
    //      4321,{rule1}
    //
    // Output would be:
    //
    //      1234,rule1
    //      1234,rule2
    //      4321,rule1
    //
    // Output is to standard out, so redirect to save.
    //
    public void extractTmxRulesOld(String events_file_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            // Find the header row by reading the file until finding the string "Request ID"
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains("Request ID")){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(!found){
                System.out.println("extractTmxRulesOld: Header column not found.");
                System.exit(0);
            }

            // Check that the rules column exists
            if(! current_line.contains("TMX Reasons")){
                System.out.println("extractTmxRulesOld: Rules column not found.");
                System.exit(0);
            }
            
            int rules_column = findColumnGivenHeader(current_line, "TMX Reasons");
            int join_column = findColumnGivenHeader(current_line, "Request ID");

            // Print the header row
            System.out.printf("%1$s,%2$s,TmxCoreRule,IsVelocity,VelocityExpression,VHr,VDay,VWeek,VMonth%n",
                                "Request ID","tmxrules");

            // Process the remainder of the file
            CSVReader reader = null;
            String[] aNextLine = null;

            String[] stra_rules = null;
            String request_id = null;
            String is_velocity = null;
            String tmx_rule_name = null;
            String core_rule_name = null;
            String velocity_expr = null;
            String vhr = null;
            String vday = null;
            String vweek = null;
            String vmonth = null;
            String[] stra_vel_expr = null;
            String vel_regx = "_\\d+_\\d+_\\d+_\\d+";
            Pattern vel_pattern = Pattern.compile(vel_regx);
            Matcher vel_matcher = null;

            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                stra_rules = extractRuleNames(aNextLine[rules_column]);
                request_id = aNextLine[join_column];

                if(stra_rules != null){
                    for(String val: stra_rules){
                        is_velocity = "0";
                        tmx_rule_name = val;
                        core_rule_name = val;
                        velocity_expr = "NA";
                        vhr = "NA";
                        vday = "NA";
                        vweek = "NA";
                        vmonth = "NA";

                        vel_matcher = vel_pattern.matcher(val);

                        if(vel_matcher.find()){
                            core_rule_name = val.substring(0,vel_matcher.start());
                            is_velocity = "1";
                            velocity_expr = vel_matcher.group().substring(1,vel_matcher.group().length());
                            stra_vel_expr = vel_matcher.group().split("_");
                            vhr = stra_vel_expr[1];
                            vday = stra_vel_expr[2];
                            vweek = stra_vel_expr[3];
                            vmonth = stra_vel_expr[4];
                        }
                        System.out.printf("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s%n",
                                            request_id,tmx_rule_name,core_rule_name,is_velocity,velocity_expr,
                                            vhr,vday,vweek,vmonth);
                    }
                }
                else{
                    System.out.printf("%1$s,no_rules,no_rules,0,NA,NA,NA,NA,NA%n",
                            request_id);
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in extractTmxRulesOld");
        }
    } // END: extractTmxRulesOld



    // Takes as input:
    //      1. TMX events export file
    //
    // Output is two columns csv file. First column is join id column value. Second column is
    // rule associated to this join id. For example for input of:
    //
    //      1234,{rule1,rule2}
    //      4321,{rule1}
    //
    // Output would be:
    //
    //      1234,rule1
    //      1234,rule2
    //      4321,rule1
    //
    // Output is to standard out, so redirect to save.
    //
    public void extractTmxRulesNew(String events_file_name){
        try{
            FileReader iFR = new FileReader (events_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            // Find the header row by reading the file until finding the string "Request ID"
            // Only check the first two rows.
            boolean found = false;
            int line_count = 0;
            while( ((current_line = iBR.readLine()) != null) && (line_count < 2) ) {
                if(current_line.contains("REQUEST_ID")){
                    found = true;
                    break;
                }
                line_count++;
            }

            if(!found){
                System.out.println("extractTmxRulesNew: Header column not found.");
                System.exit(0);
            }

            // Check that the rules column exists
            if(! current_line.contains("TMX_REASON_CODE")){
                System.out.println("extractTmxRulesNew: Rules column not found.");
                System.exit(0);
            }
            
            int rules_column = findColumnGivenHeader(current_line, "TMX_REASON_CODE");
            int join_column = findColumnGivenHeader(current_line, "REQUEST_ID");

            // Print the header row
            System.out.printf("%1$s,%2$s,TmxCoreRule,IsVelocity,VelocityExpression,VHr,VDay,VWeek,VMonth%n",
                                "REQUEST_ID","tmxrules");

            // Process the remainder of the file
            CSVReader reader = null;
            String[] aNextLine = null;

            String[] stra_rules = null;
            String request_id = null;
            String is_velocity = null;
            String tmx_rule_name = null;
            String core_rule_name = null;
            String velocity_expr = null;
            String vhr = null;
            String vday = null;
            String vweek = null;
            String vmonth = null;
            String[] stra_vel_expr = null;
            String vel_regx = "_\\d+_\\d+_\\d+_\\d+";
            Pattern vel_pattern = Pattern.compile(vel_regx);
            Matcher vel_matcher = null;

            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                stra_rules = extractRuleNames2(aNextLine[rules_column]);
                request_id = aNextLine[join_column];

                if(stra_rules != null){
                    for(String val: stra_rules){
                        is_velocity = "0";
                        tmx_rule_name = val;
                        core_rule_name = val;
                        velocity_expr = "NA";
                        vhr = "NA";
                        vday = "NA";
                        vweek = "NA";
                        vmonth = "NA";

                        vel_matcher = vel_pattern.matcher(val);

                        if(vel_matcher.find()){
                            core_rule_name = val.substring(0,vel_matcher.start());
                            is_velocity = "1";
                            velocity_expr = vel_matcher.group().substring(1,vel_matcher.group().length());
                            stra_vel_expr = vel_matcher.group().split("_");
                            vhr = stra_vel_expr[1];
                            vday = stra_vel_expr[2];
                            vweek = stra_vel_expr[3];
                            vmonth = stra_vel_expr[4];
                        }
                        System.out.printf("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s%n",
                                            request_id,tmx_rule_name,core_rule_name,is_velocity,velocity_expr,
                                            vhr,vday,vweek,vmonth);
                    }
                }
                else{
                    System.out.printf("%1$s,no_rules,no_rules,0,NA,NA,NA,NA,NA%n",
                            request_id);
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in extractTmxRulesNew");
        }
    } // END: extractTmxRulesNew


    // 
    // Takes as input the TMX Transaction Amount formatted string "500"
    // Return the string in the format below:
    //
    //      "5.00"
    //
    public String fixTransactionAmount(String transaction_amount){
        if(transaction_amount.length() > 2){
            return transaction_amount.substring(0,transaction_amount.length() - 2) + "." +
                transaction_amount.substring(transaction_amount.length() - 2);
        }

        if(transaction_amount.length() == 2){
            return "0." + transaction_amount;
        }

        if(transaction_amount.length() < 2){
            return "0.0" + transaction_amount;
        }
            return transaction_amount;
    } // END: fixTransactionAmount


    // 
    // Takes as input the TMX Transaction Amount formatted string "500.0"
    // Return the string in the format below:
    //
    //      "5.00"
    //
    public String fixTransactionAmountNew(String transaction_amount){
        if(transaction_amount.length() != 0){
            transaction_amount = transaction_amount.substring(0,transaction_amount.length() - 2);
        }

        if(transaction_amount.length() > 2){
            return transaction_amount.substring(0,transaction_amount.length() - 2) + "." +
                transaction_amount.substring(transaction_amount.length() - 2);
        }

        if(transaction_amount.length() == 2){
            return "0." + transaction_amount;
        }

        if(transaction_amount.length() < 2){
            return "0.0" + transaction_amount;
        }
            return transaction_amount;
    } // END: fixTransactionAmount


    // 
    // Takes as input the TMX Custom Attribute 9 formatted string "request_id:819893155809938868"
    // Return the string in the format below:
    //
    //      "819893155809938868"
    //
    public String fixCustomAttribute9(String custom_attr9){
        if(custom_attr9.length() > 0){
            custom_attr9 = custom_attr9.split(":")[1];
        }
        return custom_attr9;
    } // END: fixCustomAttribute9

    
    //
    // Takes as input the TMX Event Time formatted string "2015-11-13 17:59:16.932 UTC"
    // Return the string in the format below:
    //
    //      "2015/11/13 17:59:16.932"
    //
    public String fixEventTime(String event_time){
        if(event_time.length() > 0){
            event_time = event_time.split("UTC")[0];
            event_time = event_time.replace("-","/");
            event_time = event_time.trim();
        }
        return event_time;
    } // END: fixEventTime


    //
    // Input is a one column input file (name). Reads each line and adds String value
    // to TreeSet.
    //
    // Returns the TreeSet created
    //
    public TreeSet<String> loadTreeSetFromFile(String filename){
        TreeSet<String> ts = new TreeSet<String>();
        try{
            FileReader iFR = new FileReader (filename);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;

            while( (current_line = iBR.readLine()) != null){
                ts.add(current_line);
            }
        }
        catch(IOException ex){
            System.out.println("IOException in VantivUtils.loadTreeSetFromFile()");
        }

        return ts;
    } // END: loadTreeSetFromFile
    

    // Inputs are a flag and 1 or 2 filenames. The files contain one column of strings.
    // Each files string values are stored in a Set. File1 to SetA, and File2 to SetB
    //
    // Depending on the input flag, will output:
    //
    //      flag "union"
    //          A union B
    //      flag "intersection"
    //          A intersection B
    //      flag diff
    //          A - B
    //      flag dups
    //          The values from file1 that contain duplicates
    //
    public void setUtils(String[] input_values){
        if( (input_values[0].contains("union")) ){
            TreeSet<String> A = loadTreeSetFromFile(input_values[1]);
            TreeSet<String> B = loadTreeSetFromFile(input_values[2]);

            for(String val: B){
                A.add(val);
            }
            for(String val: A){
                System.out.println(val);
            }
        }
        else if( (input_values[0].contains("intersection")) ){
            TreeSet<String> A = loadTreeSetFromFile(input_values[1]);
            TreeSet<String> B = loadTreeSetFromFile(input_values[2]);

            A.retainAll(B);
            for(String val: A){
                System.out.println(val);
            }
        }
        else if( (input_values[0].contains("dif")) ){
            TreeSet<String> A = loadTreeSetFromFile(input_values[1]);
            TreeSet<String> B = loadTreeSetFromFile(input_values[2]);

            A.removeAll(B);
            for(String val: A){
                System.out.println(val);
            }
        }
        else{ // Check for duplicates
            TreeSet<String> A = new TreeSet<String>();
            TreeSet<String> B = new TreeSet<String>();
            try{
                FileReader iFR = new FileReader (input_values[1]);
	            BufferedReader iBR = new BufferedReader(iFR);
                String current_line = null;

                while( (current_line = iBR.readLine()) != null){
                    if(! A.add(current_line)){
                        B.add(current_line);
                    }
                }

                for(String val: B){
                    System.out.println(val);
                }

            }
            catch(IOException ex){
                System.out.println("IOException in VantivUtils.loadTreeSetFromFile()");
            }
        }
    } // END: SetUtils


    //
    // Input:
    //      rulename: want every "Request ID" containing this rule
    //
    //      rule_file_name: assuming 2 column csv file "Request ID,Rule"
    //          This file contains all the rules that fired for this merchant
    //
    //      id_dir_name: complete path to the directory to store the file
    //                   containing the IDs for this rule
    //                   Note: The name must include the ending directory separator character. For example:
    //                          "/Users/ernesthall1/testing/java/ids/"
    //                          "C:\\tools\\development\\java\\utils\\"
    //
    public void createIdSetFileForStandardRule(String rulename, String rule_file_name, String id_dir_name){
        // Check if ID file already exists. Note: Assumes that for standard rules the
        // name of the rule file is the name of the rule + ".txt". So "FlashDisabled" rule ids
        // will be stored in the file "FlashDisabled.txt"
        if(checkDirectoryForFile(rulename + ".txt", id_dir_name)){
            return;
        }

        // The id set file does not exist. So create it now.
        TreeSet<String> ts = new TreeSet<String>();
        try{
            FileWriter iFW = new FileWriter (id_dir_name + rulename + ".txt");
	        BufferedWriter iBW = new BufferedWriter(iFW);

            FileReader iFR = new FileReader (rule_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;
            String current_rule_name = null;

            while( (current_line = iBR.readLine()) != null){
                current_rule_name = current_line.split(",")[1];
                if(current_rule_name.equals(rulename)){
                    iBW.write(current_line.split(",")[0]);
                    iBW.newLine();
                }
            }
            iBW.flush();
            iBW.close();
            iBR.close();
        }
        catch(IOException ex){
            System.out.println("IOException in VantivUtils.createIdSetFileForStandardRule()");
        }
    } // END: createIdSetFileForStandardRule


    //
    // Input:
    //      rulename: want every "Request ID" containing this rule
    //
    //      tmx_rule_file_name: assuming tmx formatted rules file. File format is:
    //              Request ID,TmxRules,TmxCoreRule,IsVelocity,VelocityExpression,VHr,VDay,VWeek,VMonth
    //              9ba17fd6e4f349e5896467b690482e7b,_IP_GBL_VEL_2_2_2_3,_IP_GBL_VEL,1,2_2_2_3,2,2,2,3
    //              9ba17fd6e4f349e5896467b690482e7b,_IP_GBL_AGE_GT_3MTHS,_IP_GBL_AGE_GT_3MTHS,0,NA,NA,NA,NA,NA
    //          
    //      id_dir_name: complete path to the directory to store the file
    //                   containing the IDs for this rule
    //                   Note: The name must include the ending directory separator character. For example:
    //                          "/Users/ernesthall1/testing/java/ids/"
    //                          "C:\\tools\\development\\java\\utils\\"
    //
    public void createIdSetFileForTmxStandardRule(String rulename, String tmx_rule_file_name, String id_dir_name){
        // Check if ID file already exists. Note: Assumes that for standard rules the
        // name of the rule file is the name of the rule + ".txt". So "_EXACT_ID_GBL_AGE_GT_2WKS" rule ids
        // will be stored in the file "_EXACT_ID_GBL_AGE_GT_2WKS.txt"
        if(checkDirectoryForFile(rulename + ".txt", id_dir_name)){
            return;
        }

        // The id set file does not exist. So create it now.
        TreeSet<String> ts = new TreeSet<String>();
        try{
            FileWriter iFW = new FileWriter (id_dir_name + rulename + ".txt");
	        BufferedWriter iBW = new BufferedWriter(iFW);

            FileReader iFR = new FileReader (tmx_rule_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;
            String current_rule_name = null;

            while( (current_line = iBR.readLine()) != null){
                current_rule_name = current_line.split(",")[1];
                if(current_rule_name.equals(rulename)){
                    iBW.write(current_line.split(",")[0]);
                    iBW.newLine();
                }
            }
            iBW.flush();
            iBW.close();
            iBR.close();
        }
        catch(IOException ex){
            System.out.println("IOException in VantivUtils.createIdSetFileForTmxStandardRule()");
        }
    } // END: createIdSetFileForTmxStandardRule


    //
    // Input:
    //      rulename: want every "Request ID" containing this rule. Must be TmxCoreRule
    //
    //      vel: velocity must be greater then or equal to this value
    //
    //      timeframe: velocity must be greater then or equal to this value
    //
    //      tmx_rule_file_name: assuming tmx formatted rules file. File format is:
    //              Request ID,TmxRules,TmxCoreRule,IsVelocity,VelocityExpression,VHr,VDay,VWeek,VMonth
    //              9ba17fd6e4f349e5896467b690482e7b,_IP_GBL_VEL_2_2_2_3,_IP_GBL_VEL,1,2_2_2_3,2,2,2,3
    //              9ba17fd6e4f349e5896467b690482e7b,_IP_GBL_AGE_GT_3MTHS,_IP_GBL_AGE_GT_3MTHS,0,NA,NA,NA,NA,NA
    //          
    //      id_dir_name: complete path to the directory to store the file
    //                   containing the IDs for this rule
    //                   Note: The name must include the ending directory separator character. For example:
    //                          "/Users/ernesthall1/testing/java/ids/"
    //                          "C:\\tools\\development\\java\\utils\\"
    //
    public void createIdSetFileForTmxVelocityRule(String rulename, String vel, String timeframe,
                                                  String tmx_rule_file_name, String id_dir_name){
        // Check if ID file already exists. Note: Assumes that for velocity rules the
        // name of the rule file is the name of the TmxCoreRule + "_GTE_" + "vel" + "_timeframe" + ".txt".
        // where "vel" is the integer from the below simulation entry and "time" is "HR", "DY", "WK", or "MO"
        // So "_EXACT_ID_GBL_VEL,t,3,hr"
        // will be stored in the file "_EXACT_ID_GBL_VEL_GTE_3_HR.txt"
        String rule_id_filename = rulename + "_GTE_" + vel + "_" + timeframe + ".txt";
        if(checkDirectoryForFile(rule_id_filename, id_dir_name)){
            return;
        }

        // The id set file does not exist. So create it now.
        TreeSet<String> ts = new TreeSet<String>();
        try{
            FileWriter iFW = new FileWriter (id_dir_name + rule_id_filename);
	        BufferedWriter iBW = new BufferedWriter(iFW);

            FileReader iFR = new FileReader (tmx_rule_file_name);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;
            String current_rule_name = null;

            while( (current_line = iBR.readLine()) != null){
                current_rule_name = current_line.split(",")[2]; // TmxCoreRule
                if(current_rule_name.equals(rulename)){
                    // Get the velocity value for the specified timeframe
                    // Request ID,TmxRules,TmxCoreRule,IsVelocity,VelocityExpression,VHr,VDay,VWeek,VMonth
                    Integer vel_value = null;
                    Integer current_vel_value = new Integer(vel);
                    if(timeframe.equals("HR")){
                        vel_value = new Integer(current_line.split(",")[5]);
                    }
                    else if(timeframe.equals("DY")){
                        vel_value = new Integer(current_line.split(",")[6]);
                    }
                    else if(timeframe.equals("WK")){
                        vel_value = new Integer(current_line.split(",")[7]);
                    }
                    else if(timeframe.equals("MO")){
                        vel_value = new Integer(current_line.split(",")[8]);
                    }

                    if(vel_value >= current_vel_value){
                        iBW.write(current_line.split(",")[0]);
                        iBW.newLine();
                    }
                }
            }
            iBW.flush();
            iBW.close();
            iBR.close();
        }
        catch(IOException ex){
            System.out.println("IOException in VantivUtils.createIdSetFileForTmxVelocityRule()");
        }
    } // END: createIdSetFileForTmxVelocityRule



    //
    // Given (1) the name of a file, and (2) a directory to search, return true if
    // the file is found in the directory. Otherwise return false.
    //
    public boolean checkDirectoryForFile(String filename, String directory_path){
        Path dir = Paths.get(directory_path);

        PathMatcher pm = FileSystems.getDefault().getPathMatcher("regex:" + filename);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)){
            for(Path value: stream){
                if(pm.matches(value.getFileName())){
                    return true;
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in checkDirectoryForFile");
        }
        return false;
    } // END: checkDirectoryForFile


    //
    // Input:
    //      simulation_filename:
    //          sim file in the following format:
    //              rule1,r       -- regular rule, data in reasons file
    //              rule2,t,3,hr  -- tmx velocity rule, data in tmx file
    //              rule2,t,5,wk
    //              rule4,t       -- regular rule, data in tmx file
    //
    //      reasons_filename:
    //          Vantiv rules file in the following format:
    //              Request ID, rule_name
    //
    //      tmx_reasons_filename:
    //          TMX rules file in the following format:
    //              Request ID,TmxRules,TmxCoreRule,IsVelocity,VelocityExpression,VHr,VDay,VWeek,VMonth
    //              9ba17fd6e4f349e5896467b690482e7b,_IP_GBL_VEL_2_2_2_3,_IP_GBL_VEL,1,2_2_2_3,2,2,2,3
    //              9ba17fd6e4f349e5896467b690482e7b,_IP_GBL_AGE_GT_3MTHS,_IP_GBL_AGE_GT_3MTHS,0,NA,NA,NA,NA,NA
    //
    //      set_ids_directory:
    //          Directory where id sets are stored.
    //
    // Output:
    //      Set files containing all the Request IDs where the particular rule fired.
    //      The rules are those listed in the simulation_filename file. File naming
    //      convention is as follows:
    //          regular rule: "regular_rulename" + ".txt"
    //          velocity rule: "core rule name" + "vel_num" + "vel_timeframe" + ".txt"
    //
    //      The files are stored in set_ids_directory
    //
    public void createSetFilesFromList(String simulation_filename, String reasons_filename,
                                       String tmx_reasons_filename, String set_ids_directory){
        //vutils generate_files.txt ICM_Rules.csv ICM_Rules.csv "c:\\vantiv_docs\\instant_checkmate\\20151109\\ids"
        //vu.checkDirectoryForFile("WidenRules.java", "C:\\tools\\development\\java\\utils");
        //vu.checkDirectoryForFile("WidenRules.java", "/Users/ernesthall1/testing/java/ids");
        try{
            FileReader iFR = new FileReader (simulation_filename);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;
            String rule_type = null;
            while( ((current_line = iBR.readLine()) != null) ) {
                rule_type = getRuleType(current_line);
                if(rule_type.equals("REGULAR_RULE")){
                    createIdSetFileForStandardRule(current_line.split(",")[0], reasons_filename, set_ids_directory);
                }
                else if(rule_type.equals("REGULAR_TMX_RULE")){
                    createIdSetFileForTmxStandardRule(current_line.split(",")[0], tmx_reasons_filename, set_ids_directory);
                }
                else if(rule_type.equals("TMX_VELOCITY_RULE")){
                    String[] sim_value = current_line.split(",");
                    createIdSetFileForTmxVelocityRule(sim_value[0], // rulename
                                                      sim_value[2], // velocity
                                                      sim_value[3].toUpperCase(), // timeframe
                                                      tmx_reasons_filename, set_ids_directory);
                }
            }
            iBR.close();
        }
        catch(IOException ex){
            System.out.println("IOExeption in createSetFilesFromList");
        }
    } // END: createSetFilesFromList


    //
    // Input:
    //      String in the following format(s):
    //          rule1,r       -- regular rule, data in reasons file
    //          rule2,t,3,hr  -- tmx velocity rule, data in tmx file
    //          rule4,t       -- regular rule, data in tmx file
    // Output:
    //          rule1,r       -- "REGULAR_RULE"
    //          rule2,t,3,hr  -- "TMX_VELOCITY_RULE"
    //          rule4,t       -- "REGULAR_TMX_RULE"
    //
    public String getRuleType(String rule_string){
        String[] rule_info = rule_string.split(",");
        if(rule_info[1].equals("r")){
            return "REGULAR_RULE";
        }
        else if( (rule_info.length == 2) &&
                    rule_info[1].equals("t") ){
            return "REGULAR_TMX_RULE";
        }
        else if( (rule_info.length == 4) &&
                    rule_info[1].equals("t") ){
            return "TMX_VELOCITY_RULE";
        }
        return "";
    } // END: getRuleType


    //
    // Given a "cleaned" and "merged" file, assumes the following columns exists:
    //      ChargeBack, VIAlert, MCAlert, BankDeclined
    //
    // Returns the original file with a new column called "IsFraud". The value of
    // this column is:
    //      "NF": if any of the column are "NF"
    //      "BD": if BankDeclined is "1"
    //      "1": if ChargeBack, VIAlert, or MCAlert is "1"
    //      "0": otherwise
    //
    // Use this method so that I do not need to calculate the fraud value in Tableau.
    //
    public void addFraudColumn(String filename){
        String current_line = null;
        CSVReader reader = null;
        String[] aNextLine = null;

        try{
            FileReader iFR = new FileReader (filename);
	        BufferedReader iBR = new BufferedReader(iFR);

            // Read the header row and find the relevant columns
            current_line = iBR.readLine();
            int visa_alert_column = findColumnGivenHeader(current_line, "VIAlert");
            int mc_alert_column = findColumnGivenHeader(current_line, "MCAlert");
            int cbk_column = findColumnGivenHeader(current_line, "ChargeBack");
            int bank_declined_column = findColumnGivenHeader(current_line, "BankDeclined");

            // Output the new header row
            System.out.println(current_line + ",IsFraud");

            // Read remainder of file and calculate IsFraud column
            String fraud = "";
            while( (current_line = iBR.readLine()) != null){
                fraud = "";

                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                if(aNextLine[visa_alert_column].equals("NF")){
                    fraud = "NF";
                }
                else if(aNextLine[bank_declined_column].equals("1")){
                    fraud = "BD";
                }
                else if(aNextLine[visa_alert_column].equals("1") ||
                        aNextLine[mc_alert_column].equals("1") ||
                        aNextLine[cbk_column].equals("1")){
                    fraud = "1";
                }
                else{
                    fraud = "0";
                }

                System.out.println(current_line + "," + fraud);
            }
        }
        catch(IOException ex){
            System.out.println("IOException in VantivUtils.addFraudColumn");
        }
    } // END: addFraudColumn


    //
    // Input:
    //      rules_filename:
    //          Assuming that this is a "rules" file that has one column containing the single
    //          rule that fired for this transaction (multiple rules in one cell are not allowed).
    //          Currently assuming the 2 column "Request ID, Rules" .csv file.
    //
    //      rule_weight_filename:
    //          A xml policy file or a 2 column "rule name, rule weight" file.
    //          Note: The file must have either the .xml or .csv extension.
    //
    //      input_rules_column_name:
    //          The name of the column in "rules_filename" that contains the rules.
    //
    //      output_rules_weight_column_name:
    //          The name of the output column that will contain the rule weight.
    //
    // Output:
    //      The rules_filename file with an additional column containing the rule weight.
    //      Output is to standard out.
    //
    public void addRuleWeightsToRulesDataFile(String rules_filename,
                                              String rule_weight_filename,
                                              String input_rules_column_name,
                                              String output_weight_column_name){
        VantivUtils vu = new VantivUtils();
        Map<String,Integer> rules_lookup = null;

        // Check if rule_weight_filename is .xml or .csv
        String str_tmp = "";
        str_tmp = rule_weight_filename.substring(rule_weight_filename.length() - 3);
        if(str_tmp.equals("xml")){
            rules_lookup = vu.generateRulesLookupFromPolicy(rule_weight_filename);
        }
        else if(str_tmp.equals("csv")){
            rules_lookup = vu.generateRulesLookupFromCsv(rule_weight_filename);
        }
        else{
            System.out.println("Error in addRuleWeightsToRulesDataFile()");
            System.out.println("rule_weight_filename = " + rule_weight_filename);
            System.out.println(".xml or .csv file expected");
            System.exit(0);
        }

        // Process the rules file

        String current_line = null;
        CSVReader reader = null;
        String[] aNextLine = null;

        try{
            FileReader iFR = new FileReader (rules_filename);
	        BufferedReader iBR = new BufferedReader(iFR);

            // Read the header row and find the "rules" columns
            current_line = iBR.readLine();
            int rules_column = findColumnGivenHeader(current_line, input_rules_column_name);
            if(rules_column == -1){
                System.out.println("Error in addRuleWeightsToRulesDataFile()");
                System.out.println("Did not find the 'rules' column in file: " +
                                   rules_filename);
                System.exit(0);
            }

            // Output the new header row
            System.out.println(current_line + "," + output_weight_column_name);

            // Read remainder of rules file, lookup the rule weight, and then add
            // weight to output.
            int rule_weight = 0;
            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                try{
                    rule_weight = rules_lookup.get(aNextLine[rules_column]);
                    System.out.println(current_line + "," + rule_weight);
                }
                catch(NullPointerException ex){
                    System.out.println(current_line + ",0");
                }
            }
        }
        catch(IOException ex){
            System.out.println("IOException in VantivUtils.addFraudColumn");
        }
    } // END: addRuleWeightsToRulesDataFile
}

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
import com.opencsv.CSVReader;
import java.io.FileNotFoundException;

import java.util.HashMap;
import java.util.Map;

//
// Input:
//
//      results_file (args[0]: CSV file is based off the ThreatMetrix events export file.
//
//      rules_column_name (args[1]: The column from "resultsFile" that contain the rules that fired.
//
// Output: (To the terminal. So redirect if you want to save):
//
//      The output is the input file data with additional column headings for every potential rule that could fire.
//      The value for the column is "1" if the rule fired for this event, or "0" if the rule did not fire.
//      Note: there is one extra column called NoRulesFired, for obvious reasons.
//
// Sample command line (Windows/Cygwin):
//
//      java -classpath "P:\\Advanced Fraud Working Folder\\automation\\java_libraries\\opencsv-3.4.jar;." WidenRules results_file_name results_column_name 
//
public class WidenRules{
    public static void main(String[] args){
        String results_file = args[0];
        String rules_column_name = args[1];

        WidenRules tm_utils = new WidenRules();
        NavigableSet<String> rule_set = new TreeSet<String>();
        rule_set = tm_utils.generateRulesList(results_file, rule_set, rules_column_name);
        tm_utils.generateData1(results_file, rule_set, rules_column_name);
    } // END: main


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
    private NavigableSet<String> generateRulesList(String events_file, NavigableSet<String> rule_set, String rules_column_name){
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

        // Add the "NoRulesFired" rule
        rule_set.add("NoRulesFired");

        return rule_set;
    } // END: generateRulesList


    // Input is string in the following format:
    //
    //      "{TrueIPGeoOnLocalBlacklist}"
    //      "{5PaymentsOnDeviceLocalDay,5PaymentsOnFuzzyDeviceLocalDay,5PaymentsOnTrueIPLocalDay}"
    //
    // Returns a String[] containing the rule names.
    //
    private String[] extractRuleNames(String commaDelimitedRuleNames){
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
    // Returns a NavigableSet<String> containing the rule names.
    //
    private NavigableSet<String> extractRuleNamesIntoSet(String commaDelimitedRuleNames){
        if(commaDelimitedRuleNames.equals("")){
            return null;
        }

        NavigableSet<String> rule_set = new TreeSet<String>();

        // Remove leading { and trailing }
        commaDelimitedRuleNames = commaDelimitedRuleNames.substring(1,commaDelimitedRuleNames.length()-1);


        String[] rule_names = commaDelimitedRuleNames.split(",");
        for(String val: rule_names){
            rule_set.add(val);
        }

        return rule_set;
    } // END: extractRuleNamesIntoSet


    //
    // Input:
    //
    //      resultsFile: CSV file with at least one column with the header "rules_column_name". This column should have the list of rules that fired
    //      for this transaction. The results_file is based off the ThreatMetrix events export file.J
    //
    //      rule_set: NavigableSet<String> containing all of the rules contained in resultsFile. Or more specifically the
    //      rules that were found in the "rules_column_name" column of resultsFile
    //
    //      rules_column_name: The column from "resultsFile" that contain the rules the fired.
    //
    // Output:
    //
    //      The output is the input file data with additional column headings for every potential rule that could fire.
    //      The value for the column is "1" if the rule fired for this event, or "0" if the rule did not fire.
    //
    private void generateData1(String results_file, NavigableSet<String> rule_set, String rules_column_name){
        String current_line = null;
        CSVReader reader = null;
        String[] aNextLine = null;
        Boolean found = false;
        int score = 0;

        try{
            FileReader iFR = new FileReader (results_file);
            BufferedReader iBR = new BufferedReader(iFR);

            // Find the header row by reading the file until finding the string "rules_column_name"
            while((current_line = iBR.readLine()) != null) {
                if(current_line.contains(rules_column_name)){
                    found = true;
                    break;
                }
            }

            if (!found){
                System.out.printf("The input file containing the event data must contain the column \"%s\"%n",rules_column_name);
                System.exit(0);
            }

            // Find the "Reasons" column
            int reasons_column = findColumn(results_file,rules_column_name);

            // Format the header row
            String tmp_str = "";
            Iterator<String> value = rule_set.iterator();

            while(value.hasNext()){
                tmp_str += value.next() + ",";
            }
            // Remove trailing ","
            tmp_str = tmp_str.substring(0,tmp_str.length() -1);

            // Output the header row
            System.out.printf("%s,%s%n",current_line,tmp_str);

            // Generate the string that will output when no rules fire (no rules in the "Reasons" column)
            String no_rules_data = "";
            value = rule_set.iterator();
            while(value.hasNext()){
                if(value.next().equals("NoRulesFired")){
                    no_rules_data += "1,";
                }
                else{
                    no_rules_data += "0,";
                }
            }

            // Remove trailing ","
            no_rules_data = no_rules_data.substring(0,no_rules_data.length() -1);

            // Process the remainder of the input file.
            String out_str = "";
            NavigableSet<String> current_rules = null;

            while((current_line = iBR.readLine()) != null) {
                out_str = "";
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();//bobb
                if (!aNextLine[reasons_column].equals("")){ // Some rules must have fired
                    value = rule_set.iterator();

                    current_rules = extractRuleNamesIntoSet(aNextLine[reasons_column]);
                    while(value.hasNext()){
                        if(current_rules.contains(value.next())){
                            out_str += "1,";
                        }
                        else{
                            out_str += "0,";
                        }
                    }
                    // Remove trailing ","
                    out_str = out_str.substring(0,out_str.length() -1);
                    System.out.printf("%s%s%n",current_line,out_str);
                }
                else{ // No rules fired so "0" for all rules columns
                    System.out.printf("%s%s%n",current_line,no_rules_data);
                }
            }
        }
        catch(FileNotFoundException ex){System.out.println("File not found.");}
        catch(IOException ex){System.out.println("IO Exception.");}
    }


    //
    // For the given input file, returns the zero based column index of the "column_name" column.
    //
    private int findColumn(String results_file, String column_name){
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
    }
}

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
// Utility class to "pretty print" a ThreatMetrix policy file.
//
// Input:
//
//      policy file
//
//      RuleType to RuleType Name lookup (csv)
//
//          RuleType,RuleType Name
//          2,Session Anomaly
//          7,Device in Global List
//          15,Device in Global List
//          16,IP In Global List
//
//      RuleType to RuleType Category lookup (csv)
//
//          RuleType,RuleType Category
//          114,Standard
//          120,Standard
//          121,Standard
//          7,Lists
//          15,Lists
//
// Output (To the terminal. So redirect if you want to save):
//
//      CSV formatted file with the columns listed below:
//
//          UID,Parent UID,Name,RuleType Category,RuleType Name,Description,Risk Weight,Visible
//
// Sample command line (Windows/Cygwin):
//
//      java -classpath "P:\\Advanced Fraud Working Folder\\automation\\java_libraries\\opencsv-3.4.jar;." PrettyPrintPolicy policy_file name_lookup, category_lookup
//
public class PrettyPrintPolicy{
    public static void main(String[] args){
        if(args.length < 3){
            System.out.println("Usage:");
            System.out.println("arg[0]: policy file");
            System.out.println("arg[1]: name lookupfile");
            System.out.println("arg[2]: category lookupfile");
            System.exit(1);
        }
        FileReader iFR = null;
        try{
            iFR = new FileReader (args[0]);
        }
        catch(FileNotFoundException ex){
            System.out.printf("File not found in main %n");
        }
        BufferedReader iBR = new BufferedReader(iFR);

        Map<String,String> rule_name_map = new PrettyPrintPolicy().loadLookup(args[1]);
        Map<String,String> rule_category_map = new PrettyPrintPolicy().loadLookup(args[2]);

        new PrettyPrintPolicy().prettyPrint(iBR,rule_name_map,rule_category_map);
    } // END: main


    //
    // Pretty print the policy file
    //
    // Input:
    //
    //      ThreatMetrix policy file name
    //
    // Output:
    //
    //      
    //
    private void prettyPrint(BufferedReader policy_file, Map<String,String> rule_name_map,
								Map<String,String> rule_category_map){
        String current_line = "";
        String name = "";
        LinkedList<String> parentRules = new LinkedList<String>();

        // Print the header row
        System.out.println("UID,Parent UID,Name,RuleType Category,RuleType Name,Description,Risk Weight,Visible");

        try{
            while((current_line = policy_file.readLine()) != null) {
                if(current_line.contains("<ifRule ")){
                    processIfRule(current_line, policy_file, rule_name_map, rule_category_map, parentRules);
                }
                if(current_line.contains("<rule ")){
                    processRule(current_line, policy_file, rule_name_map, rule_category_map, parentRules);
                }
                if( (current_line.contains("</children>")) || (current_line.contains("<children/>")) ){
                    parentRules.removeLast();
                }
            }
        }
        catch(IOException ex){System.out.println("IO Exception.");}

    } // END: prettyPrint

    
   
    //
    // Process an "if" rule
    //
    // Input:
    //
    //      ThreatMetrix policy file name
    //
    // Output:
    //
    //      
    //private void processIfRule(String current_line, BufferedReader iBR) throws IOException{
    private void processIfRule(String current_line, BufferedReader iBR,
			Map<String,String> rule_name_map, Map<String,String> rule_category_map,
            LinkedList<String> parentRules) throws IOException{
	    //  UID,Parent UID,Name,RuleType Category,RuleType Name,Description,Risk Weight,Visible

        String rule_name = "TBD";
        String rule_weight = "TBD";
        String rule_uid = "TBD";
        String rule_parent_uid = "TBD";
        String rule_type_category = "TBD"; // Standard, Lists, Pattern...
        String rule_type_name = "TBD"; // Condition, Attribute in List, Terminate, Entity Velocity
        String rule_description = "TBD";
        String rule_visible = "TBD"; // generateReasonCode
		String tmpStr = null;

        rule_weight = current_line.split("riskWeight=\"")[1].split("\"")[0];
        rule_uid = current_line.split("uid=\"")[1].split("\"")[0];
        rule_visible = current_line.split("generateReasonCode=\"")[1].split("\"")[0];

        current_line = iBR.readLine();
        rule_name = current_line.split("<name>")[1].split("</name>")[0];

        current_line = iBR.readLine();
        tmpStr = current_line.split("<ruleType>")[1].split("</ruleType>")[0];
		rule_type_category = rule_category_map.get(tmpStr);
		rule_type_name = rule_name_map.get(tmpStr);
        
        current_line = iBR.readLine();
        try{
            rule_description = "\"" + current_line.split("<description>")[1].split("</description>")[0] + "\"";
        }
        catch(ArrayIndexOutOfBoundsException ex){
            rule_description = "None";
        }

        if(parentRules.isEmpty()){
            rule_parent_uid = "N/A";
        }
        else{
            rule_parent_uid = parentRules.peekLast();
        }

        parentRules.add(rule_uid);

        System.out.printf("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s %n",
				rule_uid, rule_parent_uid, rule_name, rule_type_category,
                rule_type_name, rule_description, rule_weight, rule_visible);
    } // END: processIfRule



    //
    // Process a "regular" rule
    //
    // Input:
    //
    //      ThreatMetrix policy file name
    //
    // Output:
    //
    //      
    private void processRule(String current_line, BufferedReader iBR,
			Map<String,String> rule_name_map, Map<String,String> rule_category_map,
            LinkedList<String> parentRules) throws IOException{
	    //  UID,Parent UID,Name,RuleType Category,RuleType Name,Description,Risk Weight,Visible

        String rule_name = "TBD";
        String rule_weight = "TBD";
        String rule_uid = "TBD";
        String rule_parent_uid = "TBD";
        String rule_type_category = "TBD"; // Standard, Lists, Pattern...
        String rule_type_name = "TBD"; // Condition, Attribute in List, Terminate, Entity Velocity
        String rule_description = "TBD";
        String rule_visible = "TBD"; // generateReasonCode
		String tmpStr = null;

        rule_weight = current_line.split("riskWeight=\"")[1].split("\"")[0];
        rule_uid = current_line.split("uid=\"")[1].split("\"")[0];
        rule_visible = current_line.split("generateReasonCode=\"")[1].split("\"")[0];

        current_line = iBR.readLine();
        rule_name = current_line.split("<name>")[1].split("</name>")[0];

        current_line = iBR.readLine();
        tmpStr = current_line.split("<ruleType>")[1].split("</ruleType>")[0];
		rule_type_category = rule_category_map.get(tmpStr);
		rule_type_name = rule_name_map.get(tmpStr);
        
        current_line = iBR.readLine();
        try{
            rule_description = "\"" + current_line.split("<description>")[1].split("</description>")[0] + "\"";
        }
        catch(ArrayIndexOutOfBoundsException ex){
            rule_description = "None";
        }

        if(parentRules.isEmpty()){
            rule_parent_uid = "N/A";
        }
        else{
            rule_parent_uid = parentRules.peekLast();
        }

        System.out.printf("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s %n",
				rule_uid, rule_parent_uid, rule_name, rule_type_category,
                rule_type_name, rule_description, rule_weight, rule_visible);
    } // END: processRule



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
    private Map<String,String> loadLookup(String lookup_file_name){
        FileReader iFR = null;
        String current_line = null;
        Map<String,String> lookup_map = new HashMap<String,String>();
        CSVReader reader = null;
        String[] values = null;

        try{
            iFR = new FileReader (lookup_file_name);
            BufferedReader iBR = new BufferedReader(iFR);

            while( (current_line = iBR.readLine()) != null){
                reader = new CSVReader(new StringReader(current_line));
                values = reader.readNext();
                lookup_map.put(values[0],values[1]);
            }
        }
        catch(FileNotFoundException ex){
            System.out.printf("File not found in main %n");
        }
        catch(IOException ex){
            System.out.println("IO Exception.");
        }

        return lookup_map;
    } // END: loadLookup
}

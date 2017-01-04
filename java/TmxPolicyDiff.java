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
import java.lang.Comparable;

import java.util.HashMap;
import java.util.Map;

//
// Utility class to list differences between ThreatMetrix policy xml file.
//
// Input:
//
//      policy file #1
//      policy file #2
//
// Output (To the terminal. So redirect if you want to save):
//
// Sample command line (Windows/Cygwin):
//
//      java -classpath "P:\\Advanced Fraud Working Folder\\automation\\java_libraries\\opencsv-3.4.jar;." TmxPolicyDiff policy_file1 policy_file2
//
public class TmxPolicyDiff{
    public static void main(String[] args){
        TmxPolicyDiff tpd = new TmxPolicyDiff();
        NavigableSet<SimpleRule> rule_set = tpd.diffPolicies(args[0], args[1]);
        tpd.generatePolicyReport(rule_set, args[1]);

    } // END: main


    //
    // Load the rule <name>s from a ThreadMetrix policy file.
    //
    // Input:
    //
    //      ThreatMetrix policy file name
    //
    // Output:
    //
    //      All the names of the rules (except "if" rules) are loaded into a Set
    //
    private NavigableSet<String> loadRuleNamesIntoSet(String policy_file){
        String current_line = "";
        NavigableSet<String> rule_set = new TreeSet<String>();
        String name = "";

        try{
            FileReader iFR = new FileReader (policy_file);
            BufferedReader iBR = new BufferedReader(iFR);

            while((current_line = iBR.readLine()) != null) {
                if( (current_line.contains("rule uid=")) || (current_line.contains("ifRule uid=")) ){
                    current_line = iBR.readLine();
                    if(current_line.contains("<name>")){
                        name = current_line.split("<name>")[1].split("</name>")[0];
                        rule_set.add(name);
                    }
                }
            }
        }
        catch(FileNotFoundException ex){System.out.println("File not found.");}
        catch(IOException ex){System.out.println("IO Exception.");}

        return rule_set;

    } // END: loadRuleNamesIntoSet

    
    //
    // Load the rule "objects" from a ThreadMetrix policy file. The key attributes are:
    //
    //      String name
    //      String type
    //      int weight
    //      String descripion
    //      
    //
    // Input:
    //
    //      ThreatMetrix policy file name
    //
    // Output:
    //
    //      All the rule objects (except "if" rules) are loaded into a Set
    //
    private NavigableSet<SimpleRule> loadRuleObjectsIntoSet(String policy_file){
        String current_line = "";
        String rule_uid_line = "";
        NavigableSet<SimpleRule> rule_set = new TreeSet<SimpleRule>();
        String name = "";
        String type = "";
        int weight = 0;
        String description = "";
        boolean if_rule = false;

        try{
            FileReader iFR = new FileReader (policy_file);
            BufferedReader iBR = new BufferedReader(iFR);

            while((current_line = iBR.readLine()) != null) {
                if( (current_line.contains("rule uid=")) || (current_line.contains("ifRule uid=")) ){
                    rule_uid_line = current_line;
                    current_line = iBR.readLine();
                    if(current_line.contains("<name>")){
                        SimpleRule sr = new SimpleRule();

                        // Get the name
                        name = current_line.split("<name>")[1].split("</name>")[0];

                        // Get the weight
                        weight = new Integer(rule_uid_line.split("riskWeight=\"")[1].split("\"")[0]);

                        // Get if_rule
                        if(rule_uid_line.contains("ifRule uid=")){
                            if_rule = true;
                        }
                        else{
                            if_rule = false;
                        }

                        // Get the type
                        current_line = iBR.readLine();
                        type = current_line.split("<ruleType>")[1].split("</ruleType>")[0];

                        // Get the description
                        current_line = iBR.readLine();
                        if(current_line.contains("<description/>")){
                            description = "";
                        }
                        else{
                            description = current_line.split("<description>")[1].split("</description>")[0];
                        }

                        // Set the SimpleRule object and add to the set
                        rule_set.add(new SimpleRule(name,type,weight,description,if_rule));
                    }
                }
            }
        }
        catch(FileNotFoundException ex){System.out.println("File not found.");}
        catch(IOException ex){System.out.println("IO Exception.");}

        return rule_set;

    } // END: loadRuleObjectsIntoSet

    
    //
    //
    // Reads two ThreatMetrix policy files and extracts the rules into SetA and SetB.
    // Returns the Set difference (SetA = SetB)
    //
    // Input:
    //
    //      Two ThreatMetrix policy files.
    //
    // Output:
    //
    //      Returns a NavigableSet<SimpleRule> object that is the Set difference of policy1 - policy2
    //
    //      Example:
    //
    //          policy1 = A,B,C
    //          policy2 = B,C
    //          policy1 - policy2 = A
    //
    //          policy1 = A,B,C
    //          policy2 = D
    //          policy1 - policy2 = A,B,C
    //
    private NavigableSet<SimpleRule> diffPolicies(String policy_file1, String policy_file2){

        NavigableSet<SimpleRule> policy1_name_set = null;
        NavigableSet<SimpleRule> policy2_name_set = null;

        policy1_name_set = loadRuleObjectsIntoSet(policy_file1);
        policy2_name_set = loadRuleObjectsIntoSet(policy_file2);

        policy1_name_set.removeAll(policy2_name_set);

        return policy1_name_set;

    } // END: diffPolicies



    //
    // Generates a report on the differences in policies.
    //
    // Input:
    //
    //      rule_set: This is the set difference of policy1 and policy2, which are the overall inputs for this utility.
    //      policy_file: This is the name of the policy2 input file.
    //
    //
    //      Example: Where A,B,C,D are rules contained in the input policy files.
    //
    //          policy1 = A,B,C
    //          policy2 = B,C
    //          policy1 - policy2 = A
    //
    //          policy1 = A,B,C
    //          policy2 = D
    //          policy1 - policy2 = A,B,C
    // 
    //
    // Output (To the terminal. So redirect if you want to save):
    //
    //      Output is a csv file with two columns "Rule Name, N-T-W-D-I". The "N-T-W-D-I" column is interpreted as follows:
    //
    //          *-*-*-*-*:  The given rule exists in policy1 but not in policy2
    //          N-*-*-*-*:  The given rule exists in policy1 and policy2. But the type,weight,description, and if_rule values differ.
    //          N-T-*-D-*:  The given rule exists in policy1 and policy2. But weight values differ.
    //          N-*-W-D-*:  The given rule exists in policy1 and policy2. But type values differ.
    //
    //          Note: if_rule: Is this rule a regular rule or an "if" rule.
    //
    //
    private void generatePolicyReport(NavigableSet<SimpleRule> rule_set, String policy_file){

        // Load the rules from the policy file into a set.
        NavigableSet<SimpleRule> policy_set = loadRuleObjectsIntoSet(policy_file);

        SimpleRule current_simple_rule = null;
        SimpleRule current_policy_simple_rule = null;

        boolean name_found = false;
        String type_report_output = "";
        String weight_report_output = "";
        String description_report_output = "";
        String if_rule_report_output = "";

        // We want to check all of the rules in rule_set to see if name and/or type and/or weight exists
        Iterator<SimpleRule> rules = rule_set.iterator();

        //Print the header row
        System.out.println("Rule Name,N-T-W-D-I");
        
        while(rules.hasNext()){
            name_found = false;
            type_report_output = "";
            weight_report_output = "";
            description_report_output = "";
            if_rule_report_output = "";

            current_simple_rule = rules.next();

            Iterator<SimpleRule> policy_rules = policy_set.iterator();
            while(policy_rules.hasNext()){
                current_policy_simple_rule = policy_rules.next();
                if(current_simple_rule.getName().equals(current_policy_simple_rule.getName())){
                    name_found = true;

                    if(current_simple_rule.getType().equals(current_policy_simple_rule.getType())){
                        type_report_output = "T";
                    }
                    else{
                        type_report_output = "*";
                    }

                    if(new Integer(current_simple_rule.getWeight()).toString().equals(new 
                                Integer(current_policy_simple_rule.getWeight()).toString())){
                        weight_report_output = "W";
                    }
                    else{
                        weight_report_output = "*";
                    }

                    if(current_simple_rule.getDescription().equals(current_policy_simple_rule.getDescription())){
                        description_report_output = "D";
                    }
                    else{
                        description_report_output = "*";
                    }

                    if(current_simple_rule.getIfRule() == current_policy_simple_rule.getIfRule()){
                        if_rule_report_output = "I";
                    }
                    else{
                        if_rule_report_output = "*";
                    }

                    System.out.printf("%s,N-%s-%s-%s-%s%n",current_simple_rule.getName(),type_report_output,
                            weight_report_output,description_report_output,if_rule_report_output);
                }
            }
                
            if(!name_found){
                System.out.printf("%s,*-*-*-*-*%n",current_simple_rule.getName());
            }
        }

    } // END: diffPolicies

}


//
// Object used to represent a ThreatMetrix rule. Core attributes are:
//
//      name
//      type
//      weight
//      description
//      if_rule (TBD)
//      nested  (TBD)
//
class SimpleRule implements Comparable<SimpleRule>{

    private String name = "";
    private String type = "";
    private int weight = 0;
    private String description = "";
    private boolean if_rule = false;

    public int compareTo(SimpleRule o){
        if(!this.name.equals(o.name)){
            return this.name.compareTo(o.name);
        }
        else if(!this.type.equals(o.type)){
            return this.type.compareTo(o.type);
        }
        else if(! (this.weight + "").equals((o.weight + "")) ){
            String str1 = "" + this.weight;
            String str2 = "" + o.weight;
            return str1.compareTo(str2);
        }
        else if(!this.description.equals(o.description)){
            return this.description.compareTo(o.description);
        }
        else{
            String str1 = "" + this.if_rule;
            String str2 = "" + o.if_rule;
            return str1.compareTo(str2);
        }
    }

    public SimpleRule(){};

    public SimpleRule(String name, String type, int weight, String description, boolean if_rule){
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.description = description;
        this.if_rule = if_rule;
    }

    public int hashCode(){
        return 0;
    }

    public boolean equals(Object o){
        if(o instanceof SimpleRule){
            SimpleRule sr = (SimpleRule)o;
            if((name.equals(sr.name)) && (type.equals(sr.type))
                    && (weight == sr.weight) && description.equals(sr.description)
                    && (if_rule == sr.if_rule) ){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    public void setName(String name){
        this.name = name;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setWeight(int weight){
        this.weight = weight;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setIfRule(boolean if_rule){
        this.if_rule = if_rule;
    }

    public String getName(){
        return this.name;
    }

    public String getType(){
        return this.type;
    }

    public int getWeight(){
        return this.weight;
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public boolean getIfRule(){
        return this.if_rule;
    }
}

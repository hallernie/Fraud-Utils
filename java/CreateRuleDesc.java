import java.io.FileReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import com.opencsv.CSVReader;
import java.io.FileNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class CreateRuleDesc{
    public static void main(String[] args){
        if(args.length < 2){
            System.out.println("Usage:");
            System.out.println("   % CreateRuleDesc rule_desc_file_name tmx_policy_filename"); 
            System.exit(0);
        }
        String ruleDescFile = args[0]; 
        String ruleXmlFile = args[1];

        Map<String,String> mRuleDesc = new HashMap<String,String>();
        CreateRuleDesc crd = new CreateRuleDesc();

        mRuleDesc = crd.loadRuleDescriptions(ruleDescFile);
        crd.createRuleDescriptionFile(ruleXmlFile,mRuleDesc);
    }

    private Map<String,String> loadRuleDescriptions(String ruleDescFile){
        Map<String,String> mRuleDesc = new HashMap<String,String>();

        try{
            //Build reader instance
            CSVReader reader = new CSVReader(new FileReader(ruleDescFile), ',', '"', 1);
            String[] nextLine = null;
            while((nextLine = reader.readNext()) != null){
                mRuleDesc.put(nextLine[0].toUpperCase(),nextLine[1]);
            }
        }
        catch(IOException ex){System.out.println("IOException");};

        return mRuleDesc;
    }

    private void createRuleDescriptionFile(String ruleXmlFile, Map<String,String> mRuleDesc){
        String currentLine = null;
        String ruleLine = null;
        String nameLine = null;
        String ruleWeight = null;
        String ruleName = null;
        String ruleDescription = null;

        try{
            FileReader iFR = new FileReader (ruleXmlFile);
	        BufferedReader iBR = new BufferedReader(iFR);

            // Print the header
            System.out.println("Rule Name,Rule Description,Rule Weight,Action,Comment");

            // Process the remainder of the file.
            while((currentLine = iBR.readLine()) != null) {
                if(currentLine.contains("<rule ")){
                    ruleLine = currentLine;
                    currentLine = iBR.readLine();

                    if(currentLine.contains("<name>")){
                        nameLine = currentLine;

                        // Get the rule weight
                        ruleWeight = ruleLine.split("riskWeight")[1].split("\"")[1];

                        // Get the rule name
                        ruleName = nameLine.split("<name>")[1].split("</name>")[0];

                        // Get the rule description
                        if(ruleName.contains("------")){
                            ruleDescription = "";
                        }
                        else {
                            ruleDescription = mRuleDesc.get(ruleName.toUpperCase());
                            ruleDescription = "\"" + ruleDescription + "\"";
                        }

                        System.out.println(ruleName + "," + ruleDescription + "," +  ruleWeight + ",,");
                    }
                }
            }
        }
        catch(FileNotFoundException ex){System.out.println("File not found.");}
        catch(IOException ex){System.out.println("IOException occurred.");}
    }
} 

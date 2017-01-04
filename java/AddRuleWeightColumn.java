public class AddRuleWeightColumn{
    public static void main(String[] args){
        if(args.length < 4){
            System.out.println("Usage:");
            System.out.println("  % AddRuleWeightColumn rules_filename rule_weight_filename " +
                                                        "input_rules_column_name output_weight_column_name");
            System.out.println();
            System.out.println("    rules_filename: Usually 2 column 'Request ID, Rules' file");
            System.out.println("    rule_weight_filename: A xml policy file or a 2 column 'rule name, rule weight' file");
            System.out.println("    input_rules_column_name: The name of the column in 'rules_filename' that contains the rules");
            System.out.println("    output_weight_column_name: The name of the output column that will contain the rule weight");
            System.exit(0);
        }
        new VantivUtils().addRuleWeightsToRulesDataFile(args[0],  // rules_filename
                                                        args[1],  // rule_weight_filename
                                                        args[2],  // input_rules_column_name
                                                        args[3]); // output_weight_column_name
    }
}

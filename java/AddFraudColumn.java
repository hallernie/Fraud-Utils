public class AddFraudColumn{
    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Usage:");
            System.out.println("  % AddFraudColumn merged_event_filename");
            System.exit(0);
        }
        new VantivUtils().addFraudColumn(args[0]);
    }
}

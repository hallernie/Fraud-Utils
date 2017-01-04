import java.util.List;
import java.util.ArrayList;

public class ExtractColumns{
    public static void main(String[] args){
        if(args.length < 2){
            System.out.println("Usage:");
            System.out.println("    ExtractColumns csv_filename column1 column2 ...");
            System.exit(0);
        }

        VantivUtils vu = new VantivUtils();
        List<String> li = new ArrayList<String>();

        int cnt = 0;
        for(String val: args){
            if(cnt != 0){
                li.add(val);
            }
            cnt++;
        }

        String csv_filename = args[0];

        vu.extractColumnsFromCsv(csv_filename, li);
    }
}

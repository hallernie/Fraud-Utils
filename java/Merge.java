import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedReader;

import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

public class Merge{
    public static void main(String[] args){
        Merge m = new Merge();
        m.mergeData(args[0], args[1]);
    }

    private void mergeData(String event_file, String order_ids){
        Map<String,String> orders = new HashMap<String,String>();
        CSVReader reader = null;
        String[] aNextLine = null;
        String current_line = "";

        try{
            FileReader iFR = new FileReader (order_ids);
            BufferedReader iBR = new BufferedReader(iFR);

            while((current_line = iBR.readLine()) != null) {
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                orders.put(aNextLine[0],aNextLine[1]);
            }

            iFR = new FileReader (event_file);
            iBR = new BufferedReader(iFR);

            current_line = iBR.readLine();
            // Output the header row
            System.out.println(current_line + ",Order_ID");

            // Process the remainder of the file.
            while((current_line = iBR.readLine()) != null) {
                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                if(orders.containsKey(aNextLine[1])){
                    System.out.println(current_line + "," + orders.get(aNextLine[1]));
                }
                else {
                    System.out.println(current_line + ",Not Found");
                }
            }

            //for (Entry<String, String> entry : orders.entrySet()) {
                //String key = entry.getKey().toString();
                //String value = entry.getValue();
                //System.out.println(key + "    " + value);
            //}
        }
        catch(IOException ex){
            System.out.println("IOException in mergeData.");
        }
    } }

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
public class DoStuff{
    public static void main(String[] args) throws IOException{
        String email_filename = args[0];
        String events_filename = args[1];
        //
        // Read email addresses into Map
        try{
            Map<String,String> m_email = new HashMap<String,String>();
            FileReader iFR = new FileReader (email_filename);
            BufferedReader iBR = new BufferedReader(iFR);
            String  current_line = null;

            while((current_line = iBR.readLine()) != null){
                m_email.put(current_line,"");
            }

            // Read the events file and add the column "SuspectedFraud"
            iFR = new FileReader (events_filename);
            iBR = new BufferedReader(iFR);

            current_line = iBR.readLine();
            // Print the header row
            System.out.println(current_line + ",SuspectedFraud");
            int email_column = new VantivUtils().findColumnGivenHeader(current_line, "Account Email");
            String tmp = null;

            while((current_line = iBR.readLine()) != null){
                tmp = new CSVReader(new StringReader(current_line)).readNext()[email_column];
                if(m_email.containsKey(tmp)){
                    System.out.println(current_line + ",Yes");
                }
                else{
                    System.out.println(current_line + ",No");
                }
            }

        }
        catch(IOException ex){
            System.out.println("IOException:");
            throw(ex);
        }
    }
}

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
// Input:
//      arg[0] = csv_file_1
//      arg[1] = column_name_1 (must be a header row column)
//      arg[2] = csv_file_2
//      arg[3] = column_name_2 (must be a header row column)
//
// Output:
//      File 1 column count: xxx
//      File 2 column count: yyy
//      F1 - F2: (Columns in F1 that are not in F2)
//          ColumnName1
//          ColumnName2
//          ....
//
//      F2 - F1: (Columns in F2 that are not in F1)
//          ColumnName1
//          ColumnName2
//          ....
//
public class CompareCsvColumns{
    public static void main(String[] args){
        if(args.length < 4){
            System.out.println("Usage:");
            System.out.println("% CompareCsvColumns csv_file1 column_name_1" +
                  " csv_file2 column_name_2");
            System.exit(1);
        }

        CompareCsvColumns cmp = new CompareCsvColumns();
        cmp.compareColumns(args[0],args[1],args[2],args[3]);
    }

    private void compareColumns(String csv_file1, String column_name_1,
                                String csv_file2, String column_name_2){
        String current_line = "";
        String header1 = null;
        String header2 = null;

        try{
            FileReader iFR = new FileReader (csv_file1);
            BufferedReader iBR = new BufferedReader(iFR);

            // Find the header row of first csv file.
            // Must be in first 2 lines.
            boolean header_found = false;
            for(int i=0; i<2; i++){
                current_line = iBR.readLine();
                if(current_line.contains(column_name_1)){
                    header_found = true;
                    break;
                }
            }
            if(!header_found){
                System.out.println("Header row not found for csv_file_1");
                System.exit(1);
            }

            header1 = current_line;

            // Find the header row of first csv file.
            // Must be in first 2 lines.
            iFR = new FileReader (csv_file2);
            iBR = new BufferedReader(iFR);

            header_found = false;
            for(int i=0; i<2; i++){
                current_line = iBR.readLine();
                if(current_line.contains(column_name_2)){
                    header_found = true;
                    break;
                }
            }
            if(!header_found){
                System.out.println("Header row not found for csv_file_2");
                System.exit(1);
            }

            header2 = current_line;

            // Store header1 and header2 values into a set.
            NavigableSet<String> file1_columns = new TreeSet<String>();
            NavigableSet<String> file1_columns_copy = new TreeSet<String>();
            NavigableSet<String> file2_columns = new TreeSet<String>();

            CSVReader reader = new CSVReader(new StringReader(header1));
            String[] values = reader.readNext();
            for(String val: values){
                file1_columns.add(val);
                file1_columns_copy.add(val);
            }

            reader = new CSVReader(new StringReader(header2));
            values = reader.readNext();
            for(String val: values){
                file2_columns.add(val);
            }

            System.out.printf("%s column count: %d%n",csv_file1,file1_columns.size());
            System.out.printf("%s column count: %d%n",csv_file2,file2_columns.size());
            System.out.println();

            System.out.println("F1 - F2: (Columns in F1 that are not in F2)");
            file1_columns.removeAll(file2_columns);
            for(String val: file1_columns){
                System.out.printf("    %s%n",val);
            }

            System.out.println();

            System.out.println("F2 - F1: (Columns in F2 that are not in F1)");
            file2_columns.removeAll(file1_columns_copy);
            for(String val: file2_columns){
                System.out.printf("    %s%n",val);
            }
        }
        catch(IOException ex){
            System.out.println("IOException in compareColumns");
        }
    }
}

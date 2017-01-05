import java.io.IOException;
import java.io.StringReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

public class CleanTmxEventFile2{
    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Usage:");
            System.out.println("  % CleanTmxEventFile2 events_filename");
            System.exit(0);
        }

        new VantivUtils().fixThreatMetrixEventExportFileNew(args[0]);
    }
}

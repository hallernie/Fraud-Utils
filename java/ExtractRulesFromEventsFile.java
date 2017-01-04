import java.io.FileReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.FileNotFoundException;
import java.lang.Comparable;
import com.opencsv.CSVReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.regex.*;


public class ExtractRulesFromEventsFile{

    public static void main(String[] args){
        if(args.length < 2){
            System.out.println("Usage:");
            System.out.println("   % ExtractRulesFromEventsFile events_file_name \"reasons\" or \"tmx_reasons\"");
            System.exit(0);
        }

        String events_file_name = args[0];
        String reasons_or_tmx = args[1];

        VantivUtils vu = new VantivUtils();

        if(reasons_or_tmx.equals("reasons")){
            vu.extractRulesOld(events_file_name);
        }

        if(reasons_or_tmx.equals("tmx_reasons")){
            vu.extractTmxRulesOld(events_file_name);
        }
    }
}

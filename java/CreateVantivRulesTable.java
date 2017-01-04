import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Iterator;
import java.lang.Comparable;
//import com.opencsv.CSVReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.regex.*;

public class CreateVantivRulesTable{
    public static void main(String[] args){
        if(args.length < 5){
            System.out.println("Usage:");
            System.out.println("    VantivDatabaseUtils " +
                                    "rules_filename " +
                                    "rules_tablename " +
                                    "base_output_filename " +
                                    "output_directory_name\\\\ " +
                                    "max_insert_statements");
            System.exit(0);
        }

        VantivDatabaseUtils vdu = new VantivDatabaseUtils();
        List<String> datafiles = vdu.createVantivRulesTable(args[0],
                                                            args[1],
                                                            args[2],
                                                            args[3],
                                                            new Integer(args[4]));

        for(String val: datafiles){
            vdu.runSqlcmdOnFile(val);
        }
    }
}

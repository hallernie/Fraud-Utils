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


public class SetUtils{

    public static void main(String[] args){
        if(args.length == 0){
            System.out.println("Usage:");
            System.out.println("   % SetUtils union file1 file1"); 
            System.out.println("   % SetUtils intersection file1 file1"); 
            System.out.println("   % SetUtils dif file1 file1"); 
            System.out.println("   % SetUtils dups file1"); 
            System.exit(0);
        }

        if(args[0].contains("union")){
            if(args.length < 3){
                System.out.println("Usage:");
                System.out.println("   % SetUtils union file1 file1"); 
                System.exit(0);
            }
        }
        else if(args[0].contains("intersection")){
            if(args.length < 3){
                System.out.println("Usage:");
                System.out.println("   % SetUtils intersection file1 file1"); 
                System.exit(0);
            }
        }
        else if(args[0].contains("dif")){
            if(args.length < 3){
                System.out.println("Usage:");
                System.out.println("   % SetUtils dif file1 file1"); 
                System.exit(0);
            }
        }
        else if (args[0].contains("dups")){
            if(args.length < 2){
                System.out.println("Usage:");
                System.out.println("   % SetUtils dups file1"); 
                System.exit(0);
            }
        }
        else{
            System.out.println("What the hell!!!");
            System.exit(0);
        }

        VantivUtils vu = new VantivUtils();
        vu.setUtils(args);
    }
}

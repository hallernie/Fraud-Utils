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
import com.opencsv.CSVReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.regex.*;


public class VantivDatabaseUtils{
    public static void main(String[] args){
        // Define the columns that are to be extracted from the csv.
        List<ColumnData> lcd = new ArrayList<ColumnData>();

        lcd.add(new ColumnData("Request ID","requestid","NVARCHAR(70)"));
        lcd.add(new ColumnData("Event Time","eventtime","DATETIME")); // 2015/09/01 03:59:59.651
        lcd.add(new ColumnData("Policy Score","policyscore","INT"));
        lcd.add(new ColumnData("Review Status","reviewstatus","NVARCHAR(20)"));
        lcd.add(new ColumnData("CC Hash","cchash","NVARCHAR(50)"));
        lcd.add(new ColumnData("Transaction Amount","transactionamount","decimal(10,2)"));
        lcd.add(new ColumnData("Custom Attribute 9","customattribute9","NVARCHAR(70)"));
        lcd.add(new ColumnData("True IP Geo","trueipgeo","NVARCHAR(3)"));
        lcd.add(new ColumnData("True IP","trueip","NVARCHAR(30)"));
        lcd.add(new ColumnData("Account Email","accountemail","NVARCHAR(50)"));
        lcd.add(new ColumnData("Account Address City","accountaddresscity","NVARCHAR(30)"));
        lcd.add(new ColumnData("Account Address State","accountaddressstate","NVARCHAR(30)"));
        lcd.add(new ColumnData("Profiled URL","profiledurl","NVARCHAR(80)"));
        //lcd.add(new ColumnData("ChargeBack","chargeback","NVARCHAR(2)"));
        //lcd.add(new ColumnData("VIAlert","vialert","NVARCHAR(2)"));
        //lcd.add(new ColumnData("MCAlert","mcalert","NVARCHAR(2)"));
        //lcd.add(new ColumnData("Response","response","NVARCHAR(100)"));
        //lcd.add(new ColumnData("IsFraud","isfraud","NVARCHAR(2)"));
        //
        //lcd.add(new ColumnData("Request ID","requestid","NVARCHAR(70)"));
        //lcd.add(new ColumnData("Rules","rules","NVARCHAR(70)"));
        //
        //lcd.add(new ColumnData("Request ID","requestid","NVARCHAR(70)"));
        //lcd.add(new ColumnData("TMXRules","tmxrules","NVARCHAR(100)"));
        //lcd.add(new ColumnData("TmxCoreRule","tmxcorerule","NVARCHAR(100)"));
        //lcd.add(new ColumnData("IsVelocity","isvelocity","NVARCHAR(2)"));
        //lcd.add(new ColumnData("VelocityExpression","velocityexpression","NVARCHAR(30)"));
        //lcd.add(new ColumnData("VHr","vhr","NVARCHAR(5)"));
        //lcd.add(new ColumnData("VDay","vday","NVARCHAR(5)"));
        //lcd.add(new ColumnData("VWeek","vweek","NVARCHAR(5)"));
        //lcd.add(new ColumnData("VMonth","vmonth","NVARCHAR(5)"));


        VantivDatabaseUtils vdu = new VantivDatabaseUtils();

        vdu.processCsvIntoSql("HRW_Events_Cleaned.csv", // ******
                              lcd,
                              "FRAUD",
                              "HrwEvents", // ******
                              40000, // This is about the max number.
                              "output",
                              "hrw_sql_events\\", // ******
                              true);


    }


    //
    // Input:
    //      rule_filename: CSV file in the form "Request ID,Rulename"
    //      table_name: The name of the sql table to create and store the data
    //      output_sql_filename: Base name of output file containing the sql to execute.
    //      output_sql_directory_name: The name of the directory to store the output sql files
    //                                 Note: the directory should end in the path separator. So for example:
    //                                 "directory_name\\"
    //      max_insert_statements: The maximum number of insert statement per file
    //
    // Output:
    //      One or more files containing SQL commands to create and insert into
    //      a data table. The first file will always contain the sql to create the table.
    //      The subsequent files will contain insert statements.
    //
    public List<String> createVantivRulesTable(String rule_filename,
                                              String table_name,
                                              String output_sql_filename,
                                              String output_sql_directory_name,
                                              int max_insert_statements){
        String filename = null;
        List<String> list_output = new ArrayList<String>();
        try{
            // Create the sql file that will create the table. Assumes that
            // "AdvancedFraud" database already exists and that the schema will be "FRAUD".
            filename = output_sql_directory_name + output_sql_filename + ".sql";
            list_output.add(filename);

            FileWriter iFW = new FileWriter (filename);
	        BufferedWriter iBW = new BufferedWriter(iFW);

            iBW.write("USE AdvancedFraud;");
            iBW.newLine();
            iBW.write("GO");
            iBW.newLine();
            iBW.newLine();
            iBW.write("CREATE TABLE FRAUD." + table_name);
            iBW.newLine();
            iBW.write("(");
            iBW.newLine();
            iBW.write("requestid  NVARCHAR(40) NOT NULL,");
            iBW.newLine();
            iBW.write("rulename  NVARCHAR(70) NOT NULL,");
            iBW.newLine();
            iBW.write(");");
            iBW.newLine();
            iBW.write("GO");
            iBW.flush();
            iBW.close();

            // Now read the input rules file and create files containing sql insert statements.
            // The max number of statements per file in given as an input to this method.
            FileReader iFR = new FileReader (rule_filename);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = null;
            String current_rule_name = null;
            iBR.readLine(); // Skip the header row

            String[] value = null;
            boolean start = true;
            int file_number = 0;
            int counter = 0;
            while( (current_line = iBR.readLine()) != null){
                if(start){
                    start = false;
                    file_number++;
                    filename = output_sql_directory_name + output_sql_filename +
                               "_" + file_number + ".sql";
                    list_output.add(filename);
                    iFW = new FileWriter (filename);
	                iBW = new BufferedWriter(iFW);
                }
                value = current_line.split(",");
                iBW.write("INSERT INTO AdvancedFraud.FRAUD." + table_name +
                          "(requestid, rulename)");
                iBW.newLine();
                iBW.write("VALUES(" + "N'" + value[0] + "', " +
                          "N'" + value[1] + "');");
                iBW.newLine();
                counter++;
                
                // Check if max insert file size has been reached.
                if(counter > max_insert_statements){
                    start = true;
                    counter = 0;
                    iBW.flush();
                    iBW.close();
                }
            }
            iBW.flush();
            iBW.close();
        }
        catch(IOException ex){
            System.out.println("IOException: VantivDatabaseUtils.createVantivRulesTable()");
        }
        return list_output;
    }

    // Given:
    //      csv_filename:
    //          Contains data that will be loaded into sql table
    //
    //
    //      column_data:
    //          List<ColumnData> containing columns from the csv file that are to be loaded into the
    //          table. ColumnData is currently:
    //              String column_name
    //              String sql_column_name
    //              String sql_data_type
    //
    //      database_name
    //      schema_name
    //      table_name
    //      max_insert_statements
    //      output_sql_filename
    //      output_sql_directory_name\\
    //      load_table - "true" if I want to load the data into Sql Server.
    //                   "false" if I just want to create the .sql files.
    //
    // Output:
    //      (1) sql file containing statements to create the table in the database
    //      (2) sql file containing insert statements for the table above
    //
    public void processCsvIntoSql(String csv_filename,
                                  List<ColumnData> column_data,
                                  String schema_name,
                                  String table_name,
                                  int max_insert_statements,
                                  String output_sql_filename,
                                  String output_sql_dir_name,
                                  Boolean load_table){
        
        // Verify that specified csv columns exist in the input csv_filename
        VantivUtils vu = new VantivUtils();
        for(ColumnData val: column_data){
            int column_value = -1;
            if((column_value = vu.findColumn(csv_filename, val.column_name)) == -1){
                System.out.println("VantivDatabaseUtils.processCsvIntoSql()");
                System.out.println("    Column: \"" + val.column_name + "\" not found.");
                System.exit(1);
            }
            else{
                val.csv_column = column_value;
            }
        }
        
        // Generate sql to create the table
        List<String> list_output = new ArrayList<String>();
        list_output = createSqlTableFile(schema_name,
                                     table_name,
                                     column_data,
                                     output_sql_filename,
                                     output_sql_dir_name);

        // Generate sql INSERT statement files
        list_output = createSqlInsertStatementFiles(csv_filename,
                                                    schema_name,
                                                    table_name,
                                                    column_data,
                                                    output_sql_filename,
                                                    output_sql_dir_name,
                                                    max_insert_statements,
                                                    list_output);


        if(load_table)
        {
            // Create the sql table and load with data.
            for(String val: list_output){
                runSqlcmdOnFile(val);
            }
        }
    }

    //
    // Given:
    //      schema_name, table_name, column_data, output_sql_filename,
    //      and output_sql_directory_name
    //
    // Generates the sql to create the table in the AdvancedFraud
    // database. Returns a List<String> with the filename of the sql
    // file as the first element.
    //
    public List<String> createSqlTableFile(String schema_name,
                                       String table_name,
                                       List<ColumnData> column_data,
                                       String output_sql_filename,
                                       String output_sql_directory_name){
        String str_out = "";
        String filename = "";
        List<String> list_output = new ArrayList<String>();

        try{
            filename = output_sql_directory_name + output_sql_filename + ".sql";
            list_output.add(filename);

            FileWriter iFW = new FileWriter (filename);
	        BufferedWriter iBW = new BufferedWriter(iFW);

            // Start building the output sql string
            str_out += "USE AdvancedFraud;\n";
            str_out += "GO\n\n";
            str_out += "CREATE TABLE " + schema_name + "." + table_name + "\n";
            str_out += "(\n";

            // Now loop through the columns
            for(ColumnData val: column_data){
                str_out += val.sql_column_name + " " + val.sql_data_type +
                           " NOT NULL,\n";
            }

            str_out += ")\n" + "GO";
            iBW.write(str_out);
            iBW.flush();
            iBW.close();
        }
        catch(IOException ex){
            System.out.println("IOException: VantivDatabaseUtils.createSqlTable()");
        }

        return list_output;
    }

    public List<String> createSqlInsertStatementFiles(String csv_filename,
                                                      String schema_name,
                                                      String table_name,
                                                      List<ColumnData> column_data,
                                                      String output_sql_filename,
                                                      String output_sql_directory_name,
                                                      int max_insert_statements,
                                                      List<String> list_output){
        // Create "INSERT INTO" string
        String insert_into_str = "INSERT INTO AdvancedFraud." +
                             schema_name + "." +
                             table_name + "(";
        for(ColumnData val: column_data){
            insert_into_str += val.sql_column_name + ",";
        }
        // Remove trailing comma
        insert_into_str = insert_into_str.substring(0,insert_into_str.length()-1);
        // Add closing paren
        insert_into_str += ")";

        // Process the csv file to create the output sql files.
        CSVReader reader = null;
        String[] aNextLine = null;

        try{
            FileReader iFR = new FileReader (csv_filename);
	        BufferedReader iBR = new BufferedReader(iFR);
            String current_line = iBR.readLine(); // Skip the header row

            FileWriter iFW = null;
	        BufferedWriter iBW = null;

            String filename = "";
            String output_str = "";
            boolean start = true;
            int file_number = 0;
            int counter = 0;
            while( (current_line = iBR.readLine()) != null){
                output_str = "VALUES(";
                if(start){
                    start = false;
                    file_number++;
                    filename = output_sql_directory_name + output_sql_filename +
                               "_" + file_number + ".sql";
                    list_output.add(filename);
                    iFW = new FileWriter (filename);
	                iBW = new BufferedWriter(iFW);
                }

                reader = new CSVReader(new StringReader(current_line));
                aNextLine = reader.readNext();
                for(ColumnData val: column_data){
                    output_str += "N'" +
                               aNextLine[val.csv_column].replace('\'',' ') +
                               // Notice above replacing apostrophe ' with a space.
                               "',";
                }
                // Remove trailing comma
                output_str = output_str.substring(0,output_str.length()-1);

                // Finish the VALUES statement
                output_str += ");";

                // Write the complete INSERT INTO statement to the output file.
                iBW.write(insert_into_str);
                iBW.newLine();
                iBW.write(output_str);
                iBW.newLine();
                counter++;
                
                // Check if max insert file size has been reached.
                if(counter > max_insert_statements){
                    start = true;
                    counter = 0;
                    iBW.flush();
                    iBW.close();
                }
            }
            iBW.flush();
            iBW.close();
        }
        catch(IOException ex){
            System.out.println("IOException: VantivDatabaseUtils.createSqlInsertStatementFiles()");
        }
        
        return list_output;
    }

    //
    // Given a filename that contain t-sql commands, runs
    // sqlcmd on the file.
    //
    public void runSqlcmdOnFile(String filename){
        try{
            String line = null;
            Process p = Runtime.getRuntime().exec("sqlcmd.exe -S EHALL-E6330\\SQLEXPRESS -U sa -P bobbuilder1! -i " +
                                                  filename);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null){
                System.out.println(line);
            }
            input.close();
        }
        catch(Exception ex){
            System.out.println("An exception occurred");
        }
    }
}

//
// Utility class for 
class ColumnData{
    public String column_name = null;
    public String sql_column_name = null;
    public String sql_data_type = null;
    public int csv_column = 0;

    public ColumnData(String column_name,
                      String sql_column_name,
                      String sql_data_type){
        this.column_name = column_name;
        this.sql_column_name = sql_column_name;
        this.sql_data_type = sql_data_type;
    }
}

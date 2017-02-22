//
// This script simulates the R str() function. Input is a csv file.
//


import com.opencsv.CSVReader

// Given an integer position, and a comma delimited string, then
// splits the string and returns the value at the integer position
def doSomething(int position, String csvString){
    String[] values = new CSVReader(new StringReader(csvString)).readNext()
    return values[position]
}

String current_line = null
CSVReader reader = null
String[] header_row = null
def found_header = false
def cnt = 0
def value_list = []
def line_cnt = -1

try{
    FileReader iFR = new FileReader (args[0])
    BufferedReader iBR = new BufferedReader(iFR)

    while( ((current_line = iBR.readLine()) != null)) {
        // Skip any blank lines at the beginning of the file.
        if(current_line == ''){
            line_cnt++
            continue
        }

        // Assumes that the first nonblank line is the header row
        if(!found_header){
            found_header = true
            reader = new CSVReader(new StringReader(current_line))
            header_row = reader.readNext()
            line_cnt++
            continue
        }

        // Try to store at least 5 rows of data
        if(cnt < 5){
            value_list.add(current_line)
            cnt++
            line_cnt++
            continue
        }
        line_cnt++
    }
}
catch(IOException ex){
    System.out.println("findColumn: IOException");
}

println "   ${line_cnt} obs.  ${header_row.size()} variables:"

def header_cnt = 0
header_row.each{header_value->
    print "${header_cnt + 1} ${header_value} : "
    value_list.each{values->
        print "\"${doSomething(header_cnt,values)}\" "
    }
    print "..."
    println ""
    header_cnt++
}


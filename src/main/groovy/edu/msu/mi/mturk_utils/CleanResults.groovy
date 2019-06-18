package edu.msu.mi.mturk_utils

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter

class CleanResults {


    public CleanResults(File f,cols = []) {
        CSVParser parser = new CSVParser(new FileReader(f),CSVFormat.EXCEL.withHeader())
        CSVPrinter out = new CSVPrinter(new FileWriter("cleaned."+f.name),CSVFormat.DEFAULT.withHeader(cols as String[]))
        parser.each { row ->
            out.printRecord(cols.collect {
                row[it]
            })
        }
        out.close()
    }

    public static void main(String[] args) {
        new CleanResults(new File(args[0]),args[1].split(",") as List)
    }

}

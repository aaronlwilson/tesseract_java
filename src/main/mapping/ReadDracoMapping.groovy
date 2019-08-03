package mapping

// This will parse a CSV file in the format Aaron invented on Google Sheets.  Create a spreadsheet with a matrix of cells each representing one LED in your grid
// There are three types of cells:
// - node cells
//   - these cells contain only an integer representing the LED's index in the strand
//   - access via result.nodes (a list)
// - metadata cells
//   - these cells can contain key/value pairs separated by a ':'
//   - they can contain anything except ',' or ':'
//   - we use this to define the panel species
//   - access via result.metadata (a map of key/value pairs)
// - all other cells
//   - these are all saved into a list on the result
//   - access via result.unstructured (a list)

// IMPORTANT THINGS
// - The code directly translates the row and column of a cell into x y coordinates.  Cell A1 is (1, 1), B2 is (2, 2), etc
//   If your block of node indexes isn't at origin in the spreadsheet, that is reflected in the returned results
// - There really isn't much error handling.  You can probably break this if you try

class ReadDracoMapping {

  // Parse all of the CSVs and return a list of results
  public static List<Map> parseCsvs(csvDir) {
    List<String> csvPaths = new FileNameFinder().getFileNames(csvDir, '**/*.csv')

    // 'collect' calls a function once for each list element, creating a new list of the same length with the result of the function
    // its useful to transform one list into another, for example here we are transforming a list of CSV paths (String) into a list of nodes (Map)
    // groovy automatically returns the last expression in a function, so no 'return' statement is necessary
    csvPaths.collect { String path -> parseCsv(new File(path).text, path) }
  }

  // we just pass the path in so we can print a better error, really.  the error checking should be moved
  public static Map parseCsv(String csvText, String csvPath = '') {
    Map result = [:]
    result.nodes = []
    result.metadata = [:]
    result.unstructured = []

    def lines = csvText.split('\n')

    if (lines.size() == 0) {
      println "WARNING: CSV file was empty ${csvPath}"
      return [:]
    }

    lines.eachWithIndex { String line, int i ->
      // One based indexing.  The current 'y' value
      Integer currentY = i + 1

      // Parse the line and merge the stuff we care about into our own 'results' map
      Map lineResult = parseLine(currentY, line)

      // The '<<' merges the Maps.  shallow merge
      result.metadata << lineResult.metadata

      // addAll: adds all the elements of one list to another list
      result.nodes.addAll lineResult.nodes
      result.unstructured.addAll lineResult.unstructured
    }

    return result
  }

  // Parses one line of the csv file.  currentY is the current y index in the matrix of nodes
  public static Map parseLine(Integer currentY, String line) {
    Map result = [:]
    result.nodes = []
    result.metadata = [:]
    result.unstructured = []

    List<String> cells = line.split(',').collect { it.trim() } // split on ',' and trim all cells

    // Parse each type of cell and put the results in the 'results' map
    cells.eachWithIndex { String cell, int i ->
      if (cell == '') {
        // Just an empty cell, ignore it.  since we are using the index to determine the x/y value of the cell, we don't
        // want to filter them out of the list before this
        return
      }

      // 1 based indexing
      Integer currentX = i + 1

      // metadata will just get added to the results, we can use it later, e.g., to define a panel species
      if (cell.contains(':')) {
        // groovy has this 'multiple assignment' thing.  sometimes useful
        def (metadataKey, metadataValue) = cell.split(':')
        result.metadata[metadataKey.trim()] = metadataValue.trim()
      } else if (cell.isInteger()) {
        result.nodes.push([x: currentX, y: currentY, strandIdx: Integer.parseInt(cell)])
      } else {
        // Random cells with text.  why not read it?
        result.unstructured.push(cell)
      }
    }

    return result
  }
}

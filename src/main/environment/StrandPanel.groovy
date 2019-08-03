package environment

import mapping.ReadDracoMapping
import hardware.*
import util.Util

public class StrandPanel {

    private int _scale = 10

    // If we don't specify environment.Node[] (instead of just Node[]), it was conflicting with an existing Node[] class in groovy
    // Strangly it would run in IntelliJ, but when I compiled the app with gradle it complained
    public environment.Node[] buildPanel(Teensy teensy, int pinNum, String panelSpecies, int globalIndex, int startX, int startY, int startZ, int rotation) {
        // Parse the draco csv files
        String csvDir = "${Util.getRootDataDir()}/mapping/draco_csv"
        List<Map> mappings = ReadDracoMapping.parseCsvs(csvDir)

        // Find the mapping that matches the species
        // The species is stored in metadata in the CSV
        Map speciesMapping = mappings.find { m -> m.metadata.species == panelSpecies }

        if (speciesMapping == null) {
          throw new RuntimeException("""\
            ERROR: No mapping for panel species '${panelSpecies}'
            Please make sure there is a CSV file in the directory '${csvDir}' that has a cell w/ contents "species: <species name>" where species name is ${panelSpecies}
          """.stripIndent())
        }

        // the node definitions.  not a Node object (yet), but a map like [ strandIdx, x, y ]
        // don't know if sorting matters, but they were sorted previously so I'm sorting them now
        List<Map> nodeDefs = speciesMapping.nodes.sort { n -> n.strandIdx }

        // This will return a new list of Nodes
        nodeDefs.collect { Map n ->
          int x = n.x * _scale
          int y = n.y * _scale
          new Node(startX + x, startY + y, startZ, globalIndex++, null)
        }
    }
}

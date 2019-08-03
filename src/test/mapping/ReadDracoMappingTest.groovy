package mapping

import app.TesseractMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import util.Util

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.junit.MatcherAssert.assertThat

@PrepareForTest([TesseractMain.class, Util.class])
class ReadDracoMappingTest {

  // Creates a temporary directory that is cleaned up after the test suite
  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder()

  // Allows us to make detailed assertions about thrown exceptions
  @Rule
  public ExpectedException thrown = ExpectedException.none()

  @Before
  void setUp() {
    Util.enableColorization()
  }

  String createSimpleCsv(Map metadata = [:]) {
    Integer numRows = 10
    Integer numCols = 10
    String mappingData = (1..numRows).collect { i ->
      (1..numCols).collect { j -> j + ((i - 1) * numCols) }.join(',')
    }.join('\n')

    String metadataStr = metadata.collect { k, v ->
      "${k}: ${v}"
    }.join(',')

    "${mappingData}\n${metadataStr}"
  }

  @Test
  void testCanParseSimpleCsv() {
    String csv = createSimpleCsv([panelName: 'talon-small-1'])
    tmpDir.newFile('mapping-1.csv').write(csv)

    List<Map> results = ReadDracoMapping.parseCsvs(tmpDir.getRoot().getCanonicalPath())
    assertThat results.size(), equalTo(1)

    results[0].nodes.each { node ->
      def strandIdx = (node.y - 1) * 10 + node.x
      assertThat node.strandIdx, equalTo(strandIdx)
    }
  }

  @Test
  void testCanParseSimpleCsvWithMetadata() {
    Map metadata = [
        panelName    : 'chuck',
        panelLocation: 'bottom',
        globalX      : '4',
        globalX      : '7',
    ]

    String csv = createSimpleCsv(metadata)
    tmpDir.newFile('mapping-1.csv').write(csv)

    println csv

    List<Map> results = ReadDracoMapping.parseCsvs(tmpDir.getRoot().getCanonicalPath())
    assertThat results.size(), equalTo(1)

    Map result = results[0]

    result.nodes.each { node ->
      def strandIdx = (node.y - 1) * 10 + node.x
      assertThat node.strandIdx, equalTo(strandIdx)
    }

    assertThat result.metadata, notNullValue()

    metadata.each { k, v ->
      assertThat result.metadata[k], equalTo(v)
    }
  }

  // These next two test cases take the lists directly from StrandPanel and the results of parsing the CSV match the existing data
  @Test
  void testCanParseCentralPillarLevel1A() {
    // these are in a format where the first element is the strandIndex, the second element is y value and the third element is the x value
    def expectedValues = [[1,10,21], [2,10,20], [3,10,19], [4,10,18], [5,10,17], [6,10,16], [7,10,15], [8,10,14], [9,10,13], [10,10,12], [11,10,11], [12,10,10], [13,10,9], [14,10,8], [15,10,7], [16,10,6], [17,10,5], [18,10,4], [19,10,3], [20,10,2], [21,10,1], [22,9,2], [23,9,3], [24,9,4], [25,9,5], [26,9,6], [27,9,7], [28,9,8], [29,9,9], [30,9,10], [31,9,11], [32,9,12], [33,9,13], [34,9,14], [35,9,15], [36,9,16], [37,9,17], [38,9,18], [39,9,19], [40,9,20], [41,9,21], [42,8,20], [43,7,20], [44,6,20], [45,6,19], [46,7,19], [47,8,19], [48,8,18], [49,7,18], [50,7,17], [51,8,17], [52,8,16], [53,8,15], [54,8,14], [55,8,13], [56,8,12], [57,8,11], [58,8,10], [59,8,9], [60,8,8], [61,8,7], [62,8,6], [63,8,5], [64,8,4], [65,8,3], [66,8,2], [67,7,2], [68,6,2], [69,6,3], [70,7,3], [71,7,4], [72,6,4], [73,7,5], [74,7,6], [75,7,7], [76,7,8], [77,7,9], [78,6,8], [79,5,7], [80,4,6], [81,3,5], [82,2,4], [83,1,4], [84,2,5], [85,3,6], [86,4,7], [87,5,8], [88,6,9], [89,7,10], [90,7,11], [91,7,12], [92,7,13], [93,7,14], [94,7,15], [95,6,15], [96,6,14], [97,6,13], [98,6,12], [99,6,11], [100,5,12], [101,5,13], [102,5,14], [103,5,15], [104,4,15], [105,4,14], [106,4,13], [107,3,14], [108,3,15]];

    File file = new File('src/test/resources/mapping_csv/center_pillar_level_1_A.csv')

    Map results = ReadDracoMapping.parseCsv(file.text)

    expectedValues.eachWithIndex { List<Integer> value, Integer idx ->
      def (expectedIdx, expectedY, expectedX) = value

      // Find the node with the same strandIdx
      Map node = results.nodes.find { n -> n.strandIdx == expectedIdx }
      assert node, notNullValue()

      if (node.x != expectedX || node.y != expectedY) {
        println "Node did not match expected value"
      }

      assertThat node.x, equalTo(expectedX)
      assertThat node.y, equalTo(expectedY)
    }
  }

  @Test
  void testCanParseCentralPillarLevel1B() {
    // these are in a format where the first element is the strandIndex, the second element is y value and the third element is the x value
    def expectedValues = [[1, 10, 10], [2, 10, 9], [3, 9, 9], [4, 9, 8], [5, 8, 7], [6, 8, 8], [7, 8, 9], [8, 7, 9], [9, 7, 8], [10, 7, 7], [11, 7, 6], [12, 7, 5], [13, 6, 5], [14, 6, 6], [15, 6, 7], [16, 6, 8], [17, 5, 8], [18, 5, 7], [19, 5, 6], [20, 5, 5], [21, 4, 5], [22, 4, 6], [23, 4, 7], [24, 4, 8], [25, 3, 7], [26, 3, 6], [27, 3, 5], [28, 2, 5], [29, 2, 6], [30, 1, 5], [31, 1, 4], [32, 2, 4], [33, 3, 4], [34, 4, 4], [35, 5, 4], [36, 6, 4], [37, 6, 3], [38, 6, 2], [39, 5, 2], [40, 4, 2], [41, 3, 2], [42, 2, 2], [43, 1, 2], [44, 1, 1], [45, 2, 1], [46, 3, 1], [47, 4, 1], [48, 5, 1], [49, 6, 1], [50, 7, 1]]

    File file = new File('src/test/resources/mapping_csv/center_pillar_level_1_B.csv')

    Map results = ReadDracoMapping.parseCsv(file.text)

    expectedValues.eachWithIndex { List<Integer> value, Integer idx ->
      def (expectedIdx, expectedY, expectedX) = value

      // Find the node with the same strandIdx
      Map node = results.nodes.find { n -> n.strandIdx == expectedIdx }
      assert node, notNullValue()

      assertThat node.x, equalTo(expectedX)
      assertThat node.y, equalTo(expectedY)
    }

    assertThat results.metadata.hole, equalTo('1 7')
  }
}

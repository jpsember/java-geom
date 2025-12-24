package cluster;

import js.base.BaseObject;
import js.file.Files;
import js.geometry.FPoint;
import js.parsing.DFA;
import js.parsing.Lexer;
import cluster.gen.Node;
import cluster.gen.NodeSet;

import java.io.File;
import java.util.List;

import static js.base.Tools.*;
import static cluster.LineStringTokens.*;
import static cluster.MatchUtil.*;

public class NodeReader extends BaseObject {

//  public void setNodeIdFieldName(String name) {
//    mNodeIdFieldName = name;
//  }

  public NodeSet parse(File databaseFile) {
    try {
      checkArgument(Files.nonEmpty(databaseFile), "database file is empty");
      prepareDfas();
      return parseNodesFromCsv(databaseFile);
    } catch (Throwable e) {
      pr("*** Error parsing file:", databaseFile);
      throw e;
    }
  }
//
//  public int geometryColumnIndex() {return mGeomColumn;}
//
//  public List<String> columnNames() {
//    return mColumnNames;
//  }

  private String mGeomColumnName;

  public void setGeomColumnName(String name) {
    mGeomColumnName = name;
  }
  private NodeSet parseNodesFromCsv(File databaseFile) {
    var p = new CsvReader();
    p.parse(databaseFile);

    checkArgument(mGeomColumnName != null,"no geometry column name specified");

 var geomColumn = p.findColumn(mGeomColumnName);
    checkArgument(geomColumn >=0,"can't find geometry column with name 'geom'");
//    mColumnNames = p.columnNames();

//    // If the node_id field name was specified, determine which column it is
//    //
//    int nodeIdColumn = -1;
//    {
//      p13("parseNodesFromCsv:",databaseFile,"node_id_field_name:",mNodeIdFieldName);
//
//      var n = mNodeIdFieldName;
//      if (nonEmpty(n)) {
//        nodeIdColumn = p.findColumn(n);
//        if (nodeIdColumn < 0)
//          badState("cannot find field named", quote(n));
//      }
//    }

//    var out = NodeSet.newBuilder();

    List<Node> nodeBuffer = arrayList();

    for (var row : p.rows()) {
//      var b = Node.newBuilder();
      parseGeometryFromCell(row.get(geomColumn), nodeBuffer);
//      var columnContents = new ArrayList<String>(row.size());
//
//      for (var s : row) {
//        var content = parseString(stripQuotes(s));
//        columnContents.add(content);
//      }

//      b.originalColumnContents(columnContents);

//      // If a node_id column was specified, store its contents in the id field
//      //
//      if (nodeIdColumn >= 0) {
//        b.id(parseString(columnContents.get(nodeIdColumn)));
//      }
    }

//      nodeBuffer.add(b.build());
//
//    // Determine which field contains geometry (e.g. LINESTRING(...)), then fill in the geometry
//    // field for all the nodes.
//    //
//    int geomColumn = findGeometryColumn(nodeBuffer);
//    checkArgument(geomColumn >= 0, "Can't find column with geometry");
//    mGeomColumn = geomColumn;
//
//    // Replace the list of nodes with versions that include parsed geometry
//    // (we delay doing this since we don't know ahead of time what fields seem to
//    // contain the geometry)
//    //
//    nodeBuffer = parseGeometryForAllRows(geomColumn, nodeBuffer);
//
//    out.nodes(nodeBuffer);

    var out = NodeSet.newBuilder();
    out.nodes(nodeBuffer);
    determineBounds(out);
    return out;
  }

//  private static final int GEOMETRY_SAMPLE_SIZE = 10;

//  private int findGeometryColumn(List<Node> nodes) {
//    checkArgument(!nodes.isEmpty(), "no rows in dataset");
//
//    int foundColumn = -1;
//    int scanMax = Math.min(nodes.size(), GEOMETRY_SAMPLE_SIZE);
//
//    for (int rowNumber = 0; rowNumber < scanMax; rowNumber++) {
//      var node = nodes.get(rowNumber);
//      {
//        var columnNumber = INIT_INDEX;
//        for (var n : node.originalColumnContents()) {
//          columnNumber++;
//          var j = n.toUpperCase();
//          if (j.contains("LINESTRING") || j.contains("POINT")) {
//            if (foundColumn >= 0 && foundColumn != columnNumber)
//              badArg("multiple columns look like they contain LINESTRINGs");
//            foundColumn = columnNumber;
//          }
//        }
//      }
//    }
//    return foundColumn;
//  }

//  private List<Node> parseGeometryForAllRows(int geometryColumn, List<Node> nodes) {
//    List<Node> out = arrayList();
//
//    for (int rowNumber = 0; rowNumber < nodes.size(); rowNumber++) {
//      var node = nodes.get(rowNumber);
//      var text = node.originalColumnContents().get(geometryColumn);
//      try {
//        var b = node.toBuilder();
//        parseGeometryFromCell(b, text);
//        out.add(b.build());
//      } catch (Throwable t) {
//        pr("...failed to parse geometry from line:", 2 + rowNumber, quote(text));
//        throw t;
//      }
//    }
//    return out;
//  }

  public static void parseGeometryFromCell(String text, List<Node> target) {
    prepareDfas();
    text = stripQuotes(text);
//    checkArgument(!text.startsWith("\""));
    var s = new Lexer(sLinestringDfa).withSkipId(LS_WS).withText(text);
    s.read(LS_LINESTRING);

    FPoint prevPt = null;
    while (!s.readIf(LS_ENDLINESTRING)) {
      s.readIf(LS_COMMA);
      var longit = parseNumber(s);
      var latit = parseNumber(s);
      var newPt = new FPoint(longit,latit);
      if (prevPt != null) {
        target.add(Node.newBuilder().a(prevPt).b(newPt).build());
      }
      prevPt = newPt;
    }
  }

  private static float parseNumber(Lexer s) {
    var t = s.read(LS_NUMBER).text();
    return Float.parseFloat(t);
  }

//  private String mNodeIdFieldName;

//  private List<String> mColumnNames;
//  private int mGeomColumn;

  // ------------------------------------------------------------------
  // DFA
  // ------------------------------------------------------------------

  private static void prepareDfas() {
    if (sLinestringDfa == null) {
      sLinestringDfa = DFA.parse(
          "{\"graph\":[0,9,1,80,1,97,1,1,77,1,-20,0,1,76,1,-97,0,1,49,9,-116,0,1,48,1,77,0,1,45,1,65,0,1,44,1,63,0,1,41,1,54,0,1,32,1,47,0,1,1,1,32,1,47,0,3,1,1,41,1,61,0,3,0,5,0,0,2,1,49,9,-116,0,1,48,1,77,0,4,2,2,69,1,101,1,112,0,1,46,1,91,0,0,1,1,48,10,98,0,4,2,2,69,1,101,1,112,0,1,48,10,98,0,0,2,1,48,10,-123,0,2,43,1,45,1,126,0,0,1,1,48,10,-123,0,4,1,1,48,10,-123,0,4,3,1,48,10,-116,0,2,69,1,101,1,112,0,1,46,1,91,0,0,1,1,73,1,-90,0,0,1,1,78,1,-83,0,0,1,1,69,1,-76,0,0,1,1,83,1,-69,0,0,1,1,84,1,-62,0,0,1,1,82,1,-55,0,0,1,1,73,1,-48,0,0,1,1,78,1,-41,0,0,1,1,71,1,-34,0,0,2,1,32,1,-34,0,1,40,1,-22,0,2,0,0,1,1,85,1,-13,0,0,1,1,76,1,-6,0,0,1,1,84,1,1,1,0,1,1,73,1,8,1,0,1,1,76,1,15,1,0,1,1,73,1,22,1,0,1,1,78,1,29,1,0,1,1,69,1,36,1,0,1,1,83,1,43,1,0,1,1,84,1,50,1,0,1,1,82,1,57,1,0,1,1,73,1,64,1,0,1,1,78,1,71,1,0,1,1,71,1,78,1,0,2,1,32,1,78,1,1,40,1,90,1,0,1,1,40,1,-22,0,0,1,1,79,1,104,1,0,1,1,73,1,111,1,0,1,1,78,1,118,1,0,1,1,84,1,-34,0],\"token_names\":\"WS LINESTRING ENDLINESTRING NUMBER COMMA\",\"version\":\"$2\"}"
           );
    }
  }

  private static DFA sLinestringDfa;

}

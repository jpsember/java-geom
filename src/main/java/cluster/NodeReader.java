package cluster;

import js.base.BaseObject;
import js.file.Files;
import js.geometry.FPoint;
import js.parsing.DFA;
import js.parsing.Lexer;
import cluster.gen.Node;
import cluster.gen.NodeSet;
import sample.SampleMain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static js.base.Tools.*;
import static cluster.LineStringTokens.*;
import static cluster.MatchUtil.*;

public class NodeReader extends BaseObject {

  public void setNodeIdFieldName(String name) {
    mNodeIdFieldName = name;
  }

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

  public int geometryColumnIndex() {return mGeomColumn;}

  public List<String> columnNames() {
    return mColumnNames;
  }

  private NodeSet parseNodesFromCsv(File databaseFile) {
    var p = new CsvReader();
    p.parse(databaseFile);
    mColumnNames = p.columnNames();

    // If the node_id field name was specified, determine which column it is
    //
    int nodeIdColumn = -1;
    {
      p13("parseNodesFromCsv:",databaseFile,"node_id_field_name:",mNodeIdFieldName);

      var n = mNodeIdFieldName;
      if (nonEmpty(n)) {
        nodeIdColumn = p.findColumn(n);
        if (nodeIdColumn < 0)
          badState("cannot find field named", quote(n));
      }
    }

    var out = NodeSet.newBuilder();

    List<Node> nodeBuffer = arrayList();

    for (var row : p.rows()) {
      var b = Node.newBuilder();
      var columnContents = new ArrayList<String>(row.size());

      for (var s : row) {
        var content = parseString(stripQuotes(s));
        columnContents.add(content);
      }

      b.originalColumnContents(columnContents);

      // If a node_id column was specified, store its contents in the id field
      //
      if (nodeIdColumn >= 0) {
        b.id(parseString(columnContents.get(nodeIdColumn)));
      }

      nodeBuffer.add(b.build());
    }

    // Determine which field contains geometry (e.g. LINESTRING(...)), then fill in the geometry
    // field for all the nodes.
    //
    int geomColumn = findGeometryColumn(nodeBuffer);
    checkArgument(geomColumn >= 0, "Can't find column with geometry");
    mGeomColumn = geomColumn;

    // Replace the list of nodes with versions that include parsed geometry
    // (we delay doing this since we don't know ahead of time what fields seem to
    // contain the geometry)
    //
    nodeBuffer = parseGeometryForAllRows(geomColumn, nodeBuffer);

    out.nodes(nodeBuffer);

    determineBounds(out);
    return out;
  }

  private static final int GEOMETRY_SAMPLE_SIZE = 10;

  private int findGeometryColumn(List<Node> nodes) {
    checkArgument(!nodes.isEmpty(), "no rows in dataset");

    int foundColumn = -1;
    int scanMax = Math.min(nodes.size(), GEOMETRY_SAMPLE_SIZE);

    for (int rowNumber = 0; rowNumber < scanMax; rowNumber++) {
      var node = nodes.get(rowNumber);
      {
        var columnNumber = INIT_INDEX;
        for (var n : node.originalColumnContents()) {
          columnNumber++;
          var j = n.toUpperCase();
          if (j.contains("LINESTRING") || j.contains("POINT")) {
            if (foundColumn >= 0 && foundColumn != columnNumber)
              badArg("multiple columns look like they contain LINESTRINGs");
            foundColumn = columnNumber;
          }
        }
      }
    }
    return foundColumn;
  }

  private List<Node> parseGeometryForAllRows(int geometryColumn, List<Node> nodes) {
    List<Node> out = arrayList();

    for (int rowNumber = 0; rowNumber < nodes.size(); rowNumber++) {
      var node = nodes.get(rowNumber);
      var text = node.originalColumnContents().get(geometryColumn);
      try {
        var b = node.toBuilder();
        parseGeometryFromCell(b, text);
        out.add(b.build());
      } catch (Throwable t) {
        pr("...failed to parse geometry from line:", 2 + rowNumber, quote(text));
        throw t;
      }
    }
    return out;
  }

  private static void parseGeometryFromCell(Node.Builder b, String text) {
    checkArgument(!text.startsWith("\""));
    var s = new Lexer(sLinestringDfa).withSkipId(LS_WS).withText(text);
    s.read(LS_LINESTRING);
    while (!s.readIf(LS_ENDLINESTRING)) {
      if (!b.vertices().isEmpty()) {
        s.read(LS_COMMA);
      }
      var longit = parseNumber(s);
      var latit = parseNumber(s);
      b.vertices().add(new FPoint(longit, latit));
    }
  }

  private static float parseNumber(Lexer s) {
    var t = s.read(LS_NUMBER).text();
    return Float.parseFloat(t);
  }

  private String mNodeIdFieldName;

  private List<String> mColumnNames;
  private int mGeomColumn;

  // ------------------------------------------------------------------
  // DFA
  // ------------------------------------------------------------------

  private static void prepareDfas() {
    if (sLinestringDfa == null) {
      todo("replace with constant string");
      sLinestringDfa = DFA.parse(Files.readString(SampleMain.class, "linestring.dfa"));
    }
  }

  private static DFA sLinestringDfa;

}

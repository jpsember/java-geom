package testbed;

/**
 * Interface to store global variables for the TestBed framework
 *
 */
interface TBGlobals {

  static final int
  // control panel indexes
      CT_MAIN = 0,
     CT_TOTAL = 2;

  /**
   * Reserved gadget ids
   */
  static final int
  // ids from 8900..8999 are not read/written to individual file headers,
      // but are written to configuration files
      CONFIGSTART = 8900,
      
      APP_FRAME = 8904,
EDITOR_ZOOM = 8903,

      //
      CONFIGEND = 9000,

      ABOUT = 9001,
      OPER = 9002,
      MENU_TESTBED = 9031,
      QUIT = 9033, GRIDACTIVE = 9036,
      GRIDON = 9040,
      GRIDLABELS = 9041,
      GRIDSIZE = 9042, 
      MOUSELOC = 9043,

      CTRLSVISIBLE = 9062,
      CONSOLEVISIBLE = 9063,
      BTN_TOGGLEWORKSPACE = 9066,
      AUXTABSET = 9067,
      AUXTAB_GRID = 9069, 
      AUXTAB_TRACE = 9070,

      TRACESTEP = 9092, TRACEPLOT = 9093,
      TRACEENABLED = 9098,

      EDITORPARMS = 9300, EDITORPARMS_MAXLENGTH = 100,

      // start of ids to assign to anonymous panels
      ID_ANON_START = 9500
      // Application ids should start at 100.
      // Each operator should have a distinct set of ids,
      // i.e. 100..199, 200.299, etc.
      ;

}

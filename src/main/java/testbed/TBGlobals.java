package testbed;

/**
 * Interface to store global variables for the TestBed framework
 */
public interface TBGlobals {

  /**
   * Reserved gadget ids
   */
  static final int
  // ids from 8900..8999 are not read/written to individual file headers,
  // but are written to configuration files
  CURRENT_SCRIPT_INDEX = 8901,

      APP_FRAME = 8904, EDITOR_ZOOM = 8903,

      OPER = 9002,

      CTRLSVISIBLE = 9062, AUXTABSET = 9067, AUXTAB_TRACE = 9070,

      TRACESTEP = 9092, TRACEPLOT = 9093, TRACEENABLED = 9098,

      // start of ids to assign to anonymous widgets
      ID_ANON_START = 9500

  // Application ids should start at 100.
  // Each operation should have a distinct set of ids,
  // i.e. 100..199, 200.299, ...
  ;

}

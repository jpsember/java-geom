package testbed;

/**
 * Interface to store global variables for the TestBed framework
 */
public interface TBGlobals {

  /**
   * Reserved gadget ids
   */
  static final String
  
      SCRIPT_NAME = "script_name", //
      MESSAGE = "message", //
      
      CURRENT_SCRIPT_INDEX = "script_index",

      APP_FRAME = "app_frame", EDITOR_ZOOM = "ed_zoom",

      OPER = "oper",

      CTRLSVISIBLE = "ctrls_visible", AUXTABSET = "aux_tabset", AUXTAB_TRACE = "aux_trace",

      TRACESTEP = "trace_step", TRACEPLOT = "trace_plot", TRACEENABLED = "trace_enabled";
 
      
      

  // Application ids should start at 100.
  // Each operation should have a distinct set of ids,
  // i.e. 100..199, 200.299, ...
  ;

}

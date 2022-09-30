/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package testbed;

import js.guiapp.UserEvent;
import js.widget.WidgetManager;

/**
 * Interface for TestBed operations
 */
public interface TestBedOperation {

  /**
   * Get the id of this operation's (TabbedPaneWidget) widget 
   */
  public String operId();
  
  /**
   * Add controls for this operation. Each operation exists in its own tab, so
   * the controls should consist of:
   * 
   * <pre>
   *   w.openTab("Oper name");
   *    :
   *    :
   *   w.closeTab();
   * </pre>
   */
  public void addControls(WidgetManager w);

  /**
   * Process a UserEvent
   */
  public void processUserEvent(UserEvent event);

  /**
   * Execute an algorithm. If no algorithm is to be run, this method can do
   * nothing. <br>
   * Tracing can be performed by inserting these lines in various places:
   *
   * <pre>
   * if (T.update())
   *   T.msg("Examining vertex #" + vertexNumber);
   * </pre>
   * 
   * If certain objects are to appear when an algorithm trace is displayed, they
   * should be included by using a show() command. For instance, if the object
   * 'vertex' implements the Traceable interface, then the following code will
   * cause it to be rendered when the algorithm results are shown:
   * 
   * <pre>
   * if (T.update()) T.msg("Processing vertex #"+vertexNumber + T.show(vertex));
   * 
   * <pre>
   * 
   */
  public void runAlgorithm();

  /**
   * Paint view for operation. This is called after runAlgorithm(), which may
   * have been interrupted by an algorithm trace.
   */
  public void paintView();

}

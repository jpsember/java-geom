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

import static js.base.Tools.*;

public class TBError extends RuntimeException {

  public TBError(Throwable cause) {
    this(cause.toString());
  }

  protected String name() {
    return "TBError";
  }

  public TBError(String s) {
    super(s);
    int trim = 0;
    int maxCalls = 5;
    boolean withSt = false;
    outer: while (true) {
      if (s.length() == trim) {
        break;
      }
      switch (s.charAt(trim)) {
      default:
        break outer;
      case '*':
        withSt = true;
        break;
      case '!':
        maxCalls += 5;
        break;
      }
      trim++;
    }
    StringBuilder str = new StringBuilder(name());
    str.append(": ");
    //    boolean withSt = s.startsWith("*");
    //    int trim = 0;
    //    int maxCalls = 5;
    //    if (withSt) {
    //      trim++;
    //      if (s.
    str.append(s.substring(trim));
    if (withSt) {
      str.append("\n");
      todo("add stack trace", maxCalls);
      //      Tools.warn("not sure about stackTrace depth");
      //   str.append(Tools.stackTrace(1, maxCalls, this));
    }
    msg = str.toString();
  }

  private String msg;

  public String toString() {
    return msg;
  }
}

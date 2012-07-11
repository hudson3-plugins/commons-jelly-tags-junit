/*
 * Copyright 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jelly.tags.junit;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hudsonci.xpath.XPath;
import org.hudsonci.xpath.XPathException;
import org.hudsonci.xpath.XPath;


/**
 * Performs an assertion that a given boolean expression, or XPath expression is
 * true. If the expression returns false then this test fails.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 155420 $
 */
public class AssertTag extends AssertTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(AssertTag.class);
    
    /** Enable/disable DEBUG output **/
    private static final boolean DEBUG = true;

    /** The expression to evaluate. */
    private Expression test;

    /** The XPath expression to evaluate */
    private XPath xpath;

    public AssertTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (test == null && xpath == null) {
            throw new MissingAttributeException( "test" );
        }
        if (test != null) {
            if (! test.evaluateAsBoolean(context)) {
                fail( getBodyText(), "evaluating test: "+ test.getExpressionText() );
            }
        }
        else {
            Object xpathContext = getXPathContext();
            try {
                if (! xpath.booleanValueOf(xpathContext)) {
                    if (DEBUG) {
                      String orig = xpath.toString();
                      String expr = stripStringEquals(xpath.toString());
                      XPath dxpath = new XPath(expr);
                      if (xpath.getVariableContext() != null)
                        dxpath.setVariableContext(xpath.getVariableContext());
                      if (xpath.getNamespaceContext() != null)
                        dxpath.setNamespaceContext(xpath.getNamespaceContext());
                      try {
                      String s = dxpath.stringValueOf(xpathContext);
                      System.out.println("**test:assert failed "+orig+" => \""+s+'"');
                      } catch (XPathException e) {
                        System.out.println("**test:assert failed "+expr+" can't evaluate as string");
                      }
                    }
                    fail( getBodyText(), "evaluating xpath: "+ xpath );
                }
            }
            catch (XPathException e) {
                throw new JellyTagException(e);
            }
        }

    }
    
    private String stripStringEquals(String expr) {
      // yes, a regex would be simpler
      int eq = expr.lastIndexOf('=');
      if (eq >= 0) {
        char quote = 0;
        boolean haveString = false;
        for (int i = expr.length()-1; i >= eq; i--) {
          char c = expr.charAt(i);
          // skip whitespace
          if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
            continue;
          // begin or end string
          if (c == '\'' || c == '"') {
            if (haveString)
              // too complicated
              break;
            if (quote == 0)
              quote = c;
            else if (quote == c)
              haveString = true;
          } else if (c == '=') {
            if (!haveString)
              break;
            expr = expr.substring(0, i);
            break;
          } else if (quote == 0 || haveString)
            // only allow arbitrary characters inside string
            break;
        }
      }
      return expr;
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the boolean expression to evaluate. If this expression returns true
     * then the test succeeds otherwise if it returns false then the text will
     * fail with the content of the tag being the error message.
     */
    public void setTest(Expression test) {
        this.test = test;
    }

    /**
     * Sets the boolean XPath expression to evaluate. If this expression returns true
     * then the test succeeds otherwise if it returns false then the text will
     * fail with the content of the tag being the error message.
     */
    public void setXpath(XPath xpath) {
        this.xpath = xpath;
    }
}

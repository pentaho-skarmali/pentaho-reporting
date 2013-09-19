/*
 *
 *  * This program is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  * Foundation.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this
 *  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  * or from the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Lesser General Public License for more details.
 *  *
 *  * Copyright (c) 2006 - 2009 Pentaho Corporation..  All rights reserved.
 *
 */

package org.pentaho.reporting.designer;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextArea;

import junit.framework.TestCase;
import org.fest.swing.annotation.RunsInEDT;
import org.fest.swing.core.BasicComponentFinder;
import org.fest.swing.core.ComponentFinder;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JListFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.openformula.ui.FormulaEditorDialog;
import org.pentaho.openformula.ui.FormulaEditorModel;
import org.pentaho.openformula.ui.FormulaEditorPanel;
import org.pentaho.reporting.designer.core.DefaultReportDesignerContext;
import org.pentaho.reporting.designer.core.ReportDesignerBoot;
import org.pentaho.reporting.designer.core.ReportDesignerFrame;
import org.pentaho.reporting.designer.core.util.GUIUtils;
import org.pentaho.reporting.designer.testsupport.TestReportDesignerView;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.ui.xul.XulException;

public class FestPRDTest extends TestCase
{
  private FrameFixture window;
  private ReportDesignerFrame frame;

  public FestPRDTest()
  {
  }

  private void block(final long secs)
  {
    try
    {
      Thread.sleep(secs * 1000);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  @Before
  public void setUp() throws XulException
  {
    System.setProperty("java.awt.headless", "false");
    ClassicEngineBoot.getInstance().start();
    ReportDesignerBoot.getInstance().start();

    frame = GuiActionRunner.execute(new GuiQuery<ReportDesignerFrame>()
    {
      protected ReportDesignerFrame executeInEDT()
      {
        try
        {
          ReportDesignerFrame frame = new ReportDesignerFrame();
          frame.pack();
          frame.initWindowLocations(null);
          frame.setVisible(true);

          return frame;
        }
        catch (XulException ex)
        {
        }

        return null;
      }
    });

    window = new FrameFixture(frame);

    window.show(); // shows the frame to test
    window.requireVisible();

    window.maximize();
  }

  @After
  public void tearDown() {
    window.cleanUp();
  }

  @Test
  public void testInvokeFormulaEditorDialog()
  {
    ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

    // Create a new report
    window.menuItemWithPath("File", "New").click();

    // Select the details band row
    Component detailsBand = finder.findByName("renderComponent_2");
    window.robot.click(detailsBand);

//    ComponentFinder newfinder = BasicComponentFinder.finderWithNewAwtHierarchy();

    // Insert a chart element
    window.menuItemWithPath("Insert", "chart").click();

    // Find the chart element and double-click it
//    Object element = (Object)finder.findByType(JFreeChart.class);


    block(10);
  }

  //  @GUITest     // takes screenshot if test case fails
  @RunsInEDT
  @Test
  public void testFormulaEditorDialog() throws XulException, InterruptedException
  {
    ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

    final FormulaEditorDialog dialog =
        GUIUtils.createFormulaEditorDialog(new DefaultReportDesignerContext(frame, new TestReportDesignerView()), frame);

    final FormulaEditorDialog formulaEditorDialog = (FormulaEditorDialog)finder.findByName("FormulaEditorDialog");
    FormulaEditorPanel panel = (FormulaEditorPanel)formulaEditorDialog.createContentPane();
    JTextArea formulaTextArea = panel.getFunctionTextArea();
    FormulaEditorModel model = panel.getEditorModel();


    GenericTypeMatcher genericTypeMatcher = new GenericTypeMatcher(FormulaEditorDialog.class, false)
    {
      protected boolean isMatching(final Component component)
      {
        if (component.getName().compareTo("FormulaEditorDialog") == 0)
        {
          return true;
        }

        return false;
      }
    };

    final DialogFixture fixture = window.dialog(genericTypeMatcher);
    fixture.show();
    fixture.requireVisible();
    fixture.requireModal();


    final JComboBox categoryComboBox = (JComboBox)finder.findByName(dialog, "categoryComboBox");
    final JComboBoxFixture comboBoxFixture = new JComboBoxFixture(window.robot, categoryComboBox);
    comboBoxFixture.selectItem("Logical");

    // Since we selected a new item, get a refreshed list
    final JList filteredFormulaList = (JList)finder.findByName(dialog, "formulaList");
    final JListFixture filteredListFixture = new JListFixture(window.robot, filteredFormulaList);

    // Select IF formula
    filteredListFixture.doubleClickItem("IF");

    block(15);
    fixture.button("OK").click();
  }
}

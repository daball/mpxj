/*
 * file:       MppViewTest.java
 * author:     Wade Golden
 * copyright:  (c) Tapster Rock Limited 2006
 * date:       19-September-2006
 */
 
/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package net.sf.mpxj.junit;

import java.util.HashSet;
import java.util.List;

import net.sf.mpxj.Column;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Table;
import net.sf.mpxj.View;
import net.sf.mpxj.ViewType;
import net.sf.mpxj.mpp.GanttChartView9;
import net.sf.mpxj.mpp.MPPReader;

/**
 * Testsb to exercise MPP file read functionality for various versions of
 * MPP file.
 */
public class MppViewTest extends MPXJTestCase 
{
   
   /**
    * Test view data read from an MPP9 file.
    * 
    * @throws Exception
    */   
    public void testMpp9View() 
       throws Exception 
    {
        ProjectFile mpp = new MPPReader().read (m_basedir + "/mpp9resource.mpp");        
        testViews(mpp);
    }

    /**
     * Test view data read from an MPP12 file.
     * 
     * @throws Exception
     */       
    public void testMpp12View() 
       throws Exception 
    {
       ProjectFile mpp = new MPPReader().read (m_basedir + "/mpp12resource.mpp");
       testViews(mpp);
    }

    /**
     * Tests MPP's Views. Not an in-depth test, but covers the basics of 
     * Views, like View Names, Column Names, and Column Widths
     * 
     * @param mpp The ProjectFile being tested.
     */
    private void testViews(ProjectFile mpp) 
    {
        List views = mpp.getViews();

        // not sure what order MPP12 will store the views in,
        // so make a Set to check against when done reading in the views
        HashSet setViewNames = new HashSet();

        for (int viewNum = 0; viewNum < views.size(); viewNum++) 
        {
            // View Names
            View view = (View) views.get(viewNum);
            String viewName = view.getName();
            setViewNames.add(viewName);
            Table table = null;

            if (view instanceof GanttChartView9) 
            {
                GanttChartView9 view9 = (GanttChartView9)view;
                if (null != view9.getTable()) 
                {
                    table = view9.getTable();

                    ViewType viewType = view.getType();
                    assertEquals(ViewType.GANTT_CHART, viewType);

                    // verify all columns
                    List cols = table.getColumns();
                    HashSet setColumnNames = new HashSet();
                    for (int n = 0; n < cols.size(); n++) 
                    {
                        Column col = (Column) cols.get(n);
                        setColumnNames.add(col.getTitle());
                        int width = col.getWidth();
                        assertTrue(width > 0);
                    }

                    assertEquals(7, setColumnNames.size());
                    assertTrue(setColumnNames.contains("ID"));
                    assertTrue(setColumnNames.contains("Unique ID"));
                    assertTrue(setColumnNames.contains("Task Name"));
                    assertTrue(setColumnNames.contains("Indicators"));
                    assertTrue(setColumnNames.contains("Start"));
                    assertTrue(setColumnNames.contains("Finish"));
                    assertTrue(setColumnNames.contains("Resource Names"));

                    assertTrue(view9.getTableWidth() > 0);
                }
            }
        }

        assertEquals(7, setViewNames.size());
        assertTrue(setViewNames.contains("Gantt Chart"));
        assertTrue(setViewNames.contains("Test View"));
        assertTrue(setViewNames.contains("Tracking Gantt"));
        assertTrue(setViewNames.contains("Resource Sheet"));
        assertTrue(setViewNames.contains("Resource Usage"));
        assertTrue(setViewNames.contains("Task Usage"));
        assertTrue(setViewNames.contains("")); // why blank?
    }


}

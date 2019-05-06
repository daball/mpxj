/*
 * file:       LocaleData_sv.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2004
 * date:       24/03/2004
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

package net.sf.mpxj.mpx

import java.util.HashMap
import java.util.ListResourceBundle

import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.DateOrder
import net.sf.mpxj.ProjectDateFormat
import net.sf.mpxj.ProjectTimeFormat

/**
 * This class defines the Swedish translation of resource required by MPX files.
 */
class LocaleData_sv : ListResourceBundle() {
    /**
     * {@inheritDoc}
     */
    val contents: Array<Array<Object>>
        @Override get() = RESOURCE_DATA

    companion object {

        private val TIME_UNITS_ARRAY_DATA = arrayOf(arrayOf("m"), arrayOf("t"), arrayOf("d"), arrayOf("w"), arrayOf("mon"), arrayOf("y"), arrayOf("%"), arrayOf("em"), arrayOf("eh"), arrayOf("ed"), arrayOf("ew"), arrayOf("emon"), arrayOf("ey"), arrayOf("e%"))
        private val TIME_UNITS_MAP_DATA = HashMap<String, Integer>()

        init {
            for (loop in TIME_UNITS_ARRAY_DATA.indices) {
                val value = Integer.valueOf(loop)
                for (name in TIME_UNITS_ARRAY_DATA[loop]) {
                    TIME_UNITS_MAP_DATA.put(name, value)
                }
            }
        }

        private val TASK_NAMES_DATA = arrayOf<String>(null, "Namn", "WBS", "Dispositionsniv\u00E5", "Text1", "Text2", "Text3", "Text4", "Text5", "Text6", "Text7", "Text8", "Text9", "Text10", "Notes", // Translate
                "Contact", // Translate
                "Resource", null, null, null, "Arbete", "Originalarbete", "Verkligt arbete", "Remaining Work", // Translate
                "Work Variance", // Translate
                "% Work Complete", null, null, null, null, "Kostnad", "Originalkostnad", "Verklig kostnad", "\u00C5terst\u00E5ende kostnad", "Cost Variance", // Translate
                "Fast kostnad", "Kostnad1", "Kostnad2", "Kostnad3", null, "Varaktighet", "Originalvaraktighet", "Actual Duration", // Translate
                "Remaining Duration", // Translate
                "% f\u00E4rdigt", "Duration Variance", // Translate
                "Varaktighet1", "Varaktighet2", "Varaktighet3", null, "Start", "Slut", "Tidig start", "Tidigt slut", "Sen start", "Sent slut", "Originalstart", "Originalslut", "Verklig start", "Verkligt slut", "Start1", // Translate
                "Finish1", // Translate
                "Start2", // Translate
                "Finish2", // Translate
                "Start3", // Translate
                "Finish3", // Translate
                "Start Variance", // Translate
                "Finish Variance", // Translate
                "Villkorsdatum", null, "F\u00F6reg\u00E5ende aktiviteter", "Successors", // Translate
                "Resource Names", // Translate
                "Resource Initials", // Translate
                "Unique ID Predecessors", // Translate
                "Unique ID Successors", null, null, null, null, "Fast", "Milstolpe", "Critical", // Translate
                "M\u00E4rkt", "Upplyft", "BCWS", // Translate
                "BCWP", // Translate
                "SV", // Translate
                "CV", null, "ID", "Villkorstyp", "F\u00F6rskjutning", "Fritt slack", "Totalt slack", "Prioritet", "Underprojektsfil", "Project", // Translate
                "Eget ID", "Outline Number", null, null, null, null, null, null, null, null, null, null, "Flagga1", "Flagga2", "Flagga3", "Flagga4", "Flagga5", "Flagga6", "Flagga7", "Flagga8", "Flagga9", "Flagga10", "Sammanfattning", "Objects", // Translate
                "Linked Fields", // Translate
                "Hide Bar", null, "Skapad", "Start4", // Translate
                "Finish4", // Translate
                "Start5", // Translate
                "Finish5", null, null, null, null, null, "Confirmed", // Translate
                "Update Needed", null, null, null, "Tal1", "Tal2", "Tal3", "Tal4", "Tal5", null, null, null, null, null, "Stopp", "Forts\u00E4tt tidigast", "Resume")// Translate
        // Translate
        // Translate
        // Translate
        // Translate
        // Translate
        // Translate
        // Translate
        // Translate

        private val RESOURCE_NAMES_DATA = arrayOf<String>(null, "Namn", "Initialer", "Grupp", "Kod", "Text1", "Text2", "Text3", "Text4", "Text5", "Notes", // translate
                "e-post", null, null, null, null, null, null, null, null, "Arbete", "Originalarbete", "Verkligt arbete", "Remaining Work", // translate
                "\u00D6vertidsarbete", "Work Variance", // translate
                "% Work Complete", null, null, null, "Kostnad", "Originalkostnad", "Verklig kostnad", "Remaining Cost", // translate
                "Cost Variance", null, null, null, null, null, "ID", "Max enheter", "Standardkostnad", "\u00D6vertidskostnad", "Kostnad per tillf\u00E4lle", "P\u00E5f\u00F6rs", "Overallocated", // translate
                "Peak", // translated
                "Base Calendar", // translate
                "Eget ID", "Objects", // translate
                "Linked Fields" // translate
        )// translate
        // translate

        private val RESOURCE_DATA = arrayOf<Array<Object>>(arrayOf<Object>(LocaleData.FILE_DELIMITER, ";"), arrayOf<Object>(LocaleData.FILE_VERSION, "4,0"),

                arrayOf<Object>(LocaleData.YES, "Ja"), arrayOf<Object>(LocaleData.NO, "Nej"),

                arrayOf<Object>(LocaleData.CURRENCY_SYMBOL, "kr"), arrayOf<Object>(LocaleData.CURRENCY_SYMBOL_POSITION, CurrencySymbolPosition.AFTER_WITH_SPACE), arrayOf<Object>(LocaleData.CURRENCY_THOUSANDS_SEPARATOR, " "), arrayOf<Object>(LocaleData.CURRENCY_DECIMAL_SEPARATOR, ","),

                arrayOf<Object>(LocaleData.DATE_ORDER, DateOrder.YMD), arrayOf<Object>(LocaleData.TIME_FORMAT, ProjectTimeFormat.TWENTY_FOUR_HOUR),

                arrayOf<Object>(LocaleData.DATE_SEPARATOR, "-"), arrayOf<Object>(LocaleData.TIME_SEPARATOR, ":"), arrayOf<Object>(LocaleData.AM_TEXT, ""), arrayOf<Object>(LocaleData.PM_TEXT, ""), arrayOf<Object>(LocaleData.DATE_FORMAT, ProjectDateFormat.EEE_DD_MM_YY),

                arrayOf<Object>(LocaleData.TIME_UNITS_ARRAY, TIME_UNITS_ARRAY_DATA), arrayOf<Object>(LocaleData.TIME_UNITS_MAP, TIME_UNITS_MAP_DATA),

                arrayOf<Object>(LocaleData.TASK_NAMES, TASK_NAMES_DATA), arrayOf<Object>(LocaleData.RESOURCE_NAMES, RESOURCE_NAMES_DATA))
    }
}

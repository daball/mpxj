/*
 * file:       LocaleData_it.java
 * author:     Elio Zoggia
 *             Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       15/11/2005
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

import net.sf.mpxj.CodePage
import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.DateOrder
import net.sf.mpxj.ProjectDateFormat
import net.sf.mpxj.ProjectTimeFormat

/**
 * This class defines the Italian translation of resource required by MPX files.
 */
class LocaleData_it : ListResourceBundle() {
    /**
     * {@inheritDoc}
     */
    val contents: Array<Array<Object>>
        @Override get() = RESOURCE_DATA

    companion object {

        private val TIME_UNITS_ARRAY_DATA = arrayOf(arrayOf("r"), arrayOf("o"), arrayOf("g"), arrayOf("s"), arrayOf("m"), arrayOf("a"), arrayOf("%"), arrayOf("tr"), arrayOf("to"), arrayOf("tg"), arrayOf("ts"), arrayOf("tm"), arrayOf("ta"), arrayOf("t%"))
        private val TIME_UNITS_MAP_DATA = HashMap<String, Integer>()

        init {
            for (loop in TIME_UNITS_ARRAY_DATA.indices) {
                val value = Integer.valueOf(loop)
                for (name in TIME_UNITS_ARRAY_DATA[loop]) {
                    TIME_UNITS_MAP_DATA.put(name, value)
                }
            }
        }

        private val ACCRUE_TYPES_DATA = arrayOf("Inizio", // "Start",
                "Fine", // "End",
                "Proporzione" // "Prorated"
        )

        private val RELATION_TYPES_DATA = arrayOf("FF", //   "FF",
                "FS", //   "FS",
                "SF", //   "SF",
                "SS" //   "SS"
        )

        private val PRIORITY_TYPES_DATA = arrayOf("Il minimo", //   "Lowest",
                "Molto Basso", //   "Very Low",
                "Piu' Basso", //   "Lower",
                "Basso", //   "Low",
                "Medio", //   "Medium",
                "Alto", //   "High",
                "Piu' Alto", //   "Higher",
                "Molto Alto", //   "Very High",
                "Il maggiore", //   "Highest",
                "Non livellare" //   "Do Not Level"
        )

        private val CONSTRAINT_TYPES_DATA = arrayOf("Appena Possibile", //   "As Soon As Possible",
                "Piu' Tardi Possibile", //   "As Late As Possible",
                "Deve Cominciare il", //   "Must Start On",
                "Deve Finire il", //   "Must Finish On",
                "Non iniziare prima di", //   "Start No Earlier Than",
                "Non iniziare piu' tardi di", //   "Start No Later Than",
                "Finire non prima", //   "Finish No Earlier Than",
                "Finire entro" //   "Finish No Later Than"
        )

        private val TASK_NAMES_DATA = arrayOf<String>(//
                null, //
                "Nome", //   "Name",
                "WBS", //   "WBS",
                "Livello Esterno", //   "Outline Level",
                "Testo1", //   "Text1",
                "Testo2", //   "Text2",
                "Testo3", //   "Text3",
                "Testo4", //   "Text4",
                "Testo5", //   "Text5",
                "Testo6", //   "Text6",
                "Testo7", //   "Text7",
                "Testo8", //   "Text8",
                "Testo9", //   "Text9",
                "Testo10", //   "Text10",
                "Note", //   "Notes",
                "Contatti", //   "Contact",
                "Gruppo delle Risorse", null, null, null, //
                "Lavoro", //   "Work",
                "Lavoro Normale", //   "Baseline Work",
                "Lavoro Reale", //   "Actual Work",
                "Lavoro Restante", //   "Remaining Work",
                "Modifiche di Lavoro", //   "Work Variance",
                "% lavoro completo", null, null, null, null, //
                "Costo", //   "Cost",
                "Costo Pianificato", //   "Baseline Cost",
                "Costo Reale", //   "Actual Cost",
                "Costo Restante", //   "Remaining Cost",
                "Variazione di Costo", //   "Cost Variance",
                "Costo Fisso", //   "Fixed Cost",
                "Costo1", //   "Cost1",
                "Costo2", //   "Cost2",
                "Costo3", null, //
                "Durata", //   "Duration",
                "Durata Pianificata", //   "Baseline Duration",
                "Durata Reale", //   "Actual Duration",
                "Durata Restante", //   "Remaining Duration",
                "% Completa", //   "% Complete",
                "Variazione di Durata", //   "Duration Variance",
                "Durata1", //   "Duration1",
                "Durata2", //   "Duration2",
                "Durata3", null, //
                "Inizio", //   "Start",
                "Fine", //   "Finish",
                "Minimo Inizio", //   "Early Start",
                "Minima Fine", //   "Early Finish",
                "Inizio Massimo", //   "Late Start",
                "Fine Massimo", //   "Late Finish",
                "Inizio Pianificato", //   "Baseline Start",
                "Fine Pianificata", //   "Baseline Finish",
                "Inizio Reale", //   "Actual Start",
                "Fine Reale", //   "Actual Finish",
                "Inizio1", //   "Start1",
                "Fine1", //   "Finish1",
                "Inizio2", //   "Start2",
                "Fine2", //   "Finish2",
                "Inizio3", //   "Start3",
                "Fine3", //   "Finish3",
                "Margine di Inizio", //   "Start Variance",
                "Margine di Fine", //   "Finish Variance",
                "Data Vincolante", null, //
                "Predecessori", //   "Predecessors",
                "Successori", //   "Successors",
                "Nomi Risorse", //   "Resource Names",
                "Iniziali Risourse", //   "Resource Initials",
                "ID Unico dei Predecessori", //   "Unique ID Predecessors",
                "ID Unique dei Successori", null, null, null, null, //
                "Fissato", //   "Fixed",
                "Jalon", //   "Milestone",
                "Critico", //   "Critical",
                "Marcato", //   "Marked",
                "Rollup", //   "Rollup",
                "BCWS", //   "BCWS",
                "BCWP", //   "BCWP",
                "SV", //   "SV",
                "CV", null, //
                "ID", //   "ID",
                "Tipo di Vincolo", //   "Constraint Type",
                "Ritardo", //   "Delay",
                "Margine Libero", //   "Free Slack",
                "Margine Totale", //   "Total Slack",
                "Priorita'", //   "Priority",
                "File Subprogetto", //   "Subproject File",
                "Progetto", //   "Project",
                "ID Unico", //   "Unique ID",
                "Numero Esterno", null, null, null, null, null, null, null, null, null, null, //
                "Indicatore1", //   "Flag1",
                "Indicatore2", //   "Flag2",
                "Indicatore3", //   "Flag3",
                "Indicatore4", //   "Flag4",
                "Indicatore5", //   "Flag5",
                "Indicatore6", //   "Flag6",
                "Indicatore7", //   "Flag7",
                "Indicatore8", //   "Flag8",
                "Indicatore9", //   "Flag9",
                "Indicatore10", //   "Flag10",
                "Indice", //   "Summary",
                "Oggetto", //   "Objects",
                "Campi Collegati", //   "Linked Fields",
                "Nascondi Barra", null, //
                "Creato", //   "Created",
                "Inizio4", //   "Start4",
                "Fine4", //   "Finish4",
                "Inizio5", //   "Start5",
                "Fine5", null, null, null, null, null, //
                "Confermato", //   "Confirmed",
                "Necessario Aggiornamento", null, null, null, //
                "Numero1", //   "Number1",
                "Numero2", //   "Number2",
                "Numero3", //   "Number3",
                "Numero4", //   "Number4",
                "Numero5", null, null, null, null, null, //
                "Stop", //   "Stop",
                "Continuare non prima del", //   "Resume No Earlier Than",
                "Continuare" //   "Resume"
        )//   "Resource Group",
        //
        //
        //   "% Work Complete",
        //
        //
        //
        //   "Cost3",
        //   "Duration3",
        //   "Constraint Date",
        //   "Unique ID Successors",
        //
        //
        //
        //   "CV",
        //   "Outline Number",
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //   "Hide Bar",
        //   "Finish5",
        //
        //
        //
        //
        //   "Update Needed",
        //
        //
        //   "Number5",
        //
        //
        //
        //

        private val RESOURCE_NAMES_DATA = arrayOf<String>(null, //
                "Nome", //   "Name",
                "Iniziali", //   "Initials",
                "Gruppo", //   "Group",
                "Codice", //   "Code",
                "Testo1", //   "Text1",
                "Testo2", //   "Text2",
                "Testo3", //   "Text3",
                "Testo4", //   "Text4",
                "Testo5", //   "Text5",
                "Note", //   "Notes",
                "Indirizzo e-mail", null, null, null, null, null, null, null, null, //
                "Lavoro", //   "Work",
                "Lavoro Pianificato", //   "Baseline Work",
                "Lavoro Reale", //   "Actual Work",
                "Lavoro Restante", //   "Remaining Work",
                "Ore sup.", //   "Overtime Work",
                "Variazione di Lavoro", //   "Work Variance",
                "% Lavoro completato", null, null, null, //
                "Costo", //   "Cost",
                "Costo Pianificato", //   "Baseline Cost",
                "Costo Reale", //   "Actual Cost",
                "Costo Restante", //   "Remaining Cost",
                "Variazione di Costo", null, null, null, null, null, //
                "ID", //   "ID",
                "Unita' Massime", //   "Max Units",
                "Tasse Standard", //   "Standard Rate",
                "Tasse ore sup.", //   "Overtime Rate",
                "Costo per Utilizzo", //   "Cost Per Use",
                "Aumentare a", //   "Accrue At",
                "Sovra Assegnato", //   "Overallocated",
                "Pointe", //   "Peak",
                "Calendario di Base", //   "Base Calendar",
                "ID Unico", //   "Unique ID",
                "Oggetti", //   "Objects",
                "Campi Collegati")//   "Email Address",
        //
        //
        //
        //
        //
        //
        //
        //   "% Work Complete",
        //
        //
        //   "Cost Variance",
        //
        //
        //
        //   "Linked Fields",

        private val RESOURCE_DATA = arrayOf<Array<Object>>(arrayOf<Object>(LocaleData.FILE_DELIMITER, ";"), arrayOf<Object>(LocaleData.PROGRAM_NAME, "Microsoft Project for Windows"), arrayOf<Object>(LocaleData.FILE_VERSION, "4.0"), arrayOf<Object>(LocaleData.CODE_PAGE, CodePage.ANSI),

                arrayOf<Object>(LocaleData.CURRENCY_SYMBOL, ""), arrayOf<Object>(LocaleData.CURRENCY_SYMBOL_POSITION, CurrencySymbolPosition.BEFORE), arrayOf<Object>(LocaleData.CURRENCY_DIGITS, Integer.valueOf(2)), arrayOf<Object>(LocaleData.CURRENCY_THOUSANDS_SEPARATOR, "."), arrayOf<Object>(LocaleData.CURRENCY_DECIMAL_SEPARATOR, ","),

                arrayOf<Object>(LocaleData.DATE_ORDER, DateOrder.DMY), arrayOf<Object>(LocaleData.TIME_FORMAT, ProjectTimeFormat.TWENTY_FOUR_HOUR), arrayOf<Object>(LocaleData.DATE_SEPARATOR, "/"), arrayOf<Object>(LocaleData.TIME_SEPARATOR, ":"), arrayOf<Object>(LocaleData.AM_TEXT, ""), arrayOf<Object>(LocaleData.PM_TEXT, ""), arrayOf<Object>(LocaleData.DATE_FORMAT, ProjectDateFormat.DD_MM_YYYY), arrayOf<Object>(LocaleData.BAR_TEXT_DATE_FORMAT, Integer.valueOf(0)), arrayOf<Object>(LocaleData.NA, "NA"),

                arrayOf<Object>(LocaleData.YES, "Si"), arrayOf<Object>(LocaleData.NO, "No"),

                arrayOf<Object>(LocaleData.TIME_UNITS_ARRAY, TIME_UNITS_ARRAY_DATA), arrayOf<Object>(LocaleData.TIME_UNITS_MAP, TIME_UNITS_MAP_DATA),

                arrayOf<Object>(LocaleData.ACCRUE_TYPES, ACCRUE_TYPES_DATA), arrayOf<Object>(LocaleData.RELATION_TYPES, RELATION_TYPES_DATA), arrayOf<Object>(LocaleData.PRIORITY_TYPES, PRIORITY_TYPES_DATA), arrayOf<Object>(LocaleData.CONSTRAINT_TYPES, CONSTRAINT_TYPES_DATA),

                arrayOf<Object>(LocaleData.TASK_NAMES, TASK_NAMES_DATA), arrayOf<Object>(LocaleData.RESOURCE_NAMES, RESOURCE_NAMES_DATA))
    }
}

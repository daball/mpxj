/*
 * file:       LocaleData_es.java
 * author:     Agust\u00EDn Bart\u00F3
 *             Jon Iles
 * copyright:  (c) Packwood Software 2004
 * date:       09/10/2008
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
 * This class defines the Spanish resources required by MPX files.
 */
class LocaleData_es : ListResourceBundle() {
    /**
     * {@inheritDoc}
     */
    val contents: Array<Array<Object>>
        @Override get() = RESOURCE_DATA

    companion object {

        private val TIME_UNITS_ARRAY_DATA = arrayOf(arrayOf("m"), // Minutes - Minutos
                arrayOf("h"), // Hours - Horas
                arrayOf("d"), // Days - D\u00ED\u00ADas
                arrayOf("s"), // Weeks - Semanas
                arrayOf("ms"), // Months - Meses
                arrayOf("a"), // Years - A\u00F1os
                arrayOf("%"), // Percent - Porcentaje
                arrayOf("em"), // Elapsed Minutes - Minutos Transcurridos
                arrayOf("eh"), // Elapsed Hours - Horas Transcurridos
                arrayOf("ed"), // Elapsed Days - D\u00C3\u00ADas Transcurridos
                arrayOf("es"), // Elapsed Weeks - Semanas Transcurridas
                arrayOf("ems"), // Elapsed Months - Meses Transcurridos
                arrayOf("ea"), // Elapsed Years - A\u00F1os Transcurridos
                arrayOf("e%"))// Elapsed Percent - Porcentaje Transcurridos

        private val TIME_UNITS_MAP_DATA = HashMap<String, Integer>()

        init {
            for (loop in TIME_UNITS_ARRAY_DATA.indices) {
                val value = Integer.valueOf(loop)
                for (name in TIME_UNITS_ARRAY_DATA[loop]) {
                    TIME_UNITS_MAP_DATA.put(name, value)
                }
            }
        }

        private val ACCRUE_TYPES_DATA = arrayOf("Comienzo", // Start
                "Fin", // End
                "Prorrateo" // Prorated
        )

        private val RELATION_TYPES_DATA = arrayOf("FF", // FF - Fin a fin (FF)
                "FC", // FS - Fin a comienzo (FC)
                "CF", // SF - Comienzo a fin (CF)
                "CC" // SS - Comienzo a comienzo (CC)
        )

        private val PRIORITY_TYPES_DATA = arrayOf("M\u00ED\u00ADnima", // Lowest
                "Muy Baja", // Very Low
                "M\u00E1s Baja", // Lower
                "Baja", // Low
                "Media", // Medium
                "Alta", // High
                "M\u00E1s Alta", // Higher
                "Muy Alta", // Very High
                "M\u00E1xima", // Highest
                "No Redistribuir" // Do Not Level
        )

        private val CONSTRAINT_TYPES_DATA = arrayOf("Lo antes posible", // As Soon As Possible
                "Lo m\u00E1s tarde posible", // As Late As Possible
                "Debe comenzar el", // Must Start On
                "Debe finalizar el", // Must Finish On
                "No comenzar antes del", // Start No Earlier Than
                "No comenzar despu\u00E9s del", // Start No Later Than
                "No finalizar antes del", // Finish No Earlier Than
                "No finalizar despu\u00E9s del" // Finish No Later Than
        )

        // TODO Complete TASK_NAMES_DATA translation
        private val TASK_NAMES_DATA = arrayOf<String>(// ???
                null, // ???
                "Nombre", // Name
                "WBS", // TODO Translate "WBS"
                "Nivel de Esquema", // Outline Level
                "Texto1", // Text1
                "Texto2", // Text2
                "Texto3", // Text3
                "Texto4", // Text4
                "Texto5", // Text5
                "Texto6", // Text6
                "Texto7", // Text7
                "Texto8", // Text8
                "Texto9", // Text9
                "Texto10", // Text10
                "Notas", // Notes
                "Contacto", // Contact
                "Grupo de Recursos", null, null, null, // ???
                "Trabajo", // Work
                "L\u00EDnea de Base de Trabajo", // Baseline Work
                "Trabajo Real", // Actual Work
                "Trabajo Restante", // Remaining Work
                "Variaci\u00F3n de Trabajo", // Work Variance
                "% Trabajo Completado", null, null, null, null, // ???
                "Costo", // Cost
                "L\u00ED\u00ADnea de Base de Costo", // Baseline Cost
                "Costo Real", // Actual Cost
                "Costo Restante", // Remaining Cost
                "Variaci\u00F3n de Costo", // Cost Variance
                "Costo Fijo", // Fixed Cost
                "Costo1", // Cost1
                "Costo2", // Cost2
                "Costo3", null, // ???
                "Duraci\u00F3n", // Duration
                "L\u00EDnea Base de Duraci\u00F3n", // Baseline Duration
                "Duraci\u00F3n Real", // Actual Duration
                "Duraci\u00F3n Restante", // Remaining Duration
                "% Completado", // % Complete
                "Variaci\u00F3n de Duraci\u00F3n", // Duration Variance
                "Duraci\u00F3n1", // Duration1
                "Duraci\u00F3n2", // Duration2
                "Duraci\u00F3n3", null, // ???
                "Comienzo", // Start
                "Fin", // Finish
                "Comienzo Temprano", // Early Start
                "Fin Temprano", // Early Finish
                "Comienzo Tard\u00ED\u00ADo", // Late Start
                "Fin Tard\u00C3\u00ADo", // Late Finish
                "L\u00ED\u00ADnea de Base de Comienzo", // Baseline Start
                "L\u00EDnea de Base de Fin", // Baseline Finish
                "Comienzo Real", // Actual Start
                "Fin Real", // Actual Finish
                "Comienzo1", // Start1
                "Fin1", // Finish1
                "Comienzo2", // Start2
                "Fin2", // Finish2
                "Comienzo3", // Start3
                "Fin3", // Finish3
                "Variaci\u00F3n de Comienzo", // Start Variance
                "Variaci\u00F3n de Fin", // Finish Variance
                "Fecha de Restricci\u00F3n", null, // ???
                "Predecesoras", // Predecessors
                "Sucesoras", // Successors
                "Nombres de Recursos", // Resource Names
                "Iniciales de Recursos", // Resource Initials
                "Predecesoras de ID \u00DAnico", // Unique ID Predecessors
                "Sucesoras de ID \u00DAnico", null, null, null, null, // ???
                "Fijo", // Fixed
                "Hito", // Milestone
                "Cr\u00ED\u00ADtico", // Critical
                "Marked", // TODO Translate "Marked"
                "Rollup", // TODO Translate "Rollup"
                "BCWS", // TODO Translate "BCWS"
                "BCWP", // TODO Translate "BCWP"
                "SV", // TODO Translate "SV"
                "CV", null, // ???
                "ID", // TODO Translate "ID"
                "Tipo de Restricci\u00F3n", // Constraint Type
                "Demora", // Delay
                "Free Slack", // TODO Translate "Free Slack"
                "Total Slack", // TODO Translate "Total Slack"
                "Prioridad", // Priority
                "Subproject File", // TODO Translate "Subproject File"
                "Proyecto", // Project
                "ID \u00DAnico", // Unique ID
                "N\u00FAmero de Esquema", null, null, null, null, null, null, null, null, null, null, // ???
                "Indicador1", // Flag1
                "Indicador2", // Flag2
                "Indicador3", // Flag3
                "Indicador4", // Flag4
                "Indicador5", // Flag5
                "Indicador6", // Flag6
                "Indicador7", // Flag7
                "Indicador8", // Flag8
                "Indicador9", // Flag9
                "Indicador10", // Flag10
                "Summary", // TODO Translate "Summary"
                "Objetos", // Objects
                "Campos Enlazados", // Linked Fields
                "Ocultar Barra", null, // ???
                "Creada", // Created
                "Comienzo4", // Start4
                "Fin4", // Finish4
                "Comienzo5", // Start5
                "Fin5", null, null, null, null, null, // ???
                "Confirmed", // TODO Translate "Confirmed"
                "Update Needed", null, null, null, // ???
                "N\u00FAmero1", // Number1
                "N\u00FAmero2", // Number2
                "N\u00FAmero3", // Number3
                "N\u00FAmero4", // Number4
                "N\u00FAmero5", null, null, null, null, null, // ???
                "Stop", // TODO Translate "Stop"
                "Resume No Earlier Than", // TODO Translate "Resume No Earlier Than"
                "Resume" // TODO Translate "Resume"
        )// Resource Group
        // ???
        // ???
        // % Work Complete
        // ???
        // ???
        // ???
        // Cost3
        // Duration3
        // Constraint Date
        // Unique ID Successors
        // ???
        // ???
        // ???
        // TODO Translate "CV"
        // Outline Number
        // ???
        // ???
        // ???
        // ???
        // ???
        // ???
        // ???
        // ???
        // ???
        // Hide Bar
        // Finish5
        // ???
        // ???
        // ???
        // ???
        // TODO Translate "Update Needed"
        // ???
        // ???
        // Number5
        // ???
        // ???
        // ???
        // ???

        // TODO Complete RESOURCE_NAMES_DATA translation
        private val RESOURCE_NAMES_DATA = arrayOf<String>(null, // ???
                "Nombre", // Name
                "Iniciales", // Initials
                "Grupo", // Group
                "C\u00F3digo", // Code
                "Texto1", // Text1
                "Texto2", // Text2
                "Texto3", // Text3
                "Texto4", // Text4
                "Texto5", // Text5
                "Notas", // Notes
                "Correo electr\u00F3nico", null, null, null, null, null, null, null, null, // ???
                "Trabajo", // Work
                "L\u00EDnea de Base de Trabajo", // Baseline Work
                "Trabajo Real", // Actual Work
                "Trabajo Restante", // Remaining Work
                "Overtime Work", // TODO Translate "Overtime Work"
                "Variaci\u00F3n de Trabajo", // Work Variance
                "% Trabajo Completado", null, null, null, // ???
                "Costo", // Cost
                "L\u00ED\u00ADnea de Base de Costo", // Baseline Cost
                "Costo Real", // Actual Cost
                "Costo Restante", // Remaining Cost
                "Variaci\u00F3n de Costo", null, null, null, null, null, // ???
                "ID", // TODO Translate "ID"
                "Max Units", // TODO Translate "Max Units"
                "Standard Rate", // TODO Translate "Standard Rate"
                "Overtime Rate", // TODO Translate "Overtime Rate"
                "Costo Por Uso", // Cost Per Use
                "Acumulaci\u00F3n de costos", // Accrue At
                "Sobreasignado", // Overallocated
                "Pico", // Peak
                "Calendario Base", // Base Calendar
                "ID \u00DAnico", // Unique ID
                "Objetos", // Objects
                "Campos Enlazados")// Email Address
        // ???
        // ???
        // ???
        // ???
        // ???
        // ???
        // ???
        // % Work Complete
        // ???
        // ???
        // Cost Variance
        // ???
        // ???
        // ???
        // Linked Fields

        private val RESOURCE_DATA = arrayOf<Array<Object>>(arrayOf<Object>(LocaleData.FILE_DELIMITER, ","), arrayOf<Object>(LocaleData.PROGRAM_NAME, "Microsoft Project for Windows"), arrayOf<Object>(LocaleData.FILE_VERSION, "4.0"), arrayOf<Object>(LocaleData.CODE_PAGE, CodePage.ANSI),

                arrayOf<Object>(LocaleData.CURRENCY_SYMBOL, "$"), arrayOf<Object>(LocaleData.CURRENCY_SYMBOL_POSITION, CurrencySymbolPosition.BEFORE), arrayOf<Object>(LocaleData.CURRENCY_DIGITS, Integer.valueOf(2)), arrayOf<Object>(LocaleData.CURRENCY_THOUSANDS_SEPARATOR, "."), arrayOf<Object>(LocaleData.CURRENCY_DECIMAL_SEPARATOR, ","),

                arrayOf<Object>(LocaleData.DATE_ORDER, DateOrder.DMY), arrayOf<Object>(LocaleData.TIME_FORMAT, ProjectTimeFormat.TWENTY_FOUR_HOUR), arrayOf<Object>(LocaleData.DEFAULT_START_TIME, Integer.valueOf(480)), arrayOf<Object>(LocaleData.DATE_SEPARATOR, "/"), arrayOf<Object>(LocaleData.TIME_SEPARATOR, ":"), arrayOf<Object>(LocaleData.AM_TEXT, "am"), arrayOf<Object>(LocaleData.PM_TEXT, "pm"), arrayOf<Object>(LocaleData.DATE_FORMAT, ProjectDateFormat.DD_MM_YYYY), arrayOf<Object>(LocaleData.BAR_TEXT_DATE_FORMAT, Integer.valueOf(0)), arrayOf<Object>(LocaleData.NA, "NA"),

                arrayOf<Object>(LocaleData.YES, "S\u00ED\u00AD"), arrayOf<Object>(LocaleData.NO, "No"),

                arrayOf<Object>(LocaleData.TIME_UNITS_ARRAY, TIME_UNITS_ARRAY_DATA), arrayOf<Object>(LocaleData.TIME_UNITS_MAP, TIME_UNITS_MAP_DATA),

                arrayOf<Object>(LocaleData.ACCRUE_TYPES, ACCRUE_TYPES_DATA), arrayOf<Object>(LocaleData.RELATION_TYPES, RELATION_TYPES_DATA), arrayOf<Object>(LocaleData.PRIORITY_TYPES, PRIORITY_TYPES_DATA), arrayOf<Object>(LocaleData.CONSTRAINT_TYPES, CONSTRAINT_TYPES_DATA),

                arrayOf<Object>(LocaleData.TASK_NAMES, TASK_NAMES_DATA), arrayOf<Object>(LocaleData.RESOURCE_NAMES, RESOURCE_NAMES_DATA))
    }
}

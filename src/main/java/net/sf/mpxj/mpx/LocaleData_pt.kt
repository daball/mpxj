/*
 * file:       LocaleData_pt.java
 * author:     Cl\u00E1udio Engelsdorff Avila
 *             Jon Iles
 * copyright:  (c) Packwood Software 2004
 * date:       16/08/2004
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
 * This class defines the Portuguese translation of resource required by MPX files.
 */
class LocaleData_pt : ListResourceBundle() {
    /**
     * {@inheritDoc}
     */
    val contents: Array<Array<Object>>
        @Override get() = RESOURCE_DATA

    companion object {

        private val TIME_UNITS_ARRAY_DATA = arrayOf(arrayOf("m"), arrayOf("h"), arrayOf("d"), arrayOf("s"), arrayOf("mes"), arrayOf("a"), arrayOf("%"), arrayOf("em"), arrayOf("eh"), arrayOf("ed"), arrayOf("es"), arrayOf("emes"), arrayOf("ea"), arrayOf("e%"))
        private val TIME_UNITS_MAP_DATA = HashMap<String, Integer>()

        init {
            for (loop in TIME_UNITS_ARRAY_DATA.indices) {
                val value = Integer.valueOf(loop)
                for (name in TIME_UNITS_ARRAY_DATA[loop]) {
                    TIME_UNITS_MAP_DATA.put(name, value)
                }
            }
        }

        private val ACCRUE_TYPES_DATA = arrayOf("Inicio", //   "Start",
                "Fim", //   "End",
                "" //   "Prorated"
        )

        private val RELATION_TYPES_DATA = arrayOf("TT", //   "FF",
                "TI", //   "FS",
                "IT", //   "SF",
                "II" //   "SS"
        )

        private val PRIORITY_TYPES_DATA = arrayOf("Muito Baixa", //   "Lowest",
                "Muito Baixo", //   "Very Low",
                "Lower", //   "Lower",
                "Baixo", //   "Low",
                "M\u00E9dia", //   "Medium",
                "Alta", //   "High",
                "Higher", //   "Higher",
                "Muito Alta", //   "Very High",
                "Alt\u00EDssimo", //   "Highest",
                "N\u00E3o Nivelar" //   "Do Not Level"
        )

        private val CONSTRAINT_TYPES_DATA = arrayOf("O Mais Breve Poss\u00EDvel", //   "As Soon As Possible",
                "O Mais Tarde Poss\u00EDvel", //   "As Late As Possible",
                "Deve Iniciar Em", //   "Must Start On",
                "Deve Terminar Em", //   "Must Finish On",
                "N\u00E3o Iniciar Antes De", //   "Start No Earlier Than",
                "N\u00E3o Iniciar Depois De", //   "Start No Later Than",
                "N\u00E3o Terminar Antes De", //   "Finish No Earlier Than",
                "N\u00E3o Terminar Depois De" //   "Finish No Later Than"
        )

        private val TASK_NAMES_DATA = arrayOf<String>(//
                null, //
                "Nome", //   "Name",
                "WBS", //   "WBS",
                "N\u00EDvel Externo", //   "Outline Level",
                "Texto1", //   "Text1",
                "Texto2", //   "Text2",
                "Texto3", //   "Text3",
                "Texto4", //   "Text4",
                "Texto5", //   "Text5",
                "Texto6", //   "Text6",
                "Texto7", //   "Text7",
                "Texto8", //   "Text8",
                "Texto9", //   "Text9",
                "Texto10", //   "Text10",
                "Anota\u00E7\u00F5es", //   "Notes",
                "Contacto", //   "Contact",
                "Grupo de Recursos", null, null, null, //
                "Trabalho", //   "Work",
                "Linha Base de Trabalho", //   "Baseline Work",
                "Trabalho Realizado", //   "Actual Work",
                "Trabalho Restante", //   "Remaining Work",
                "Varia\u00E7\u00E3o Trabalho", //   "Work Variance",
                "% Trabalho Completo", null, null, null, null, //
                "Custo", //   "Cost",
                "Linha de Base de Custo", //   "Baseline Cost",
                "Custo Real", //   "Actual Cost",
                "Custo Restante", //   "Remaining Cost",
                "Varia\u00E7\u00E3o de Custo", //   "Cost Variance",
                "Custo Fixo", //   "Fixed Cost",
                "Custo1", //   "Cost1",
                "Custo2", //   "Cost2",
                "Custo3", null, //
                "Dura\u00E7\u00E3o", //   "Duration",
                "Linha Base de Dura\u00E7\u00E3o", //   "Baseline Duration",
                "Dura\u00E7\u00E3o Real", //   "Actual Duration",
                "Dura\u00E7\u00E3o Restante", //   "Remaining Duration",
                "% Completo", //   "% Complete",
                "Varia\u00E7\u00E3o de Dura\u00E7\u00E3o", //   "Duration Variance",
                "Dura\u00E7\u00E3o1", //   "Duration1",
                "Dura\u00E7\u00E3o2", //   "Duration2",
                "Dura\u00E7\u00E3o3", null, //
                "Inicio", //   "Start",
                "Fim", //   "Finish",
                "Inicio Cedo", //   "Early Start",
                "Fim Cedo", //   "Early Finish",
                "Inicio Tardio", //   "Late Start",
                "Fim Tardio", //   "Late Finish",
                "Inicio Linha de Base", //   "Baseline Start",
                "Fim Linha de Base", //   "Baseline Finish",
                "Inicio Real", //   "Actual Start",
                "Fim Real", //   "Actual Finish",
                "Inicio1", //   "Start1",
                "Fim1", //   "Finish1",
                "Inicio2", //   "Start2",
                "Fim2", //   "Finish2",
                "Inicio3", //   "Start3",
                "Fim3", //   "Finish3",
                "Varia\u00E7\u00E3o Inicio", //   "Start Variance",
                "Varia\u00E7\u00E3o Fim", //   "Finish Variance",
                "Limita\u00E7\u00E3o de Data", null, //
                "Predecessores", //   "Predecessors",
                "Sucessores", //   "Successors",
                "Nome dos Recursos", //   "Resource Names",
                "Iniciais dos Recursos", //   "Resource Initials",
                "ID Unico dos Predecessores", //   "Unique ID Predecessors",
                "ID Unico dos Sucessores", null, null, null, null, //
                "Fixado", //   "Fixed",
                "Milestone", //   "Milestone",
                "Cr\u00EDtico", //   "Critical",
                "Marcado", //   "Marked",
                "Rollup", //   "Rollup",
                "BCWS", //   "BCWS",
                "BCWP", //   "BCWP",
                "SV", //   "SV",
                "CV", null, //
                "ID", //   "ID",
                "Tipo de Limita\u00E7\u00E3o", //   "Constraint Type",
                "Espera", //   "Delay",
                "Folga Livre", //   "Free Slack",
                "Total Folga", //   "Total Slack",
                "Prioridade", //   "Priority",
                "Arquivo Subprojeto", //   "Subproject File",
                "Projeto", //   "Project",
                "ID Unico", //   "Unique ID",
                "N\u00FAmero Externo", null, null, null, null, null, null, null, null, null, null, //
                "Flag1", //   "Flag1",
                "Flag2", //   "Flag2",
                "Flag3", //   "Flag3",
                "Flag4", //   "Flag4",
                "Flag5", //   "Flag5",
                "Flag6", //   "Flag6",
                "Flag7", //   "Flag7",
                "Flag8", //   "Flag8",
                "Flag9", //   "Flag9",
                "Flag10", //   "Flag10",
                "Sum\u00E1rio", //   "Summary",
                "Objetos", //   "Objects",
                "Campos Ligados", //   "Linked Fields",
                "Esconder Barra", null, //
                "Criado", //   "Created",
                "Inicio4", //   "Start4",
                "Fim4", //   "Finish4",
                "Inicio5", //   "Start5",
                "Fim5", null, null, null, null, null, //
                "Confirmado", //   "Confirmed",
                "Atualiza\u00E7\u00E3o Necess\u00E1ria", null, null, null, //
                "N\u00FAmero1", //   "Number1",
                "N\u00FAmero2", //   "Number2",
                "N\u00FAmero3", //   "Number3",
                "N\u00FAmero4", //   "Number4",
                "N\u00FAmero5", null, null, null, null, null, //
                "Pare", //   "Stop",
                "N\u00E3o Continuar Antes de", //   "Resume No Earlier Than",
                "Continuar" //   "Resume"
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
                "Iniciais", //   "Initials",
                "Grupo", //   "Group",
                "C\u00F3digo", //   "Code",
                "Texto1", //   "Text1",
                "Texto2", //   "Text2",
                "Texto3", //   "Text3",
                "Texto4", //   "Text4",
                "Texto5", //   "Text5",
                "Anota\u00E7\u00F5es", //   "Notes",
                "Endere\u00E7o Email", null, null, null, null, null, null, null, null, //
                "Trabaho", //   "Work",
                "Linha Base de Trabalho", //   "Baseline Work",
                "Trabalho Real", //   "Actual Work",
                "Trabalho Restante", //   "Remaining Work",
                "Trabalho Extra", //   "Overtime Work",
                "Varia\u00E7\u00E3o Trabalho", //   "Work Variance",
                "% Trabalho Completo", null, null, null, //
                "Custo", //   "Cost",
                "Linha Base de Custo", //   "Baseline Cost",
                "Custo Real", //   "Actual Cost",
                "Custo Restante", //   "Remaining Cost",
                "Varia\u00E7\u00E3o de Custo", null, null, null, null, null, //
                "ID", //   "ID",
                "M\u00E1ximo de Unidades", //   "Max Units",
                "Taxa Padr\u00E3o", //   "Standard Rate",
                "Taxa de Tempo Extra", //   "Overtime Rate",
                "Custo por Uso", //   "Cost Per Use",
                "Resulte em", //   "Accrue At",
                "Sobrecarregado", //   "Overallocated",
                "Pico", //   "Peak",
                "Calend\u00E1rio Base", //   "Base Calendar",
                "ID Unico", //   "Unique ID",
                "Objetos", //   "Objects",
                "Campos Ligados")//   "Email Address",
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

        private val RESOURCE_DATA = arrayOf<Array<Object>>(arrayOf<Object>(LocaleData.FILE_DELIMITER, ";"),

                arrayOf<Object>(LocaleData.CURRENCY_SYMBOL, ""), arrayOf<Object>(LocaleData.CURRENCY_SYMBOL_POSITION, CurrencySymbolPosition.BEFORE), arrayOf<Object>(LocaleData.CURRENCY_DIGITS, Integer.valueOf(2)), arrayOf<Object>(LocaleData.CURRENCY_THOUSANDS_SEPARATOR, "."), arrayOf<Object>(LocaleData.CURRENCY_DECIMAL_SEPARATOR, ","),

                arrayOf<Object>(LocaleData.DATE_ORDER, DateOrder.DMY), arrayOf<Object>(LocaleData.TIME_FORMAT, ProjectTimeFormat.TWENTY_FOUR_HOUR), arrayOf<Object>(LocaleData.DATE_SEPARATOR, "/"), arrayOf<Object>(LocaleData.TIME_SEPARATOR, ":"), arrayOf<Object>(LocaleData.AM_TEXT, ""), arrayOf<Object>(LocaleData.PM_TEXT, ""), arrayOf<Object>(LocaleData.DATE_FORMAT, ProjectDateFormat.DD_MM_YYYY), arrayOf<Object>(LocaleData.BAR_TEXT_DATE_FORMAT, Integer.valueOf(0)), arrayOf<Object>(LocaleData.NA, "NA"),

                arrayOf<Object>(LocaleData.YES, "Sim"), arrayOf<Object>(LocaleData.NO, "N\u00E3o"),

                arrayOf<Object>(LocaleData.TIME_UNITS_ARRAY, TIME_UNITS_ARRAY_DATA), arrayOf<Object>(LocaleData.TIME_UNITS_MAP, TIME_UNITS_MAP_DATA),

                arrayOf<Object>(LocaleData.ACCRUE_TYPES, ACCRUE_TYPES_DATA), arrayOf<Object>(LocaleData.RELATION_TYPES, RELATION_TYPES_DATA), arrayOf<Object>(LocaleData.PRIORITY_TYPES, PRIORITY_TYPES_DATA), arrayOf<Object>(LocaleData.CONSTRAINT_TYPES, CONSTRAINT_TYPES_DATA),

                arrayOf<Object>(LocaleData.TASK_NAMES, TASK_NAMES_DATA), arrayOf<Object>(LocaleData.RESOURCE_NAMES, RESOURCE_NAMES_DATA))
    }
}

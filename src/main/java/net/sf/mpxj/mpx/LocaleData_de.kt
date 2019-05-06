/*
 * file:       LocaleData_de.java
 * author:     Harald Hett
 *             Jon Iles
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
import net.sf.mpxj.ProjectDateFormat
import net.sf.mpxj.ProjectTimeFormat

/**
 * This class defines the German translation of resource required by MPX files.
 */
class LocaleData_de : ListResourceBundle() {
    /**
     * {@inheritDoc}
     */
    val contents: Array<Array<Object>>
        @Override get() = RESOURCE_DATA

    companion object {

        private val TIME_UNITS_ARRAY_DATA = arrayOf(arrayOf("m"), arrayOf("h"), arrayOf("t"), arrayOf("w"), arrayOf("mon"), arrayOf("y"), arrayOf("%"), arrayOf("fm"), arrayOf("fh"), arrayOf("ft"), arrayOf("fw"), arrayOf("fmon"), arrayOf("fy"), arrayOf("f%"))
        private val TIME_UNITS_MAP_DATA = HashMap<String, Integer>()

        init {
            for (loop in TIME_UNITS_ARRAY_DATA.indices) {
                val value = Integer.valueOf(loop)
                for (name in TIME_UNITS_ARRAY_DATA[loop]) {
                    TIME_UNITS_MAP_DATA.put(name, value)
                }
            }
        }

        private val ACCRUE_TYPES_DATA = arrayOf("Anfang", "Ende", "Anteilig")

        private val RELATION_TYPES_DATA = arrayOf("EE", "EA", "AE", "AA")

        private val PRIORITY_TYPES_DATA = arrayOf("Am niedrigsten", "Sehr niedrig", "Niedriger", "Niedrig", "Mittel", "Hoch", "H\u00F6her", "Sehr hoch", "Am h\u00F6chsten", "Nicht abgleichen")

        private val CONSTRAINT_TYPES_DATA = arrayOf("So fr\u00FCh wie m\u00F6glich", "So sp\u00E4t wie m\u00F6glich", "Muss anfangen am", "Muss enden am", "Anfang nicht fr\u00FCher als", "Anfang nicht sp\u00E4ter als", "Ende nicht fr\u00FCher als", "Ende nicht sp\u00E4ter als")

        private val TASK_NAMES_DATA = arrayOf<String>(null, "Name", "PSP-Code", "Gliederungsebene", "Text1", "Text2", "Text3", "Text4", "Text5", "Text6", "Text7", "Text8", "Text9", "Text10", "Notizen", "Kontaktperson", "Ressourcengruppe", null, null, null, "Arbeit", "Geplante Arbeit", "Aktuelle Arbeit", "Verbleibende Arbeit", "Abweichung Arbeit", "% Arbeit abgeschlossen", null, null, null, null, "Kosten", "Geplante Kosten", "Aktuelle Kosten", "Verbleibende Kosten", "Abweichung Kosten", "Feste Kosten", "Kosten1", "Kosten2", "Kosten3", null, "Dauer", "Geplante Dauer", "Aktuelle Dauer", "Verbleibende Dauer", "% Abgeschlossen", "Abweichung Dauer", "Dauer1", "Dauer2", "Dauer3", null, "Anfang", "Ende", "Fr\u00FChester Anfang", "Fr\u00FChestes Ende", "Sp\u00E4tester Anfang", "Sp\u00E4testes Ende", "Geplanter Anfang", "Geplantes Ende", "Aktueller Anfang", "Aktuelles Ende", "Anfang1", "Ende1", "Anfang2", "Ende2", "Anfang3", "Ende3", "Abweichung Anfang", "Abweichung Ende", "Einschr\u00E4nkungstermin", null, "Vorg\u00E4nger", "Nachfolger", "Ressourcenamen", "Ressourcenk\u00FCrzel", "Einmalige Nr. f\u00FCr Vorg\u00E4nger", "Einmalige Nr. f\u00FCr Nachfolger", null, null, null, null, "Fest", "Meilenstein", "Kritisch", "Markiert", "Rollup", "SKAA", "SKBA", "PA", "KA", null, "Nr.", "Einschr\u00E4nkungsart", "Verz\u00F6gerung", "Freie Pufferzeit", "Gesamte Pufferzeit", "Priorit\u00E4t", "Teilprojektdatei", "Projekt", "Einmalige Nr.", "Gliederungsnummer", null, null, null, null, null, null, null, null, null, null, "Attribut1", "Attribut2", "Attribut3", "Attribut4", "Attribut5", "Attribut6", "Attribut7", "Attribut8", "Attribut9", "Attribut10", "Sammelvorgang", "Objekte", "Verkn\u00FCpfte Felder", "Balken ausblenden", null, "Erzeugt", "Anfang4", "Ende4", "Anfang5", "Ende5", null, null, null, null, null, "Best\u00E4tigt", "Aktualisierung erforderlich", null, null, null, "Zahl1", "Zahl2", "Zahl3", "Zahl4", "Zahl5", null, null, null, null, null, "Unterbrechungstermin", "Wiederaufnahme nicht fr\u00FCher als", "Wiederaufnahme")

        private val RESOURCE_NAMES_DATA = arrayOf<String>(null, "Name", "K\u00FCrzel", "Gruppe", "Code", "Text1", "Text2", "Text3", "Text4", "Text5", "Notizen", "E-Mail-Adresse", null, null, null, null, null, null, null, null, "Arbeit", "Geplante Arbeit", "Aktuelle Arbeit", "Verbleibende Arbeit", "\u00DCberstundenarbeit", "Abweichung Arbeit", "% Arbeit abgeschlossen", null, null, null, "Kosten", "Geplante Kosten", "Aktuelle Kosten", "Verbleibende Kosten", "Abweichung Kosten", null, null, null, null, null, "Nr.", "Max. Einheiten", "Standardsatz", "\u00DCberstundensatz", "Kosten pro Einsatz", "F\u00E4llig am", "\u00DCberlastet", "Spitze", "Basiskalender", "Einmalige Nr.", "Objekte", "Verkn\u00FCpfte Felder")

        private val RESOURCE_DATA = arrayOf<Array<Object>>(arrayOf<Object>(LocaleData.FILE_DELIMITER, ";"), arrayOf<Object>(LocaleData.FILE_VERSION, "4,0"),

                arrayOf<Object>(LocaleData.YES, "Ja"), arrayOf<Object>(LocaleData.NO, "Nein"),

                arrayOf<Object>(LocaleData.CURRENCY_SYMBOL, "\u20AC"), arrayOf<Object>(LocaleData.CURRENCY_SYMBOL_POSITION, CurrencySymbolPosition.AFTER_WITH_SPACE), arrayOf<Object>(LocaleData.CURRENCY_THOUSANDS_SEPARATOR, "."), arrayOf<Object>(LocaleData.CURRENCY_DECIMAL_SEPARATOR, ","),

                arrayOf<Object>(LocaleData.TIME_FORMAT, ProjectTimeFormat.TWENTY_FOUR_HOUR), arrayOf<Object>(LocaleData.DATE_SEPARATOR, "."), arrayOf<Object>(LocaleData.AM_TEXT, ""), arrayOf<Object>(LocaleData.PM_TEXT, ""), arrayOf<Object>(LocaleData.DATE_FORMAT, ProjectDateFormat.EEE_DD_MM_YY), arrayOf<Object>(LocaleData.NA, "NV"),

                arrayOf<Object>(LocaleData.TIME_UNITS_ARRAY, TIME_UNITS_ARRAY_DATA), arrayOf<Object>(LocaleData.TIME_UNITS_MAP, TIME_UNITS_MAP_DATA),

                arrayOf<Object>(LocaleData.ACCRUE_TYPES, ACCRUE_TYPES_DATA), arrayOf<Object>(LocaleData.RELATION_TYPES, RELATION_TYPES_DATA), arrayOf<Object>(LocaleData.PRIORITY_TYPES, PRIORITY_TYPES_DATA), arrayOf<Object>(LocaleData.CONSTRAINT_TYPES, CONSTRAINT_TYPES_DATA),

                arrayOf<Object>(LocaleData.TASK_NAMES, TASK_NAMES_DATA), arrayOf<Object>(LocaleData.RESOURCE_NAMES, RESOURCE_NAMES_DATA))
    }
}

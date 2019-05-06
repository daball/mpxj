/*
 * file:       LocaleUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Jan 23, 2006
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

import java.util.Locale

import net.sf.mpxj.CodePage
import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.DateOrder
import net.sf.mpxj.ProjectDateFormat
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.ProjectTimeFormat
import net.sf.mpxj.common.DateHelper

/**
 * This class contains methods used to configure the locale of an MPX file,
 * along with other common locale related methods.
 */
internal object LocaleUtility {

    /**
     * Retrieves an array containing the locales supported by MPXJ's
     * MPX functionality.
     *
     * @return array of supported locales
     */
    val supportedLocales: Array<Locale>
        get() = SUPPORTED_LOCALES

    /**
     * Array of locales supported by MPXJ's MPX functionality.
     */
    private val SUPPORTED_LOCALES = arrayOf<Locale>(Locale("EN"), Locale("DE"), Locale("FR"), Locale("IT"), Locale("PT"), Locale("SV"), Locale("ZH"), Locale("ES"), Locale("RU"))

    /**
     * This method is called when the locale of the parent file is updated.
     * It resets the locale specific currency attributes to the default values
     * for the new locale.
     *
     * @param properties project properties
     * @param locale new locale
     */
    fun setLocale(properties: ProjectProperties, locale: Locale) {
        properties.mpxDelimiter = LocaleData.getChar(locale, LocaleData.FILE_DELIMITER)
        properties.mpxProgramName = LocaleData.getString(locale, LocaleData.PROGRAM_NAME)
        properties.mpxCodePage = LocaleData.getObject(locale, LocaleData.CODE_PAGE) as CodePage

        properties.currencySymbol = LocaleData.getString(locale, LocaleData.CURRENCY_SYMBOL)
        properties.symbolPosition = LocaleData.getObject(locale, LocaleData.CURRENCY_SYMBOL_POSITION) as CurrencySymbolPosition
        properties.currencyDigits = LocaleData.getInteger(locale, LocaleData.CURRENCY_DIGITS)
        properties.thousandsSeparator = LocaleData.getChar(locale, LocaleData.CURRENCY_THOUSANDS_SEPARATOR)
        properties.decimalSeparator = LocaleData.getChar(locale, LocaleData.CURRENCY_DECIMAL_SEPARATOR)

        properties.dateOrder = LocaleData.getObject(locale, LocaleData.DATE_ORDER) as DateOrder
        properties.timeFormat = LocaleData.getObject(locale, LocaleData.TIME_FORMAT) as ProjectTimeFormat
        properties.defaultStartTime = DateHelper.getTimeFromMinutesPastMidnight(LocaleData.getInteger(locale, LocaleData.DEFAULT_START_TIME))
        properties.dateSeparator = LocaleData.getChar(locale, LocaleData.DATE_SEPARATOR)
        properties.timeSeparator = LocaleData.getChar(locale, LocaleData.TIME_SEPARATOR)
        properties.amText = LocaleData.getString(locale, LocaleData.AM_TEXT)
        properties.pmText = LocaleData.getString(locale, LocaleData.PM_TEXT)
        properties.dateFormat = LocaleData.getObject(locale, LocaleData.DATE_FORMAT) as ProjectDateFormat
        properties.barTextDateFormat = LocaleData.getObject(locale, LocaleData.DATE_FORMAT) as ProjectDateFormat
    }
}
/**
 * Constructor.
 */// Private constructor to prevent instantiation

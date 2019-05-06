/*
 * file:       PrimaveraPMFileWriter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2012
 * date:       2012-03-16
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

package net.sf.mpxj.primavera

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.EnumSet
import java.util.HashMap
import java.util.UUID

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.CustomField
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DataType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.FieldTypeClass
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.FieldTypeHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.primavera.schema.APIBusinessObjects
import net.sf.mpxj.primavera.schema.ActivityType
import net.sf.mpxj.primavera.schema.CalendarType
import net.sf.mpxj.primavera.schema.CalendarType.HolidayOrExceptions
import net.sf.mpxj.primavera.schema.CalendarType.HolidayOrExceptions.HolidayOrException
import net.sf.mpxj.primavera.schema.CalendarType.StandardWorkWeek
import net.sf.mpxj.primavera.schema.CalendarType.StandardWorkWeek.StandardWorkHours
import net.sf.mpxj.primavera.schema.CurrencyType
import net.sf.mpxj.primavera.schema.ObjectFactory
import net.sf.mpxj.primavera.schema.ProjectType
import net.sf.mpxj.primavera.schema.RelationshipType
import net.sf.mpxj.primavera.schema.ResourceAssignmentType
import net.sf.mpxj.primavera.schema.ResourceType
import net.sf.mpxj.primavera.schema.UDFAssignmentType
import net.sf.mpxj.primavera.schema.UDFTypeType
import net.sf.mpxj.primavera.schema.WBSType
import net.sf.mpxj.primavera.schema.WorkTimeType
import net.sf.mpxj.writer.AbstractProjectWriter

/**
 * This class creates a new MSPDI file from the contents of an ProjectFile
 * instance.
 */
class PrimaveraPMFileWriter : AbstractProjectWriter() {

    /**
     * Package-private accessor method used to retrieve the project file
     * currently being processed by this writer.
     *
     * @return project file instance
     */
    internal val projectFile: ProjectFile?
        get() = m_projectFile

    private var m_projectFile: ProjectFile? = null
    private var m_factory: ObjectFactory? = null
    private var m_apibo: APIBusinessObjects? = null
    private var m_project: ProjectType? = null
    private var m_wbsSequence: Int = 0
    private var m_relationshipObjectID: Int = 0
    /**
     * Retrieve the task field which will be used to populate the Activity ID attribute
     * in the PMXML file.
     *
     * @return TaskField instance
     */
    /**
     * Set the task field which will be used to populate the Activity ID attribute
     * in the PMXML file. Currently this defaults to TaskField.WBS. If you are
     * reading in a project from Primavera, typically the original Activity ID will
     * be in the Text1 field, so calling this method with TaskField.TEXT1 will write
     * the original Activity ID values in the PMXML file.
     *
     * @param field TaskField instance
     */
    var activityIdField: TaskField? = null
    /**
     * Retrieve the task field which will be used to populate the Activity Type attribute
     * in the PMXML file.
     *
     * @return TaskField instance
     */
    /**
     * Set the task field which will be used to populate the Activity Type attribute
     * in the PMXML file.
     *
     * @param field TaskField instance
     */
    var activityTypeField: TaskField? = null
    private var m_sortedCustomFieldsList: List<CustomField>? = null

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    override fun write(projectFile: ProjectFile, stream: OutputStream) {
        try {
            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            //
            // The Primavera schema defines elements as nillable, which by
            // default results in
            // JAXB generating elements like this <element xsl:nil="true"/>
            // whereas Primavera itself simply omits these elements.
            //
            // The XSLT stylesheet below transforms the XML generated by JAXB on
            // the fly to remove any nil elements.
            //
            val transFact = TransformerFactory.newInstance()
            val handler = (transFact as SAXTransformerFactory).newTransformerHandler(StreamSource(ByteArrayInputStream(NILLABLE_STYLESHEET.getBytes())))
            handler.setResult(StreamResult(stream))
            val transformer = handler.getTransformer()

            try {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            } catch (ex: Exception) {
                // https://sourceforge.net/p/mpxj/bugs/291/
                // Output indentation is a nice to have.
                // If we're working with a transformer which doesn't
                // support it, swallow any errors raised trying to configure it.
            }

            m_projectFile = projectFile

            val marshaller = CONTEXT!!.createMarshaller()

            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "")

            m_factory = ObjectFactory()
            m_apibo = m_factory!!.createAPIBusinessObjects()

            configureCustomFields()
            populateSortedCustomFieldsList()

            writeCurrency()
            writeUserFieldDefinitions()
            writeProjectProperties()
            writeCalendars()
            writeResources()
            writeTasks()
            writeAssignments()

            marshaller.marshal(m_apibo, handler)
        } catch (ex: JAXBException) {
            throw IOException(ex.toString())
        } catch (ex: TransformerConfigurationException) {
            throw IOException(ex.toString())
        } finally {
            m_projectFile = null
            m_factory = null
            m_apibo = null
            m_project = null
            m_wbsSequence = 0
            m_relationshipObjectID = 0
            m_sortedCustomFieldsList = null
        }
    }

    /**
     * Create a handful of default currencies to keep Primavera happy.
     */
    private fun writeCurrency() {
        val props = m_projectFile!!.projectProperties
        val currency = m_factory!!.createCurrencyType()
        m_apibo!!.currency.add(currency)

        val positiveSymbol = getCurrencyFormat(props.symbolPosition)
        val negativeSymbol = "($positiveSymbol)"

        currency.decimalPlaces = props.currencyDigits
        currency.decimalSymbol = getSymbolName(props.decimalSeparator)
        currency.digitGroupingSymbol = getSymbolName(props.thousandsSeparator)
        currency.exchangeRate = Double.valueOf(1.0)
        currency.id = "CUR"
        currency.name = "Default Currency"
        currency.negativeSymbol = negativeSymbol
        currency.objectId = DEFAULT_CURRENCY_ID
        currency.positiveSymbol = positiveSymbol
        currency.symbol = props.currencySymbol
    }

    /**
     * Map the currency separator character to a symbol name.
     *
     * @param c currency separator character
     * @return symbol name
     */
    private fun getSymbolName(c: Char): String? {
        var result: String? = null

        when (c) {
            ',' -> {
                result = "Comma"
            }

            '.' -> {
                result = "Period"
            }
        }

        return result
    }

    /**
     * Generate a currency format.
     *
     * @param position currency symbol position
     * @return currency format
     */
    private fun getCurrencyFormat(position: CurrencySymbolPosition): String {
        val result: String

        when (position) {
            AFTER -> {
                result = "1.1#"
            }

            AFTER_WITH_SPACE -> {
                result = "1.1 #"
            }

            BEFORE_WITH_SPACE -> {
                result = "# 1.1"
            }
            BEFORE -> {
                result = "#1.1"
            }

            else -> {
                result = "#1.1"
            }
        }

        return result
    }

    /**
     * Add UDFType objects to a PM XML file.
     *
     * @author kmahan
     * @date 2014-09-24
     * @author lsong
     * @date 2015-7-24
     */
    private fun writeUserFieldDefinitions() {
        for (cf in m_sortedCustomFieldsList!!) {
            if (cf.getFieldType() != null && cf.getFieldType().getDataType() != null) {
                val udf = m_factory!!.createUDFTypeType()
                udf.objectId = Integer.valueOf(FieldTypeHelper.getFieldID(cf.getFieldType()))

                udf.dataType = UserFieldDataType.inferUserFieldDataType(cf.getFieldType().getDataType())
                udf.subjectArea = UserFieldDataType.inferUserFieldSubjectArea(cf.getFieldType())
                udf.title = cf.getAlias()
                m_apibo!!.udfType.add(udf)
            }
        }
    }

    /**
     * This method writes project properties data to a PM XML file.
     */
    private fun writeProjectProperties() {
        m_project = m_factory!!.createProjectType()
        m_apibo!!.project.add(m_project)

        val mpxj = m_projectFile!!.projectProperties
        val rootTask = m_projectFile!!.getTaskByUniqueID(Integer.valueOf(0))
        val guid = rootTask?.guid

        m_project!!.activityDefaultActivityType = "Task Dependent"
        m_project!!.activityDefaultCalendarObjectId = getCalendarUniqueID(m_projectFile!!.defaultCalendar)
        m_project!!.activityDefaultDurationType = "Fixed Duration and Units"
        m_project!!.activityDefaultPercentCompleteType = "Duration"
        m_project!!.activityDefaultPricePerUnit = NumberHelper.DOUBLE_ZERO
        m_project!!.isActivityIdBasedOnSelectedActivity = Boolean.TRUE
        m_project!!.activityIdIncrement = Integer.valueOf(10)
        m_project!!.activityIdPrefix = "A"
        m_project!!.activityIdSuffix = Integer.valueOf(1000)
        m_project!!.isActivityPercentCompleteBasedOnActivitySteps = Boolean.FALSE
        m_project!!.isAddActualToRemaining = Boolean.FALSE
        m_project!!.isAllowNegativeActualUnitsFlag = Boolean.FALSE
        m_project!!.isAssignmentDefaultDrivingFlag = Boolean.TRUE
        m_project!!.assignmentDefaultRateType = "Price / Unit"
        m_project!!.isCheckOutStatus = Boolean.FALSE
        m_project!!.isCostQuantityRecalculateFlag = Boolean.FALSE
        m_project!!.createDate = mpxj.creationDate
        m_project!!.criticalActivityFloatLimit = NumberHelper.DOUBLE_ZERO
        m_project!!.criticalActivityPathType = "Critical Float"
        m_project!!.dataDate = m_projectFile!!.projectProperties.statusDate
        m_project!!.defaultPriceTimeUnits = "Hour"
        m_project!!.discountApplicationPeriod = "Month"
        m_project!!.earnedValueComputeType = "Activity Percent Complete"
        m_project!!.earnedValueETCComputeType = "ETC = Remaining Cost for Activity"
        m_project!!.earnedValueETCUserValue = Double.valueOf(0.88)
        m_project!!.earnedValueUserPercent = Double.valueOf(0.06)
        m_project!!.isEnableSummarization = Boolean.TRUE
        m_project!!.fiscalYearStartMonth = Integer.valueOf(1)
        m_project!!.finishDate = mpxj.finishDate
        m_project!!.guid = DatatypeConverter.printUUID(guid)
        m_project!!.id = PROJECT_ID
        m_project!!.lastUpdateDate = mpxj.lastSaved
        m_project!!.levelingPriority = Integer.valueOf(10)
        m_project!!.isLinkActualToActualThisPeriod = Boolean.TRUE
        m_project!!.isLinkPercentCompleteWithActual = Boolean.TRUE
        m_project!!.isLinkPlannedAndAtCompletionFlag = Boolean.TRUE
        m_project!!.name = if (mpxj.name == null) PROJECT_ID else mpxj.name
        m_project!!.objectId = PROJECT_OBJECT_ID
        m_project!!.plannedStartDate = mpxj.startDate
        m_project!!.isPrimaryResourcesCanMarkActivitiesAsCompleted = Boolean.TRUE
        m_project!!.isResetPlannedToRemainingFlag = Boolean.FALSE
        m_project!!.isResourceCanBeAssignedToSameActivityMoreThanOnce = Boolean.TRUE
        m_project!!.isResourcesCanAssignThemselvesToActivities = Boolean.TRUE
        m_project!!.isResourcesCanEditAssignmentPercentComplete = Boolean.FALSE
        m_project!!.isResourcesCanMarkAssignmentAsCompleted = Boolean.FALSE
        m_project!!.isResourcesCanViewInactiveActivities = Boolean.FALSE
        m_project!!.riskLevel = "Medium"
        m_project!!.startDate = mpxj.startDate
        m_project!!.status = "Active"
        m_project!!.strategicPriority = Integer.valueOf(500)
        m_project!!.summarizeToWBSLevel = Integer.valueOf(2)
        m_project!!.summaryLevel = "Assignment Level"
        m_project!!.isUseProjectBaselineForEarnedValue = Boolean.TRUE
        m_project!!.wbsCodeSeparator = "."
        m_project!!.udf.addAll(writeUDFType(FieldTypeClass.PROJECT, mpxj))
    }

    /**
     * This method writes calendar data to a PM XML file.
     */
    private fun writeCalendars() {
        for (calendar in m_projectFile!!.calendars) {
            writeCalendar(calendar)
        }
    }

    /**
     * This method writes data for an individual calendar to a PM XML file.
     *
     * @param mpxj ProjectCalander instance
     */
    private fun writeCalendar(mpxj: ProjectCalendar) {
        val xml = m_factory!!.createCalendarType()
        m_apibo!!.calendar.add(xml)
        val type = if (mpxj.resource == null) "Global" else "Resource"

        xml.baseCalendarObjectId = getCalendarUniqueID(mpxj.parent)
        xml.isIsPersonal = if (mpxj.resource == null) Boolean.FALSE else Boolean.TRUE
        xml.name = mpxj.name
        xml.objectId = mpxj.uniqueID
        xml.type = type

        val xmlStandardWorkWeek = m_factory!!.createCalendarTypeStandardWorkWeek()
        xml.standardWorkWeek = xmlStandardWorkWeek

        for (day in EnumSet.allOf(Day::class.java)) {
            val xmlHours = m_factory!!.createCalendarTypeStandardWorkWeekStandardWorkHours()
            xmlStandardWorkWeek.standardWorkHours.add(xmlHours)
            xmlHours.dayOfWeek = getDayName(day)

            for (range in mpxj.getHours(day)!!) {
                val xmlWorkTime = m_factory!!.createWorkTimeType()
                xmlHours.workTime.add(xmlWorkTime)

                xmlWorkTime.start = range.getStart()
                xmlWorkTime.finish = getEndTime(range.getEnd())
            }
        }

        val xmlExceptions = m_factory!!.createCalendarTypeHolidayOrExceptions()
        xml.holidayOrExceptions = xmlExceptions

        if (!mpxj.calendarExceptions.isEmpty()) {
            val calendar = DateHelper.popCalendar()
            for (mpxjException in mpxj.calendarExceptions) {
                calendar.setTime(mpxjException.fromDate)
                while (calendar.getTimeInMillis() < mpxjException.toDate!!.getTime()) {
                    val xmlException = m_factory!!.createCalendarTypeHolidayOrExceptionsHolidayOrException()
                    xmlExceptions.holidayOrException.add(xmlException)

                    xmlException.date = calendar.getTime()

                    for (range in mpxjException) {
                        val xmlHours = m_factory!!.createWorkTimeType()
                        xmlException.workTime.add(xmlHours)

                        xmlHours.start = range.getStart()

                        if (range.getEnd() != null) {
                            xmlHours.finish = getEndTime(range.getEnd())
                        }
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            DateHelper.pushCalendar(calendar)
        }
    }

    /**
     * This method writes resource data to a PM XML file.
     */
    private fun writeResources() {
        for (resource in m_projectFile!!.resources) {
            if (resource.uniqueID.intValue() !== 0) {
                writeResource(resource)
            }
        }
    }

    /**
     * Write a single resource.
     *
     * @param mpxj Resource instance
     */
    private fun writeResource(mpxj: Resource) {
        val xml = m_factory!!.createResourceType()
        m_apibo!!.resource.add(xml)

        xml.isAutoComputeActuals = Boolean.TRUE
        xml.isCalculateCostFromUnits = Boolean.TRUE
        xml.calendarObjectId = getCalendarUniqueID(mpxj.resourceCalendar)
        xml.currencyObjectId = DEFAULT_CURRENCY_ID
        xml.defaultUnitsPerTime = Double.valueOf(1.0)
        xml.emailAddress = mpxj.emailAddress
        xml.guid = DatatypeConverter.printUUID(mpxj.guid)
        xml.id = RESOURCE_ID_PREFIX + mpxj.uniqueID
        xml.isIsActive = Boolean.TRUE
        xml.maxUnitsPerTime = getPercentage(mpxj.maxUnits)
        xml.name = mpxj.name
        xml.objectId = mpxj.uniqueID
        xml.parentObjectId = mpxj.parentID
        xml.resourceNotes = mpxj.notes
        xml.resourceType = getResourceType(mpxj)
        xml.udf.addAll(writeUDFType(FieldTypeClass.RESOURCE, mpxj))
    }

    /**
     * This method writes task data to a PM XML file.
     *
     */
    private fun writeTasks() {
        for (task in m_projectFile!!.childTasks) {
            writeTask(task)
        }
    }

    /**
     * Used to write the child tasks of a parent task to the PM XML file.
     *
     * @param parent parent Task instance
     */
    private fun writeChildTasks(parent: Task) {
        for (task in parent.childTasks) {
            writeTask(task)
        }
    }

    /**
     * Given a Task instance, this task determines if it should be written to the
     * PM XML file as an activity or as a WBS item, and calls the appropriate
     * method.
     *
     * @param task Task instance
     */
    private fun writeTask(task: Task) {
        if (!task.`null`) {
            if (extractAndConvertTaskType(task) == null || task.summary) {
                writeWBS(task)
            } else {
                writeActivity(task)
            }
        }
    }

    /**
     * Writes a WBS entity to the PM XML file.
     *
     * @param mpxj MPXJ Task entity
     */
    private fun writeWBS(mpxj: Task) {
        if (mpxj.uniqueID.intValue() !== 0) {
            val xml = m_factory!!.createWBSType()
            m_project!!.wbs.add(xml)
            var code: String? = mpxj.wbs
            code = if (code == null || code.length() === 0) DEFAULT_WBS_CODE else code

            val parentTask = mpxj.parentTask
            val parentObjectID = parentTask?.uniqueID

            xml.code = code
            xml.guid = DatatypeConverter.printUUID(mpxj.guid)
            xml.name = mpxj.name

            xml.objectId = mpxj.uniqueID
            xml.parentObjectId = parentObjectID
            xml.projectObjectId = PROJECT_OBJECT_ID
            xml.sequenceNumber = Integer.valueOf(m_wbsSequence++)

            xml.status = "Active"
        }

        writeChildTasks(mpxj)
    }

    /**
     * Writes an activity to a PM XML file.
     *
     * @param mpxj MPXJ Task instance
     */
    private fun writeActivity(mpxj: Task) {
        val xml = m_factory!!.createActivityType()
        m_project!!.activity.add(xml)

        val parentTask = mpxj.parentTask
        val parentObjectID = parentTask?.uniqueID

        xml.actualStartDate = mpxj.actualStart
        xml.actualFinishDate = mpxj.actualFinish
        xml.atCompletionDuration = getDuration(mpxj.duration)
        xml.calendarObjectId = getCalendarUniqueID(mpxj.calendar)
        xml.durationPercentComplete = getPercentage(mpxj.percentageComplete)
        xml.durationType = DURATION_TYPE_MAP.get(mpxj.type)
        xml.finishDate = mpxj.finish
        xml.guid = DatatypeConverter.printUUID(mpxj.guid)
        xml.id = getActivityID(mpxj)
        xml.name = mpxj.name
        xml.objectId = mpxj.uniqueID
        xml.percentComplete = getPercentage(mpxj.percentageComplete)
        xml.percentCompleteType = "Duration"
        xml.primaryConstraintType = CONSTRAINT_TYPE_MAP.get(mpxj.constraintType)
        xml.primaryConstraintDate = mpxj.constraintDate
        xml.plannedDuration = getDuration(mpxj.duration)
        xml.plannedFinishDate = mpxj.finish
        xml.plannedStartDate = mpxj.start
        xml.projectObjectId = PROJECT_OBJECT_ID
        xml.remainingDuration = getDuration(mpxj.remainingDuration)
        xml.remainingEarlyFinishDate = mpxj.earlyFinish
        xml.remainingEarlyStartDate = mpxj.resume
        xml.remainingLaborCost = NumberHelper.DOUBLE_ZERO
        xml.remainingLaborUnits = NumberHelper.DOUBLE_ZERO
        xml.remainingNonLaborCost = NumberHelper.DOUBLE_ZERO
        xml.remainingNonLaborUnits = NumberHelper.DOUBLE_ZERO
        xml.startDate = mpxj.start
        xml.status = getActivityStatus(mpxj)
        xml.type = extractAndConvertTaskType(mpxj)
        xml.wbsObjectId = parentObjectID
        xml.udf.addAll(writeUDFType(FieldTypeClass.TASK, mpxj))

        writePredecessors(mpxj)
    }

    /**
     * Attempts to locate the activity type value extracted from an existing P6 schedule.
     * If necessary converts to the form which can be used in the PMXML file.
     * Returns "Resource Dependent" as the default value.
     *
     * @param task parent task
     * @return activity type
     */
    private fun extractAndConvertTaskType(task: Task): String? {
        var activityType = task.getCachedValue(activityTypeField) as String
        if (activityType == null) {
            activityType = "Resource Dependent"
        } else {
            if (ACTIVITY_TYPE_MAP.containsKey(activityType)) {
                activityType = ACTIVITY_TYPE_MAP.get(activityType)
            }
        }
        return activityType
    }

    /**
     * Writes assignment data to a PM XML file.
     */
    private fun writeAssignments() {
        for (assignment in m_projectFile!!.resourceAssignments) {
            val resource = assignment.resource
            if (resource != null) {
                val task = assignment.task
                if (task != null && task!!.uniqueID.intValue() !== 0 && !task!!.summary) {
                    writeAssignment(assignment)
                }
            }
        }
    }

    /**
     * Writes a resource assignment to a PM XML file.
     *
     * @param mpxj MPXJ ResourceAssignment instance
     */
    private fun writeAssignment(mpxj: ResourceAssignment) {
        val xml = m_factory!!.createResourceAssignmentType()
        m_project!!.resourceAssignment.add(xml)
        val task = mpxj.task
        val parentTask = task!!.parentTask
        val parentTaskUniqueID = parentTask?.uniqueID

        xml.activityObjectId = mpxj.taskUniqueID
        xml.actualCost = getDouble(mpxj.actualCost)
        xml.actualFinishDate = mpxj.actualFinish
        xml.actualOvertimeUnits = getDuration(mpxj.actualOvertimeWork)
        xml.actualRegularUnits = getDuration(mpxj.actualWork)
        xml.actualStartDate = mpxj.actualStart
        xml.actualUnits = getDuration(mpxj.actualWork)
        xml.atCompletionUnits = getDuration(mpxj.remainingWork)
        xml.plannedCost = getDouble(mpxj.actualCost)
        xml.finishDate = mpxj.finish
        xml.guid = DatatypeConverter.printUUID(mpxj.guid)
        xml.objectId = mpxj.uniqueID
        xml.plannedDuration = getDuration(mpxj.work)
        xml.plannedFinishDate = mpxj.finish
        xml.plannedStartDate = mpxj.start
        xml.plannedUnits = getDuration(mpxj.work)
        xml.plannedUnitsPerTime = getPercentage(mpxj.units)
        xml.projectObjectId = PROJECT_OBJECT_ID
        xml.rateSource = "Resource"
        xml.remainingCost = getDouble(mpxj.actualCost)
        xml.remainingDuration = getDuration(mpxj.remainingWork)
        xml.remainingFinishDate = mpxj.finish
        xml.remainingStartDate = mpxj.start
        xml.remainingUnits = getDuration(mpxj.remainingWork)
        xml.remainingUnitsPerTime = getPercentage(mpxj.units)
        xml.resourceObjectId = mpxj.resourceUniqueID
        xml.startDate = mpxj.start
        xml.wbsObjectId = parentTaskUniqueID
        xml.udf.addAll(writeUDFType(FieldTypeClass.ASSIGNMENT, mpxj))
    }

    /**
     * Writes task predecessor links to a PM XML file.
     *
     * @param task MPXJ Task instance
     */
    private fun writePredecessors(task: Task) {
        val relations = task.predecessors
        for (mpxj in relations) {
            val xml = m_factory!!.createRelationshipType()
            m_project!!.relationship.add(xml)

            xml.lag = getDuration(mpxj.lag)
            xml.objectId = Integer.valueOf(++m_relationshipObjectID)
            xml.predecessorActivityObjectId = mpxj.targetTask.uniqueID
            xml.successorActivityObjectId = mpxj.sourceTask.uniqueID
            xml.predecessorProjectObjectId = PROJECT_OBJECT_ID
            xml.successorProjectObjectId = PROJECT_OBJECT_ID
            xml.type = RELATION_TYPE_MAP.get(mpxj.type)
        }
    }

    /**
     * Writes a list of UDF types.
     *
     * @author lsong
     * @param type parent entity type
     * @param mpxj parent entity
     * @return list of UDFAssignmentType instances
     */
    private fun writeUDFType(type: FieldTypeClass, mpxj: FieldContainer): List<UDFAssignmentType> {
        val out = ArrayList<UDFAssignmentType>()
        for (cf in m_sortedCustomFieldsList!!) {
            val fieldType = cf.getFieldType()
            if (fieldType != null && type === fieldType!!.getFieldTypeClass()) {
                val value = mpxj.getCachedValue(fieldType)
                if (FieldTypeHelper.valueIsNotDefault(fieldType, value)) {
                    val udf = m_factory!!.createUDFAssignmentType()
                    udf.typeObjectId = FieldTypeHelper.getFieldID(fieldType!!)
                    setUserFieldValue(udf, fieldType!!.getDataType(), value)
                    out.add(udf)
                }
            }
        }
        return out
    }

    /**
     * Sets the value of a UDF.
     *
     * @param udf user defined field
     * @param dataType MPXJ data type
     * @param value field value
     */
    private fun setUserFieldValue(udf: UDFAssignmentType, dataType: DataType, value: Object) {
        var value = value
        when (dataType) {
            DURATION -> {
                udf.textValue = (value as Duration).toString()
            }

            CURRENCY -> {
                if (value !is Double) {
                    value = Double.valueOf((value as Number).doubleValue())
                }
                udf.costValue = value
            }

            BINARY -> {
                udf.textValue = ""
            }

            STRING -> {
                udf.textValue = value
            }

            DATE -> {
                udf.startDateValue = value as Date
            }

            NUMERIC -> {
                if (value !is Double) {
                    value = Double.valueOf((value as Number).doubleValue())
                }
                udf.doubleValue = value
            }

            BOOLEAN -> {
                udf.integerValue = if (BooleanHelper.getBoolean(value as Boolean)) Integer.valueOf(1) else Integer.valueOf(0)
            }

            INTEGER, SHORT -> {
                udf.integerValue = NumberHelper.getInteger(value as Number)
            }

            else -> {
                throw RuntimeException("Unconvertible data type: $dataType")
            }
        }
    }

    /**
     * Retrieve a duration in the form required by Primavera.
     *
     * @param duration Duration instance
     * @return formatted duration
     */
    private fun getDuration(duration: Duration?): Double? {
        var duration = duration
        val result: Double?
        if (duration == null) {
            result = null
        } else {
            if (duration!!.getUnits() !== TimeUnit.HOURS) {
                duration = duration!!.convertUnits(TimeUnit.HOURS, m_projectFile!!.projectProperties)
            }

            result = Double.valueOf(duration!!.getDuration())
        }
        return result
    }

    /**
     * Formats a day name.
     *
     * @param day MPXJ Day instance
     * @return Primavera day instance
     */
    private fun getDayName(day: Day): String {
        return DAY_NAMES[day.getValue() - 1]
    }

    /**
     * Formats a resource type.
     *
     * @param resource MPXJ resource
     * @return Primavera resource type
     */
    private fun getResourceType(resource: Resource): String {
        val result: String
        var type: net.sf.mpxj.ResourceType? = resource.type
        if (type == null) {
            type = net.sf.mpxj.ResourceType.WORK
        }

        when (type) {
            ResourceType.MATERIAL -> {
                result = "Material"
            }

            ResourceType.COST -> {
                result = "Nonlabor"
            }

            else -> {
                result = "Labor"
            }
        }

        return result
    }

    /**
     * Formats a percentage value.
     *
     * @param number MPXJ percentage value
     * @return Primavera percentage value
     */
    private fun getPercentage(number: Number?): Double? {
        var result: Double? = null

        if (number != null) {
            result = Double.valueOf(number.doubleValue() / 100)
        }

        return result
    }

    /**
     * Formats a double value.
     *
     * @param number numeric value
     * @return Double instance
     */
    private fun getDouble(number: Number?): Double? {
        var result: Double? = null

        if (number != null) {
            result = Double.valueOf(number.doubleValue())
        }

        return result
    }

    /**
     * The end of a Primavera time range finishes on the last minute
     * of the period, so a range of 12:00 -> 13:00 is represented by
     * Primavera as 12:00 -> 12:59.
     *
     * @param date MPXJ end time
     * @return Primavera end time
     */
    private fun getEndTime(date: Date): Date {
        return Date(date.getTime() - 60000)
    }

    /**
     * Retrieve a calendar unique ID.
     *
     * @param calendar ProjectCalendar instance
     * @return calendar unique ID
     */
    private fun getCalendarUniqueID(calendar: ProjectCalendar?): Integer? {
        return calendar?.uniqueID
    }

    /**
     * Retrieve an activity status.
     *
     * @param mpxj MPXJ Task instance
     * @return activity status
     */
    private fun getActivityStatus(mpxj: Task): String {
        val result: String
        if (mpxj.actualStart == null) {
            result = "Not Started"
        } else {
            if (mpxj.actualFinish == null) {
                result = "In Progress"
            } else {
                result = "Completed"
            }
        }
        return result
    }

    /**
     * Retrieve the Activity ID value for this task.
     * @param task Task instance
     * @return Activity ID value
     */
    private fun getActivityID(task: Task): String? {
        var result: String? = null
        if (activityIdField != null) {
            val value = task.getCachedValue(activityIdField)
            if (value != null) {
                result = value.toString()
            }
        }
        return result
    }

    /**
     * Find the fields in which the Activity ID and Activity Type are stored.
     */
    private fun configureCustomFields() {
        val customFields = m_projectFile!!.customFields

        // If the caller hasn't already supplied a value for this field
        if (activityIdField == null) {
            activityIdField = customFields.getFieldByAlias(FieldTypeClass.TASK, "Code") as TaskField
            if (activityIdField == null) {
                activityIdField = TaskField.WBS
            }
        }

        // If the caller hasn't already supplied a value for this field
        if (activityTypeField == null) {
            activityTypeField = customFields.getFieldByAlias(FieldTypeClass.TASK, "Activity Type") as TaskField
        }
    }

    /**
     * Populate a sorted list of custom fields to ensure that these fields
     * are written to the file in a consistent order.
     */
    private fun populateSortedCustomFieldsList() {
        m_sortedCustomFieldsList = ArrayList<CustomField>()
        for (field in m_projectFile!!.customFields) {
            val fieldType = field.getFieldType()
            if (fieldType != null) {
                m_sortedCustomFieldsList!!.add(field)
            }
        }

        // Sort to ensure consistent order in file
        Collections.sort(m_sortedCustomFieldsList, object : Comparator<CustomField>() {
            @Override
            fun compare(customField1: CustomField, customField2: CustomField): Int {
                val o1 = customField1.getFieldType()
                val o2 = customField2.getFieldType()
                val name1 = o1.getClass().getSimpleName() + "." + o1.getName() + " " + customField1.getAlias()
                val name2 = o2.getClass().getSimpleName() + "." + o2.getName() + " " + customField2.getAlias()
                return name1.compareTo(name2)
            }
        })
    }

    companion object {

        /**
         * Cached context to minimise construction cost.
         */
        private var CONTEXT: JAXBContext? = null

        /**
         * Note any error occurring during context construction.
         */
        private var CONTEXT_EXCEPTION: JAXBException? = null

        init {
            try {
                //
                // JAXB RI property to speed up construction
                //
                System.setProperty("com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot", "true")

                //
                // Construct the context
                //
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.primavera.schema", PrimaveraPMFileWriter::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }

        private val NILLABLE_STYLESHEET = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><xsl:output method=\"xml\" indent=\"yes\"/><xsl:template match=\"node()[not(@xsi:nil = 'true')]|@*\"><xsl:copy><xsl:apply-templates select=\"node()|@*\"/></xsl:copy></xsl:template></xsl:stylesheet>"
        private val PROJECT_OBJECT_ID = Integer.valueOf(1)
        private val PROJECT_ID = "PROJECT"
        private val RESOURCE_ID_PREFIX = "RESOURCE-"
        private val DEFAULT_WBS_CODE = "WBS"
        private val DEFAULT_CURRENCY_ID = Integer.valueOf(1)

        private val DAY_NAMES = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        private val RELATION_TYPE_MAP = HashMap<RelationType, String>()

        init {
            RELATION_TYPE_MAP.put(RelationType.FINISH_START, "Finish to Start")
            RELATION_TYPE_MAP.put(RelationType.FINISH_FINISH, "Finish to Finish")
            RELATION_TYPE_MAP.put(RelationType.START_START, "Start to Start")
            RELATION_TYPE_MAP.put(RelationType.START_FINISH, "Start to Finish")
        }

        private val DURATION_TYPE_MAP = HashMap<TaskType, String>()

        init {
            DURATION_TYPE_MAP.put(TaskType.FIXED_DURATION, "Fixed Duration and Units/Time")
            DURATION_TYPE_MAP.put(TaskType.FIXED_UNITS, "Fixed Units")
            DURATION_TYPE_MAP.put(TaskType.FIXED_WORK, "Fixed Duration and Units")
        }

        private val CONSTRAINT_TYPE_MAP = HashMap<ConstraintType, String>()

        init {
            CONSTRAINT_TYPE_MAP.put(ConstraintType.START_NO_LATER_THAN, "Start On or Before")
            CONSTRAINT_TYPE_MAP.put(ConstraintType.START_NO_EARLIER_THAN, "Start On or After")
            CONSTRAINT_TYPE_MAP.put(ConstraintType.MUST_FINISH_ON, "Finish On")
            CONSTRAINT_TYPE_MAP.put(ConstraintType.FINISH_NO_LATER_THAN, "Finish On or Before")
            CONSTRAINT_TYPE_MAP.put(ConstraintType.FINISH_NO_EARLIER_THAN, "Finish On or After")
            CONSTRAINT_TYPE_MAP.put(ConstraintType.AS_LATE_AS_POSSIBLE, "As Late As Possible")
            CONSTRAINT_TYPE_MAP.put(ConstraintType.MUST_START_ON, "Mandatory Start")
            CONSTRAINT_TYPE_MAP.put(ConstraintType.MUST_FINISH_ON, "Mandatory Finish")
        }

        private val ACTIVITY_TYPE_MAP = HashMap<String, String>()

        init {
            ACTIVITY_TYPE_MAP.put("TT_Task", "Task Dependent")
            ACTIVITY_TYPE_MAP.put("TT_Rsrc", "Resource Dependent")
            ACTIVITY_TYPE_MAP.put("TT_LOE", "Level of Effort")
            ACTIVITY_TYPE_MAP.put("TT_Mile", "Start Milestone")
            ACTIVITY_TYPE_MAP.put("TT_FinMile", "Finish Milestone")
            ACTIVITY_TYPE_MAP.put("TT_WBS", "WBS Summary")
        }
    }
}

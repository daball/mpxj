/*
 * file:       ResourceAssignmentFactory.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       21/03/2010
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

package net.sf.mpxj.mpp

import net.sf.mpxj.AssignmentField
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceType
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.WorkContour
import net.sf.mpxj.common.DefaultTimephasedWorkContainer
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.RtfHelper
import net.sf.mpxj.common.SplitTaskFactory
import net.sf.mpxj.common.TimephasedCostNormaliser
import net.sf.mpxj.common.TimephasedWorkNormaliser

/**
 * Common implementation detail to extract resource assignment data from
 * MPP9, MPP12, and MPP14 files.
 */
class ResourceAssignmentFactory {
    /**
     * Main entry point when called to process assignment data.
     *
     * @param file parent project file
     * @param fieldMap assignment field map
     * @param enterpriseCustomFieldMap enterprise custom field map
     * @param useRawTimephasedData use raw timephased data flag
     * @param preserveNoteFormatting preserve note formatting flag
     * @param assnVarMeta var meta
     * @param assnVarData var data
     * @param assnFixedMeta fixed meta
     * @param assnFixedData fixed data
     * @param assnFixedData2 fixed data
     * @param count expected number of assignments
     */
    fun process(file: ProjectFile, fieldMap: FieldMap, enterpriseCustomFieldMap: FieldMap?, useRawTimephasedData: Boolean, preserveNoteFormatting: Boolean, assnVarMeta: VarMeta, assnVarData: Var2Data, assnFixedMeta: FixedMeta, assnFixedData: FixedData, assnFixedData2: FixedData?, count: Int) {
        val set = assnVarMeta.uniqueIdentifierSet
        val timephasedFactory = TimephasedDataFactory()
        val splitFactory = SplitTaskFactory()
        val normaliser = MPPTimephasedWorkNormaliser()
        val baselineWorkNormaliser = MPPTimephasedBaselineWorkNormaliser()
        val baselineCostNormaliser = MPPTimephasedBaselineCostNormaliser()
        val baselineCalendar = file.baselineCalendar

        //System.out.println(assnFixedMeta);
        //System.out.println(assnFixedData);
        //System.out.println(assnVarMeta.toString(fieldMap));
        //System.out.println(assnVarData);

        val metaDataBitFlags: Array<MppBitFlag>
        if (NumberHelper.getInt(file.projectProperties.mppFileType) == 14) {
            if (NumberHelper.getInt(file.projectProperties.applicationVersion) > ApplicationVersion.PROJECT_2010) {
                metaDataBitFlags = PROJECT_2013_ASSIGNMENT_META_DATA_BIT_FLAGS
            } else {
                metaDataBitFlags = PROJECT_2010_ASSIGNMENT_META_DATA_BIT_FLAGS
            }
        } else {
            metaDataBitFlags = ASSIGNMENT_META_DATA_BIT_FLAGS
        }

        for (loop in 0 until count) {
            val meta = assnFixedMeta.getByteArrayValue(loop)
            if (meta == null || meta[0].toInt() != 0) {
                continue
            }

            val offset = MPPUtility.getInt(meta, 4)
            var data: ByteArray? = assnFixedData.getByteArrayValue(assnFixedData.getIndexFromOffset(offset)) ?: continue

            if (data!!.size < fieldMap.getMaxFixedDataSize(0)) {
                val newData = ByteArray(fieldMap.getMaxFixedDataSize(0))
                System.arraycopy(data, 0, newData, 0, data.size)
                data = newData
            }

            val id = MPPUtility.getInt(data, fieldMap.getFixedDataOffset(AssignmentField.UNIQUE_ID))
            val varDataId = Integer.valueOf(id)
            if (set.contains(varDataId) === false) {
                continue
            }

            var data2: ByteArray? = null
            if (assnFixedData2 != null) {
                data2 = assnFixedData2.getByteArrayValue(loop)
            }

            val assignment = ResourceAssignment(file, null)

            assignment.disableEvents()

            fieldMap.populateContainer(AssignmentField::class.java, assignment, varDataId, arrayOf<ByteArray>(data, data2), assnVarData)

            enterpriseCustomFieldMap?.populateContainer(AssignmentField::class.java, assignment, varDataId, null, assnVarData)

            assignment.enableEvents()

            for (flag in metaDataBitFlags) {
                flag.setValue(assignment, meta)
            }

            assignment.confirmed = meta[8] and 0x80 != 0
            assignment.responsePending = meta[9] and 0x01 != 0
            assignment.teamStatusPending = meta[10] and 0x02 != 0

            processHyperlinkData(assignment, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.HYPERLINK_DATA)))

            //
            // Post processing
            //
            if (NumberHelper.getInt(file.projectProperties.mppFileType) == 9 && assignment.createDate == null) {
                val creationData = assnVarData.getByteArray(varDataId, MPP9_CREATION_DATA)
                if (creationData != null && creationData.size >= 28) {
                    assignment.createDate = MPPUtility.getTimestamp(creationData, 24)
                }
            }

            var notes: String? = assignment.notes
            if (!preserveNoteFormatting) {
                notes = RtfHelper.strip(notes)
            }
            assignment.notes = notes

            val task = file.getTaskByUniqueID(assignment.taskUniqueID)
            if (task != null) {
                task.addResourceAssignment(assignment)

                val resource = file.getResourceByUniqueID(assignment.resourceUniqueID)
                val resourceType = if (resource == null) ResourceType.WORK else resource.type
                var calendar: ProjectCalendar? = null

                if (resource != null && resourceType == ResourceType.WORK && !task.ignoreResourceCalendar) {
                    calendar = resource.resourceCalendar
                }

                if (calendar == null) {
                    calendar = task.effectiveCalendar
                }

                assignment.setTimephasedBaselineWork(0, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(1, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE1_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(2, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE2_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(3, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE3_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(4, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE4_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(5, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE5_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(6, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE6_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(7, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE7_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(8, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE8_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(9, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE9_WORK)), !useRawTimephasedData))
                assignment.setTimephasedBaselineWork(10, timephasedFactory.getBaselineWork(assignment, baselineCalendar, baselineWorkNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE10_WORK)), !useRawTimephasedData))

                assignment.setTimephasedBaselineCost(0, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(1, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE1_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(2, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE2_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(3, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE3_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(4, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE4_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(5, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE5_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(6, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE6_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(7, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE7_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(8, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE8_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(9, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE9_COST)), !useRawTimephasedData))
                assignment.setTimephasedBaselineCost(10, timephasedFactory.getBaselineCost(baselineCalendar, baselineCostNormaliser, assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_BASELINE10_COST)), !useRawTimephasedData))

                val timephasedActualWorkData = assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_ACTUAL_WORK))
                val timephasedWorkData = assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_WORK))
                val timephasedActualOvertimeWorkData = assnVarData.getByteArray(varDataId, fieldMap.getVarDataKey(AssignmentField.TIMEPHASED_ACTUAL_OVERTIME_WORK))

                val timephasedActualWork = timephasedFactory.getCompleteWork(calendar, assignment, timephasedActualWorkData)
                val timephasedWork = timephasedFactory.getPlannedWork(calendar, assignment.start, assignment.units.doubleValue(), timephasedWorkData, timephasedActualWork, resourceType)
                val timephasedActualOvertimeWork = timephasedFactory.getCompleteWork(calendar, assignment, timephasedActualOvertimeWorkData)

                assignment.actualStart = if (timephasedActualWork.isEmpty()) null else assignment.start
                assignment.actualFinish = if (assignment.remainingWork.getDuration() === 0 && resource != null) assignment.finish else null

                if (task.splits != null && task.splits!!.isEmpty()) {
                    splitFactory.processSplitData(task, timephasedActualWork, timephasedWork)
                }

                createTimephasedData(file, assignment, timephasedWork, timephasedActualWork)

                assignment.setTimephasedWork(DefaultTimephasedWorkContainer(calendar, normaliser, timephasedWork, !useRawTimephasedData))
                assignment.setTimephasedActualWork(DefaultTimephasedWorkContainer(calendar, normaliser, timephasedActualWork, !useRawTimephasedData))
                assignment.setTimephasedActualOvertimeWork(DefaultTimephasedWorkContainer(calendar, normaliser, timephasedActualOvertimeWork, !useRawTimephasedData))

                if (timephasedWorkData != null) {
                    if (timephasedFactory.getWorkModified(timephasedWork)) {
                        assignment.workContour = WorkContour.CONTOURED
                    } else {
                        if (timephasedWorkData.size >= 30) {
                            assignment.workContour = WorkContour.getInstance(MPPUtility.getShort(timephasedWorkData, 28))
                        } else {
                            assignment.workContour = WorkContour.FLAT
                        }
                    }
                }

                file.eventManager.fireAssignmentReadEvent(assignment)
            }
        }
    }

    /**
     * Extract assignment hyperlink data.
     *
     * @param assignment assignment instance
     * @param data hyperlink data
     */
    private fun processHyperlinkData(assignment: ResourceAssignment, data: ByteArray?) {
        if (data != null) {
            var offset = 12

            offset += 12
            val hyperlink = MPPUtility.getUnicodeString(data, offset)
            offset += (hyperlink.length() + 1) * 2

            offset += 12
            val address = MPPUtility.getUnicodeString(data, offset)
            offset += (address.length() + 1) * 2

            offset += 12
            val subaddress = MPPUtility.getUnicodeString(data, offset)
            offset += (subaddress.length() + 1) * 2

            offset += 12
            val screentip = MPPUtility.getUnicodeString(data, offset)

            assignment.hyperlink = hyperlink
            assignment.hyperlinkAddress = address
            assignment.hyperlinkSubAddress = subaddress
            assignment.hyperlinkScreenTip = screentip
        }
    }

    /**
     * Method used to create missing timephased data.
     *
     * @param file project file
     * @param assignment resource assignment
     * @param timephasedPlanned planned timephased data
     * @param timephasedComplete complete timephased data
     */
    private fun createTimephasedData(file: ProjectFile, assignment: ResourceAssignment, timephasedPlanned: List<TimephasedWork>, timephasedComplete: List<TimephasedWork>) {
        if (timephasedPlanned.isEmpty() && timephasedComplete.isEmpty()) {
            var totalMinutes = assignment.work!!.convertUnits(TimeUnit.MINUTES, file.projectProperties)

            var workPerDay: Duration

            if (assignment.resource == null || assignment.resource!!.type == ResourceType.WORK) {
                workPerDay = if (totalMinutes.getDuration() === 0) totalMinutes else ResourceAssignmentFactory.DEFAULT_NORMALIZER_WORK_PER_DAY
                val units = NumberHelper.getInt(assignment.units)
                if (units != 100) {
                    workPerDay = Duration.getInstance(workPerDay.getDuration() * units / 100.0, workPerDay.getUnits())
                }
            } else {
                if (assignment.variableRateUnits == null) {
                    val workingDays = assignment.calendar!!.getWork(assignment.start, assignment.finish, TimeUnit.DAYS)
                    val units = NumberHelper.getDouble(assignment.units)
                    val unitsPerDayAsMinutes = units * 60 / (workingDays.getDuration() * 100)
                    workPerDay = Duration.getInstance(unitsPerDayAsMinutes, TimeUnit.MINUTES)
                } else {
                    val unitsPerHour = NumberHelper.getDouble(assignment.units)
                    workPerDay = ResourceAssignmentFactory.DEFAULT_NORMALIZER_WORK_PER_DAY
                    val hoursPerDay = workPerDay.convertUnits(TimeUnit.HOURS, file.projectProperties)
                    val unitsPerDayAsHours = unitsPerHour * hoursPerDay.getDuration() / 100
                    val unitsPerDayAsMinutes = unitsPerDayAsHours * 60
                    workPerDay = Duration.getInstance(unitsPerDayAsMinutes, TimeUnit.MINUTES)
                }
            }

            val overtimeWork = assignment.overtimeWork
            if (overtimeWork != null && overtimeWork.getDuration() !== 0) {
                val totalOvertimeMinutes = overtimeWork.convertUnits(TimeUnit.MINUTES, file.projectProperties)
                totalMinutes = Duration.getInstance(totalMinutes.getDuration() - totalOvertimeMinutes.getDuration(), TimeUnit.MINUTES)
            }

            val tra = TimephasedWork()
            tra.setStart(assignment.start)
            tra.setAmountPerDay(workPerDay)
            tra.setModified(false)
            tra.setFinish(assignment.finish)
            tra.setTotalAmount(totalMinutes)
            timephasedPlanned.add(tra)
        }
    }

    companion object {

        private val MPP9_CREATION_DATA = Integer.valueOf(138)

        private val ASSIGNMENT_META_DATA_BIT_FLAGS = arrayOf(MppBitFlag(AssignmentField.FLAG1, 28, 0x00000080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG2, 28, 0x00000100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG3, 28, 0x00000200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG4, 28, 0x00000400, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG5, 28, 0x00000800, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG6, 28, 0x00001000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG7, 28, 0x00002000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG8, 28, 0x00004000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG9, 28, 0x00008000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG10, 28, 0x00000040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG11, 28, 0x00010000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG12, 28, 0x00020000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG13, 28, 0x00040000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG14, 28, 0x00080000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG15, 28, 0x00100000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG16, 28, 0x00200000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG17, 28, 0x00400000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG18, 28, 0x00800000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG19, 28, 0x01000000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG20, 28, 0x02000000, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT_2010_ASSIGNMENT_META_DATA_BIT_FLAGS = arrayOf(MppBitFlag(AssignmentField.FLAG10, 28, 0x000002, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG1, 28, 0x000004, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG2, 28, 0x000008, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG3, 28, 0x000010, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG4, 28, 0x000020, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG5, 28, 0x000040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG6, 28, 0x000080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG7, 28, 0x000100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG8, 28, 0x000200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG9, 28, 0x000400, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG11, 28, 0x000800, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG12, 28, 0x001000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG13, 28, 0x002000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG14, 28, 0x004000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG15, 28, 0x008000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG16, 28, 0x010000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG17, 28, 0x020000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG18, 28, 0x040000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG19, 28, 0x080000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG20, 28, 0x100000, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT_2013_ASSIGNMENT_META_DATA_BIT_FLAGS = arrayOf(MppBitFlag(AssignmentField.FLAG1, 20, 0x000002, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG2, 20, 0x000004, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG3, 20, 0x000008, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG4, 20, 0x000010, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG5, 20, 0x000020, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG6, 20, 0x000040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG7, 20, 0x000080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG8, 20, 0x000100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG9, 20, 0x000200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG10, 20, 0x000001, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG11, 25, 0x000008, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG12, 25, 0x000010, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG13, 25, 0x000020, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG14, 25, 0x000040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG15, 25, 0x000080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG16, 25, 0x000100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG17, 25, 0x000200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG18, 25, 0x000400, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG19, 25, 0x000800, Boolean.FALSE, Boolean.TRUE), MppBitFlag(AssignmentField.FLAG20, 25, 0x001000, Boolean.FALSE, Boolean.TRUE))

        private val DEFAULT_NORMALIZER_WORK_PER_DAY = Duration.getInstance(480, TimeUnit.MINUTES)
    }
}

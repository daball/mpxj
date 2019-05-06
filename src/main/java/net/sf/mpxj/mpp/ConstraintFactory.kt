package net.sf.mpxj.mpp

import java.io.FileNotFoundException
import java.io.IOException

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.NumberHelper

/**
 * Common implementation detail to extract task constraint data from
 * MPP9, MPP12, and MPP14 files.
 */
class ConstraintFactory {
    /**
     * Main entry point when called to process constraint data.
     *
     * @param projectDir project directory
     * @param file parent project file
     * @param inputStreamFactory factory to create input stream
     */
    @Throws(IOException::class)
    fun process(projectDir: DirectoryEntry, file: ProjectFile, inputStreamFactory: DocumentInputStreamFactory) {
        var consDir: DirectoryEntry?
        try {
            consDir = projectDir.getEntry("TBkndCons") as DirectoryEntry
        } catch (ex: FileNotFoundException) {
            consDir = null
        }

        if (consDir != null) {
            val consFixedMeta = FixedMeta(DocumentInputStream(consDir.getEntry("FixedMeta") as DocumentEntry), 10)
            val consFixedData = FixedData(consFixedMeta, 20, inputStreamFactory.getInstance(consDir, "FixedData"))
            //         FixedMeta consFixed2Meta = new FixedMeta(new DocumentInputStream(((DocumentEntry) consDir.getEntry("Fixed2Meta"))), 9);
            //         FixedData consFixed2Data = new FixedData(consFixed2Meta, 48, getEncryptableInputStream(consDir, "Fixed2Data"));

            val count = consFixedMeta.adjustedItemCount
            var lastConstraintID = -1

            val properties = file.projectProperties
            val eventManager = file.eventManager

            val project15 = NumberHelper.getInt(properties.mppFileType) == 14 && NumberHelper.getInt(properties.applicationVersion) > ApplicationVersion.PROJECT_2010
            val durationUnitsOffset = if (project15) 18 else 14
            val durationOffset = if (project15) 14 else 16

            for (loop in 0 until count) {
                val metaData = consFixedMeta.getByteArrayValue(loop)

                //
                // SourceForge bug 2209477: we were reading an int here, but
                // it looks like the deleted flag is just a short.
                //
                if (MPPUtility.getShort(metaData, 0) != 0) {
                    continue
                }

                val index = consFixedData.getIndexFromOffset(MPPUtility.getInt(metaData, 4))
                if (index == -1) {
                    continue
                }

                //
                // Do we have enough data?
                //
                val data = consFixedData.getByteArrayValue(index)
                if (data!!.size < 14) {
                    continue
                }

                val constraintID = MPPUtility.getInt(data, 0)
                if (constraintID <= lastConstraintID) {
                    continue
                }

                lastConstraintID = constraintID
                val taskID1 = MPPUtility.getInt(data, 4)
                val taskID2 = MPPUtility.getInt(data, 8)

                if (taskID1 == taskID2) {
                    continue
                }

                // byte[] metaData2 = consFixed2Meta.getByteArrayValue(loop);
                // int index2 = consFixed2Data.getIndexFromOffset(MPPUtility.getInt(metaData2, 4));
                // byte[] data2 = consFixed2Data.getByteArrayValue(index2);

                val task1 = file.getTaskByUniqueID(Integer.valueOf(taskID1))
                val task2 = file.getTaskByUniqueID(Integer.valueOf(taskID2))
                if (task1 != null && task2 != null) {
                    val type = RelationType.getInstance(MPPUtility.getShort(data, 12))
                    val durationUnits = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, durationUnitsOffset))
                    val lag = MPPUtility.getAdjustedDuration(properties, MPPUtility.getInt(data, durationOffset), durationUnits)
                    val relation = task2.addPredecessor(task1, type, lag)
                    relation.uniqueID = Integer.valueOf(constraintID)
                    eventManager.fireRelationReadEvent(relation)
                }
            }
        }
    }
}

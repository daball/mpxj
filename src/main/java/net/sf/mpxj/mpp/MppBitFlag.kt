package net.sf.mpxj.mpp

import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType

/**
 * Represents the type and location of a bit flag within a block of data.
 */
class MppBitFlag
/**
 * Constructor.
 *
 * @param type field type
 * @param offset offset in buffer
 * @param mask bit mask
 * @param zeroValue value to return if expression is zero
 * @param nonZeroValue value to return if expression is non-zero
 */
(private val m_type: FieldType, private val m_offset: Int, private val m_mask: Int, private val m_zeroValue: Object, private val m_nonZeroValue: Object) {

    /**
     * Extracts the value of this bit flag from the supplied byte array
     * and sets the value in the supplied container.
     *
     * @param container container
     * @param data byte array
     */
    fun setValue(container: FieldContainer, data: ByteArray?) {
        if (data != null) {
            container.set(m_type, if (MPPUtility.getInt(data, m_offset) and m_mask == 0) m_zeroValue else m_nonZeroValue)
        }
    }
}

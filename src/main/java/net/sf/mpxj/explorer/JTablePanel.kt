package net.sf.mpxj.explorer

import java.awt.GridLayout
import java.awt.Point
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.table.TableModel

/**
 * Presents a pair of JTables side by side in a panel.
 * The scrolling and selection for these tables are synchronized.
 */
class JTablePanel : JPanel() {
    private val m_leftTable: JTableExtra
    private val m_rightTable: JTableExtra

    /**
     * Retrieve the model used by the left table.
     *
     * @return table model
     */
    /**
     * Set the model used by the left table.
     *
     * @param model table model
     */
    var leftTableModel: TableModel
        get() = m_leftTable.getModel()
        set(model) {
            val old = m_leftTable.getModel()
            m_leftTable.setModel(model)
            firePropertyChange("leftTableModel", old, model)
        }

    /**
     * Retrieve the model used by the right table.
     *
     * @return table model
     */
    /**
     * Set the model used by the right table.
     *
     * @param model table model
     */
    var rightTableModel: TableModel
        get() = m_rightTable.getModel()
        set(model) {
            val old = m_rightTable.getModel()
            m_rightTable.setModel(model)
            firePropertyChange("rightTableModel", old, model)
        }

    /**
     * Retrieve the currently selected cell.
     *
     * @return selected cell
     */
    val selectedCell: Point
        get() = m_leftTable.selectedCell

    /**
     * Constructor.
     */
    init {
        setBorder(null)
        setLayout(GridLayout(1, 0, 0, 0))

        m_leftTable = JTableExtra()
        m_leftTable.setFillsViewportHeight(true)
        m_leftTable.setBorder(null)
        m_leftTable.setShowVerticalLines(false)
        m_leftTable.setShowHorizontalLines(false)
        m_leftTable.setShowGrid(false)
        m_leftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        m_leftTable.setCellSelectionEnabled(true)
        m_leftTable.setTableHeader(null)
        m_leftTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)

        val leftScrollPane = JScrollPane(m_leftTable)
        leftScrollPane.setBorder(null)
        add(leftScrollPane)

        m_rightTable = JTableExtra()
        m_rightTable.setFillsViewportHeight(true)
        m_rightTable.setBorder(null)
        m_rightTable.setShowVerticalLines(false)
        m_rightTable.setShowHorizontalLines(false)
        m_rightTable.setShowGrid(false)
        m_rightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        m_rightTable.setCellSelectionEnabled(true)
        m_rightTable.setTableHeader(null)
        m_rightTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)

        val rightScrollPane = JScrollPane(m_rightTable)
        rightScrollPane.setBorder(null)
        add(rightScrollPane)

        leftScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)
        leftScrollPane.getVerticalScrollBar().setModel(rightScrollPane.getVerticalScrollBar().getModel())
        leftScrollPane.getHorizontalScrollBar().setModel(rightScrollPane.getHorizontalScrollBar().getModel())
        m_leftTable.setSelectionModel(m_rightTable.getSelectionModel())
        m_leftTable.setColumnModel(m_rightTable.getColumnModel())

        m_leftTable.addPropertyChangeListener("selectedCell", object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                firePropertyChange(evt)
            }
        })

        m_rightTable.addPropertyChangeListener("selectedCell", object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                firePropertyChange(evt)
            }
        })
    }

    /**
     * Fire a property change event in response to a cell selection.
     *
     * @param evt event data
     */
    protected fun firePropertyChange(evt: PropertyChangeEvent) {
        firePropertyChange("selectedCell", evt.getOldValue(), evt.getNewValue())
    }
}

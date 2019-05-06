//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.09.18 at 02:35:45 PM BST
//

package net.sf.mpxj.primavera.schema

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java class for ProjectPrivilegesType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ProjectPrivilegesType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="AddEditDeleteRisks" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEPSActivityCodes" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditActivitiesExceptRelationships" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditProjectLevelLayouts" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteTemplateDocuments" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddProjectActivityCodes" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AdministerProjectExternalApplications" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="ApplyActuals" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="ApproveTimesheetsAsProjectManager" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AssignProjectBaselines" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="CheckInAndCheckOutProjects" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddProjects" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditWorkgroups" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="DeleteEPSActivityCodes" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="DeleteActivities" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="DeleteProjectActivityCodes" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="DeleteProjectDataWithTimesheetActuals" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="DeleteProjects" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="DeleteWorkgroups" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditActivityId" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditCommittedFlagForResourcePlanning" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditEPSActivityCodes" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteEPSExceptCostsAndFinancials" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditEPSCostsAndFinancials" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditFuturePeriods" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditPeriodPerformance" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditProjectActivityCodes" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteActivityRelationships" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteProjectCalendars" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditContractManagementProjectLink" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditProjectDetailsExceptCostsAndFinancials" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteExpenses" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditProjectReports" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteIssuesAndIssueThreshold" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteWBSExceptCostsAndFinancials" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditWBSCostsAndFinancials" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteWorkProductsAndDocuments" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteResourceAssignmentsForResourcePlanning" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteRoleAssignmentsForResourcePlanning" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="ImportAndViewContractManagerData" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="LevelResources" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteProjectBaselines" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditWorkspaceAndWorkgroupPreferences" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="MonitorProjectThresholds" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditActivityResourceRequests" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="PublishProjectWebsite" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="RunBaselineUpdate" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="RunGlobalChange" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="ScheduleProjects" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="StorePeriodPerformance" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="SummarizeProjects" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="ViewProjectCostsAndFinancials" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AddEditDeleteIssuesandIssueThresholds" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="AllowIntegrationwithERPSystem" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;element name="EditPublicationPriority" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProjectPrivilegesType", propOrder = {
    "addEditDeleteRisks",
    "addEPSActivityCodes",
    "addEditActivitiesExceptRelationships",
    "addEditProjectLevelLayouts",
    "addEditDeleteTemplateDocuments",
    "addProjectActivityCodes",
    "administerProjectExternalApplications",
    "applyActuals",
    "approveTimesheetsAsProjectManager",
    "assignProjectBaselines",
    "checkInAndCheckOutProjects",
    "addProjects",
    "addEditWorkgroups",
    "deleteEPSActivityCodes",
    "deleteActivities",
    "deleteProjectActivityCodes",
    "deleteProjectDataWithTimesheetActuals",
    "deleteProjects",
    "deleteWorkgroups",
    "editActivityId",
    "editCommittedFlagForResourcePlanning",
    "editEPSActivityCodes",
    "addEditDeleteEPSExceptCostsAndFinancials",
    "editEPSCostsAndFinancials",
    "editFuturePeriods",
    "editPeriodPerformance",
    "editProjectActivityCodes",
    "addEditDeleteActivityRelationships",
    "addEditDeleteProjectCalendars",
    "editContractManagementProjectLink",
    "editProjectDetailsExceptCostsAndFinancials",
    "addEditDeleteExpenses",
    "editProjectReports",
    "addEditDeleteIssuesAndIssueThreshold",
    "addEditDeleteWBSExceptCostsAndFinancials",
    "editWBSCostsAndFinancials",
    "addEditDeleteWorkProductsAndDocuments",
    "addEditDeleteResourceAssignmentsForResourcePlanning",
    "addEditDeleteRoleAssignmentsForResourcePlanning",
    "importAndViewContractManagerData",
    "levelResources",
    "addEditDeleteProjectBaselines",
    "editWorkspaceAndWorkgroupPreferences",
    "monitorProjectThresholds",
    "addEditActivityResourceRequests",
    "publishProjectWebsite",
    "runBaselineUpdate",
    "runGlobalChange",
    "scheduleProjects",
    "storePeriodPerformance",
    "summarizeProjects",
    "viewProjectCostsAndFinancials",
    "addEditDeleteIssuesandIssueThresholds",
    "allowIntegrationwithERPSystem",
    "editPublicationPriority"
})
class ProjectPrivilegesType {

    /**
     * Gets the value of the addEditDeleteRisks property.
     *
     */
    /**
     * Sets the value of the addEditDeleteRisks property.
     *
     */
    @XmlElement(name = "AddEditDeleteRisks")
    var isAddEditDeleteRisks: Boolean = false
    /**
     * Gets the value of the addEPSActivityCodes property.
     *
     */
    /**
     * Sets the value of the addEPSActivityCodes property.
     *
     */
    @XmlElement(name = "AddEPSActivityCodes")
    var isAddEPSActivityCodes: Boolean = false
    /**
     * Gets the value of the addEditActivitiesExceptRelationships property.
     *
     */
    /**
     * Sets the value of the addEditActivitiesExceptRelationships property.
     *
     */
    @XmlElement(name = "AddEditActivitiesExceptRelationships")
    var isAddEditActivitiesExceptRelationships: Boolean = false
    /**
     * Gets the value of the addEditProjectLevelLayouts property.
     *
     */
    /**
     * Sets the value of the addEditProjectLevelLayouts property.
     *
     */
    @XmlElement(name = "AddEditProjectLevelLayouts")
    var isAddEditProjectLevelLayouts: Boolean = false
    /**
     * Gets the value of the addEditDeleteTemplateDocuments property.
     *
     */
    /**
     * Sets the value of the addEditDeleteTemplateDocuments property.
     *
     */
    @XmlElement(name = "AddEditDeleteTemplateDocuments")
    var isAddEditDeleteTemplateDocuments: Boolean = false
    /**
     * Gets the value of the addProjectActivityCodes property.
     *
     */
    /**
     * Sets the value of the addProjectActivityCodes property.
     *
     */
    @XmlElement(name = "AddProjectActivityCodes")
    var isAddProjectActivityCodes: Boolean = false
    /**
     * Gets the value of the administerProjectExternalApplications property.
     *
     */
    /**
     * Sets the value of the administerProjectExternalApplications property.
     *
     */
    @XmlElement(name = "AdministerProjectExternalApplications")
    var isAdministerProjectExternalApplications: Boolean = false
    /**
     * Gets the value of the applyActuals property.
     *
     */
    /**
     * Sets the value of the applyActuals property.
     *
     */
    @XmlElement(name = "ApplyActuals")
    var isApplyActuals: Boolean = false
    /**
     * Gets the value of the approveTimesheetsAsProjectManager property.
     *
     */
    /**
     * Sets the value of the approveTimesheetsAsProjectManager property.
     *
     */
    @XmlElement(name = "ApproveTimesheetsAsProjectManager")
    var isApproveTimesheetsAsProjectManager: Boolean = false
    /**
     * Gets the value of the assignProjectBaselines property.
     *
     */
    /**
     * Sets the value of the assignProjectBaselines property.
     *
     */
    @XmlElement(name = "AssignProjectBaselines")
    var isAssignProjectBaselines: Boolean = false
    /**
     * Gets the value of the checkInAndCheckOutProjects property.
     *
     */
    /**
     * Sets the value of the checkInAndCheckOutProjects property.
     *
     */
    @XmlElement(name = "CheckInAndCheckOutProjects")
    var isCheckInAndCheckOutProjects: Boolean = false
    /**
     * Gets the value of the addProjects property.
     *
     */
    /**
     * Sets the value of the addProjects property.
     *
     */
    @XmlElement(name = "AddProjects")
    var isAddProjects: Boolean = false
    /**
     * Gets the value of the addEditWorkgroups property.
     *
     */
    /**
     * Sets the value of the addEditWorkgroups property.
     *
     */
    @XmlElement(name = "AddEditWorkgroups")
    var isAddEditWorkgroups: Boolean = false
    /**
     * Gets the value of the deleteEPSActivityCodes property.
     *
     */
    /**
     * Sets the value of the deleteEPSActivityCodes property.
     *
     */
    @XmlElement(name = "DeleteEPSActivityCodes")
    var isDeleteEPSActivityCodes: Boolean = false
    /**
     * Gets the value of the deleteActivities property.
     *
     */
    /**
     * Sets the value of the deleteActivities property.
     *
     */
    @XmlElement(name = "DeleteActivities")
    var isDeleteActivities: Boolean = false
    /**
     * Gets the value of the deleteProjectActivityCodes property.
     *
     */
    /**
     * Sets the value of the deleteProjectActivityCodes property.
     *
     */
    @XmlElement(name = "DeleteProjectActivityCodes")
    var isDeleteProjectActivityCodes: Boolean = false
    /**
     * Gets the value of the deleteProjectDataWithTimesheetActuals property.
     *
     */
    /**
     * Sets the value of the deleteProjectDataWithTimesheetActuals property.
     *
     */
    @XmlElement(name = "DeleteProjectDataWithTimesheetActuals")
    var isDeleteProjectDataWithTimesheetActuals: Boolean = false
    /**
     * Gets the value of the deleteProjects property.
     *
     */
    /**
     * Sets the value of the deleteProjects property.
     *
     */
    @XmlElement(name = "DeleteProjects")
    var isDeleteProjects: Boolean = false
    /**
     * Gets the value of the deleteWorkgroups property.
     *
     */
    /**
     * Sets the value of the deleteWorkgroups property.
     *
     */
    @XmlElement(name = "DeleteWorkgroups")
    var isDeleteWorkgroups: Boolean = false
    /**
     * Gets the value of the editActivityId property.
     *
     */
    /**
     * Sets the value of the editActivityId property.
     *
     */
    @XmlElement(name = "EditActivityId")
    var isEditActivityId: Boolean = false
    /**
     * Gets the value of the editCommittedFlagForResourcePlanning property.
     *
     */
    /**
     * Sets the value of the editCommittedFlagForResourcePlanning property.
     *
     */
    @XmlElement(name = "EditCommittedFlagForResourcePlanning")
    var isEditCommittedFlagForResourcePlanning: Boolean = false
    /**
     * Gets the value of the editEPSActivityCodes property.
     *
     */
    /**
     * Sets the value of the editEPSActivityCodes property.
     *
     */
    @XmlElement(name = "EditEPSActivityCodes")
    var isEditEPSActivityCodes: Boolean = false
    /**
     * Gets the value of the addEditDeleteEPSExceptCostsAndFinancials property.
     *
     */
    /**
     * Sets the value of the addEditDeleteEPSExceptCostsAndFinancials property.
     *
     */
    @XmlElement(name = "AddEditDeleteEPSExceptCostsAndFinancials")
    var isAddEditDeleteEPSExceptCostsAndFinancials: Boolean = false
    /**
     * Gets the value of the editEPSCostsAndFinancials property.
     *
     */
    /**
     * Sets the value of the editEPSCostsAndFinancials property.
     *
     */
    @XmlElement(name = "EditEPSCostsAndFinancials")
    var isEditEPSCostsAndFinancials: Boolean = false
    /**
     * Gets the value of the editFuturePeriods property.
     *
     */
    /**
     * Sets the value of the editFuturePeriods property.
     *
     */
    @XmlElement(name = "EditFuturePeriods")
    var isEditFuturePeriods: Boolean = false
    /**
     * Gets the value of the editPeriodPerformance property.
     *
     */
    /**
     * Sets the value of the editPeriodPerformance property.
     *
     */
    @XmlElement(name = "EditPeriodPerformance")
    var isEditPeriodPerformance: Boolean = false
    /**
     * Gets the value of the editProjectActivityCodes property.
     *
     */
    /**
     * Sets the value of the editProjectActivityCodes property.
     *
     */
    @XmlElement(name = "EditProjectActivityCodes")
    var isEditProjectActivityCodes: Boolean = false
    /**
     * Gets the value of the addEditDeleteActivityRelationships property.
     *
     */
    /**
     * Sets the value of the addEditDeleteActivityRelationships property.
     *
     */
    @XmlElement(name = "AddEditDeleteActivityRelationships")
    var isAddEditDeleteActivityRelationships: Boolean = false
    /**
     * Gets the value of the addEditDeleteProjectCalendars property.
     *
     */
    /**
     * Sets the value of the addEditDeleteProjectCalendars property.
     *
     */
    @XmlElement(name = "AddEditDeleteProjectCalendars")
    var isAddEditDeleteProjectCalendars: Boolean = false
    /**
     * Gets the value of the editContractManagementProjectLink property.
     *
     */
    /**
     * Sets the value of the editContractManagementProjectLink property.
     *
     */
    @XmlElement(name = "EditContractManagementProjectLink")
    var isEditContractManagementProjectLink: Boolean = false
    /**
     * Gets the value of the editProjectDetailsExceptCostsAndFinancials property.
     *
     */
    /**
     * Sets the value of the editProjectDetailsExceptCostsAndFinancials property.
     *
     */
    @XmlElement(name = "EditProjectDetailsExceptCostsAndFinancials")
    var isEditProjectDetailsExceptCostsAndFinancials: Boolean = false
    /**
     * Gets the value of the addEditDeleteExpenses property.
     *
     */
    /**
     * Sets the value of the addEditDeleteExpenses property.
     *
     */
    @XmlElement(name = "AddEditDeleteExpenses")
    var isAddEditDeleteExpenses: Boolean = false
    /**
     * Gets the value of the editProjectReports property.
     *
     */
    /**
     * Sets the value of the editProjectReports property.
     *
     */
    @XmlElement(name = "EditProjectReports")
    var isEditProjectReports: Boolean = false
    /**
     * Gets the value of the addEditDeleteIssuesAndIssueThreshold property.
     *
     */
    /**
     * Sets the value of the addEditDeleteIssuesAndIssueThreshold property.
     *
     */
    @XmlElement(name = "AddEditDeleteIssuesAndIssueThreshold")
    var isAddEditDeleteIssuesAndIssueThreshold: Boolean = false
    /**
     * Gets the value of the addEditDeleteWBSExceptCostsAndFinancials property.
     *
     */
    /**
     * Sets the value of the addEditDeleteWBSExceptCostsAndFinancials property.
     *
     */
    @XmlElement(name = "AddEditDeleteWBSExceptCostsAndFinancials")
    var isAddEditDeleteWBSExceptCostsAndFinancials: Boolean = false
    /**
     * Gets the value of the editWBSCostsAndFinancials property.
     *
     */
    /**
     * Sets the value of the editWBSCostsAndFinancials property.
     *
     */
    @XmlElement(name = "EditWBSCostsAndFinancials")
    var isEditWBSCostsAndFinancials: Boolean = false
    /**
     * Gets the value of the addEditDeleteWorkProductsAndDocuments property.
     *
     */
    /**
     * Sets the value of the addEditDeleteWorkProductsAndDocuments property.
     *
     */
    @XmlElement(name = "AddEditDeleteWorkProductsAndDocuments")
    var isAddEditDeleteWorkProductsAndDocuments: Boolean = false
    /**
     * Gets the value of the addEditDeleteResourceAssignmentsForResourcePlanning property.
     *
     */
    /**
     * Sets the value of the addEditDeleteResourceAssignmentsForResourcePlanning property.
     *
     */
    @XmlElement(name = "AddEditDeleteResourceAssignmentsForResourcePlanning")
    var isAddEditDeleteResourceAssignmentsForResourcePlanning: Boolean = false
    /**
     * Gets the value of the addEditDeleteRoleAssignmentsForResourcePlanning property.
     *
     */
    /**
     * Sets the value of the addEditDeleteRoleAssignmentsForResourcePlanning property.
     *
     */
    @XmlElement(name = "AddEditDeleteRoleAssignmentsForResourcePlanning")
    var isAddEditDeleteRoleAssignmentsForResourcePlanning: Boolean = false
    /**
     * Gets the value of the importAndViewContractManagerData property.
     *
     */
    /**
     * Sets the value of the importAndViewContractManagerData property.
     *
     */
    @XmlElement(name = "ImportAndViewContractManagerData")
    var isImportAndViewContractManagerData: Boolean = false
    /**
     * Gets the value of the levelResources property.
     *
     */
    /**
     * Sets the value of the levelResources property.
     *
     */
    @XmlElement(name = "LevelResources")
    var isLevelResources: Boolean = false
    /**
     * Gets the value of the addEditDeleteProjectBaselines property.
     *
     */
    /**
     * Sets the value of the addEditDeleteProjectBaselines property.
     *
     */
    @XmlElement(name = "AddEditDeleteProjectBaselines")
    var isAddEditDeleteProjectBaselines: Boolean = false
    /**
     * Gets the value of the editWorkspaceAndWorkgroupPreferences property.
     *
     */
    /**
     * Sets the value of the editWorkspaceAndWorkgroupPreferences property.
     *
     */
    @XmlElement(name = "EditWorkspaceAndWorkgroupPreferences")
    var isEditWorkspaceAndWorkgroupPreferences: Boolean = false
    /**
     * Gets the value of the monitorProjectThresholds property.
     *
     */
    /**
     * Sets the value of the monitorProjectThresholds property.
     *
     */
    @XmlElement(name = "MonitorProjectThresholds")
    var isMonitorProjectThresholds: Boolean = false
    /**
     * Gets the value of the addEditActivityResourceRequests property.
     *
     */
    /**
     * Sets the value of the addEditActivityResourceRequests property.
     *
     */
    @XmlElement(name = "AddEditActivityResourceRequests")
    var isAddEditActivityResourceRequests: Boolean = false
    /**
     * Gets the value of the publishProjectWebsite property.
     *
     */
    /**
     * Sets the value of the publishProjectWebsite property.
     *
     */
    @XmlElement(name = "PublishProjectWebsite")
    var isPublishProjectWebsite: Boolean = false
    /**
     * Gets the value of the runBaselineUpdate property.
     *
     */
    /**
     * Sets the value of the runBaselineUpdate property.
     *
     */
    @XmlElement(name = "RunBaselineUpdate")
    var isRunBaselineUpdate: Boolean = false
    /**
     * Gets the value of the runGlobalChange property.
     *
     */
    /**
     * Sets the value of the runGlobalChange property.
     *
     */
    @XmlElement(name = "RunGlobalChange")
    var isRunGlobalChange: Boolean = false
    /**
     * Gets the value of the scheduleProjects property.
     *
     */
    /**
     * Sets the value of the scheduleProjects property.
     *
     */
    @XmlElement(name = "ScheduleProjects")
    var isScheduleProjects: Boolean = false
    /**
     * Gets the value of the storePeriodPerformance property.
     *
     */
    /**
     * Sets the value of the storePeriodPerformance property.
     *
     */
    @XmlElement(name = "StorePeriodPerformance")
    var isStorePeriodPerformance: Boolean = false
    /**
     * Gets the value of the summarizeProjects property.
     *
     */
    /**
     * Sets the value of the summarizeProjects property.
     *
     */
    @XmlElement(name = "SummarizeProjects")
    var isSummarizeProjects: Boolean = false
    /**
     * Gets the value of the viewProjectCostsAndFinancials property.
     *
     */
    /**
     * Sets the value of the viewProjectCostsAndFinancials property.
     *
     */
    @XmlElement(name = "ViewProjectCostsAndFinancials")
    var isViewProjectCostsAndFinancials: Boolean = false
    /**
     * Gets the value of the addEditDeleteIssuesandIssueThresholds property.
     *
     */
    /**
     * Sets the value of the addEditDeleteIssuesandIssueThresholds property.
     *
     */
    @XmlElement(name = "AddEditDeleteIssuesandIssueThresholds")
    var isAddEditDeleteIssuesandIssueThresholds: Boolean = false
    /**
     * Gets the value of the allowIntegrationwithERPSystem property.
     *
     */
    /**
     * Sets the value of the allowIntegrationwithERPSystem property.
     *
     */
    @XmlElement(name = "AllowIntegrationwithERPSystem")
    var isAllowIntegrationwithERPSystem: Boolean = false
    /**
     * Gets the value of the editPublicationPriority property.
     *
     */
    /**
     * Sets the value of the editPublicationPriority property.
     *
     */
    @XmlElement(name = "EditPublicationPriority")
    var isEditPublicationPriority: Boolean = false

}

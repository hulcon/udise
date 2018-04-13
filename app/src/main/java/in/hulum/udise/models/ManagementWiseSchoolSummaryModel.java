package in.hulum.udise.models;

/**
 * Created by Irshad on 03-04-2018.
 */

public class ManagementWiseSchoolSummaryModel {

    /*
     * These two variables hold the Udise Code
     * and name of the entity (State, District, Zone or Cluster)
     */
    private String code;
    private String name;

    /*
     * This variable is used for storing code of
     * parent state,district or zone in case of
     * assembly constituency model
     */
    private String extraPayLoad;

    /**
     * This variable holds value for determining the type of
     * data this model contains like State, District, Zone, Cluster,
     * Assembly Constituency or Management Detail.
     * It contains one of the MODEL_TYPE values contained in the
     * {@link in.hulum.udise.utils.SchoolReportsConstants}
     */
    private int modelType;

    /*
     * Stores Summary heading in case of State, District, Zone, Cluster
     * or Assembly Constituency.
     *
     * Stores Management Name in case of management details
     */
    private String managementNameOrSummaryHeading;

    /*
     * Variables for storing number of schools
     */
    private int primarySchools;
    private int middleSchools;
    private int highSchools;
    private int higherSecondarySchools;
    private int flagIsSummaryOrManagementDetail;

    /*
     * Getters and Setters
     */

    public int getFlagIsSummaryOrManagementDetail() {
        return flagIsSummaryOrManagementDetail;
    }

    public void setFlagIsSummaryOrManagementDetail(int flagIsSummaryOrManagementDetail) {
        this.flagIsSummaryOrManagementDetail = flagIsSummaryOrManagementDetail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getModelType() {
        return modelType;
    }

    public void setModelType(int modelType) {
        this.modelType = modelType;
    }

    public String getManagementNameOrSummaryHeading() {
        return managementNameOrSummaryHeading;
    }

    public void setManagementNameOrSummaryHeading(String managementNameOrSummaryHeading) {
        this.managementNameOrSummaryHeading = managementNameOrSummaryHeading;
    }

    public int getPrimarySchools() {
        return primarySchools;
    }

    public void setPrimarySchools(int primarySchools) {
        this.primarySchools = primarySchools;
    }
    public void incrementPrimarySchools(){
        this.primarySchools = this.primarySchools + 1;
    }

    public int getMiddleSchools() {
        return middleSchools;
    }

    public void setMiddleSchools(int middleSchools) {
        this.middleSchools = middleSchools;
    }

    public void incrementMiddleSchools(){
        this.middleSchools = this.middleSchools + 1;
    }

    public int getHighSchools() {
        return highSchools;
    }

    public void setHighSchools(int highSchools) {
        this.highSchools = highSchools;
    }

    public void incrementHighSchools(){
        this.highSchools = this.highSchools + 1;
    }

    public int getHigherSecondarySchools() {
        return higherSecondarySchools;
    }

    public void setHigherSecondarySchools(int higherSecondarySchools) {
        this.higherSecondarySchools = higherSecondarySchools;
    }

    public void incrementHigherSecondarySchools(){
        this.higherSecondarySchools = this.higherSecondarySchools + 1;
    }

    public String getExtraPayLoad() {
        return extraPayLoad;
    }

    public void setExtraPayLoad(String extraPayLoad) {
        this.extraPayLoad = extraPayLoad;
    }
}

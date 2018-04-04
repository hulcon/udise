package in.hulum.udise.models;

/**
 * Created by Irshad on 03-04-2018.
 */

public class SchoolCountSummaryModel {

    private String stateName;
    private String districtCode;
    private String districtName;
    private String zoneCode;
    private String zoneName;
    private String clusterCode;
    private String clusterName;

    private int modelType;
    private String summaryHeading;
    private int primarySchools;
    private int middleSchools;
    private int highSchools;
    private int higherSecondarySchools;

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getModelType() {
        return modelType;
    }

    public void setModelType(int modelType) {
        this.modelType = modelType;
    }

    public String getSummaryHeading() {
        return summaryHeading;
    }

    public void setSummaryHeading(String summaryHeading) {
        this.summaryHeading = summaryHeading;
    }

    public int getPrimarySchools() {
        return primarySchools;
    }

    public void setPrimarySchools(int primarySchools) {
        this.primarySchools = primarySchools;
    }

    public int getMiddleSchools() {
        return middleSchools;
    }

    public void setMiddleSchools(int middleSchools) {
        this.middleSchools = middleSchools;
    }

    public int getHighSchools() {
        return highSchools;
    }

    public void setHighSchools(int highSchools) {
        this.highSchools = highSchools;
    }

    public int getHigherSecondarySchools() {
        return higherSecondarySchools;
    }

    public void setHigherSecondarySchools(int higherSecondarySchools) {
        this.higherSecondarySchools = higherSecondarySchools;
    }
}

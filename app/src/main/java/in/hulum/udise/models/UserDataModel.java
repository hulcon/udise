package in.hulum.udise.models;

import java.util.List;

/**
 * Created by Irshad on 20-03-2018.
 * This class contains data structures for Academic Years, States,
 * Districts and Zones
 * This class also contains a UserDataModel which contains the user type
 * determined from the data stored in SQLite database along with the list
 * of academic years, states, districts and zones found in the database.
 */

public class UserDataModel {
    /*
     * Constants for designating type of data found
     * in the SQLite database
     */
    public static final int USER_TYPE_NATIONAL = 10;
    public static final int USER_TYPE_STATE = 11;
    public static final int USER_TYPE_DISTRICT = 12;
    public static final int USER_TYPE_ZONE = 13;
    public static final int USER_TYPE_UNKNOWN = 0;


    /*
     * Variable containing type of user.
     * Hold one of the above mentioned constant values
     */
    private int userType;

    /*
     * Variables containing lists of states, districts, zones and academic years.
     * By creating a list of each of these entities, we can determine the what
     * kind of user (National/State/District/Zone) is using the app
     */
    List<States> statesList;
    List<Districts> districtsList;
    List<Zones> zoneList;
    List<AcademicYears> academicYearsList;


    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public List<States> getStatesList() {
        return statesList;
    }

    public void setStatesList(List<States> statesList) {
        this.statesList = statesList;
    }

    public List<Districts> getDistrictsList() {
        return districtsList;
    }

    public void setDistrictsList(List<Districts> districtsList) {
        this.districtsList = districtsList;
    }

    public List<Zones> getZoneList() {
        return zoneList;
    }

    public void setZoneList(List<Zones> zoneList) {
        this.zoneList = zoneList;
    }

    public List<AcademicYears> getAcademicYearsList() {
        return academicYearsList;
    }

    public void setAcademicYearsList(List<AcademicYears> academicYearsList) {
        this.academicYearsList = academicYearsList;
    }

    /*
                 * Inner class States for holding data of states
                 */
    public static class States{
        private String stateName;


        public String getStateName() {
            return stateName;
        }

        public void setStateName(String stateName) {
            this.stateName = stateName;
        }
    }

    /*
     * Inner class Districts for holding data of districts
     */
    public static class Districts{
        private String districtCode;
        private String districtName;

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
    }

    /*
     * Inner class Zones for holding data of zones
     */
    public static class Zones{
        private String zoneCode;
        private String zoneName;

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
    }

    public static class AcademicYears{
        private String ac_year;

        public String getAc_year() {
            return ac_year;
        }

        public void setAc_year(String ac_year) {
            this.ac_year = ac_year;
        }
    }
}

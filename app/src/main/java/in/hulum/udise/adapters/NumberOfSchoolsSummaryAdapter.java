package in.hulum.udise.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import in.hulum.udise.R;
import in.hulum.udise.models.ManagementWiseSchoolSummaryModel;
import in.hulum.udise.models.NumberOfSchoolsModel;
import in.hulum.udise.utils.SchoolReportsConstants;

/**
 * Created by Irshad on 02-04-2018.
 * This class contains the recyclerview adapter for displaying number of schools report
 */

public class NumberOfSchoolsSummaryAdapter extends RecyclerView.Adapter<NumberOfSchoolsSummaryAdapter.NumberOfSchoolsViewHolder>{

    private static final int VIEW_TYPE_SUMMARY = 1;
    private static final int VIEW_TYPE_MANAGEMENT_ITEM = 2;


    private final Context mContext;
    private List<ManagementWiseSchoolSummaryModel> mSchoolSummaryList;


    /*
     * Below, i've defined an interface to handle clicks on items within this adapter.
     * In the constructor of this adapter {NumberOfSchoolsSummaryAdapter}, an instance of a
     * class (NumberOfSchools) is received that has implemented said interface. We store that instance
     * in this variable to call the onClick method whenever an item is clicked in the list.
     */
    final private NumberOfSchoolsSummaryAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages
     */
    public interface NumberOfSchoolsSummaryAdapterOnClickHandler {
        void onClick(int reportDisplayLevel, String zoneDistrictOrStateCode, String zoneDistrictOrStateName);
    }

    private List<NumberOfSchoolsModel> numberOfSchoolsModelList;

    public NumberOfSchoolsSummaryAdapter(@NonNull Context context,NumberOfSchoolsSummaryAdapterOnClickHandler clickHandler){
        mContext = context;
        mClickHandler = clickHandler;
    }
    /**
     * Called when RecyclerView needs a new {@link android.support.v7.widget.RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int)
     */
    @Override
    public NumberOfSchoolsSummaryAdapter.NumberOfSchoolsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;

        switch (viewType){
            case VIEW_TYPE_SUMMARY:
                layoutId = R.layout.summary_list_item_number_of_schools;
                break;
            case VIEW_TYPE_MANAGEMENT_ITEM:
                layoutId = R.layout.summary_list_item_number_of_schools_management_wise;
                break;
            default:
                throw new IllegalArgumentException("Invalid view type passed to NumberOfSchools RecyclerView");
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId,parent,false);
        view.setFocusable(true);
        return new NumberOfSchoolsViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link android.support.v7.widget.RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link android.support.v7.widget.RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(NumberOfSchoolsSummaryAdapter.NumberOfSchoolsViewHolder holder, int position) {
        //mCursor.moveToPosition(position);
        Log.d("BindView","position is " + position);
        Log.d("BindView","Heading " + mSchoolSummaryList.get(position).getManagementNameOrSummaryHeading());

        holder.summaryCardViewTitle.setText(mSchoolSummaryList.get(position).getManagementNameOrSummaryHeading());


        holder.numberOfPrimarySchools.setText(Integer.toString(mSchoolSummaryList.get(position).getPrimarySchools()));
        Log.d("BindView","Primary " + mSchoolSummaryList.get(position).getPrimarySchools());

        Log.d("BindView","Middle " + mSchoolSummaryList.get(position).getMiddleSchools());
        holder.numberOfMiddleSchools.setText(Integer.toString(mSchoolSummaryList.get(position).getMiddleSchools()));
        holder.numberOfHighSchools.setText(Integer.toString(mSchoolSummaryList.get(position).getHighSchools()));
        holder.numberOfHigherSecondarySchools.setText(Integer.toString(mSchoolSummaryList.get(position).getHigherSecondarySchools()));
        int totalSchools = mSchoolSummaryList.get(position).getPrimarySchools() + mSchoolSummaryList.get(position).getMiddleSchools() +
                mSchoolSummaryList.get(position).getHighSchools() + mSchoolSummaryList.get(position).getHigherSecondarySchools();
        holder.totalSchools.setText(Integer.toString(totalSchools));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if(mSchoolSummaryList==null){
            return 0;
        }
        return mSchoolSummaryList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return VIEW_TYPE_SUMMARY;
        } else {
            return VIEW_TYPE_MANAGEMENT_ITEM;
        }
    }

    public void swapDataList(List<ManagementWiseSchoolSummaryModel> managementWiseSchoolSummaryModelList){
        mSchoolSummaryList = managementWiseSchoolSummaryModelList;
        notifyDataSetChanged();
    }

    /*
     * Inner class for recyclerview view holder
     */

    class NumberOfSchoolsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView summaryCardViewTitle;
        final TextView numberOfPrimarySchools;
        final TextView numberOfMiddleSchools;
        final TextView numberOfHighSchools;
        final TextView numberOfHigherSecondarySchools;
        final TextView totalSchools;

        NumberOfSchoolsViewHolder(View view){
            super(view);
            summaryCardViewTitle = view.findViewById(R.id.textview_summary_cardview_title);
            numberOfPrimarySchools = view.findViewById(R.id.textview_summary_number_of_primary_schools);
            numberOfMiddleSchools = view.findViewById(R.id.textview_summary_number_of_middle_schools);
            numberOfHighSchools = view.findViewById(R.id.textview_summary_number_of_high_schools);
            numberOfHigherSecondarySchools = view.findViewById(R.id.textview_summary_number_of_higher_secondary_schools);
            totalSchools = view.findViewById(R.id.textview_summary_total_schools);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            int model = mSchoolSummaryList.get(adapterPosition).getModelType();
            String code = null;
            String name = null;
            int reportDisplayLevel;
            switch (model){
                case SchoolReportsConstants.MODEL_TYPE_NATIONAL:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_STATEWISE;
                    // As there is no national code, no code is required to generate statewise reports
                    break;
                case SchoolReportsConstants.MODEL_TYPE_STATE:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE;
                    name = mSchoolSummaryList.get(adapterPosition).getStateName();
                    break;
                case SchoolReportsConstants.MODEL_TYPE_DISTRICT:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE;
                    code = mSchoolSummaryList.get(adapterPosition).getDistrictCode();
                    name = mSchoolSummaryList.get(adapterPosition).getDistrictName();
                    break;
                case SchoolReportsConstants.MODEL_TYPE_ZONE:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE;
                    code = mSchoolSummaryList.get(adapterPosition).getZoneCode();
                    name = mSchoolSummaryList.get(adapterPosition).getZoneName();
                    break;
                case SchoolReportsConstants.MODEL_TYPE_CLUSTER:
                    /*
                     * If we are viewing cluster level summary, we cannot go any deeper
                     * We will take no action on any clicks for this case
                     */
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_TAKE_NO_ACTION;
                    code = mSchoolSummaryList.get(adapterPosition).getClusterCode();
                    name = mSchoolSummaryList.get(adapterPosition).getClusterName();
                    break;
                default:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_INVALID;

            }
            mClickHandler.onClick(reportDisplayLevel,code,name);
        }
    }
}

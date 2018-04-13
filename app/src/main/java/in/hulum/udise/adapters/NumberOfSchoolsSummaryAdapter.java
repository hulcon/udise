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
import in.hulum.udise.utils.SchoolReportsConstants;

/**
 * Created by Irshad on 02-04-2018.
 * This class contains the recyclerview adapter for displaying number of schools report
 * This adapter is used by both {@link in.hulum.udise.NumberOfSchools} as well as
 * {@link in.hulum.udise.NumberOfSchoolsLevelWise} classes. So it is used both in
 * case of "Number of Schools Management Summary" reports as well as
 * "Number of Schools Level-Wise Summary" reports
 */

public class NumberOfSchoolsSummaryAdapter extends RecyclerView.Adapter<NumberOfSchoolsSummaryAdapter.NumberOfSchoolsViewHolder>{

    private static final int VIEW_TYPE_SUMMARY = 1;
    private static final int VIEW_TYPE_MANAGEMENT_ITEM = 2;


    private final Context mContext;
    private List<ManagementWiseSchoolSummaryModel> mSchoolSummaryList;

    private static final String TAG = "NumberOfSchoolsAdapter";


    /*
     * Below, i've defined an interface to handle clicks on items within this adapter.
     * In the constructor of this adapter {NumberOfSchoolsSummaryAdapter}, an instance of a
     * class (NumberOfSchools or NumberOfSchoolsLevelWise) is received that has implemented
     * said interface. We store that instance in this variable to call the onClick method
     * whenever an item is clicked in the list.
     */
    final private NumberOfSchoolsSummaryAdapterOnClickHandler mClickHandler;

    /*
     * This boolean flag is used to determine if we need to use the same layout
     * for all the items in the recyclerview or use two different layouts, one
     * for the first item and a second layout for the rest of the items.
     *
     * When displaying a management-wise summary like the summary of a district
     * this flag should be false. Because in such a case, the first item in the
     * recyclerview displays the summary of the whole district and the rest of the
     * items display the management wise details. So a different layout is used for
     * the first item (summary) and a different layout is used for the rest of the
     * items (management-wise details).
     *
     * When displaying level-wise list like zone-wise list summary, each entry
     * represents a similar entity, summary of a zone in this case. So each item must use
     * the same layout file. Hence this flag must be true in this case.
     */
    private boolean mShouldUseOneLayoutInsteadOfTwo;

    /**
     * The interface that receives onClick events
     */
    public interface NumberOfSchoolsSummaryAdapterOnClickHandler {
        void onClick(int reportDisplayLevel, String zoneDistrictOrStateCode, String zoneDistrictOrStateName,String parentCode);
    }


    /**
     * This method is the constructor for this Adapter.
     * @param context context of the calling class
     * @param clickHandler name of the class (typically calling class) which has the clickHandler
     *                     method. The clickHandler method simply processes the clicks on the
     *                     recyclerview items. Click events are forwarded to this clickHandler
     *                     through the interface {@link NumberOfSchoolsSummaryAdapterOnClickHandler}
     *                     of this class.
     * @param isLevelWiseModule a boolean representing whether this adapter is used to display
     *                          a management-wise summary or a level-wise list. In case of
     *                          management-wise summary the adapter uses two layouts, one for the
     *                          first item and the second one for the rest of the items. In case of
     *                          level-wise list, the adapter uses a single layout file for all the
     *                          items.
     */
    public NumberOfSchoolsSummaryAdapter(@NonNull Context context,NumberOfSchoolsSummaryAdapterOnClickHandler clickHandler,boolean isLevelWiseModule){
        mContext = context;
        mClickHandler = clickHandler;
        mShouldUseOneLayoutInsteadOfTwo = isLevelWiseModule;
    }


    /**
     * Called when RecyclerView needs a new {@link android.support.v7.widget.RecyclerView.ViewHolder}
     * of the given type to represent an item.
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

        /*
         * Determine whether we should use one or two layout files
         * to inflate the recyclerview items based on the argument
         * passed to this method.
         *
         * In case of management-wise summary the adapter uses two
         * layouts, one for the first item and the second one for
         * the rest of the items. In case of level-wise list, the
         * adapter uses a single layout file for all the items.
         */
        if(mShouldUseOneLayoutInsteadOfTwo){
            /*
             * If this is a level-wise summary list like a zone-wise list,
             * use a single layout file
             */
            layoutId = R.layout.summary_list_item_number_of_schools;

        } else{
            /*
             * Otherwise, use two layouts. One for the first item, the summary.
             * Another layout for the rest of the items, the management-wise
             * summary.
             */
            switch (viewType){
                case VIEW_TYPE_SUMMARY:
                    layoutId = R.layout.summary_list_item_number_of_schools;
                    break;
                case VIEW_TYPE_MANAGEMENT_ITEM:
                    layoutId = R.layout.summary_list_item_number_of_schools_management_wise;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid view type passed to NumberOfSchools RecyclerView onCreateViewHolder method");
            }
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

        holder.summaryCardViewTitle.setText(mSchoolSummaryList.get(position).getManagementNameOrSummaryHeading());
        holder.numberOfPrimarySchools.setText(Integer.toString(mSchoolSummaryList.get(position).getPrimarySchools()));
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

        /**
         * This gets called by the child views (item views) during a click.
         * We determine the type of item clicked and then call the
         * onClick handler registered with this adapter, passing the desired
         * level of report to be displayed, code, name and parent code
         * @param v the View that was clicked
         */

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            int model = mSchoolSummaryList.get(adapterPosition).getModelType();

            String code = null;
            String name = null;
            String parentCode = null;

            int reportDisplayLevel;
            switch (model){
                case SchoolReportsConstants.MODEL_TYPE_NATIONAL:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_STATEWISE;
                    // As there is no national code, no code is required to generate statewise reports
                    break;
                case SchoolReportsConstants.MODEL_TYPE_STATE:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE;
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    break;
                case SchoolReportsConstants.MODEL_TYPE_DISTRICT:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    break;
                case SchoolReportsConstants.MODEL_TYPE_ZONE:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    break;
                case SchoolReportsConstants.MODEL_TYPE_CLUSTER:
                    /*
                     * If we are viewing cluster level summary, we cannot go any deeper
                     * We will take no action on any clicks for this case
                     */
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_LEVEL_TAKE_NO_ACTION;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    break;

                    /*
                     * Level-wise report related cases start from here
                     */

                case SchoolReportsConstants.MODEL_TYPE_STATE_WISE_LIST:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_STATE_SUMMARY;
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    code = mSchoolSummaryList.get(adapterPosition).getName();
                    break;

                case SchoolReportsConstants.MODEL_TYPE_DISTRICT_WISE_LIST:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_DISTRICT_SUMMARY;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    break;

                case SchoolReportsConstants.MODEL_TYPE_ZONE_WISE_LIST:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_ZONE_SUMMARY;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    break;

                case SchoolReportsConstants.MODEL_TYPE_CLUSTER_WISE_LIST:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_CLUSTER_SUMMARY;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    break;

                /*
                 * Cases related to Assembly Constituency reports start from here
                 */
                case SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_STATE:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    parentCode = mSchoolSummaryList.get(adapterPosition).getExtraPayLoad();
                    break;

                case SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_DISTRICT:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    parentCode = mSchoolSummaryList.get(adapterPosition).getExtraPayLoad();
                    Log.d(TAG,"Assem Code " + code + " Assem name " + name);
                    break;

                case SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_ZONE:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE;
                    code = mSchoolSummaryList.get(adapterPosition).getCode();
                    name = mSchoolSummaryList.get(adapterPosition).getName();
                    parentCode = mSchoolSummaryList.get(adapterPosition).getExtraPayLoad();
                    Log.d(TAG,"Assem Code " + code + " Assem name " + name);
                    break;

                default:
                    reportDisplayLevel = SchoolReportsConstants.REPORT_DISPLAY_INVALID;
                    Log.e(TAG,"Invalid Model Type passed to the NumberOfSchoolsSummaryAdapter class with model code " + model);

            }
            mClickHandler.onClick(reportDisplayLevel,code,name,parentCode);
        }
    }
}

package com.ovit.app.map.bdc.ggqj.map.view.v;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZD;
import com.ovit.app.util.DateUtil;

/**
 * Created by Lichun on 2018/5/29.
 */

public class V_Task  extends com.ovit.app.map.view.V_Base{

    public MapInstance mapInstance;
    public V_Task(MapInstance mapInstance) {
        super(mapInstance);
        this.mapInstance = mapInstance;
    }

    ViewGroup m_view_task ;
    //region 我的待办已办
    public View getView_Task() {
        if (m_view_task == null) {
            updateTaskLayout();
        }
        return m_view_task;
    }

    // 更新项目数据
    public void updateTaskLayout() {
        if (m_view_task == null) {
            m_view_task = new LinearLayout(activity);
        } else {
            m_view_task.removeAllViews();
        }
        LinearLayout ll_view = (LinearLayout) LayoutInflater.from(activity).inflate(
                com.ovit.R.layout.app_ui_ai_aimap_opt_me, null);
        m_view_task.addView(ll_view);
        ll_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final TextView tv_header_jr = (TextView) ll_view.findViewById(com.ovit.R.id.tv_header_jr);
        final TextView tv_header_bz = (TextView) ll_view.findViewById(com.ovit.R.id.tv_header_bz);
        final TextView tv_header_by = (TextView) ll_view.findViewById(com.ovit.R.id.tv_header_by);
        final ListView lv_list = (ListView) ll_view.findViewById(com.ovit.R.id.lv_list);
        final FeatureViewZD fv = FeatureViewZD.From(mapInstance);
        View.OnClickListener listener_jr = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date_jr = String.valueOf(DateUtil.getDateToJD(DateUtil.getDayStartTime().getTime()));
                fv.buildListView(lv_list, " PRO_UPDATETIME >= '" + date_jr + "'  ", 0, null);
            }
        };
        View.OnClickListener listener_bz = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date_start = String.valueOf(DateUtil.getDateToJD(DateUtil.getWeekStartTime().getTime()));
                String date_end = String.valueOf(DateUtil.getDateToJD(DateUtil.getWeekEndTime(1).getTime()));
                fv.buildListView(lv_list, "PRO_UPDATETIME >= '" + date_start + "' and PRO_UPDATETIME < '" + date_end + "'", 0, null);
            }
        };
        View.OnClickListener listener_by = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date_start = String.valueOf(DateUtil.getDateToJD(DateUtil.getMonthStartTime().getTime()));
                String date_end = String.valueOf(DateUtil.getDateToJD(DateUtil.getMonthEndTime(1).getTime()));
                fv.buildListView(lv_list, "PRO_UPDATETIME >= '" + date_start + "' and PRO_UPDATETIME < '" + date_end + "'", 0, null);
            }
        };
        tv_header_jr.setOnClickListener(listener_jr);
        tv_header_bz.setOnClickListener(listener_bz);
        tv_header_by.setOnClickListener(listener_by);
        listener_jr.onClick(tv_header_jr);

    }

    //endregion
}

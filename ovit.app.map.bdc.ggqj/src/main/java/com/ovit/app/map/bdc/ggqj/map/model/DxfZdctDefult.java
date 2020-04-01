package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xw on 2020/4/1.
 */

public class DxfZdctDefult extends BaseDxf {
    private Feature f_zd;
    public DxfZdctDefult(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    protected void getHeader() throws Exception {
        getDefultHeader();
    }

    @Override
    protected void getBody() throws Exception {

    }

    @Override
    protected void getFooter() throws Exception {
        getDefultFooter();
    }

    @Override
    public double getHeightScale() {
        return 0;
    }

    @Override
    public double getWidthScale() {
        return 0;
    }

    @Override
    public Envelope getChildExtend() {
        Geometry buffer = GeometryEngine.buffer(f_zd.getGeometry(), 5);
        return MapHelper.geometry_get(buffer.getExtent(), spatialReference);

    }
    public DxfZdctDefult set(Feature f_zd){
        this.f_zd = f_zd;
        return this;
    }
}

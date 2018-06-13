/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package outlierdetection;

/**
 *
 * @author Nadian
 */
public class Point {
    private float[] mIndex; //1-6
    private float  mLabel;
    private String memberStatus;
    private float distToCentroid;

    public Point() {
    }
    
    public Point(float[] mIndex) {
        this.mIndex = mIndex;
    }
    
    public Point(float[] mIndex, float mLabel) {
        this.mIndex = mIndex;
        this.mLabel = mLabel;
    }

    public float getDistToCentroid() {
        return distToCentroid;
    }

    public void setDistToCentroid(float distToCentroid) {
        this.distToCentroid = distToCentroid;
    }
    
    

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }
    
    

    public float[] getmIndex() {
        return mIndex;
    }

    public void setmIndex(float[] mIndex) {
        this.mIndex = mIndex;
    }

    public float getmLabel() {
        return mLabel;
    }

    public void setmLabel(float mLabel) {
        this.mLabel = mLabel;
    }
}

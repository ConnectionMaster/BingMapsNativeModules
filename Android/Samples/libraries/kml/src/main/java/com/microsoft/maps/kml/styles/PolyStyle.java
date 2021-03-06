// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml.styles;

public class PolyStyle {

  private boolean mShouldFill = true;
  private boolean mUseFillTag = false;
  private boolean mShouldOutline = true;
  private boolean mUseOutlineTag = false;
  private int mFillColor = 0xffffffff;

  private static final int TRANSPARENT = 0x00ffffff;

  public PolyStyle() {}

  public void setShouldFill(boolean shouldFill) {
    mShouldFill = shouldFill;
  }

  public boolean shouldFill() {
    return mShouldFill;
  }

  public void setShouldOutline(boolean shouldOutline) {
    mShouldOutline = shouldOutline;
  }

  public boolean shouldOutline() {
    return mShouldOutline;
  }

  public void setFillColor(int fillColor) {
    mFillColor = fillColor;
  }

  public int getFillColor() {
    return mFillColor;
  }

  public int getTransparent() {
    return TRANSPARENT;
  }

  public boolean useFillTag() {
    return mUseFillTag;
  }

  public void setUseFillTag(boolean useFillTag) {
    mUseFillTag = useFillTag;
  }

  public boolean useOutlineTag() {
    return mUseOutlineTag;
  }

  public void setUseOutlineTag(boolean useOutlineTag) {
    mUseOutlineTag = useOutlineTag;
  }
}

// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.kml.styles.IconStyle;
import com.microsoft.maps.kml.styles.LineStyle;
import com.microsoft.maps.kml.styles.PolyStyle;
import com.microsoft.maps.kml.styles.StylesHolder;
import com.microsoft.maps.moduletools.AltitudeReferenceSystemWrapper;
import com.microsoft.maps.moduletools.DefaultMapFactories;
import com.microsoft.maps.moduletools.MapFactories;
import com.microsoft.maps.moduletools.ParsingHelpers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class that parses KML and returns a new MapElementLayer containing all the shapes outlined in the
 * KML.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 07/21/2020
 */
public class KMLParser {

  private final MapElementLayer mLayer;
  private final MapFactories mFactory;
  private boolean mDidWarn;
  private String mNameSpace;
  private final XmlPullParser mParser = Xml.newPullParser();
  private final Map<String, StylesHolder> mSharedStyles = new HashMap<>();
  private final Map<String, ArrayList<MapElement>> mMapElementStyles = new HashMap<>();
  private final Map<String, String> mKmlStyleMap = new HashMap<>();
  private final Map<MapElement, StylesHolder> mInlineStyles = new HashMap<>();
  private final Map<StylesHolder, String> mMergeStyles = new HashMap<>();

  private static final MapFactories DEFAULT_MAP_FACTORIES = new DefaultMapFactories();

  @VisibleForTesting
  KMLParser(@NonNull MapFactories factory) {
    mFactory = factory;
    mLayer = mFactory.createMapElementLayer();
  }

  /**
   * Method to parse given kml and return MapElementLayer containing the shapes outlined in the kml.
   * Note: If the KML may contain references to external resources, parse should not be called on
   * the UI thread. The external resources will be downloaded synchronously.
   *
   * @param kml input String
   * @return MapElementLayer
   * @throws KMLParseException
   */
  @NonNull
  public static MapElementLayer parse(@NonNull String kml) throws KMLParseException {
    if (kml == null) {
      throw new IllegalArgumentException("Input String cannot be null.");
    }
    if (kml.equals("")) {
      throw new KMLParseException("Input String cannot be empty.");
    }
    KMLParser instance = new KMLParser(DEFAULT_MAP_FACTORIES);
    try {
      return instance.internalParse(kml);
    } catch (Exception e) {
      throw new KMLParseException(e.getMessage());
    }
  }

  @VisibleForTesting
  @NonNull
  MapElementLayer internalParse(@NonNull String kml)
      throws XmlPullParserException, IOException, KMLParseException {
    try (InputStream stream = new ByteArrayInputStream(kml.getBytes(UTF_8))) {
      mParser.setInput(stream, null);
      mParser.nextTag();
      mNameSpace = mParser.getNamespace();
      parseOuterLayer();
      mergeSharedStyleIntoInlineStyle();
      applyStyles();
    }
    return mLayer;
  }

  private void parseOuterLayer() throws IOException, XmlPullParserException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      switch (type) {
        case "Placemark":
          parsePlacemark();
          break;
        case "Style":
          parseStyleAddToMapStylesHolders();
          break;
        case "StyleMap":
          parseStyleMap();
          break;
        case "Document":
        case "Folder":
          parseOuterLayer();
          break;
        default:
          skipToEndOfTag();
          break;
      }
    }
  }

  private void parseStyleAddToMapStylesHolders()
      throws XmlPullParserException, IOException, KMLParseException {
    String id = mParser.getAttributeValue(null, "id");
    verifyIdNotInMap(mSharedStyles, id);
    mSharedStyles.put(id, parseStyle());
  }

  @NonNull
  private StylesHolder parseStyle() throws XmlPullParserException, IOException, KMLParseException {
    StylesHolder stylesHolder = new StylesHolder();
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      switch (type) {
        case "IconStyle":
          parseIconStyle(stylesHolder.getIconStyle());
          break;
        case "LineStyle":
          parseLineStyle(stylesHolder.getLineStyle());
          break;
        case "PolyStyle":
          parsePolyStyle(stylesHolder.getPolyStyle());
          break;
        default:
          skipToEndOfTag();
          break;
      }
    }
    return stylesHolder;
  }

  private void parseIconStyle(@NonNull IconStyle iconStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      if (mParser.getName().equals("Icon")) {
        parseIcon(iconStyle);
      } else {
        skipToEndOfTag();
      }
    }
  }

  private void parseIcon(@NonNull IconStyle iconStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      if (mParser.getName().equals("href")) {
        String url = parseText();
        InputStream inputStream = new URL(url).openConnection().getInputStream();
        iconStyle.setImage(mFactory.createMapImage(inputStream));
      }
    }
  }

  private void parseLineStyle(@NonNull LineStyle lineStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("width")) {
        double parsedWidth = Double.parseDouble(parseText());
        if (Double.isNaN(parsedWidth)) {
          throw new KMLParseException(
              "Error at: " + mParser.getPositionDescription() + " width cannot be NaN.");
        }
        int width = (int) parsedWidth;
        if (width <= 0) {
          throw new KMLParseException(
              "Error at: "
                  + mParser.getPositionDescription()
                  + " width must be greater than 0."
                  + "Instead saw int value: "
                  + width);
        }
        lineStyle.setWidth(width);
        lineStyle.setUseWidth(true);
      } else if (type.equals("color")) {
        lineStyle.setUseStrokeColor(true);
        lineStyle.setStrokeColor(parseColor());
      } else {
        skipToEndOfTag();
      }
    }
  }

  private void parsePolyStyle(@NonNull PolyStyle polyStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      switch (mParser.getName()) {
        case "fill":
          polyStyle.setShouldFill(parseBoolean());
          polyStyle.setUseFillTag(true);
          break;
        case "outline":
          polyStyle.setShouldOutline(parseBoolean());
          polyStyle.setUseOutlineTag(true);
          break;
        case "color":
          polyStyle.setFillColor(parseColor());
          break;
        default:
          skipToEndOfTag();
          break;
      }
    }
  }

  private void parseStyleMap() throws XmlPullParserException, IOException, KMLParseException {
    String id = mParser.getAttributeValue(null, "id");
    int numPair = 0;
    verifyIdNotInMap(mSharedStyles, id);
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      if (mParser.getName().equals("Pair")) {
        parsePair(id);
        numPair++;
      } else {
        skipToEndOfTag();
      }
    }
    if (numPair < 2) {
      throw new KMLParseException(
          "Error around: "
              + mParser.getPositionDescription()
              + " A StyleMap element should have two Pair elements. Instead saw: "
              + numPair
              + ".");
    }
  }

  private void parsePair(@NonNull String styleMapId)
      throws XmlPullParserException, IOException, KMLParseException {
    String key = null;
    String styleUrl = null;
    StylesHolder stylesHolder = null;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      switch (type) {
        case "key":
          String text = parseText();
          if (!text.equals("normal") && !text.equals("highlight")) {
            throw new KMLParseException(
                "Error around: "
                    + mParser.getPositionDescription()
                    + " A key element must have a value of either \"normal\" or \"highlight\" "
                    + "Instead saw: "
                    + text);
          }
          key = text;
          break;
        case "styleUrl":
          styleUrl = parseStyleUrl();
          break;
        case "Style":
          stylesHolder = parseStyle();
          break;
        default:
          skipToEndOfTag();
          break;
      }
    }
    if (key != null && key.equals("normal")) {
      if (styleUrl != null) {
        mKmlStyleMap.put(styleMapId, styleUrl);
      } else if (stylesHolder != null) {
        verifyIdNotInMap(mSharedStyles, key);
        mSharedStyles.put(styleMapId, stylesHolder);
      }
    }
  }

  private void parsePlacemark() throws IOException, XmlPullParserException, KMLParseException {
    String title = null;
    MapElement element = null;
    String styleId = null;
    StylesHolder stylesHolder = null;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      switch (mParser.getName()) {
        case "name":
          mParser.require(XmlPullParser.START_TAG, mNameSpace, "name");
          title = parseText();
          mParser.require(XmlPullParser.END_TAG, mNameSpace, "name");
          break;
        case "styleUrl":
          styleId = parseStyleUrl();
          break;
        case "Style":
          stylesHolder = parseStyle();
          break;
        case "MultiGeometry":
          parseMultiGeometry();
          break;
        default:
          element = parseGeometryIfApplicable();
          break;
      }
    }
    if (element != null) {
      if (title != null && element instanceof MapIcon) {
        ((MapIcon) element).setTitle(title);
      }
      if (stylesHolder != null) {
        mInlineStyles.put(element, stylesHolder);
        if (styleId != null) {
          mMergeStyles.put(stylesHolder, styleId);
        }
      } else {
        if (styleId != null) {
          ArrayList<MapElement> stylesList = mMapElementStyles.get(styleId);
          if (stylesList == null) {
            stylesList = new ArrayList<>();
            mMapElementStyles.put(styleId, stylesList);
          }
          stylesList.add(element);
        }
      }
      mLayer.getElements().add(element);
    }
  }

  @Nullable
  private String parseStyleUrl() throws XmlPullParserException, IOException, KMLParseException {
    String url = parseText();
    if (url.indexOf('#') == 0) {
      return url.substring(1);
    }
    return null;
  }

  private void parseMultiGeometry() throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      MapElement element = parseGeometryIfApplicable();
      if (element != null) {
        mLayer.getElements().add(element);
      }
    }
  }

  @Nullable
  private MapElement parseGeometryIfApplicable()
      throws XmlPullParserException, IOException, KMLParseException {
    switch (mParser.getName()) {
      case "Point":
        return parsePoint();
      case "LineString":
        return parseLineString();
      case "Polygon":
        return parsePolygon();
      default:
        skipToEndOfTag();
        return null;
    }
  }

  @NonNull
  private MapIcon parsePoint() throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "Point");
    MapIcon icon = mFactory.createMapIcon();
    boolean hasParsedCoordinates = false;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("coordinates")) {
        verifyElementNotSeen("coordinates", hasParsedCoordinates);
        AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
            new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.GEOID);
        ArrayList<Geoposition> coordinates = parseCoordinates(altitudeReferenceSystemWrapper);
        if (coordinates.size() > 1) {
          throw new KMLParseException(
              "coordinates for a Point can only contain one position. Instead saw: "
                  + coordinates.size()
                  + " at position: "
                  + mParser.getPositionDescription());
        }
        icon.setLocation(
            new Geopoint(
                coordinates.get(0), altitudeReferenceSystemWrapper.getAltitudeReferenceSystem()));
        hasParsedCoordinates = true;
      } else {
        skipToEndOfTag();
      }
    }
    verifyElementSeen("coordinates", hasParsedCoordinates);
    return icon;
  }

  @NonNull
  private MapPolyline parseLineString()
      throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "LineString");
    MapPolyline line = mFactory.createMapPolyline();
    // set default kml strokeColor
    line.setStrokeColor(0xffffffff);
    boolean hasParsedCoordinates = false;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("coordinates")) {
        verifyElementNotSeen("coordinates", hasParsedCoordinates);
        AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
            new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.GEOID);
        ArrayList<Geoposition> positions = parseCoordinates(altitudeReferenceSystemWrapper);
        if (positions.size() < 2) {
          throw new KMLParseException(
              "coordinates for a LineString must contain at least two positions. Instead saw: "
                  + positions.size()
                  + " at position: "
                  + mParser.getPositionDescription());
        }
        ParsingHelpers.setAltitudesToZeroIfAtSurface(
            positions, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
        line.setPath(
            new Geopath(positions, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem()));
        hasParsedCoordinates = true;
      } else {
        skipToEndOfTag();
      }
    }
    verifyElementSeen("coordinates", hasParsedCoordinates);
    return line;
  }

  /* A Polygon MUST have only one outer boundary, and a Polygon may have 0 or more
   * inner boundaries.
   */
  @NonNull
  private MapPolygon parsePolygon() throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "Polygon");
    MapPolygon polygon = mFactory.createMapPolygon();
    // set default kml colors
    polygon.setStrokeColor(0xffffffff);
    polygon.setFillColor(0xffffffff);
    ArrayList<ArrayList<Geoposition>> rings = new ArrayList<>();
    boolean hasOuterBoundary = false;
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.GEOID);
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("outerBoundaryIs") || type.equals("innerBoundaryIs")) {
        if (type.equals("outerBoundaryIs")) {
          verifyElementNotSeen("outerBoundaryIs", hasOuterBoundary);
          hasOuterBoundary = true;
        }
        rings.add(parsePolygonRing(type, altitudeReferenceSystemWrapper));
      } else {
        skipToEndOfTag();
      }
    }
    verifyElementSeen("outerBoundaryIs", hasOuterBoundary);
    ArrayList<Geopath> paths = new ArrayList<>(rings.size());
    for (ArrayList<Geoposition> ring : rings) {
      ParsingHelpers.setAltitudesToZeroIfAtSurface(
          ring, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
      paths.add(new Geopath(ring, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem()));
    }
    polygon.setPaths(paths);
    return polygon;
  }

  /* ArrayList positions is initialized by parseCoordinates, or an error is thrown if no
   * <coordinates> tag is present. */
  @NonNull
  private ArrayList<Geoposition> parsePolygonRing(
      @NonNull String tag, @NonNull AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper)
      throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, tag);
    mParser.nextTag();
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "LinearRing");
    ArrayList<Geoposition> positions = null;
    boolean hasParsedCoordinates = false;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("coordinates")) {
        verifyElementNotSeen(type, hasParsedCoordinates);
        positions = parseCoordinates(altitudeReferenceSystemWrapper);
        String exceptionMessage = ParsingHelpers.getErrorMessageForPolygonRing(positions);
        if (exceptionMessage != null) {
          throw new KMLParseException(
              "Error at: " + mParser.getPositionDescription() + " " + exceptionMessage);
        }
        hasParsedCoordinates = true;
      }
    }
    verifyElementSeen("coordinates", hasParsedCoordinates);
    mParser.require(XmlPullParser.END_TAG, mNameSpace, "LinearRing");
    mParser.nextTag();
    mParser.require(XmlPullParser.END_TAG, mNameSpace, tag);
    return positions;
  }

  /* parseCoordinates throws an error if coordinates given are not valid.
   * Otherwise, the returned ArrayList will contain at least one Geoposition. */
  @NonNull
  private ArrayList<Geoposition> parseCoordinates(
      @NonNull AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper)
      throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "coordinates");
    String coordinates = parseText();
    String[] allCoordinates = coordinates.split("\\s+");
    ArrayList<Geoposition> positions = new ArrayList<>(allCoordinates.length);
    for (String str : allCoordinates) {
      String[] latLongAlt = str.split(",");
      if (latLongAlt.length < 2) {
        throw new KMLParseException(
            "Error at: "
                + mParser.getPositionDescription()
                + " coordinates must contain at least latitude and longitude, separated by only a comma.");
      }
      double longitude = Double.parseDouble(latLongAlt[0]);
      if (Double.isNaN(longitude)) {
        throw new KMLParseException(
            "Error at: " + mParser.getPositionDescription() + " longitude cannot be NaN.");
      }
      if (longitude < -180 || longitude > 180) {
        throw new KMLParseException(
            "Longitude must be in the range [-180, 180], instead saw: "
                + longitude
                + " at position: "
                + mParser.getPositionDescription());
      }
      double latitude = Double.parseDouble(latLongAlt[1]);
      if (Double.isNaN(latitude)) {
        throw new KMLParseException(
            "Error at: " + mParser.getPositionDescription() + " latitude cannot be NaN.");
      }
      if (latitude < -90 || latitude > 90) {
        throw new KMLParseException(
            "Latitude must be in the range [-90, 90], instead saw: "
                + latitude
                + " at position: "
                + mParser.getPositionDescription());
      }
      double altitude = 0;
      if (latLongAlt.length > 2) {
        altitude = Double.parseDouble(latLongAlt[2]);
        if (Double.isNaN(altitude)) {
          throw new KMLParseException(
              "Error at: " + mParser.getPositionDescription() + " altitude cannot be NaN.");
        }
      } else {
        altitudeReferenceSystemWrapper.setAltitudeReferenceSystem(AltitudeReferenceSystem.SURFACE);
        if (!mDidWarn) {
          ParsingHelpers.logAltitudeWarning();
          mDidWarn = true;
        }
      }
      positions.add(new Geoposition(latitude, longitude, altitude));
    }
    mParser.require(XmlPullParser.END_TAG, mNameSpace, "coordinates");
    return positions;
  }

  @NonNull
  private String parseText() throws IOException, XmlPullParserException, KMLParseException {
    if (mParser.next() != XmlPullParser.TEXT) {
      throw new KMLParseException("Expected TEXT at position: " + mParser.getPositionDescription());
    } else {
      String result = mParser.getText().trim();
      mParser.nextTag();
      return result;
    }
  }

  private boolean parseBoolean() throws XmlPullParserException, IOException, KMLParseException {
    String text = parseText();
    if (text.equals("1") || text.equalsIgnoreCase("true")) {
      return true;
    }
    if (text.equals("0") || text.equalsIgnoreCase("false")) {
      return false;
    }
    throw new KMLParseException(
        "Invalid value (" + text + ") at position " + mParser.getPositionDescription());
  }

  private int parseColor() throws XmlPullParserException, IOException, KMLParseException {
    long alphaBlueGreenRed = Long.parseLong(parseText(), 16);
    moveToNext();
    return formatColorForMapControl((int) alphaBlueGreenRed);
  }

  private static int formatColorForMapControl(int oldColor) {
    int newColor = oldColor;
    newColor = newColor & 0xFF00FF00;
    newColor = ((oldColor & 0xFF) << 16) | newColor;
    return ((oldColor & 0x00FF0000) >> 16) | newColor;
  }

  /* This method expects to begin at a start tag. If it does not see a start tag to begin with, the
   * XML is malformed and an exception is thrown.*/
  private void skipToEndOfTag() throws XmlPullParserException, IOException, KMLParseException {
    if (mParser.getEventType() != XmlPullParser.START_TAG) {
      throw new KMLParseException(
          "Expected start tag at position: " + mParser.getPositionDescription());
    }
    int depth = 1;
    while (depth != 0) {
      switch (moveToNext()) {
        case XmlPullParser.END_TAG:
          depth--;
          break;
        case XmlPullParser.START_TAG:
          depth++;
          break;
        default:
          break;
      }
    }
  }

  private void verifyElementNotSeen(@NonNull String tag, boolean hasSeenTag)
      throws KMLParseException {
    if (hasSeenTag) {
      throw new KMLParseException(
          "Error at: + "
              + mParser.getPositionDescription()
              + " Geometry Object can only contain one"
              + tag
              + " element.");
    }
  }

  private void verifyElementSeen(@NonNull String tag, boolean hasSeenTag) throws KMLParseException {
    if (!hasSeenTag) {
      throw new KMLParseException(
          "Geometry Object must contain "
              + tag
              + " element around XML position "
              + mParser.getPositionDescription());
    }
  }

  private void verifyIdNotInMap(Map map, String id) throws KMLParseException {
    if (map.containsKey(id)) {
      throw new KMLParseException(
          "Error at: " + mParser.getPositionDescription() + " ID " + id + " already seen.");
    }
  }

  /* When style tags are set by the shared style and not set by the inline style, the
   * inline style inherits those values set by the shared style.*/
  private void mergeSharedStyleIntoInlineStyle() {
    for (StylesHolder inlineStyle : mMergeStyles.keySet()) {
      StylesHolder sharedStyle = mSharedStyles.get(mMergeStyles.get(inlineStyle));
      if (sharedStyle.getIconStyle().getImage() != null) {
        inlineStyle.getIconStyle().setImage(sharedStyle.getIconStyle().getImage());
      }
      LineStyle sharedLineStyle = sharedStyle.getLineStyle();
      LineStyle inlineLineStyle = inlineStyle.getLineStyle();
      if (!inlineLineStyle.useWidth() && sharedLineStyle.useWidth()) {
        inlineLineStyle.setWidth(sharedLineStyle.getWidth());
        inlineLineStyle.setUseWidth(true);
      }
      if (!inlineLineStyle.useStrokeColor() && sharedLineStyle.useStrokeColor()) {
        inlineLineStyle.setStrokeColor(sharedLineStyle.getStrokeColor());
        inlineLineStyle.setUseStrokeColor(true);
      }
      PolyStyle sharedPolyStyle = sharedStyle.getPolyStyle();
      PolyStyle inlinePolyStyle = inlineStyle.getPolyStyle();
      if (!inlinePolyStyle.useFillTag() && sharedPolyStyle.useFillTag()) {
        inlinePolyStyle.setShouldFill(sharedPolyStyle.shouldFill());
        inlinePolyStyle.setUseFillTag(true);
      }
      if (!inlinePolyStyle.useOutlineTag() && sharedPolyStyle.useOutlineTag()) {
        inlinePolyStyle.setShouldOutline(sharedPolyStyle.shouldOutline());
        inlinePolyStyle.setUseOutlineTag(true);
      }
    }
  }

  private void applyStyles() throws KMLParseException {
    for (String id : mMapElementStyles.keySet()) {
      // The styleUrl of a Placemark can either point to a Style element or a StyleMap element.
      // The id is first looked for in the SharedStyles map (which holds all individual shared Style
      // elements). If the id is not found, it is looked for in the StyleMaps map. The StyleMap
      // points to a Style element id. If the id is not found in either map, an exception is thrown.
      StylesHolder stylesHolder = mSharedStyles.get(id);
      if (stylesHolder == null) {
        stylesHolder = mSharedStyles.get(mKmlStyleMap.get(id));
      }
      if (stylesHolder == null) {
        throw new KMLParseException("Style id " + id + " not found.");
      }
      for (MapElement element : mMapElementStyles.get(id)) {
        applyStyles(element, stylesHolder);
      }
    }
    for (MapElement element : mInlineStyles.keySet()) {
      applyStyles(element, mInlineStyles.get(element));
    }
  }

  private void applyStyles(@NonNull MapElement element, @NonNull StylesHolder stylesHolder)
      throws KMLParseException {
    if (element instanceof MapIcon) {
      ((MapIcon) element).setImage(stylesHolder.getIconStyle().getImage());
    } else if (element instanceof MapPolyline) {
      MapPolyline line = (MapPolyline) element;
      LineStyle lineStyle = stylesHolder.getLineStyle();
      if (lineStyle.useWidth()) {
        line.setStrokeWidth(lineStyle.getWidth());
      }
      if (lineStyle.useStrokeColor()) {
        line.setStrokeColor(lineStyle.getStrokeColor());
      }
    } else if (element instanceof MapPolygon) {
      MapPolygon polygon = (MapPolygon) element;
      LineStyle lineStyle = stylesHolder.getLineStyle();
      PolyStyle polyStyle = stylesHolder.getPolyStyle();
      if (polyStyle.shouldOutline()) {
        if (lineStyle.useStrokeColor()) {
          polygon.setStrokeColor(lineStyle.getStrokeColor());
        }
        if (lineStyle.useWidth()) {
          polygon.setStrokeWidth(lineStyle.getWidth());
        }
      } else {
        polygon.setStrokeWidth(0);
      }
      if (polyStyle.shouldFill()) {
        polygon.setFillColor(polyStyle.getFillColor());
      } else {
        polygon.setFillColor(polyStyle.getTransparent());
      }
    }
  }

  private int moveToNext() throws IOException, XmlPullParserException, KMLParseException {
    int eventType = mParser.next();
    if (eventType == XmlPullParser.END_DOCUMENT) {
      throw new KMLParseException(
          "Unexpected end of document around position " + mParser.getPositionDescription());
    }
    return eventType;
  }
}

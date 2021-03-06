// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockBingMapsLoader;
import com.microsoft.maps.MockMapElementCollection;
import com.microsoft.maps.moduletools.MapFactories;
import com.microsoft.maps.moduletoolstest.MockParserMapFactories;
import com.microsoft.maps.moduletoolstest.TestHelpers;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParserException;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class KMLParserTest {

  private static final MapFactories MOCK_MAP_FACTORIES = new MockParserMapFactories();

  @Before
  public void setup() {
    MockBingMapsLoader.mockInitialize();
  }

  @Test
  public void testNoNameText() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentFirstLine() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!--Cool comment-->"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentLastLine() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>\n"
            + "<!--Cool comment-->";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentAfterXmlDeclaration()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<!--Cool comment-->"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentAfterPlacemark()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "<!--Cool comment-->"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentNameText() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name><!--Cool comment-->city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,9\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43, 9};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.GEOID, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
  }

  @Test
  public void testParsePoint() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
  }

  @Test
  public void testExtraCoordinates() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,7,98,6\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43, 7};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.GEOID, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
  }

  @Test
  public void testParseMultiplePlacemarksPoint()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <TagToSkip></TagToSkip>"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,45\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "<Placemark>\n"
            + "    <name>city2</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -109.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {-109.55, 43}};
    String[] expectedTitles = {"city", "city2"};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapIcon icon = (MapIcon) element;
      TestHelpers.assertPositionEquals(expectedPoints[index], icon.getLocation().getPosition());
      assertEquals(
          AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
      assertEquals(expectedTitles[index], icon.getTitle());
      index++;
    }
  }

  @Test
  public void testCoordinatesNotLastTag()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98 67,78\n"
            + "        </coordinates>\n"
            + "        <extrude>1</extrude>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "<Placemark>\n"
            + "    <name>city2</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -67,-78\n"
            + "        </coordinates>\n"
            + "        <extrude>1</extrude>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {67, 78}, {-67, -78}};
    int index = 0;
    MapPolyline line = (MapPolyline) elementCollection.getElements().get(0);
    assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
    for (Geoposition position : line.getPath()) {
      TestHelpers.assertPositionEquals(expectedPoints[index], position);
      index++;
    }
    MapIcon icon = (MapIcon) elementCollection.getElements().get(1);
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    TestHelpers.assertPositionEquals(expectedPoints[index], icon.getLocation().getPosition());
  }

  @Test
  public void testParseLineStringWithAltitudes()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98 67,78,89\n"
            + "        </coordinates>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45, 98}, {67, 78, 89}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(AltitudeReferenceSystem.GEOID, line.getPath().getAltitudeReferenceSystem());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testParseLineStringNotAllAltitudes()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98 67,78\n"
            + "        </coordinates>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {67, 78}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testParsePolygonNotAllAltitudes()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,24\n"
            + "            11,24\n"
            + "            13,25\n"
            + "            10,24\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {
      {10, 20}, {30, 40}, {50, 60}, {60, 70}, {10, 20}, {10, 24}, {11, 24}, {13, 25}, {10, 24}
    };
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(2, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testParsePolygonAllAltitudes()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            100,24,9\n"
            + "            11,24,7\n"
            + "            13,25,8\n"
            + "            100,24,9\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {
      {10, 20, 20},
      {30, 40, 34},
      {50, 60, 56},
      {60, 70, 78},
      {10, 20, 20},
      {100, 24, 9},
      {11, 24, 7},
      {13, 25, 8},
      {100, 24, 9}
    };
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(2, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.GEOID, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testParseMultiGeometry()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "  <name>SF Marina Harbor Master</name>\n"
            + "  <visibility>0</visibility>\n"
            + "  <MultiGeometry>\n"
            + "    <LineString>\n"
            + "      <coordinates>\n"
            + "        -122,37\n"
            + "        -122,38\n"
            + "      </coordinates>\n"
            + "    </LineString>\n"
            + "    <Point>\n"
            + "      <coordinates>\n"
            + "         -123,47\n"
            + "      </coordinates>\n"
            + "    </Point>"
            + "    <Polygon>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            -104,41\n"
            + "            -104,45\n"
            + "            -111,45\n"
            + "            -111,40\n"
            + "            -104,41\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "    </Polygon>"
            + "  </MultiGeometry>\n"
            + "</Placemark>"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(3, elementCollection.getElements().size());
    double[][] expectedPoints = {
      {-122, 37}, {-122, 38}, {-123, 47}, {-104, 41}, {-104, 45}, {-111, 45}, {-111, 40}, {-104, 41}
    };
    int index = 0;
    MapElement element = elementCollection.getElements().get(0);
    assertNotNull(element);
    MapPolyline line = (MapPolyline) element;
    assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
    for (Geoposition position : line.getPath()) {
      TestHelpers.assertPositionEquals(expectedPoints[index], position);
      index++;
    }

    element = elementCollection.getElements().get(1);
    assertNotNull(element);
    MapIcon icon = (MapIcon) element;
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    TestHelpers.assertPositionEquals(expectedPoints[index], icon.getLocation().getPosition());
    index++;

    element = elementCollection.getElements().get(2);
    assertNotNull(element);
    MapPolygon polygon = (MapPolygon) element;
    assertEquals(1, polygon.getPaths().size());
    for (Geopath path : polygon.getPaths()) {
      assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
      for (Geoposition position : path) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testParseMultiGeometryNoGeometries()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "  <name>SF Marina Harbor Master</name>\n"
            + "  <visibility>0</visibility>\n"
            + "  <MultiGeometry>\n"
            + "  </MultiGeometry>\n"
            + "</Placemark>"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(0, elementCollection.getElements().size());
  }

  @Test
  public void testNestedLevels() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\\\">\n"
            + "    <Document>\n"
            + "        <NetworkLink>\n"
            + "            <name>NE US Radar</name>\n"
            + "            <refreshVisibility>1</refreshVisibility>\n"
            + "            <flyToView>1</flyToView>\n"
            + "            <Link>...</Link>\n"
            + "        </NetworkLink>\n"
            + "        <Folder>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <LineString>\n"
            + "                <coordinates>\n"
            + "                    67,78 -107,45\n"
            + "                </coordinates>\n"
            + "            </LineString>\n"
            + "        </Placemark>\n"
            + "        <Placemark>\n"
            + "            <Polygon>\n"
            + "                <extrude>1</extrude>\n"
            + "                <altitudeMode>relativeToGround</altitudeMode>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            35,10 45,45 15,40 10,20 35,10\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "                <innerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            20,30 35,35 30,20 20,30\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </innerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "        </Folder>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {
      {67, 78},
      {-107, 45},
      {35, 10},
      {45, 45},
      {15, 40},
      {10, 20},
      {35, 10},
      {20, 30},
      {35, 35},
      {30, 20},
      {20, 30}
    };
    int index = 0;
    assertNotNull(elementCollection.getElements().get(0));
    MapPolyline line = (MapPolyline) elementCollection.getElements().get(0);
    assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
    for (Geoposition position : line.getPath()) {
      TestHelpers.assertPositionEquals(expectedPoints[index], position);
      index++;
    }
    assertNotNull(elementCollection.getElements().get(1));
    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(1);
    assertEquals(2, polygon.getPaths().size());
    for (Geopath path : polygon.getPaths()) {
      assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
      for (Geoposition position : path) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testSubFolders() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <name>Folders</name>\n"
            + "        <Folder><name>Folder one</name><open>1</open>\n"
            + "            <Folder><name>subfolder</name><open>1</open>\n"
            + "                <Placemark>\n"
            + "                    <name>Path in subfolder</name>\n"
            + "                    <styleUrl>#orange-5px</styleUrl>\n"
            + "                    <LineString>\n"
            + "                        <tessellate>1</tessellate>\n"
            + "                        <coordinates>\n"
            + "                            8,47 9,47\n"
            + "                        </coordinates>\n"
            + "                    </LineString>\n"
            + "                </Placemark>\n"
            + "            </Folder>\n"
            + "            <Placemark>\n"
            + "                <name>Path in folder one</name>\n"
            + "                <styleUrl>#orange-5px</styleUrl>\n"
            + "                <LineString>\n"
            + "                    <tessellate>1</tessellate>\n"
            + "                    <coordinates>\n"
            + "                        9,47 10,47\n"
            + "                    </coordinates>\n"
            + "                </LineString>\n"
            + "            </Placemark>\n"
            + "        </Folder>\n"
            + "        <Folder><name>Folder two</name><open>1</open>\n"
            + "            <Placemark><name>Polygon in 2nd folder</name>\n"
            + "                <styleUrl>#orange-5px</styleUrl>\n"
            + "                <LineString>\n"
            + "                    <tessellate>1</tessellate>\n"
            + "                    <coordinates>\n"
            + "                        10,47 11,47\n"
            + "                    </coordinates>\n"
            + "                </LineString>\n"
            + "            </Placemark>\n"
            + "        </Folder>\n"
            + "        <Style id=\"orange-5px\">\n"
            + "            <LineStyle>\n"
            + "                <color>ff00aaff</color>\n"
            + "                <width>5</width>\n"
            + "            </LineStyle>\n"
            + "        </Style>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(3, elementCollection.getElements().size());
    double[][] expectedPoints = {
      {8, 47},
      {9, 47},
      {9, 47},
      {10, 47},
      {10, 47},
      {11, 47}
    };
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testIconStyleForPoint()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <IconStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "      <Icon>\n"
            + "        <href>https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png</href>\n"
            + "      </Icon>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <styleUrl>#normalState</styleUrl>"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
    String url = "https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png";
    InputStream inputStream = new URL(url).openConnection().getInputStream();
    assertNotNull(inputStream);
    Bitmap expected = BitmapFactory.decodeStream(inputStream);
    assertNotNull(expected);
    MapImage image = icon.getImage();
    assertNotNull(image);
    Bitmap actual = image.getBitmap();
    assertNotNull(actual);
    assertTrue(expected.sameAs(actual));
  }

  @Test
  public void testNoIconStyle() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <styleUrl>#normalState</styleUrl>"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
  }

  @Test
  public void testLineStyleWidth() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LineStyle>\n"
            + "      <width>3</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <styleUrl>#normalState</styleUrl>"
            + "    <LineString>"
            + "    <tessellate>1</tessellate>"
            + "       <coordinates>"
            + "          -107.55,45 67,78"
            + "       </coordinates>"
            + "     </LineString>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {67, 78}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
      assertEquals(3, line.getStrokeWidth());
      assertEquals(-1, line.getStrokeColor());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testLineStyleColor() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LineStyle>\n"
            + "      <color>FFE83FA7</color>\n"
            + "    </LineStyle>\n"
            + "  </Style>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <styleUrl>#normalState</styleUrl>"
            + "    <LineString>"
            + "    <tessellate>1</tessellate>"
            + "       <coordinates>"
            + "          -107.55,45 67,78"
            + "       </coordinates>"
            + "     </LineString>"
            + "</Placemark>\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>"
            + "    <tessellate>1</tessellate>"
            + "       <coordinates>"
            + "          -108.55,45 68,78"
            + "       </coordinates>"
            + "     </LineString>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {67, 78}, {-108.55, 45}, {68, 78}};
    int index = 0;
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapPolyline line = (MapPolyline) elementCollection.getElements().get(i);
      assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
      assertEquals(1, line.getStrokeWidth());
      if (i == 0) {
        assertEquals(0xFFA73FE8, line.getStrokeColor());
      } else {
        assertEquals(0xFFFFFFFF, line.getStrokeColor());
      }
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testPolygonNoOutline() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <outline>false</outline>\n"
            + "            <fill>true</fill>\n"
            + "            <color>ff00b9ff</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -122,48\n"
            + "                            -121,48\n"
            + "                            -121,47\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-122, 48}, {-121, 48}, {-121, 47}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(0xFFFFB900, polygon.getFillColor());
      assertEquals(0, polygon.getStrokeWidth());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testPolygonFillAfterColor()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <outline>false</outline>\n"
            + "            <color>ff00b9ff</color>\n"
            + "            <fill>false</fill>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -122,48\n"
            + "                            -121,48\n"
            + "                            -121,47\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-122, 48}, {-121, 48}, {-121, 47}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(0x00ffffff, polygon.getFillColor());
      assertEquals(0, polygon.getStrokeWidth());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testPolygonNoFill() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <outline>true</outline>\n"
            + "            <fill>false</fill>\n"
            + "            <color>ff00b9ff</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -122,48\n"
            + "                            -121,48\n"
            + "                            -121,47\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-122, 48}, {-121, 48}, {-121, 47}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(0x00ffffff, polygon.getFillColor());
      assertEquals(0xFFCC0000, polygon.getStrokeColor());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testPolygonNoFillValuesInts()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <outline>1</outline>\n"
            + "            <fill>0</fill>\n"
            + "            <color>ff00b9ff</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -122,48\n"
            + "                            -121,48\n"
            + "                            -121,47\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-122, 48}, {-121, 48}, {-121, 47}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(0x00ffffff, polygon.getFillColor());
      assertEquals(0xFFCC0000, polygon.getStrokeColor());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testPolygonNoOutlineValuesInts()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <outline>0</outline>\n"
            + "            <fill>1</fill>\n"
            + "            <color>ff00b9ff</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -122,48\n"
            + "                            -121,48\n"
            + "                            -121,47\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-122, 48}, {-121, 48}, {-121, 47}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(0xFFFFB900, polygon.getFillColor());
      assertEquals(0, polygon.getStrokeWidth());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testStyleMapForPoint() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "  <Style id=\"normalState\">\n"
            + "    <IconStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "      <Icon>\n"
            + "        <href>https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png</href>\n"
            + "      </Icon>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "  <Style id=\"highlightState\">\n"
            + "    <IconStyle>\n"
            + "      <Icon>\n"
            + "        <href>http://maps.google.com/mapfiles/kml/pal3/icon60.png</href>\n"
            + "      </Icon>\n"
            + "      <scale>1.1</scale>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.1</scale>\n"
            + "      <color>ff0000c0</color>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "  <StyleMap id=\"styleMapExample\">\n"
            + "    <Pair>\n"
            + "      <key>normal</key>\n"
            + "      <styleUrl>#normalState</styleUrl>\n"
            + "    </Pair>\n"
            + "    <Pair>\n"
            + "      <key>highlight</key>\n"
            + "      <styleUrl>#highlightState</styleUrl>\n"
            + "    </Pair>\n"
            + "  </StyleMap>\n"
            + "  <Placemark>\n"
            + "    <name>John Hancock Building</name>\n"
            + "    <styleUrl>#styleMapExample</styleUrl>\n"
            + "    <Point>\n"
            + "      <coordinates>-87.62349,41.89863</coordinates>\n"
            + "    </Point>\n"
            + "  </Placemark>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-87.62349, 41.89863};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("John Hancock Building", icon.getTitle());
    String url = "https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png";
    InputStream inputStream = new URL(url).openConnection().getInputStream();
    assertNotNull(inputStream);
    Bitmap expected = BitmapFactory.decodeStream(inputStream);
    assertNotNull(expected);
    MapImage image = icon.getImage();
    assertNotNull(image);
    Bitmap actual = image.getBitmap();
    assertNotNull(actual);
    assertTrue(expected.sameAs(actual));
  }

  @Test
  public void testStyleMapContainsStyle()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "  <Style id=\"highlightState\">\n"
            + "    <IconStyle>\n"
            + "      <Icon>\n"
            + "        <href>http://maps.google.com/mapfiles/kml/pal3/icon60.png</href>\n"
            + "      </Icon>\n"
            + "      <scale>1.1</scale>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.1</scale>\n"
            + "      <color>ff0000c0</color>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "  <StyleMap id=\"styleMapExample\">\n"
            + "    <Pair>\n"
            + "      <key>normal</key>\n"
            + "  <Style>\n"
            + "    <IconStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "      <Icon>\n"
            + "        <href>https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png</href>\n"
            + "      </Icon>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "    </Pair>\n"
            + "    <Pair>\n"
            + "      <key>highlight</key>\n"
            + "      <styleUrl>#highlightState</styleUrl>\n"
            + "    </Pair>\n"
            + "  </StyleMap>\n"
            + "  <Placemark>\n"
            + "    <name>John Hancock Building</name>\n"
            + "    <styleUrl>#styleMapExample</styleUrl>\n"
            + "    <Point>\n"
            + "      <coordinates>-87.62349,41.89863</coordinates>\n"
            + "    </Point>\n"
            + "  </Placemark>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-87.62349, 41.89863};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("John Hancock Building", icon.getTitle());
    String url = "https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png";
    InputStream inputStream = new URL(url).openConnection().getInputStream();
    assertNotNull(inputStream);
    Bitmap expected = BitmapFactory.decodeStream(inputStream);
    assertNotNull(expected);
    MapImage image = icon.getImage();
    assertNotNull(image);
    Bitmap actual = image.getBitmap();
    assertNotNull(actual);
    assertTrue(expected.sameAs(actual));
  }

  @Test
  public void testStyleInsidePlacemark()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>\n"
            + "  <Placemark>\n"
            + "    <Style>\n"
            + "    <IconStyle>\n"
            + "      <Icon>\n"
            + "        <href>https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png</href>\n"
            + "      </Icon>\n"
            + "      <scale>1.1</scale>\n"
            + "    </IconStyle>\n"
            + "  </Style>\n"
            + "    <Point>\n"
            + "      <coordinates>-122,37</coordinates>\n"
            + "    </Point>\n"
            + "  </Placemark>\n"
            + "  <Placemark>\n"
            + "    <Point>\n"
            + "      <coordinates>-125,39</coordinates>\n"
            + "    </Point>\n"
            + "  </Placemark>\n"
            + "</Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 37}, {-125, 39}};
    int index = 0;
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapIcon icon = (MapIcon) elementCollection.getElements().get(i);
      assertNotNull(icon);
      assertEquals(
          AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
      TestHelpers.assertPositionEquals(expectedPoints[index], icon.getLocation().getPosition());
      if (i == 0) {
        String url = "https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png";
        InputStream inputStream = new URL(url).openConnection().getInputStream();
        assertNotNull(inputStream);
        Bitmap expected = BitmapFactory.decodeStream(inputStream);
        assertNotNull(expected);
        MapImage image = icon.getImage();
        assertNotNull(image);
        Bitmap actual = image.getBitmap();
        assertNotNull(actual);
        assertTrue(expected.sameAs(actual));
      }
      index++;
    }
  }

  @Test
  public void testMultipleInlineStyles()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>\n"
            + "  <Placemark>\n"
            + "    <Style>\n"
            + "    <LineStyle>\n"
            + "      <color>ff4500ff</color>\n"
            + "    </LineStyle>\n"
            + "  </Style>\n"
            + "  <Style id=\"wide\">\n"
            + "    <LineStyle>\n"
            + "      <width>10</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>\n"
            + "    <LineString>\n"
            + "      <coordinates>-122,37 -121,38</coordinates>\n"
            + "    </LineString>\n"
            + "  </Placemark>\n"
            + "</Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 37}, {-121, 38}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(10, line.getStrokeWidth());
      assertEquals(0xffffffff, line.getStrokeColor());
      assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testInlineStyleAndSharedStyle()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>\n"
            + "  <Style id=\"wide\">\n"
            + "    <LineStyle>\n"
            + "      <color>eeeeeeee</color>"
            + "      <width>10</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>\n"
            + "  <Placemark>\n"
            + "    <Style>\n"
            + "    <LineStyle>\n"
            + "      <color>ff4500ff</color>\n"
            + "    </LineStyle>\n"
            + "  </Style>\n"
            + "  <styleUrl>#wide</styleUrl>\n"
            + "    <LineString>\n"
            + "      <coordinates>-122,37 -121,38</coordinates>\n"
            + "    </LineString>\n"
            + "  </Placemark>\n"
            + "</Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 37}, {-121, 38}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(10, line.getStrokeWidth());
      assertEquals(0xffff0045, line.getStrokeColor());
      assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testSharedAndInlinePolyStyle()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <color>ffaa00aa</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -121,48\n"
            + "                            -128,46\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "            <Style>\n"
            + "              <PolyStyle>\n"
            + "                <color>ffaaaaaa</color>\n"
            + "              </PolyStyle>\n"
            + "            </Style>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-121, 48}, {-128, 46}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(5, polygon.getStrokeWidth());
      assertEquals(0xffcc0000, polygon.getStrokeColor());
      assertEquals(0xffaaaaaa, polygon.getFillColor());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testInlineOverwritesLineStyleForPolygon()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <color>ffaa00aa</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -121,48\n"
            + "                            -128,46\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "            <Style>\n"
            + "              <PolyStyle>\n"
            + "                <color>ffaaaaaa</color>\n"
            + "              </PolyStyle>\n"
            + "              <LineStyle>\n"
            + "                 <color>ffaabbcc</color>\n"
            + "                 <width>3</width>\n"
            + "              </LineStyle>\n"
            + "            </Style>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-121, 48}, {-128, 46}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(3, polygon.getStrokeWidth());
      assertEquals(0xffccbbaa, polygon.getStrokeColor());
      assertEquals(0xffaaaaaa, polygon.getFillColor());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testPolyStyleInlineOverwritesSharedStyle()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <fill>false</fill>\n"
            + "            <outline>false</outline>\n"
            + "            <color>ffaa00aa</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47 -121,48 -128,46 -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "            <Style>\n"
            + "              <PolyStyle>\n"
            + "                <fill>true</fill>\n"
            + "                <color>ffaaaaaa</color>\n"
            + "              </PolyStyle>\n"
            + "            </Style>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-121, 48}, {-128, 46}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(0, polygon.getStrokeWidth());
      assertEquals(0xffaaaaaa, polygon.getFillColor());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  @Test
  public void testPolyStyleInlineOverwritesOutline()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <fill>false</fill>\n"
            + "            <outline>false</outline>\n"
            + "            <color>ffaa00aa</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47 -121,48 -128,46 -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "            <Style>\n"
            + "              <PolyStyle>\n"
            + "                <fill>true</fill>\n"
            + "                <color>ffaaaaaa</color>\n"
            + "                <outline>true</outline>"
            + "              </PolyStyle>\n"
            + "            </Style>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-122, 47}, {-121, 48}, {-128, 46}, {-122, 47}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolygon polygon = (MapPolygon) element;
      assertEquals(5, polygon.getStrokeWidth());
      assertEquals(0xffcc0000, polygon.getStrokeColor());
      assertEquals(0xffaaaaaa, polygon.getFillColor());
      assertEquals(1, polygon.getPaths().size());
      for (Geopath path : polygon.getPaths()) {
        assertEquals(AltitudeReferenceSystem.SURFACE, path.getAltitudeReferenceSystem());
        for (Geoposition position : path) {
          TestHelpers.assertPositionEquals(expectedPoints[index], position);
          index++;
        }
      }
    }
  }

  /**
   * Tests the public method to catch null. Note: parse(null) will not call internalParse with null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullKMLThrowsException() throws KMLParseException {
    KMLParser.parse(null);
  }

  @Test(expected = KMLParseException.class)
  public void testEmptyStringThrowsException() throws KMLParseException {
    String kml = "";
    KMLParser.parse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testNotXMLThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml = "foo";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testUnexpectedEndTagThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "</BadEnding>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testNoCoordinatesTextThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates></coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testNoCoordinatesElementThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLongitudeTooHighThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -189,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLongitudeTooLowThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            189,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLatitudeTooHighThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            78,98,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLatitudeTooLowThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            78,-98,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = NumberFormatException.class)
  public void testCoordinatesNotDoublesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            foo,bar\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testNotEnoughCoordinatesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            5\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testCoordinatesEmptyWhitespaceThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            \n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testEmptyNameTextThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name></name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testMalformedEndTagThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <MalformedEnd>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    /MalformedEnd>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPointHasMultipleCoordinatesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0 98,7,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLongitudeNaNThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            NaN,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLatitudeNaNThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            78,NaN,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testAltitudeNaNThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            98,43,NaN\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testEmptyStringCoordinatesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            \n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testParseLineStringOnePositionThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98\n"
            + "        </coordinates>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testMultipleCoordinatesTagThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,7,98,6\n"
            + "        </coordinates>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,7,98,6\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  public void testPolygonOuterRingFirstLastUnequalLongitudeThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            30,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,24,9\n"
            + "            11,24,7\n"
            + "            13,25,8\n"
            + "            10,24,9\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonOuterRingFirstLastUnequalLatitudeThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            10,30,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,24,9\n"
            + "            11,24,7\n"
            + "            13,25,8\n"
            + "            10,24,9\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonOuterRingFirstLastUnequalAltitudeThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            10,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,24,9\n"
            + "            11,24,7\n"
            + "            13,25,8\n"
            + "            10,24,9\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonInnerRingFirstLastUnequalLongitudeThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,24,9\n"
            + "            11,24,7\n"
            + "            13,25,8\n"
            + "            30,24,9\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonInnerRingFirstLastUnequalLatitudeThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,24,9\n"
            + "            11,24,7\n"
            + "            13,25,8\n"
            + "            10,34,9\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonInnerRingFirstLastUnequalAltitudeThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            50,60,56\n"
            + "            60,70,78\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,24,9\n"
            + "            11,24,7\n"
            + "            13,25,8\n"
            + "            10,24,30\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonNotEnoughPositionsThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonNoOuterBoundaryThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <InnerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            30,42,35\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </InnerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testOuterBoundaryExtraTagThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <ExtraTag>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            30,42,35\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "        </ExtraTag>\n"
            + "      </outerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testMultipleOuterBoundarysThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            30,42,35\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "        </ExtraTag>\n"
            + "      </outerBoundaryIs>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "            10,20,20\n"
            + "            30,40,34\n"
            + "            30,42,35\n"
            + "            10,20,20\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "        </ExtraTag>\n"
            + "      </outerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonNoCoordinatesValues()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>hollow box</name>\n"
            + "    <Polygon>\n"
            + "      <extrude>1</extrude>\n"
            + "      <altitudeMode>relativeToGround</altitudeMode>\n"
            + "      <outerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </outerBoundaryIs>\n"
            + "      <innerBoundaryIs>\n"
            + "        <LinearRing>\n"
            + "          <coordinates>\n"
            + "          </coordinates>\n"
            + "        </LinearRing>\n"
            + "      </innerBoundaryIs>\n"
            + "    </Polygon>"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testUnexpectedDocumentEndAtPointThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testUnexpectedDocumentEndAtPlacemarkThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testUnexpectedDocumentEndAtOpeningThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testUnexpectedDocumentEndAfterNameTextThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testUnexpectedDocumentEndInTagToSkipThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <TagToSkip>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testUnexpectedDocumentEndAtCoordinatesTextThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testUnexpectedDocumentEndAtCoordinatesEndThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testUnexpectedDocumentEndAtLineStringStartThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testUnexpectedDocumentEndAtPolygonStartThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Polygon>\n";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testStyleIdsNotUniqueThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <IconStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "      <Icon>\n"
            + "        <href>http://maps.google.com/mapfiles/kml/pal3/icon55.png</href>\n"
            + "      </Icon>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>"
            + "<Style id=\"normalState\">\n"
            + "    <IconStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "      <Icon>\n"
            + "        <href>http://maps.google.com/mapfiles/kml/pal3/icon55.png</href>\n"
            + "      </Icon>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testStyleIdsNotFoundThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <styleUrl>#styleNotFound</styleUrl>"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLineStyleWidthNaNThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LineStyle>\n"
            + "      <width>NaN</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <styleUrl>#styleNotFound</styleUrl>"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = NumberFormatException.class)
  public void testLineStyleWidthStringThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LineStyle>\n"
            + "      <width>foo</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <styleUrl>#styleNotFound</styleUrl>"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLineStyleWidthNegativeThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LineStyle>\n"
            + "      <width>-3</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLineStyleWidthZeroThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LineStyle>\n"
            + "      <width>0</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLineStyleWidthFractionThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Style id=\"normalState\">\n"
            + "    <LineStyle>\n"
            + "      <width>0.4</width>\n"
            + "    </LineStyle>\n"
            + "  </Style>"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonOutlineInvalidValueThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <outline>foo</outline>\n"
            + "            <fill>true</fill>\n"
            + "            <color>ff00b9ff</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -122,48\n"
            + "                            -121,48\n"
            + "                            -121,47\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPolygonFillInvalidValueThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "    <Document>\n"
            + "        <Style id=\"normalState\">\n"
            + "        <LineStyle>\n"
            + "            <color>ff0000cc</color>\n"
            + "            <width>5</width>\n"
            + "        </LineStyle>\n"
            + "        <PolyStyle>\n"
            + "            <outline>true</outline>\n"
            + "            <fill>foo</fill>\n"
            + "            <color>ff00b9ff</color>\n"
            + "        </PolyStyle>\n"
            + "        </Style>\n"
            + "        <Placemark>\n"
            + "            <name>city</name>\n"
            + "            <styleUrl>#normalState</styleUrl>\n"
            + "            <Polygon>\n"
            + "                <outerBoundaryIs>\n"
            + "                    <LinearRing>\n"
            + "                        <coordinates>\n"
            + "                            -122,47\n"
            + "                            -122,48\n"
            + "                            -121,48\n"
            + "                            -121,47\n"
            + "                            -122,47\n"
            + "                        </coordinates>\n"
            + "                    </LinearRing>\n"
            + "                </outerBoundaryIs>\n"
            + "            </Polygon>\n"
            + "        </Placemark>\n"
            + "    </Document>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testStyleMapNoPairThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "  <Style id=\"normalState\">\n"
            + "    <IconStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "      <Icon>\n"
            + "        <href>https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png</href>\n"
            + "      </Icon>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "  <Style id=\"highlightState\">\n"
            + "    <IconStyle>\n"
            + "      <Icon>\n"
            + "        <href>http://maps.google.com/mapfiles/kml/pal3/icon60.png</href>\n"
            + "      </Icon>\n"
            + "      <scale>1.1</scale>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.1</scale>\n"
            + "      <color>ff0000c0</color>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "  <StyleMap id=\"styleMapExample\">\n"
            + "  </StyleMap>\n"
            + "  <Placemark>\n"
            + "    <name>John Hancock Building</name>\n"
            + "    <styleUrl>#styleMapExample</styleUrl>\n"
            + "    <Point>\n"
            + "      <coordinates>-87.62349,41.89863</coordinates>\n"
            + "    </Point>\n"
            + "  </Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testStyleMapForPointKeyInvalidValueThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "  <Style id=\"normalState\">\n"
            + "    <IconStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "      <Icon>\n"
            + "        <href>https://cdn4.iconfinder.com/data/icons/small-n-flat/24/map-marker-128.png</href>\n"
            + "      </Icon>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.0</scale>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "  <Style id=\"highlightState\">\n"
            + "    <IconStyle>\n"
            + "      <Icon>\n"
            + "        <href>http://maps.google.com/mapfiles/kml/pal3/icon60.png</href>\n"
            + "      </Icon>\n"
            + "      <scale>1.1</scale>\n"
            + "    </IconStyle>\n"
            + "    <LabelStyle>\n"
            + "      <scale>1.1</scale>\n"
            + "      <color>ff0000c0</color>\n"
            + "    </LabelStyle>\n"
            + "  </Style>\n"
            + "  <StyleMap id=\"styleMapExample\">\n"
            + "    <Pair>\n"
            + "      <key>celery</key>\n"
            + "      <styleUrl>#normalState</styleUrl>\n"
            + "    </Pair>\n"
            + "    <Pair>\n"
            + "      <key>highlight</key>\n"
            + "      <styleUrl>#highlightState</styleUrl>\n"
            + "    </Pair>\n"
            + "  </StyleMap>\n"
            + "  <Placemark>\n"
            + "    <name>John Hancock Building</name>\n"
            + "    <styleUrl>#styleMapExample</styleUrl>\n"
            + "    <Point>\n"
            + "      <coordinates>-87.62349,41.89863</coordinates>\n"
            + "    </Point>\n"
            + "  </Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testStyleMapDoesNotExist()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <styleUrl>#notFound</styleUrl>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }
}

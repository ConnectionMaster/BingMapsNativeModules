//
//  MSMapsModulesTests.m
//  MSMapsModulesTests
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/17/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonParser.h"
#import <MicrosoftMaps/MicrosoftMaps.h>
#import <XCTest/XCTest.h>

@interface MSMapGeoJsonParserTest : XCTestCase

@end

@implementation MSMapGeoJsonParserTest

- (void)testParsePoint {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  MSMapIcon *icon;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
  }
  XCTAssertNotNil(icon);
  XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                 icon.location.altitudeReferenceSystem);
  NSArray *expectedPoints =
      [[NSArray alloc] initWithObjects:[NSNumber numberWithDouble:30],
                                       [NSNumber numberWithDouble:10], nil];

  [self checkExpectedPosition:expectedPoints
           withActualPosition:icon.location.position];
}

- (void)testParseMultiPoint {
  NSString *geojson = @"{\"type\": \"MultiPoint\", \"coordinates\": [[10, 40], "
                      @"[40, 30, 7], [20, 20], [30, 10]]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(4, collection.count);

  double expected[4][2] = {{10, 40}, {40, 30}, {20, 20}, {30, 10}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 4; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }

  MSMapIcon *icon;
  int index = 0;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
    XCTAssertNotNil(icon);
    XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                   icon.location.altitudeReferenceSystem);
    [self checkExpectedPosition:expectedPoints[index]
             withActualPosition:icon.location.position];
    index++;
  }
}

- (void)testParseLineString {
  NSString *geojson = @"{\"type\": \"LineString\",\"coordinates\": [[30, 10], "
                      @"[10, 30, 8], [40, 40]]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  double expected[3][2] = {{30, 10}, {10, 30}, {40, 40}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 3; row++) {
    NSArray *pair;
    pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }

  MSMapPolyline *line;

  for (id obj in collection) {
    XCTAssertEqual([MSMapPolyline class], [obj class]);
    line = (MSMapPolyline *)obj;
    XCTAssertNotNil(line);
    XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                   line.path.altitudeReferenceSystem);
    int index = 0;

    for (id pos in line.path) {
      MSGeoposition *position = (MSGeoposition *)pos;
      [self checkExpectedPosition:expectedPoints[index]
               withActualPosition:position];
      index++;
    }
  }
}

- (void)testParseMultiLineString {
  NSString *geojson =
      @"{\"type\": \"MultiLineString\",\"coordinates\": [[[10, 10], [20, 20], "
      @"[10, 40]], [[40, 40], [30, 30], [40, 20], [30, 10]]]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(2, collection.count);

  double expected[7][2] = {{10, 10}, {20, 20}, {10, 40}, {40, 40},
                           {30, 30}, {40, 20}, {30, 10}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 7; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }
  MSMapPolyline *line;
  int index = 0;
  for (id obj in collection) {
    XCTAssertEqual([MSMapPolyline class], [obj class]);
    line = (MSMapPolyline *)obj;
    XCTAssertNotNil(line);
    XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                   line.path.altitudeReferenceSystem);
    for (id pos in line.path) {
      MSGeoposition *position = (MSGeoposition *)pos;
      [self checkExpectedPosition:expectedPoints[index]
               withActualPosition:position];
      index++;
    }
  }
}

- (void)testParseOneRingPolygon {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[30, 10], "
                      @"[40, 40], [20, 40], [10, 20], [30, 10]]]}";

  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  double expected[5][2] = {{30, 10}, {40, 40}, {20, 40}, {10, 20}, {30, 10}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 5; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }
  MSMapPolygon *polygon;
  for (id obj in collection) {
    XCTAssertEqual([MSMapPolygon class], [obj class]);
    polygon = (MSMapPolygon *)obj;
    XCTAssertNotNil(polygon);
    XCTAssertEqual(1, polygon.paths.count);
    for (id obj in polygon.paths) {
      MSGeopath *path = (MSGeopath *)obj;
      XCTAssertEqual(5, path.size);
      XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                     path.altitudeReferenceSystem);
      int index = 0;
      for (id pos in path) {
        MSGeoposition *position = (MSGeoposition *)pos;
        [self checkExpectedPosition:expectedPoints[index]
                 withActualPosition:position];
        index++;
      }
    }
  }
}

- (void)testParseMultiRingPolygon {
  NSString *geojson =
      @"{\"type\": \"Polygon\", \"coordinates\": [[[35, 10], [45, 45], [15, "
      @"40], [10, 20], [35, 10]], [[20, 30], [35, 35], [30, 20], [20, 30]]]}";

  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  double expected[9][2] = {{35, 10}, {45, 45}, {15, 40}, {10, 20}, {35, 10},
                           {20, 30}, {35, 35}, {30, 20}, {20, 30}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 9; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }
  MSMapPolygon *polygon;
  for (id obj in collection) {
    XCTAssertEqual([MSMapPolygon class], [obj class]);
    polygon = (MSMapPolygon *)obj;
    XCTAssertNotNil(polygon);
    XCTAssertEqual(2, polygon.paths.count);
    int index = 0;
    for (id obj in polygon.paths) {
      MSGeopath *path = (MSGeopath *)obj;
      XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                     path.altitudeReferenceSystem);
      for (id pos in path) {
        MSGeoposition *position = (MSGeoposition *)pos;
        [self checkExpectedPosition:expectedPoints[index]
                 withActualPosition:position];
        index++;
      }
    }
  }
}

- (void)testParseMultiRingPolygonOneAltitudePointReferenceSystemSurface {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[35, 10], "
                      @"[45, 45], [15, 40], [10, 20], [35, 10]], [[20, 30], "
                      @"[35, 35, 8], [30, 20], [20, 30]]]}";

  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  double expected[9][2] = {{35, 10}, {45, 45}, {15, 40}, {10, 20}, {35, 10},
                           {20, 30}, {35, 35}, {30, 20}, {20, 30}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 9; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }
  MSMapPolygon *polygon;
  for (id obj in collection) {
    XCTAssertEqual([MSMapPolygon class], [obj class]);
    polygon = (MSMapPolygon *)obj;
    XCTAssertNotNil(polygon);
    XCTAssertEqual(2, polygon.paths.count);
    int index = 0;
    for (id obj in polygon.paths) {
      MSGeopath *path = (MSGeopath *)obj;
      XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                     path.altitudeReferenceSystem);
      for (id pos in path) {
        MSGeoposition *position = (MSGeoposition *)pos;
        [self checkExpectedPosition:expectedPoints[index]
                 withActualPosition:position];
        index++;
      }
    }
  }
}

- (void)testParseMultiRingPolygonWithAllAltitudes {
  NSString *geojson =
      @"{\"type\": \"Polygon\", \"coordinates\": [[[35, 10, 7], "
      @"[45, 45, 66], [15, 40, 77], [10, 20, 88], [35, 10, 7]], [[20, 30, 8], "
      @"[35, 35, 66], [30, 20, 77], [20, 30, 8]]]}";

  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  double expected[9][3] = {{35, 10, 7},  {45, 45, 66}, {15, 40, 77},
                           {10, 20, 88}, {35, 10, 7},  {20, 30, 8},
                           {35, 35, 66}, {30, 20, 77}, {20, 30, 8}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 9; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]],
                        [NSNumber numberWithDouble:expected[row][2]], nil];
    [expectedPoints addObject:pair];
  }
  MSMapPolygon *polygon;
  for (id obj in collection) {
    XCTAssertEqual([MSMapPolygon class], [obj class]);
    polygon = (MSMapPolygon *)obj;
    XCTAssertNotNil(polygon);
    XCTAssertEqual(2, polygon.paths.count);
    int index = 0;
    for (id obj in polygon.paths) {
      MSGeopath *path = (MSGeopath *)obj;
      XCTAssertEqual(MSMapAltitudeReferenceSystemEllipsoid,
                     path.altitudeReferenceSystem);
      for (id pos in path) {
        MSGeoposition *position = (MSGeoposition *)pos;
        [self checkExpectedPosition:expectedPoints[index]
                 withActualPosition:position];
        index++;
      }
    }
  }
}

- (void)testParseMultiPolygonNotAllAltitudes {
  NSString *geojson =
      @"{\"type\": \"MultiPolygon\", \"coordinates\": [[[[40, 40], [20, 45, "
      @"8], [45, 30], [40, 40]]], [[[20, 35], [10, 30], [10, 10], [30, 5], "
      @"[45, 20], [20, 35]],						"
      @"		 [[30, 20], [20, 15], [20, 25], [30, 20]]]]}";

  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(2, collection.count);

  double expected[14][2] = {{40, 40}, {20, 45}, {45, 30}, {40, 40}, {20, 35},
                            {10, 30}, {10, 10}, {30, 5},  {45, 20}, {20, 35},
                            {30, 20}, {20, 15}, {20, 25}, {30, 20}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 14; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }
  int index = 0;
  for (id obj in collection) {
    XCTAssertEqual([MSMapPolygon class], [obj class]);
    MSMapPolygon *polygon = (MSMapPolygon *)obj;
    XCTAssertNotNil(polygon);
    for (id obj in polygon.paths) {
      MSGeopath *path = (MSGeopath *)obj;
      XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                     path.altitudeReferenceSystem);
      for (id pos in path) {
        MSGeoposition *position = (MSGeoposition *)pos;
        [self checkExpectedPosition:expectedPoints[index]
                 withActualPosition:position];
        index++;
      }
    }
  }
}

- (void)testParseMultiPolygonWithAllAltitudes {
  NSString *geojson =
      @"{\"type\": \"MultiPolygon\", \"coordinates\": [[[[40, 40, 8], [20, 45, "
      @"11], [45, 30, 22], [40, 40, 8]]], [[[20, 35, 33], [10, 30, 44], [10, "
      @"10, 55], [30, 5, 66], [45, 20, 77], [20, 35, 33]], [[30, 20, 88], [20, "
      @"15, 99], [20, 25, 111], [30, 20, 88]]]]}";

  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(2, collection.count);

  double expected[14][3] = {
      {40, 40, 8},  {20, 45, 11}, {45, 30, 22},  {40, 40, 8},  {20, 35, 33},
      {10, 30, 44}, {10, 10, 55}, {30, 5, 66},   {45, 20, 77}, {20, 35, 33},
      {30, 20, 88}, {20, 15, 99}, {20, 25, 111}, {30, 20, 88}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 14; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]],
                        [NSNumber numberWithDouble:expected[row][2]], nil];
    [expectedPoints addObject:pair];
  }
  int index = 0;
  for (id obj in collection) {
    XCTAssertEqual([MSMapPolygon class], [obj class]);
    MSMapPolygon *polygon = (MSMapPolygon *)obj;
    XCTAssertNotNil(polygon);
    for (id obj in polygon.paths) {
      MSGeopath *path = (MSGeopath *)obj;
      XCTAssertEqual(MSMapAltitudeReferenceSystemEllipsoid,
                     path.altitudeReferenceSystem);
      for (id pos in path) {
        MSGeoposition *position = (MSGeoposition *)pos;
        [self checkExpectedPosition:expectedPoints[index]
                 withActualPosition:position];
        index++;
      }
    }
  }
}

- (void)testParsePointWithAltitude {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10, 5]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  MSMapIcon *icon;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
  }
  XCTAssertNotNil(icon);
  XCTAssertEqual(MSMapAltitudeReferenceSystemEllipsoid,
                 icon.location.altitudeReferenceSystem);
  NSArray *expectedPoints =
      [[NSArray alloc] initWithObjects:[NSNumber numberWithDouble:30],
                                       [NSNumber numberWithDouble:10],
                                       [NSNumber numberWithDouble:5], nil];

  [self checkExpectedPosition:expectedPoints
           withActualPosition:icon.location.position];
}

- (void)testParsePointManyCoordinates {
  NSString *geojson =
      @"{\"type\": \"Point\", \"coordinates\": [30, 10, 5, 3, 4]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  MSMapIcon *icon;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
  }
  XCTAssertNotNil(icon);
  NSArray *expectedPoints =
      [[NSArray alloc] initWithObjects:[NSNumber numberWithDouble:30],
                                       [NSNumber numberWithDouble:10],
                                       [NSNumber numberWithDouble:5], nil];

  [self checkExpectedPosition:expectedPoints
           withActualPosition:icon.location.position];
}

- (void)testParseFeature {
  NSString *geojson = @"{\"type\": \"Feature\",\"geometry\": {\"type\": "
                      @"\"Point\",\"coordinates\": [30, 10]},\"properties\": "
                      @"{\"prop0\": \"value0\"}}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  MSMapIcon *icon;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
  }
  XCTAssertNotNil(icon);
  XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                 icon.location.altitudeReferenceSystem);
  NSArray *expectedPoints =
      [[NSArray alloc] initWithObjects:[NSNumber numberWithDouble:30],
                                       [NSNumber numberWithDouble:10], nil];

  [self checkExpectedPosition:expectedPoints
           withActualPosition:icon.location.position];
}

- (void)testParseFeatureCollection {
  NSString *geojson =
      @"{\"type\": \"FeatureCollection\",\"features\": [{\"type\": "
      @"\"Feature\",\"geometry\": {\"type\": \"Point\", \"coordinates\": "
      @"[102.0, 0.5]}, \"properties\": {\"prop0\": \"value0\"}}, {\"type\": "
      @"\"Feature\", \"geometry\": {\"type\": \"LineString\", \"coordinates\": "
      @"[[102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]]}, "
      @"\"properties\": {\"prop0\": \"value0\", \"prop1\": 0.0}}, {\"type\": "
      @"\"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": "
      @"[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, "
      @"0.0]]]},\"properties\": {\"prop0\": \"value0\",\"prop1\": { \"this\": "
      @"\"that\" }}}]}";

  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(3, collection.count);

  double expected[10][2] = {{102, 0.5}, {102, 0}, {103, 1}, {104, 0}, {105, 1},
                            {100, 0},   {101, 0}, {101, 1}, {100, 1}, {100, 0}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 10; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    [expectedPoints addObject:pair];
  }
  int objectNum = 0;
  int expectedPointsIndex = 0;
  for (id obj in collection) {
    if (objectNum == 0) {
      XCTAssertEqual([MSMapIcon class], [obj class]);
      MSMapIcon *icon = (MSMapIcon *)obj;
      XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                     icon.location.altitudeReferenceSystem);
      [self checkExpectedPosition:expectedPoints[expectedPointsIndex]
               withActualPosition:icon.location.position];
      expectedPointsIndex++;
    } else if (objectNum == 1) {
      XCTAssertEqual([MSMapPolyline class], [obj class]);
      MSMapPolyline *polyline = (MSMapPolyline *)obj;
      MSGeopath *path = polyline.path;
      XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                     path.altitudeReferenceSystem);
      for (MSGeoposition *position in path) {
        [self checkExpectedPosition:expectedPoints[expectedPointsIndex]
                 withActualPosition:position];
        expectedPointsIndex++;
      }
    } else if (objectNum == 2) {
      XCTAssertEqual([MSMapPolygon class], [obj class]);
      MSMapPolygon *polygon = (MSMapPolygon *)obj;
      for (MSGeopath *path in polygon.paths) {
        XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                       path.altitudeReferenceSystem);
        for (MSGeoposition *position in path) {
          [self checkExpectedPosition:expectedPoints[expectedPointsIndex]
                   withActualPosition:position];
          expectedPointsIndex++;
        }
      }
    }
    objectNum++;
  }
}

- (void)testParseGeometryCollection {
  NSString *geojson =
      @"{\"type\": \"GeometryCollection\", \"geometries\": "
      @"[{\"type\":\"Point\", \"coordinates\": [40, 10]}, {\"type\": "
      @"\"LineString\", \"coordinates\": [[10, 10, 9], [20, 20, 8], [10, 40, "
      @"7]]}, {\"type\": \"Polygon\", \"coordinates\": [[[40, 40], [20, 45], "
      @"[45, 30], [40, 40]]]}]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(3, collection.count);

  double expected[8][3] = {{40, 10, 0}, {10, 10, 9}, {20, 20, 8}, {10, 40, 7},
                           {40, 40, 0}, {20, 45, 0}, {45, 30, 0}, {40, 40, 0}};
  NSMutableArray *expectedPoints = [[NSMutableArray alloc] init];
  for (int row = 0; row < 10; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]],
                        [NSNumber numberWithDouble:expected[row][2]], nil];
    [expectedPoints addObject:pair];
  }
  int objectNum = 0;
  int expectedPointsIndex = 0;
  for (id obj in collection) {
    if (objectNum == 0) {
      XCTAssertEqual([MSMapIcon class], [obj class]);
      MSMapIcon *icon = (MSMapIcon *)obj;
      XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                     icon.location.altitudeReferenceSystem);
      [self checkExpectedPosition:expectedPoints[expectedPointsIndex]
               withActualPosition:icon.location.position];
      expectedPointsIndex++;
    } else if (objectNum == 1) {
      XCTAssertEqual([MSMapPolyline class], [obj class]);
      MSMapPolyline *polyline = (MSMapPolyline *)obj;
      MSGeopath *path = polyline.path;
      XCTAssertEqual(MSMapAltitudeReferenceSystemEllipsoid,
                     path.altitudeReferenceSystem);
      for (MSGeoposition *position in path) {
        [self checkExpectedPosition:expectedPoints[expectedPointsIndex]
                 withActualPosition:position];
        expectedPointsIndex++;
      }
    } else if (objectNum == 2) {
      XCTAssertEqual([MSMapPolygon class], [obj class]);
      MSMapPolygon *polygon = (MSMapPolygon *)obj;
      for (MSGeopath *path in polygon.paths) {
        XCTAssertEqual(MSMapAltitudeReferenceSystemSurface,
                       path.altitudeReferenceSystem);
        for (MSGeoposition *position in path) {
          [self checkExpectedPosition:expectedPoints[expectedPointsIndex]
                   withActualPosition:position];
          expectedPointsIndex++;
        }
      }
    }
    objectNum++;
  }
}

- (void)testNullInputGivesError {
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:NULL error:&error];
  XCTAssertNil(layer);
  XCTAssertEqual(-100, error.code);
}

- (void)testNullInputNullError {
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:NULL error:nil];
  XCTAssertNil(layer);
}

- (void)testEmptyInputGivesError {
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:@"" error:&error];
  XCTAssertNil(layer);
  XCTAssertEqual(-100, error.code);
}

- (void)testNoBracketsJsonGivesError {
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser
      parse:@"\"type\": \"Point\", \"coordinates\": [-122.26, 47.609]"
      error:&error]);
  XCTAssertEqual(3840, error.code);
}

- (void)testNoCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"Point\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testMultiPointNoCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"MultiPoint\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLineStringNoCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"LineString\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testMultiPolygonNoCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"MultiPolygon\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testEmptyCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": []}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testNoCoordinatesValueGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": }";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(3840, error.code);
}

- (void)testNoTypeGivesError {
  NSString *geojson = @"{\"coordinates\": [30, 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testCoordinateNaNGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [NaN, 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(3840, error.code);
}

- (void)testLongitudeNullGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [null, 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLatitudeNullGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, null]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testAltitudeNullGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10, null]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testCoordinatesObjectGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": {}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testCoordinatesArraysGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [[], []]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testCoordinatesStringGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": \"foo\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLongitudeTooHighGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [189, 7]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLongitudeTooLowGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [-189, 7]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLatitudeTooHighGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [7, 95]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLatitudeTooLowGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [7, -95]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLongitudeStringGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [\"a\", 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLatitudeStringGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, \"b\"]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testAltitudeStringGivesError {
  NSString *geojson =
      @"{\"type\": \"Point\", \"coordinates\": [30, 10, \"c\"]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiPointBadPointGivesError {
  NSString *geojson = @"{\"type\": \"MultiPoint\", \"coordinates\": [[10, 40], "
                      @"[40, \"cat\"], [20, 20], [30, 10]]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiPointStringPointGivesError {
  NSString *geojson = @"{\"type\": \"MultiPoint\", \"coordinates\": [[10, 40], "
                      @"\"foo\", [20, 20], [30, 10]]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiPointNotArraysGivesError {
  NSString *geojson = @"{\"type\": \"MultiPoint\", \"coordinates\": [10, 40, "
                      @"40, 10, 20, 20, 30, 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiPointObjectPointsGivesError {
  NSString *geojson = @"{\"type\": \"MultiPoint\", \"coordinates\": [{10, 40}, "
                      @"{40, 10}, {20, 20}, {30, 10}]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(3840, error.code);
}

- (void)testParseMultiPointObjectArrayGivesError {
  NSString *geojson = @"{\"type\": \"MultiPoint\", \"coordinates\": {[10, 40], "
                      @"[40, 10], [20, 20], [30, 10]}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(3840, error.code);
}

- (void)testParseLineStringLessThan2CoordinatesGivesError {
  NSString *geojson =
      @"{\"type\": \"LineString\", \"coordinates\": [[30, 10]]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseLineStringNotArraysGivesError {
  NSString *geojson =
      @"{\"type\": \"LineString\", \"coordinates\": [30, 10, 4, 5]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiLineStringBadLineGivesError {
  NSString *geojson = @"{\"type\": \"MultiLineString\",\"coordinates\": [[[10, "
                      @"10]], [[40, 40], [30, 30], [40, 20], [30, 10]]]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiLineStringStringCoordinatesGivesError {
  NSString *geojson =
      @"{\"type\": \"MultiLineString\",\"coordinates\": \"foo\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiLineStringStringLineGivesError {
  NSString *geojson = @"{\"type\": \"MultiLineString\",\"coordinates\": [[[40, "
                      @"40], [30, 30], [40, 20], [30, 10]], \"foo\"]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonNotEnoughCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[30, 10], "
                      @"[40, 40], [20, 40]]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonFirstLastLatitudeUnequalGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[30, 10], "
                      @"[40, 40], [20, 40], [10, 20], [30, 50]]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonFirstLastLongitudeUnequalGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[30, 10], "
                      @"[40, 40], [20, 40], [10, 20], [60, 10]]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonFirstLastAltitudeUnequalGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[30, 10, "
                      @"7], [40, 40], [20, 40], [10, 20], [30, 10, 8]]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonFirstLastUnequalLengthsGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[30, 10, "
                      @"7], [40, 40], [20, 40], [10, 20], [30, 10]]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonRingsNotArrayGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[30, 10], "
                      @"[40, 40], [20, 40], [10, 20], [30, 10]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonNotArrayGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [30, 10, 40, "
                      @"40, 20, 40, 10, 20, 30, 10]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonStringCoordinatesValueGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": \"foo\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonObjectCoordinatesValueGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": {}}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonEmptyCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": []}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonEmptyRingGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[]]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonEmptyPositionGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[[]]]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonStringInsteadOfRingGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [\"foo\"]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParsePolygonStringInsteadOfCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"Polygon\", \"coordinates\": [[\"foo\"]]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiPolygonStringPolygonGivesError {
  NSString *geojson =
      @"{\"type\": \"MultiPolygon\", \"coordinates\": [ \"foo\", [[[20, 35], "
      @"[10, 30], [10, 10], [30, 5], [45, 20], [20, 35]], [[30, 20], [20, 15], "
      @"[20, 25], [30, 20]]]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseMultiPolygonStringRingGivesError {
  NSString *geojson =
      @"{\"type\": \"MultiPolygon\", \"coordinates\": [ [\"foo\", [10, 30], "
      @"[10, 10], [30, 5], [45, 20], [20, 35]], [[[20, 35], [10, 30], [10, "
      @"10], [30, 5], [45, 20], [20, 35]], [[30, 20], [20, 15], [20, 25], [30, "
      @"20]]]]}";

  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseFeatureNoGeometryGivesError {
  NSString *geojson = @"{\"type\": \"Feature\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testParseFeatureGeometryNotObjectGivesError {
  NSString *geojson = @"{\"type\": \"Feature\",\"geometry\": \"foo\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionNoFeaturesGivesError {
  NSString *geojson = @"{\"type\": \"FeatureCollection\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionFeaturesNotArrayGivesError {
  NSString *geojson = @"{\"type\": \"FeatureCollection\", \"features\": {}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionFeaturesEmptyArrayGivesError {
  NSString *geojson = @"{\"type\": \"FeatureCollection\", \"features\": []}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionFeatureNotObjectGivesError {
  NSString *geojson =
      @"{\"type\": \"FeatureCollection\", \"features\": [\"foo\"]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionFeatureTypeNotFeatureGivesError {
  NSString *geojson =
      @"{\"type\": \"FeatureCollection\", \"features\": [{\"type\": \"foo\", "
      @"\"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]}, "
      @"\"properties\": {\"prop0\": \"value0\"}}]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionFeatureNoGeometryGivesError {
  NSString *geojson = @"{\"type\": \"FeatureCollection\", \"features\": "
                      @"[{\"type\": \"Feature\"}]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionGeometryNotObjectGivesError {
  NSString *geojson = @"{\"type\": \"FeatureCollection\", \"features\": "
                      @"[{\"type\": \"Feature\", \"geometry\": []}]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureContainesFeaturesGivesError {
  NSString *geojson = @"{\"type\": \"Feature\",\"geometry\": {\"type\": "
                      @"\"Point\",\"coordinates\": [30, 10]},\"properties\": "
                      @"{\"prop0\": \"value0\"}, \"features\": \"foo\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testPointContainsGeometryMemberGivesError {
  NSString *geojson =
      @"{\"type\": \"Point\", \"coordinates\": [30, 10], \"geometry\": {}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testLineStringContainsPropertiesMemberGivesError {
  NSString *geojson = @"{\"type\": \"LineString\",\"coordinates\": [[30, 10], "
                      @"[10, 30, 8], [40, 40]], \"properties\": {}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testPolygonContainsFeaturesMemberGivesError {
  NSString *geojson =
      @"{\"type\": \"Polygon\", \"coordinates\": [[[30, 10], [40, 40], [20, "
      @"40], [10, 20], [30, 10]]], \"features\": {}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionMemberContainsFeaturesGivesError {
  NSString *geojson =
      @"{\"type\": \"FeatureCollection\",\"features\": [{\"type\": "
      @"\"Feature\",\"geometry\": {\"type\": \"Point\", \"coordinates\": "
      @"[102.0, 0.5]}, \"properties\": {\"prop0\": \"value0\"}}, "
      @"{\"features\": {}, \"type\": \"Feature\", \"geometry\": {\"type\": "
      @"\"LineString\", \"coordinates\": [[102.0, 0.0], [103.0, 1.0], [104.0, "
      @"0.0], [105.0, 1.0]]}, \"properties\": {\"prop0\": \"value0\", "
      @"\"prop1\": 0.0}}, {\"type\": \"Feature\", \"geometry\": {\"type\": "
      @"\"Polygon\", \"coordinates\": [[[100.0, 0.0], [101.0, 0.0], [101.0, "
      @"1.0], [100.0, 1.0], [100.0, 0.0]]]},\"properties\": {\"prop0\": "
      @"\"value0\",\"prop1\": { \"this\": \"that\" }}}]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testFeatureCollectionContainsGeometryGivesError {
  NSString *geojson =
      @"{\"type\": \"FeatureCollection\",\"features\": [{\"type\": "
      @"\"Feature\",\"geometry\": {\"type\": \"Point\", \"coordinates\": "
      @"[102.0, 0.5]}, \"properties\": {\"prop0\": \"value0\"}}], "
      @"\"geometries\": {}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testGeometryCollectionNoGeometriesMemberGivesError {
  NSString *geojson = @"{\"type\": \"GeometryCollection\"}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testGeometryCollectionNotArrayGivesError {
  NSString *geojson = @"{\"type\": \"GeometryCollection\", \"geometries\": {}}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testGeometryCollectionArrayMemberGivesError {
  NSString *geojson = @"{\"type\": \"GeometryCollection\", \"geometries\": "
                      @"[{\"type\":\"Point\", \"coordinates\": [40, 10]}, [], "
                      @"{\"type\": \"Polygon\", \"coordinates\": [[[40, 40], "
                      @"[20, 45], [45, 30], [40, 40]]]}]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)testGeometryCollectionEmptyObjectMemberGivesError {
  NSString *geojson = @"{\"type\": \"GeometryCollection\", \"geometries\": "
                      @"[{\"type\":\"Point\", \"coordinates\": [40, 10]}, {}, "
                      @"{\"type\": \"Polygon\", \"coordinates\": [[[40, 40], "
                      @"[20, 45], [45, 30], [40, 40]]]}]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertNotNil(error);
  XCTAssertEqual(-500, error.code);
}

- (void)checkExpectedPosition:(NSArray *)expectedPoints
           withActualPosition:(MSGeoposition *)position {
  XCTAssertEqual([[expectedPoints objectAtIndex:0] doubleValue],
                 position.longitude);
  XCTAssertEqual([[expectedPoints objectAtIndex:1] doubleValue],
                 position.latitude);
  if (expectedPoints.count > 2) {
    XCTAssertEqual([[expectedPoints objectAtIndex:2] doubleValue],
                   position.altitude);
  } else {
    XCTAssertEqual(0, position.altitude);
  }
}

@end

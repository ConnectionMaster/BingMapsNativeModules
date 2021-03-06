//
//  ViewController.swift
//  MSMapsModulesReferenceApp
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/24/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

import MicrosoftMaps
import MSMapsModules
import UIKit

class ViewController: UIViewController {
    @IBOutlet var mMapView: MSMapView!

    override func viewDidLoad() {
        super.viewDidLoad()

        /* Add your credential key in a keys.plist file.
         	 To acquire a key, visit:
         https://docs.microsoft.com/en-us/bingmaps/getting-started/bing-maps-dev-center-help/getting-a-bing-maps-key
         */
        var keys: NSDictionary?
        if let path = Bundle.main.path(forResource: "keys", ofType: "plist") {
            keys = NSDictionary(contentsOfFile: path)
        }
        mMapView.credentialsKey = keys?.object(forKey: "credentialsKey") as! String

        let LOCATION_LAKE_WASHINGTON = MSGeopoint(latitude: 47.61, longitude: -122.27)
        let scene = MSMapScene(location: LOCATION_LAKE_WASHINGTON, zoomLevel: 10)
        mMapView.setScene(scene, with: .none)

        let geojson = "{\"type\": \"Point\", \"coordinates\": [-122.27, 47.61]}"
        do {
            let layer = try MSMapGeoJsonParser.parse(geojson)
            mMapView.layers.add(layer)
        } catch {
            print(error.self)
        }
    }
}

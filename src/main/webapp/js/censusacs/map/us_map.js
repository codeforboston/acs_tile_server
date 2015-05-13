

define( ["jquery", "underscore", "leaflet-0.7.3/leaflet.js", "js/censusacs/map/us-states.js" ], function (jquery, underscore, leaflet, states) {

	function UsMap(id){

		this._id = id;
		this._map = L.map(id).setView([37.8, -96], 4);

		// control that shows state info on hover
		this._info = L.control();

		this._info.onAdd = function (map) {
			this._div = L.DomUtil.create('div', 'info');
			this.update();
			return this._div;
		};

		this._info.update = function (props) {
			this._div.innerHTML = '<h4>US State</h4>' +  (props ?
					'<b>' + props.name + '</b>' : '<b>No state selected</b>' );
		};

		this._info.addTo(this._map);

		this._geojson = L.geoJson(states.statesData, {
			style: $.proxy(this.style, this) ,
			onEachFeature: $.proxy(this.onEachFeature, this)
		}).addTo(this._map);
		
		this._noAction = false;
		
		this._selected = null;

	}

	UsMap.prototype.onEachFeature = function(feature, layer) {
		layer.on({
			mouseover: $.proxy(this.highlightFeature,this),
			mouseout: $.proxy(this.resetHighlight,this),
			click: $.proxy(this.zoomToFeature,this)
		});
	};

	UsMap.prototype.setStatesInfo = function(iInfo){
		this._stateInfo = iInfo;
	}
	
	UsMap.prototype.getSelectedStateCode = function(iCode){
		return this._selected?this._stateInfo[this._selected.feature.properties.name]:null;
	}
	
	UsMap.prototype.getSelectedState = function(iCode){
		return this._selected?this._selected.feature.properties.name:null;
	}
	
	UsMap.prototype.noAction = function(){
		 $( "#"+this._id ).fadeTo(0,0.2);
		 this._noAction = true;
		 
		 var toReset = this._selected;
		 this._selected = null;
		 if (toReset) this._geojson.resetStyle(toReset);
		 this._info.update(); // unset state selection
	};
	
	UsMap.prototype.activeAction = function(iDownloadedStates){
		 $( "#"+this._id ).fadeTo(300,1.0);
		 this._noAction = false;
		 this._map.setView([37.8, -96], 4); // Switch to global view
		 this._info.update(); // unset state selection
		 this._downloadedStates = iDownloadedStates;
		 
		 var allLayers = this._geojson.getLayers();
		 for (var idx in allLayers){
			 this._geojson.resetStyle(allLayers[idx]);
		 }
		
	};
	
	// get color depending on population density value
	UsMap.prototype.getColor = function(selected, downloaded) {
		return downloaded?selected?'#00AF00':'#AFFFAF':selected?'#7F7F7F':'#AFAFAF';
	};
	
	UsMap.prototype.style = function(feature){
	
		var selected = false;
		if ((this._selected) && (this._selected.feature.properties.name==feature.properties.name)) {
			 selected = true;
			 this._selected.bringToFront();
		}
		
		var downloaded = false;
		if (this._downloadedStates) {
			var stateCode = this._stateInfo[feature.properties.name];
			if ($.inArray(stateCode,this._downloadedStates)>=0) {
				downloaded = true;
			}
		}
		
		return {
			weight: selected ? 5 : 2,
			opacity: 1,
			color: selected ? 'green': 'white',
			dashArray: '3',
			fillOpacity: 0.7,
			fillColor: this.getColor(selected,downloaded)
		};
	}

	UsMap.prototype.onStateSelected = function(iCallback){
		this._onStateSelected = iCallback;
	}
	
	UsMap.prototype.highlightFeature = function(e) {
		
		if (!this._noAction) {
			var layer = e.target;
	
			layer.setStyle({
				weight: 5,
				color: '#666',
				dashArray: '',
				fillOpacity: 0.7
			});
	
			if (!L.Browser.ie && !L.Browser.opera) {
				layer.bringToFront();
			}
			
		}
		
	};

	UsMap.prototype.resetHighlight = function(e) {
		if (!this._noAction) {
			var layer = e.target;
			this._geojson.resetStyle(layer);
		}
	};

	UsMap.prototype.zoomToFeature = function(e) {
		if (!this._noAction) {
			var layer = e.target;
			this._map.fitBounds(layer.getBounds());
			this._info.update(layer.feature.properties);
			
			var toReset = this._selected;
			this._selected = layer;
			this._geojson.resetStyle(this._selected);
			if (toReset) this._geojson.resetStyle(toReset);
			
			if (this._onStateSelected) {
				this._onStateSelected(layer.feature.properties);
			}
			
		}
	};

	return UsMap;

});
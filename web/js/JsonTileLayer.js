/*
 * L.JsonTileLayer is used for standard xyz-numbered json tile layers.
 */

L.JsonTileLayer = L.GridLayer.extend({

	options: {
		maxZoom: 18,

		subdomains: 'abc',
		errorTileUrl: '',
		zoomOffset: 0,

		maxNativeZoom: null, // Number
		tms: false,
		zoomReverse: false,
		detectRetina: false,
		crossOrigin: false
	},
	
	initialize: function (url, options) {

		this._url = url;
		this._layers_id = {};
		this._tilesKeys = {};
		
		options = L.setOptions(this, options);
		
		var pathArray = this._url.split( '/' );
		this._protocol = pathArray[0];
		this._host = pathArray[2];

		// detecting retina displays, adjusting tileSize and zoom levels
		if (options.detectRetina && L.Browser.retina && options.maxZoom > 0) {

			options.tileSize = Math.floor(options.tileSize / 2);
			options.zoomOffset++;

			options.minZoom = Math.max(0, options.minZoom);
			options.maxZoom--;
		}

		if (typeof options.subdomains === 'string') {
			options.subdomains = options.subdomains.split('');
		}

		// for https://github.com/Leaflet/Leaflet/issues/137
		if (!L.Browser.android) {
			this.on('tileunload', this._onTileRemove);
		}

		var self = this;
		var prop_url =  this._protocol+"//"+this._host+"/"+options.id+"/list/properties.json?callback=?";
		
		$.getJSON(prop_url , function(data){
			
			console.log(data);
			self._list_properties = data;
			self._properties_classes = {};
			
			if (options.onLoadedPropertiesList) {
				options.onLoadedPropertiesList(data);
			}		
		
		});
		
	},

	getProperties: function(){
		return this._list_properties;	
	},
	
	selectProperty: function(property){
		
		console.log("Entry select property");
		console.log(this);
		if (this._list_properties){
			
			if (this._list_properties[property]) {
				this._field = property;
				var self = this;
				$.getJSON( this._protocol+"//"+this._host+"/"+this.options.id+"/classes/"+this._field+".json?callback=?" , function(data){
					
					console.log("Loaded classes properties ...");
					console.log(data);
					console.log(self);
					self._properties_classes[self._field]=data;
					self._refreshStyles();
					
				} );
			}
			
		} else {
			
			var self = this;
			setTimeout(function(){
				console.log(self);
				self.selectProperty(property);
			}, 2000);
			
		}
		
	},
	
	setUrl: function (url, noRedraw) {
		this._url = url;

		if (!noRedraw) {
			this.redraw();
		}
		return this;
	},

	_style : function(feature){
		
		if (this._field){
			
			var val = feature.properties[this._field];
			var classes = this._properties_classes[this._field];
			
			if (classes) {
				for (var i=1; i<classes.length;i++){
					if (val<=classes[i]) {
						return {color: colorbrewer['YlGnBu'][classes.length-1][i], "weight": 0 , "fillOpacity": 0.5};
					}
				}
			} else {
				return {color: "#AF0000", "weight": 0 , "fillOpacity": 0.1};
			}
			
		} else {
			return {color: "#AF0000", "weight": 0 , "fillOpacity": 0.1};
		}
	},
	
	_refreshStyles : function(){
		
		console.log("Refresh Styles");
		console.log(this);
		var self = this;
		for (var key in this._layers_id) {
			var layer = this._layers_id[key].layer;
			$.each(layer._layers,function(idx){	
				this.setStyle(self._style(this.feature));
			});
		}
			
	},
	
	_getJsonRetriever : function(url,tile,done){
		
		var self = this;
		return function( data ) {
	
			var inner_url = url;
			var inner_tile = tile;
			var inner_done = done;
			var inner_zoom = self._tileZoom;
			
			var aFeature = topojson.feature(data, data.objects.MAP);
			
			var keys = [];
			
			for (var j=0; j<aFeature.features.length; j++) {
	
				var aEntity = aFeature.features[j]; // Entity, 1 sector
	
				var idKey = aEntity.id+"_"+inner_zoom;
				var layerContainer = self._layers_id[idKey];
				
				if (typeof layerContainer === 'undefined') {
			
					var geojsonLayer = L.geoJson(aEntity);
					$.each(geojsonLayer._layers,function(idx){	
						this.setStyle(self._style(this.feature));
					});
					
					layerContainer = {
							link : [inner_url],
							layer : geojsonLayer
					};
					
					self._map.addLayer(geojsonLayer);
					self._layers_id[idKey] = layerContainer;
				
				} else {
					
					if (layerContainer.link.indexOf(url)<0){
						layerContainer.link.push(inner_url);
					}
				
				}
				
				keys.push(idKey);
				
			}
			self._tilesKeys[url] = keys;
			
			inner_done(null, inner_tile);

		};
				
	},
	
	createTile: function (coords, done) {
		
		var tile = document.createElement('div');
		var url =  this.getTileUrl(coords);
		
		$.getJSON( url+"?callback=?" , $.proxy(this._getJsonRetriever(url,tile,done),this)  );
		
		return tile;
	},

	getTileUrl: function (coords) {
		return L.Util.template(this._url, L.extend({
			r: this.options.detectRetina && L.Browser.retina && this.options.maxZoom > 0 ? '@2x' : '',
			s: this._getSubdomain(coords),
			x: coords.x,
			y: this.options.tms ? this._globalTileRange.max.y - coords.y : coords.y,
			z: coords.z
		}, this.options));
	},

	_tileOnLoad: function (done, tile) {
		// For https://github.com/Leaflet/Leaflet/issues/3332
		if (L.Browser.ielt9) {
			setTimeout(L.bind(done, this, null, tile), 0);
		} else {
			done(null, tile);
		}
	},

	_tileOnError: function (done, tile, e) {
		var errorUrl = this.options.errorTileUrl;
		if (errorUrl) {
			tile.src = errorUrl;
		}
		done(e, tile);
	},

	_getTileSize: function () {
		var map = this._map,
		    options = this.options,
		    zoom = this._tileZoom + options.zoomOffset,
		    zoomN = options.maxNativeZoom;

		// increase tile size when overscaling
		return zoomN !== null && zoom > zoomN ?
				Math.round(options.tileSize / map.getZoomScale(zoomN, zoom)) :
				options.tileSize;
	},

	_onTileRemove: function (e) {
		
		var url =  this.getTileUrl(e.coords);
		var keys = this._tilesKeys[url];
			
		if (keys) {
		
			for (var index in keys) {
				
				var container = this._layers_id[keys[index]];
		
				if (container) {
					
					var idx = container.link.indexOf(url);
					if (idx>=0){
						container.link.splice(idx,1);
					}
					
					if (container.link.length==0){
						this._map.removeLayer(container.layer);
						delete this._layers_id[keys[index]];
					}
					
				}
				
			}
		
		}
		
	},

	_getZoomForUrl: function () {

		var options = this.options,
		    zoom = this._tileZoom;

		if (options.zoomReverse) {
			zoom = options.maxZoom - zoom;
		}

		zoom += options.zoomOffset;

		return options.maxNativeZoom ? Math.min(zoom, options.maxNativeZoom) : zoom;
	},

	_getSubdomain: function (tilePoint) {
		var index = Math.abs(tilePoint.x + tilePoint.y) % this.options.subdomains.length;
		return this.options.subdomains[index];
	},

	// stops loading all tiles in the background layer
	_abortLoading: function () {
		var i, tile;
		for (i in this._tiles) {
			tile = this._tiles[i].el;

			tile.onload = L.Util.falseFn;
			tile.onerror = L.Util.falseFn;

			if (!tile.complete) {
				tile.src = L.Util.emptyImageUrl;
				L.DomUtil.remove(tile);
			}
		}
	}
	
});

L.jsonTileLayer = function (url, options) {
	return new L.JsonTileLayer(url, options);
};

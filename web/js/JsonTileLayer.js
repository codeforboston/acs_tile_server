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
	},

	setUrl: function (url, noRedraw) {
		this._url = url;

		if (!noRedraw) {
			this.redraw();
		}
		return this;
	},

	_getJsonRetriever : function(url,tile,done){
		
		var inner_url = url;
		var inner_tile = tile;
		var inner_done = done;
		var inner_zoom = this._tileZoom;
		var self = this;
		
		return function( data ) {
	
			var aFeature = topojson.feature(data, data.objects.MAP);
			
			var keys = [];
			
			for (var j=0; j<aFeature.features.length; j++) {
	
				var aEntity = aFeature.features[j]; // Entity, 1 sector
	
				var idKey = aEntity.id+"_"+inner_zoom;
				var layerContainer = self._layers_id[idKey];
				
				if (typeof layerContainer === 'undefined') {
			
					var geojsonLayer = L.geoJson(aEntity, {
					    style: {color: "#AF0000", "weight": 0 , "fillOpacity": 0.2}
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
		
		$.getJSON( url+"?callback=?" , this._getJsonRetriever(url,tile,done)  );
		
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

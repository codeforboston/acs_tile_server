

// Todo find a way to solve dependency between underscore & backbone
define(["jquery", "js/backbone-min.js", 
        			"js/censusacs/censusacs_view_buildmap.js",
        			"js/censusacs/censusacs_view_selectdata.js",
        			"js/censusacs/censusacs_view_displaymap.js"],
			function (jquery, backbone, 
					censusview_buildmap,
					censusview_selectdata,
					censusview_displaymap) {

	var router;
	
	return {
		startBackboneRouter: function(){
			
			console.log("Starting backbone router");
			var Workspace = Backbone.Router.extend({

				routes: {
					"":                                     "base",
					"buildmap":        		         "buildMap",
					"table/:path/:state"       : "selectData",
					"map/:path/:state/:list" : "mapDisplay",
					"about":					         "about"
				},

				base : function() {
					
				},

				buildMap : function (){

					console.log("Building map ...");
					censusview_buildmap.buildBuildMapView(router);
					
				},
				
				selectData : function(path, state) {
					
					console.log("Select data ...");
					censusview_selectdata.buildSelectDataView(router,path,state);
					
				},
				
				mapDisplay : function(path, state, list){
					
					console.log("Display map");
					censusview_displaymap.buildMapDisplay(router,path,state,list);
					
				}

			});

			// Starting the router
			router = new Workspace();
			Backbone.history.start();

		}
	}

});
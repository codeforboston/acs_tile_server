
define(["jquery", "underscore", "bootstrap-dialog", "js/censusacs/censusacs_proxy.js" ], function (jquery, underscore, dialog, proxy  ) {
	
	return {

		buildMapDisplay : function(router,path,state,list){
			
			$("#main_view").html("");
			console.log("Building map display :");
			
			// Call build map to consolidate and build json map
			proxy.buildMap(function(iObject, iData){
				
			}, path, state, list);
			
		}
	
	};
	
});
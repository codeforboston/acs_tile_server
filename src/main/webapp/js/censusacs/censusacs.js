
require.config({
	appDir: '.',
    baseUrl: 'js',
    paths: {
        "jquery": 'jquery-1.8.3', 
        "underscore": 'underscore-min',
        "bootstrap": '../bootstrap/js/bootstrap.min',
        "bootstrap-dialog": "../bootstrap/js/bootstrap-dialog.min"
    },
    shim: {
        /* Set bootstrap dependencies (just jQuery) */
        'bootstrap' : ['jquery'],
    	'bootstrap-dialog' :  ['bootstrap']
    }
});

require(["js/censusacs/censusacs_router.js","bootstrap" ], function(router) {
    
	$(function() {
		
		router.startBackboneRouter();
			
	});
	
});



define(["jquery", "underscore", "bootstrap-dialog", "js/censusacs/censusacs_proxy.js" ], function (jquery, underscore, dialog, proxy  ) {

	function buildNext(router,path,state,listfields){
		
		// Here calling the router
		router.navigate('map/'+path+"/"+state+"/"+listfields, {trigger: true});
		
	}
	
	function buildTable(router,path,state){
		
		var stateCodesByName = null; 
		var activePath = null;

		proxy.retrieveLocalSequenceTableNumberStruct(function(object, data){
				
				console.log(data);
				var template = _.template($('#listdata').html(), {list:data } );
				$("#main_view").html(template);
				
				$(".next_button").click(function(e){
				
						var toFilter = [];
						$.each($(".checkbox_ref"), function(idx,elem){
							
							if ($(elem).attr('checked')) {
								
								var match = $(elem).attr('id').match(/field_(\w+)/);
								if (match) {
									toFilter.push(match[1]);
								}
								
							}
							
						});
						
						if (toFilter.length==0) {
							
							// Here display message to ask to select some fields to generate the map
							dialog.show({
					            title: 'Field selection',
					            message: 'Please select one or more fields to continue.',
					            buttons: [{
					                label: 'OK',
					                action: function(dialog) {
					                    dialog.close();
					                }
					            }]
					        });
							
						} else { // Here we have data to display
							
							  console.log(toFilter);
							  var list = btoa(toFilter.join(":"));
							  buildNext(router,path,state,list);
							  
						}
						
				});
				

		},path,state);
		
	}
	
	function monitorDownload(iList, router, path, state){
		
		var template = _.template($('#progress_dialog').html() );
		
		var downloadInterval = null;
		
		var dialogref = dialog.show({
			title: "Downloading files ...",
            message: $(template({data : {
            	currentfile : 'None',
            	current : 0,
            	global: 0
            }})) ,
            closable: false,
            closeByBackdrop: false,
            closeByKeyboard: false,
            buttons: [{
                label: 'Cancel',
                action: function(dialogRef){
                	dialogRef.close();
                	clearInterval(downloadInterval);
                }
            }]
        });
		
		var keys = [];
		$.each(iList, function(key, element) {
		    keys.push(key);
		});
		
		downloadInterval = setInterval(function(){ 
			
			proxy.getDownloadStatus(function(iObject, iData){
					
				    var global_p = 0, count = 0;
				    for (var key in iData) {
				    	global_p+= iData[key]._percent;
				    	count++;
				    }
				   global_p = global_p/count;
				    
					var finished = true;
					for (var key in iData) {
						
						if (iData[key]._percent>0) {
							dialogref.setMessage($(template({data : {
				            	currentfile : key,
				            	current : iData[key]._percent,
				            	global: global_p
				            }})));
						}
						
						if (!iData[key]._finish) finished = false;
				
					}
					
					if (finished) {
						clearInterval(downloadInterval);
						dialogref.close();
						buildTable(router,path,state);
					}
					
			}, keys);
			
		}, 1000);
		
	}
	
	return {


		buildSelectDataView : function(router,path,state){

			$("#main_view").html("");
			
			var stateCodesByName;
			
			proxy.listStateCodesByName(function(object, data_states){

				stateCodesByName  = data_states;
			
				var listAlreadyDownloaded = null;
				proxy.listStatesForACSData(function(iObj, iData){
	
					console.log(iData); // print result
					console.log("Identified state:"+stateCodesByName[state])
					listAlreadyDownloaded = iData;
					
					if ($.inArray(stateCodesByName[state],listAlreadyDownloaded)>=0) { // Check if we have already downloaded the set of data
	
						buildTable(router,path,state);
						
					} else {
						
						dialog.show({
							title: "Download request",
				            message: 'To build your map, we need to download Census/ACS files, proceed ?',
				            buttons: [{
				                label: 'Download',
				                cssClass: 'btn-primary',
				                action: function(dialogRef){
				                	dialogRef.close();
				                	
				                	proxy.retrieveACSFiles(function(iObject, iData){
				                		
				                		monitorDownload(iData, router, path, state);
				                		
				                	},  path , state);
				         
				                }
				            }, {
				                label: 'Cancel',
				                action: function(dialogRef){
				                	dialogRef.close();
				                }
				            }]
				        });
						
					}
	
				}, path);
		
			});
		}

	};

});


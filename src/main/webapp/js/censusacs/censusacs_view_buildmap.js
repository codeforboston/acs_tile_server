
define(["jquery", "underscore", "bootstrap-dialog", "js/censusacs/censusacs_proxy.js",  "js/censusacs/map/us_map.js" ], function (jquery, underscore, dialog, proxy, UsMap ) {

	function buildNextToSequenceNumber(router,path,state){
		
		$("#acsfolderlist").animate({width:'toggle'},350);
		// Here calling the router
		router.navigate('table/'+path+"/"+state, {trigger: true});
		
	}
	
	return {

		buildBuildMapView : function(router){

			$("#main_view").html("");
			
			var stateCodesByName = null; 
			var activePath = null;

			proxy.listStateCodesByName(function(object, data_states){

				stateCodesByName  = data_states;

				proxy.listACSFolders(function(object, data){

					console.log(data);

					var aNewList = [];
					for (var idx in data){

						var match = data[idx].match(/acs(\d+)(_(\d+)yr)?/);
						aNewList.push({
							data: "ACS",
							year: match?match[1]:"N/A",
									aggregate: match?match[3]?match[3]+' year':'None':"N/A",
											path:data[idx]
						});

					}

					var template = _.template($('#checkdata').html(), {list:aNewList } );
					$("#main_view").html(template);

					var usMap = new UsMap("us_map");
					usMap.setStatesInfo(stateCodesByName);
					usMap.noAction();

					usMap.onStateSelected(function(state){

						$(".next_button").removeClass('disabled');
						$(".next_button").addClass('active');

					});

					var listAlreadyDownloaded = null;
					$(".acs_select").click(function(evt){

						$(".next_button").removeClass('active');
						$(".next_button").addClass('disabled');

						$(".acs_select").removeClass("active");

						var line = $(evt.target).parents(".acs_select");
						$(line).addClass("active");

						usMap.noAction();

						activePath =  $(line).attr("data-path");
						proxy.listStatesForACSData(function(iObj, iData){

							console.log(iData); // print result
							listAlreadyDownloaded = iData;
							usMap.activeAction(iData);

						}, activePath);

					});

					$(".next_button").click(function(e){

						if ($(".next_button").hasClass("active")) {

							buildNextToSequenceNumber(router, activePath , usMap.getSelectedState());
																
						}

					});
					$(".next_button").addClass('disabled');

				});

			});

		}


	};

});




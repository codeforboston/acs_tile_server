
define(["jquery", "underscore" ], function (jquery, underscore) {

	var base = "/censusacs/rest/censusacs/";

	return  {
		// URL relocation
		listACSFolders : function(iCallback){

			var self = this;
			$.ajax({ 
				type: "GET",
				dataType: "json",
				url: base+"listACSFolders",
				success: function(data){        
					iCallback(self , data);
				}
			});

		},
		listStateCodesByName : function(iCallback) {

			var self = this;
			$.ajax({ 
				type: "GET",
				dataType: "json",
				url: base+"listStateCodesByName",
				success: function(data){        
					iCallback(self , data);
				}
			});

		},
		listStatesForACSData : function(iCallback, iPath){

			var self = this;
			$.ajax({ 
				type: "GET",
				dataType: "json",
				url: base+"listStatesForACSData/"+iPath,
				success: function(data){        
					iCallback(self , data);
				}
			});

		},
		
		retrieveACSFiles : function(iCallback, iPath, iState){

			var self = this;
			$.ajax({ 
				type: "GET",
				dataType: "json",
				url: base+"retrieveACSFiles/"+iPath+"/"+iState,
				success: function(data){        
					iCallback(self , data);
				}
			});

		},
		
		getDownloadStatus : function(iCallback, iObject){

			var self = this;
			$.ajax({ 
				type: "POST",
				dataType: "json",
				data : JSON.stringify(iObject),
				contentType:"application/json; charset=utf-8", 
				url: base+"getDownloadStatus",
				success: function(data){        
					iCallback(self , data);
				}
			});

		},
		 
		retrieveLocalSequenceTableNumberStruct : function(iCallback, iPath, iState){

			var self = this;
			$.ajax({ 
				type: "GET",
				dataType: "json",
				url: base+"retrieveLocalSequenceTableNumberStruct/"+iPath+"/"+iState,
				success: function(data){        
					iCallback(self , data);
				}
			});

		},
		
		buildMap : function(iCallback, iPath, iState, iList){

			var self = this;
			$.ajax({ 
				type: "GET",
				dataType: "json",
				url: base+"buildMap/"+iPath+"/"+iState+"/"+iList,
				success: function(data){        
					iCallback(self , data);
				}
			});

		}
		
	};
	
});

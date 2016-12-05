
   var result;

   var filter = "" + url.extension;
   
   var url = "/api/audit/query/alfresco-archive-toolkit?verbose=true&limit=100&forward=false";
   
   if (filter == "error"){
	   url = "/api/audit/query/alfresco-archive-toolkit/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/error/message?verbose=true&forward=false";
   }
  
   // Call the repo for audit records
   result = remote.call(url);
   if (result.status == 200)
   {
      // Create javascript objects from the server response
      model.auditResult = JSON.parse(result);
      
   }



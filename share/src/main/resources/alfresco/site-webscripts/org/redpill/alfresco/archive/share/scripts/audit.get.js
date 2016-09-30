
function main()
{
   var result;

   // Call the repo for audit records
   result = remote.call("/api/audit/query/alfresco-archive-toolkit?verbose=true&limit=100");
   if (result.status == 200)
   {
      // Create javascript objects from the server response
      model.auditResult = JSON.parse(result);
      
   }
}

main();
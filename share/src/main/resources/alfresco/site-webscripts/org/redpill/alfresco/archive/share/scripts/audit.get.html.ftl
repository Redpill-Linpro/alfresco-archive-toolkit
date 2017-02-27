<#assign auditRecords=auditResult.entries>
<@markup id="html">
   <@uniqueIdDiv>
<style>
table, th, td {
    border: 1px solid black;
}
th, td {
    padding: 15px;
}
td {
    text-align: left;
}
</style>
    <table style="width:100%">
      <tr>
        <th>Id</th>
        <th>Audit Application</th>
        <th>User</th>
        <th>Time</th>
        <th>Values</th>
      </tr>
<#if (auditRecords?size > 0)>
  <#list auditRecords as auditRecord>
      <tr>
        <td valign="top">${auditRecord.id?string}</td>
        <td valign="top">${auditRecord.application?string}</td>
        <td valign="top">${auditRecord.user?string}</td>
        <td valign="top">${auditRecord.time?string}</td>
        <td valign="top">
          <#if (auditRecord.values?size > 0)>
            <#list auditRecord.values?keys as key>
              <b>${key?string}:</b> ${auditRecord.values[key]?string}<br />
            </#list>
          </#if>
        </td>
      </tr>
  </#list>
</#if>
    </table>
   </@>
</@>
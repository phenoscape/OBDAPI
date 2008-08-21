<#include "Macros.ftl"> 

<@page title="Annotation Results">

<@section title="Background">
Class of interest from an ontology
</@section>

<#macro node id>
<a href="/html/nodes/${id}">${labels[id]!"?"})}</a>
</#macro>

<@section title="Results">
 <table>
 <#list statements as statement>
  <tr>
   <td>
    <@node id="${statement.getNodeId()}"/>
   </td>
   <td>
    <@node id="${statement.getRelationId()}"/>
   </td>
  <td>
    <#if statement.getTargetId()?exists>
    <@node id="${statement.getTargetId()}"/>
    </#if>
   </td>
  </tr>
 </#list>
 </table>
</@section>

</@page>



<#macro hrefNode id format="html">
<a href="/${format}/nodes/${id}">${id}</a>
</#macro>

<#macro section title>
<div class="section">
 <h2>${title}</h2>
 <#nested>
</div>
</#macro>

<#macro page title="OBD">
 <html>
<head>
  <title>${title}</title>
</head>
<body>
  <h1>
    ${title}
  </h1>
  <div class="content">
   <#nested>
  </div>
 </body>
</html>
</#macro>

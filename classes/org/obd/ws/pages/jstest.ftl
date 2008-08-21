<#include "PageMacros.ftl"> 
<head>

  <title>
   OBD Search
  </title>

  <link rel="stylesheet"
        title="standard"
        type="text/css"
        href="http://toy.lbl.gov:9012/amigo2/amigo2.css" />

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/external/Prototype/prototype.js">
  </script>

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/amigo/store.js">
  </script>

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/bbop/ajax.js">
  </script>

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/bbop/protocontrol.js">
  </script>

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/bbop/timer.js">
  </script>

  <link rel="stylesheet"
        title="standard"
        type="text/css"
        href="http://toy.lbl.gov:9012/amigo2/bbop/progress/tag.css" />
  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/bbop/progress/tag.js">
  </script>

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/bbop/shadowsortfunctions.js">
  </script>

  <link rel="stylesheet"
        title="standard"
        type="text/css"
        href="http://toy.lbl.gov:9012/amigo2/bbop/sortabletable.css" />
  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/bbop/sortabletable.js">
  </script>

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/obd/model.js">
  </script>

  <script type="text/javascript"
          src="http://toy.lbl.gov:9012/amigo2/obd/handle.js">
  </script>

  <script type="text/javascript"
          src="/js/obd-search-client.js">
  </script>

 </head>

  <body onload="AmiGO2_Init('http://toy.lbl.gov:9012/amigo2',
                            'http://toy.lbl.gov:9012/cgi-bin/amigo2',
                            'AmiGO2_User_ID');"
        onunload="">
   <div class="page_header">
     <span class="page_logo">[AmiGO logo]</span>

     <span class="page_title">Welcome to AmiGO!</span>
<!--
     <a href="http://toy.lbl.gov:9012/amigo2/data-manager-client.cgi">Manage Files</a>
     <a href="http://toy.lbl.gov:9012/amigo2/slim-client.cgi">Slim</a>
     <a href="http://toy.lbl.gov:9012/amigo2/termfinder-client.cgi">TermFinder</a>
-->
   </div>
   <div class="container">
    <table>
<!--
     <tr>
      <td>
       <fieldset>
        <legend>Species Database:</legend>
        <select id="speciesdbs" name="speciesdbs" size="5" multiple="true">
         <option value="AgBase">AgBase</option><option value="CGD">CGD</option><option value="DDB">DDB</option><option value="Ensembl">Ensembl</option><option value="FB">FB</option><option value="GDB">GDB</option><option value="GeneDB_Lmajor">GeneDB_Lmajor</option><option value="GeneDB_Pfalciparum">GeneDB_Pfalciparum</option><option value="GeneDB_Spombe">GeneDB_Spombe</option><option value="GeneDB_Tbrucei">GeneDB_Tbrucei</option><option value="GR">GR</option><option value="HGNC">HGNC</option><option value="IntAct">IntAct</option><option value="LIFEdb">LIFEdb</option><option value="MGI">MGI</option><option value="PINC">PINC</option><option value="PseudoCAP">PseudoCAP</option><option value="Reactome">Reactome</option><option value="RGD">RGD</option><option value="Roslin_Institute">Roslin_Institute</option><option value="SGD">SGD</option><option value="TAIR">TAIR</option><option value="TIGR">TIGR</option><option value="UniProt">UniProt</option><option value="UniProtKB">UniProtKB</option><option value="WB">WB</option><option value="ZFIN">ZFIN</option>
        </select>
       </fieldset>
      </td>
      <td>
       <fieldset>
        <legend>Evidence Codes:</legend>
        <select id="evcodes" name="evcodes" size="5" multiple="true">
         <option value="IC">IC</option><option value="IDA">IDA</option><option value="IEA">IEA</option><option value="IEP">IEP</option><option value="IGI">IGI</option><option value="IMP">IMP</option><option value="IPI">IPI</option><option value="ISS">ISS</option><option value="NAS">NAS</option><option value="ND">ND</option><option value="NR">NR</option><option value="RCA">RCA</option><option value="TAS">TAS</option>
        </select>
       </fieldset>
      </td>
      <td>
       <fieldset>
        <legend>Type:</legend>
        <select id="ontologies" name="ontologies" size="5" multiple="true">
         <option value="biological_process">Biological Process</option>
         <option value="cellular_component">Cellular Component</option>
         <option value="molecular_function">Molecular Function</option>
        </select>
       </fieldset>
      </td>
     </tr>
-->
     <tr>
      <td>Note that 'Full Search' with 'GP Search' does not work (TODO/BUG)</td>
     </tr>

     <tr>
      <td class="input">
       <input id="query" name="query" type="textbox" />
      </td>
      <td class="input">
       <input name="button"
            type="button"
            onclick="dataSearch()"
            value="Full Results" />
      </td>
     </tr>
     <tr>

      <td class="input">
       <input name="type"
            type="radio"
            value="term"
            checked="true" />
       Term
       <br />
       <input name="fields"
            type="checkbox"
            value="term_name"
            checked="true" />
       Term name
       <br />
       <input name="fields"
            type="checkbox"
            value="term_definition" />
       Term definition
       <br />

       <input name="fields"
            type="checkbox"
            value="term_synonym" />
       Term synonym
      </td>
      <td class="input">
       <input name="type"
            type="radio"
            value="gp" />
       Gene Product
       <br />
       <input name="fields"
            type="checkbox"
            value="gp_name"
            checked="true" />
       GP name
       <br />

       <input name="fields"
            type="checkbox"
            value="gp_symbol" />
       GP symbol
       <br />
       <input name="fields"
            type="checkbox"
            value="gp_synonym" />
       GP synonym
      </td>
     </tr>
    </table>
   </div>

   <div class="container">
    <table border="1">
     <thead>
      <tr>
       <td>
        Parsed
       </td>
       <td>
        OK?
       </td>

       <td>
        Distinct Hits
       </td>
       <td>
        HTTP API
       </td>
       <td>
        Full Search Time
       </td>
      </tr>

     </thead>
     <tbody>
      <tr>
       <td>
        <div id="parsed">[Parsed]</div>
       </td>
       <td>
        <div id="parser_status">[Parser Status]</div>

       </td>
       <td>
        <div id="search_summary">[Summary Results]</div>
       </td>
       <td>
        <div id="cgi_string">[String to Send]</div>
       </td>
       <td>

        <div id="timer">[Search Speed]</div>
       </td>
      </tr>
     </tbody>
    </table>
   </div>

   <div id="search_results" class="container">[Search Results]</div>

  <div class="page_footer"></div>
  <div id="load_tag"></div>
  <div id="work_tag"></div>

  </body>

</html>

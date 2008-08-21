<#include "PageMacros.ftl"> 

<@page title="Use Case 4: Annotation filtering">

<@section title="See Also">
<p>
<@usecase id="1"/>
</p>
</@section>
<@section title="Goal">
<p>
An extension of <@usecase id="1"/> (classes of interest). Often there will be large numbers of
annotations pertaining to the COI. The user needs assistance in filtering these in order to hone
in on annotations of relevance to their specific questions.
</p>
 
</@section>

<@section title="Summary">
p>
A BioPortal user arrives at a class of interest (COI) in the course of using BP. They may be on a detail page for that class; or they may be on a page summarising search results,
or some other ontology filter. The user elects to see annotations to this COI; there may be thousands or possibly even millions.
BP provides some summary of the annotation count, and allows the user to filter according to certain
preset or configurable filters.
</p>
<p>
 EXAMPLE: A researcher is interested in cardiovascular annotations. This would include annotations
 to pre-coordinated classes:
 <ul>
   <li>
      <@hrefNode id="MP:0005385" label="cardiovascular system phenotype (MP)"/>
   </li>
   <li>
    <@hrefNode id="DOID:1287" label="cardiovascular system disease (DO)"/>
   </li>
   <li>
    <@hrefNode id="FMA:7088" label="Heart (FMA)"/>
   </li>
 </ul>
 plus many others. It would also include annotations to class expressions (post-compositions) involving
 these classes.
</p>
<p>
 These classes are all very general, which means the annotation count will be extremely high. 
 The user may elect to filter on criteria such as:
 <ul>
   <li>
     The source of the annotation. For example, some annotation sources may be more trusted than
     others. This can hook into the BioPortal Web-of-Trust system.
   </li>
   <li>
     The source of the entity of interest (EOI). This will often, but not always be the same
     as the annotation source (for example, one of the DBPs may annotate an OMIM genotype)
   </li>
   <li>
     Evidence type
   </li>
   <li>
     Evidence source
   </li>
   <li>
     Organism type
   </li>
   <li>
     Annotations to other classes
   </li>
   
 </ul>
</p>


</@section>

<@section title="Pre-conditions">
<p>
As for <@usecase id="1"/>
</p>
</@section>

<@section title="Triggers">
The following in sequence
<ul>
<li>
User selects COI within BioPortal
</li>
<li>
User is given summary of annotations to this COI (including deductive closure)
</li>
<li>
User selects one or more filters
</li>
<li>
User sees annotations to COI that also fulfil filter criteria
</li>
</ul>
</p>

</@section>

<@section title="Course of events">
<ul>
<li>
As for <@usecase id="1"/>
</li>
</@section>

<@section title="Post-conditions">
<p>
Query results satisfy business rules, below
</p>

</@section>

<@section title="Business rules">
 </@section>

<@section title="Variants">
</@section>
</@page>

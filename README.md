Protege 5.0+ Plugin for Rule to OWL Axiom Conversion

For details please visit http://dase.cs.wright.edu/content/modeling-owl-rules



###Installation
1. Download ROWL plugin jar from <a href="https://github.com/md-k-sarker/ROWL/blob/master/plugin/binary/ROWL-0.0.1.jar?raw=true" title="plugin"> plugin/binary</a> folder of git repository
2. Put the jar file inside plugins folder of Protege.

###How to find plugin folder of protege 
####MAC OS
See the video <a href="https://github.com/md-k-sarker/OWLAx/blob/master/plugin/docs/Video/macPluginFolder.mov?raw=true" title="plugin"> Find Plugin Folder in Protege Mac OS Version</a>
####Windows OS
See the video <a href="https://github.com/md-k-sarker/OWLAx/blob/master/plugin/docs/Video/windowsPluginFolder.webm?raw=true" title="plugin"> Find Plugin Folder in Protege Windows OS Version</a>


###How to activate Plugin
1. Start Protege
2. Select ROWL Tab from
	 Window -> Tabs -> ROWL
	 
	 ![Alt Click on ROWLTab to Select](https://github.com/md-k-sarker/ROWL/blob/master/plugin/doc/screenshot/SelectROWLTab.png)
	 
3. Start Using OWLAx Plugin

###How to Use
####Create New Rule
1. Write SWRL Rule
2. Click on Convert to OWL Axiom Button
3. If the rule is convertible to OWL Axiom the it will show the generated axioms
4. Select those(or single) axioms to Ontology and click Integrate Button
5. The Axioms will be integrated with Ontology
6. The Rule will also be saved in the Ontology as annotation.
7. If the rule is not convertible to OWL Axiom then it gives the facility to switch to SWRLTab Plugin with this rule.

####Edit Existing Rule
1. Select a rule from the Table.
2. Click on Edit
3. Then the rule will appear in rule edit field and user can modify the rule.

####Delete Existing Rule
1. Select a rule from the Table.
2. Click on Delete
3. Then the rule will be deleted.

####Create New OWLEntity
<p>If an atom not found in the active Ontology then user can create new OWLEntity on the fly</p>

1. Right Click to see the possible suggestion to create OWL Entity

	![Alt Right Click to see the possible suggestion](https://github.com/md-k-sarker/ROWL/blob/master/plugin/doc/screenshot/SeeSuggestion.png)

2. Click on the popup menu to create new OWL Entity



###Capabilities of ROWL
<ol>
<li> Gives user way to enter OWL axioms by writing rules rather than creating axioms in protege. 
<br>
<li> If a rule is successfully converted to OWL Axiom User will get the option to choose which axioms he want to integrate.
<li> If a rule is not successfully converted to OWL Axiom it gives the option to switch to SWRL tab(Existing to SWRLTab Plugin)
<li> It can save and reload the rules(Which rules is converted to OWL Axioms and at-least 1 axiom is integrated with ontology from that rule.
<li> It checks syntax of the rule. It supports SWRL Rule Syntax.
<li> It can create new OWL Entity on the fly. That means user can create new OWL Entity like Class, ObjectProperty etc from this plugin.
</ol>

     
###Current Limitations:
<ul>
<li> It checks the syntax of the rule, not semantics. It is possible to insert meaningless rule which is syntactically correct.
<li> It doesn't support DataLog Syntax.
</ul>


###Acknowledgement
This work was supported by the National Science Foundation under award 1017225 III: Small: TROn – Tractable Reasoning with Ontologies.



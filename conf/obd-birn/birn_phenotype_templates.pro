:- op(900,xfy,where).

ce_template(Q and 'http://purl.org/obo/owl/obo#towards' some E2)
 where
  (   valid_class(Q),
      valid_class(E2)).

ce_template(Q and 'http://purl.org/obo/owl/obo#inheres_in' some E)
 where
  (   valid_class(Q),
      valid_class(E)).

ce_template(Q and 'http://purl.org/obo/owl/obo#inheres_in' some E and 'http://purl.org/obo/owl/obo#towards' some (W and 'http://www.obofoundry.org/ro/ro.owl#has_part' some P))
 where
  (   valid_class(Q),
      valid_class(W),
      valid_class(P),
      valid_class(E)).

ce_template(Q and 'http://purl.org/obo/owl/obo#towards' some (W and 'http://www.obofoundry.org/ro/ro.owl#has_part' some P))
 where
  (   valid_class(Q),
      valid_class(W),
      valid_class(P)).


ce_template(Q and 'http://purl.org/obo/owl/obo#inheres_in' some E and 'http://purl.org/obo/owl/obo#towards' some E2)
 where
  (   valid_class(Q),
      valid_class(E2),
      valid_class(E)).


% TODO: left vs right associativity
ce_template(Q and 'http://purl.org/obo/owl/obo#inheres_in' some (E and 'http://www.obofoundry.org/ro/ro.owl#part_of' some W) and 'http://purl.org/obo/owl/obo#towards' some E2)
 where
  (   valid_class(Q),
      valid_class(W),
      valid_class(E2),
      valid_class(E)).


ce_template(Q and 'http://purl.org/obo/owl/obo#inheres_in' some (E and 'http://www.obofoundry.org/ro/ro.owl#part_of' some W))
 where
  (   valid_class(Q),
      valid_class(W),
      valid_class(E)).


valid_class(C) :-
        atom(C),
        %class(C), -- will fail unless all ontologies loaded
        \+ sub_atom(C,_,_,_,'^'),
        \+ atom_concat('http://www.ifomis.org/bfo/1.1',_,C),
        \+ atom_concat('http://ontology.neuinfo.org/NIF/Backend',_,C),
        \+ atom_concat('http://ontology.neuinfo.org/NIF/DigitalEntities/NIF-Investigation.owl#',_,C),
        \+ atom_concat('http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Organism.owl#',_,C).

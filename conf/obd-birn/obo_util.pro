write_stanza(CE) :-
        CE=intersectionOf(DL),
        select(Genus,DL,DL2),
        atom(Genus),
        format('[Term]~n'),
        ce_id(CE,ID),
        format('id: ~w~n',[ID]),
        ce_id(Genus,GenusID),
        format('intersection_of: ~w~n',[GenusID]),
        forall(member(someValuesFrom(P,D),DL2),
               (   ce_id(P,PropID),
                   ce_id(D,DID),
                   format('intersection_of: ~w ~w~n',[PropID,DID]))),
        nl,
        !.
write_stanza(CE) :-
        format(user_error,'cannot write ~w~n',[CE]).

ce_id(intersectionOf(DL),ID) :-
        !,
        findall(DID,(member(D,DL),ce_id(D,DID)),DIDs),
        concat_atom(DIDs,'^',ID).
ce_id(someValuesFrom(P,D),ID) :-
        !,
        ce_id(P,PID),
        ce_id(D,DID),
        sformat(ID,'~w(~w)',[PID,DID]).
ce_id(inverseOf(P),PID) :-
        !,
        ce_id(P,PID1),
        atom_concat(PID1,'-inv',PID).
ce_id(URI,ID) :-
        urimap(URI,URI2),
        !,
        ce_id(URI2,ID).
ce_id(URI,ID) :-
        atom(URI),
        rdf_global_id(NS:Local,URI),
        !,
        concat_atom([NS,Local],':',ID).
ce_id(X,X).

urimap('http://purl.org/obo/owl/obo#towards','OBO_REL:towards').
urimap('http://purl.org/obo/owl/obo#inheres_in','OBO_REL:inheres_in').


        
        

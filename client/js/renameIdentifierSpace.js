load("obdapi.js");

var from = arguments.shift(); // eg entrezgene
var to = arguments.shift(); // eg NCGI_Gene

//jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_mouse";
jdbcPath = "jdbc:postgresql://spitz.lbl.gov:5432/obd_refg";

obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;
obd.renameIdentifierSpace(from,to);


var proof = db.runCommand({getProof:19,format:'binary'});

printjson(proof);
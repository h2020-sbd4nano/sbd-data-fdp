// Copyright (c) 2022  Egon Willighagen <egon.willighagen@gmail.com>
//
// GPL v3

@Grab(group='io.github.egonw.bacting', module='managers-rdf', version='0.3.2')
@Grab(group='io.github.egonw.bacting', module='managers-ui', version='0.3.2')

import groovy.json.JsonSlurper

bioclipse = new net.bioclipse.managers.BioclipseManager(".");
rdf = new net.bioclipse.managers.RDFManager(".");

ttlContent = bioclipse.download("https://diamonds.tno.nl/fairdatapoint?format=ttl")

kg = rdf.createInMemoryStore()
rdf.importFromString(kg, ttlContent, "Turtle")

println "@prefix dc:    <http://purl.org/dc/elements/1.1/> ."
println "@prefix dct:   <http://purl.org/dc/terms/> ."
println "@prefix foaf:  <http://xmlns.com/foaf/0.1/> ."
println "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> ."
println "@prefix sbd:   <https://www.sbd4nano.eu/rdf/#> ."
println "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> ."
println "@prefix void:  <http://rdfs.org/ns/void#> ."

// general info first

query = """
SELECT * WHERE {
  ?fdp a <https://w3id.org/fdp/fdp-o#FAIRDataPoint> ;
    <http://purl.org/dc/terms/title> ?title ;
    <http://purl.org/dc/terms/license> ?license .
}
"""
results = rdf.sparql(kg, query)

// the catalogs

query = """
SELECT DISTINCT ?catalog WHERE {
  ?fdp <http://www.w3.org/ns/ldp#contains> ?catalog .
}
"""
results = rdf.sparql(kg, query)
for (cat in results.getColumn("catalog")) {
  ttlContent = bioclipse.download("${cat}?format=ttl")
  rdf.importFromString(kg, ttlContent, "Turtle")
}

query = """
SELECT * WHERE {
  ?cat a <http://www.w3.org/ns/dcat#Catalog> ;
    <http://www.w3.org/2000/01/rdf-schema#label> ?title ;
    <http://purl.org/dc/terms/license> ?license .
}
"""
results = rdf.sparql(kg, query)

for (rowCounter=1;rowCounter<=results.getRowCount();rowCounter++) {
  println ""
  println "<${results.get(rowCounter,"cat")}>"
  println " a                    void:DatasetDescription ;"
  println " dc:source            <https://diamonds.tno.nl/> ;"
  println " dct:title            \"${results.get(rowCounter,"title")}\"@en ;"
  //println " foaf:img             <https://images.nieuwsbrieven.rivm.nl/101500/0/5763/fe1e7915ce28f7a96ca25ed234631504.png> ;"
  println " dct:license          <${results.get(rowCounter,"license")}> . # license of this metadata"
  println ""
}

// the datasets

query = """
SELECT DISTINCT ?catalog WHERE {
  ?fdp <http://www.w3.org/ns/ldp#contains> ?catalog .
}
"""
results = rdf.sparql(kg, query)
for (cat in results.getColumn("catalog")) {
  ttlContent = bioclipse.download("${cat}?format=ttl")
  rdf.importFromString(kg, ttlContent, "Turtle")
}

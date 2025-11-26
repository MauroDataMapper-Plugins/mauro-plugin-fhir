package org.maurodata.plugin.fhir.codesystem

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.maurodata.domain.terminology.Terminology
import org.maurodata.plugin.fhir.importer.FHIRCodeSystemImporter
import org.maurodata.plugin.importer.FileImportParameters
import org.maurodata.plugin.importer.FileParameter
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest
class CodeSystemImportSpec extends Specification {

    @Inject
    FHIRCodeSystemImporter fhirCodeSystemImporter

    @Unroll
    void "Test Import of Example CodeSystem"() {

        FileImportParameters fileImportParameters = new FileImportParameters()
        FileParameter fileParameter = new FileParameter()
        fileParameter.fileName = fileName
        fileParameter.fileType = "application/${format}"
        fileParameter.fileContents = this.class.classLoader.getResourceAsStream("examples/codesystem/${fhirVersion}/${format}/${fileName}").readAllBytes()
        fileImportParameters.importFile = fileParameter

        List<Terminology> terminologies = fhirCodeSystemImporter.importDomain(fileImportParameters)

        expect:

        terminologies.size() == 1
        terminologies[0].label == label
        terminologies[0].terms.size() == termsSize
        terminologies[0].terms.find {it.code == exampleTerm}


        where:

        fileName                            | format        | fhirVersion   | termsSize     | exampleTerm           | label
        'codesystem-example.json'           | 'json'        | 'r4'          | 3             | 'SChol (mmol/L)'      | 'ACME Codes for Cholesterol in Serum/Plasma'
        'codesystem-example.ttl'            | 'rdf'         | 'r4'          | 3             | 'SChol (mmol/L)'      | 'ACME Codes for Cholesterol in Serum/Plasma'
        'codesystem-example.xml'            | 'xml'         | 'r4'          | 3             | 'SChol (mmol/L)'      | 'ACME Codes for Cholesterol in Serum/Plasma'
        'codesystem-nhin-purposeofuse.json' | 'json'        | 'r4'          | 27            | 'Treatment'           | 'NHIN PurposeOfUse'
        'codesystem-nhin-purposeofuse.ttl'  | 'rdf'         | 'r4'          | 27            | 'Treatment'           | 'NHIN PurposeOfUse'
        'codesystem-nhin-purposeofuse.xml'  | 'xml'         | 'r4'          | 27            | 'Treatment'           | 'NHIN PurposeOfUse'

    }
}

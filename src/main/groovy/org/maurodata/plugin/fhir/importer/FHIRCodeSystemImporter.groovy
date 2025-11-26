package org.maurodata.plugin.fhir.importer

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.CodeSystem
import org.maurodata.domain.terminology.Term
import org.maurodata.domain.terminology.Terminology
import org.maurodata.plugin.fhir.FhirParsingHelper
import org.maurodata.plugin.importer.FileImportParameters
import org.maurodata.plugin.importer.FileParameter
import org.maurodata.plugin.importer.TerminologyImporterPlugin

@Slf4j
@Singleton
@CompileStatic
class FHIRCodeSystemImporter implements TerminologyImporterPlugin<FileImportParameters> {

    @Inject
    FhirParsingHelper fhirParsingHelper

    // TODO: Every plugin should update this to match the required version number
    final String version = '1.0.0'

    void setVersion(String version) {
        throw new RuntimeException("Must not set the version")
    }

    // TODO: Every plugin should update this to match the required plugin name
    final String displayName = 'FHIR CodeSystem Import Plugin'

    void setDisplayName(String displayName) {
        throw new RuntimeException("Must not set the display name")
    }

    // TODO: If you don't manually supply a namespace, then the package of this class will be used.
    // String namespace = 'org.maurodata.plugin.template'

    /**
     * Helper method to help determine whether this importer can handle a particular file.
     * TODO: Rewrite this to suit your application - for example, replace with something like:
     *   return contentType == 'application/xml'
     * for an importer that deals with xml files
     */
    @Override
    Boolean handlesContentType(String contentType) {
        return false
    }

    /**
     * Helper method to return the class of parameters this importer will handle.
     * TODO: Extend `FileImportParameters` or simply `ImportParameters` to include additional options.
     */
    @Override
    Class<FileImportParameters> importParametersClass() {
        return FileImportParameters
    }


    /**
     * TODO: This is the method that does all the work - implement this method.
     * Takes as input, a `params` object with all the parameters.
     * Returns a list of DataModel objects, which will be persisted by the framework.
     */
    @Override
    List<Terminology> importDomain(FileImportParameters params) {

        Object resource = fhirParsingHelper.parseFhirFile(params.importFile)
        if(!resource) {
            throw new Exception("Cannot interpret file!")
        }
        if(resource instanceof CodeSystem) {
            importDomainR4((CodeSystem) resource)
        } else if(resource instanceof org.hl7.fhir.r5.model.CodeSystem){
            importDomainR5((org.hl7.fhir.r5.model.CodeSystem) resource)
        } else return []
    }

    static List<Terminology> importDomainR4(CodeSystem codeSystem) {
        Terminology terminology = new Terminology()
        terminology.label = codeSystem.title ?: codeSystem.name

        codeSystem.concept.each {conceptDefinitionComponent ->
            Term term = new Term()
            term.code = conceptDefinitionComponent.display
            term.definition = conceptDefinitionComponent.definition
            terminology.terms.add(term)
        }
        return [terminology]
    }

    static List<Terminology> importDomainR5(org.hl7.fhir.r5.model.CodeSystem codeSystem) {
        Terminology terminology = new Terminology()
        terminology.label = codeSystem.title ?: codeSystem.name

        codeSystem.concept.each {conceptDefinitionComponent ->
            Term term = new Term()
            term.code = conceptDefinitionComponent.display
            term.definition = conceptDefinitionComponent.definition
            terminology.terms.add(term)
        }

        return [terminology]
    }


}

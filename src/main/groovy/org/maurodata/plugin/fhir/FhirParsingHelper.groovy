package org.maurodata.plugin.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import groovy.util.logging.Slf4j
import jakarta.inject.Singleton
import org.hl7.fhir.instance.model.api.IBaseResource
import org.maurodata.plugin.importer.FileParameter

@Singleton
@Slf4j
class FhirParsingHelper {

    static IBaseResource parseFhirFile(FileParameter fileParameter) {
        FhirFormat fhirFormat = getFHIRFormatFromMimeType(fileParameter)

        // e.g. application/fhir+json; fhirVersion=4.0
        if(fileParameter.fileType.contains('fhirVersion=4.0')) {
            FhirContext fhirContext = FhirContext.forR4()
            IParser parser = getParserFromFhirFormat(fhirContext, fhirFormat)
            parser.parseResource( new ByteArrayInputStream(fileParameter.fileContents))
        }

        // Try parsing the content
        Object returnedResource = null
        [FhirVersionEnum.R4, FhirVersionEnum.DSTU3, FhirVersionEnum.DSTU2, FhirVersionEnum.R5].find {fhirVersion ->
            FhirContext fhirContext = FhirContext.forCached(fhirVersion)
            IParser parser = getParserFromFhirFormat(fhirContext, fhirFormat)
            try {
                Object resource = parser.parseResource(new ByteArrayInputStream(fileParameter.fileContents))
                if (resource != null) {
                    // Try to obtain the version from resource if possible
                    // Many IBaseResource implementations expose getStructureFhirVersionEnum()
                    try {
                        FhirVersionEnum detected = (FhirVersionEnum) resource.getClass()
                            .getMethod("getStructureFhirVersionEnum")
                            .invoke(resource)
                        if (detected == fhirVersion) {
                            returnedResource = resource
                        }
                    } catch (NoSuchMethodException e) {
                        // Fall back to the candidate we used to parse
                        returnedResource = resource
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
                log.info("Cannot parse as version: ${fhirVersion}")
            }
        }
        return (IBaseResource) returnedResource
    }

    static FhirFormat getFHIRFormatFromMimeType(FileParameter fileParameter) {
        if (fileParameter.fileType.contains("fhir+json") || fileParameter.fileType.contains("json+fhir") || fileParameter.fileType.contains("application/json")) {
            return FhirFormat.JSON
        }
        if (fileParameter.fileType.contains("fhir+xml") || fileParameter.fileType.contains("xml+fhir") || fileParameter.fileType.contains("application/xml")) {
            return FhirFormat.XML
        }
        if (fileParameter.fileType.contains("fhir+rdf") || fileParameter.fileType.contains("rdf+fhir") || fileParameter.fileType.contains("application/rdf")) {
            return FhirFormat.RDF
        }
        // otherwise lets try and interpret the content:
        String content = new String(fileParameter.fileContents).trim()
        if(content.startsWith("[") || content.startsWith("{")) {
            return FhirFormat.JSON
        }
        if(content.startsWith("<")) {
            return FhirFormat.XML
        }
        if(content.startsWith("@")) {
            return FhirFormat.RDF
        }
        // default
        log.debug("Cannot detect FHIR format (JSON, XML, RDF) from the file type or the first few characters of the content")
        return null
    }

    enum FhirFormat {
        JSON, XML, RDF
    }

    static IParser getParserFromFhirFormat(FhirContext fhirContext, FhirFormat fhirFormat) {
        switch (fhirFormat) {
            case FhirFormat.XML:
                return fhirContext.newXmlParser()
                break
            case FhirFormat.RDF:
                return fhirContext.newRDFParser()
                break
            case FhirFormat.JSON:
            default:
                return fhirContext.newJsonParser()
                break
        }
    }


}

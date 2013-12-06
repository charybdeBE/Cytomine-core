package be.cytomine.utils

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ViewPortToBuildXML
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.sql.AlgoAnnotationListing
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.sql.UserAnnotationListing

import java.text.SimpleDateFormat

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:48
 */
class ReportService {

    def projectService
    def paramsService
    def grailsApplication
    def annotationListingService
    def exportService


    def createAnnotationDocuments(Long idProject, def termsParam, def usersParam, def imagesParam, def format,def response, String type) {

        Project project = projectService.read(idProject)

        if (!project) {
            throw new ObjectNotFoundException("Project $idProject was not found!")
        }

        def users = paramsService.getParamsSecUserList(usersParam,project)
        def terms = paramsService.getParamsTermList(termsParam,project)
        def images = paramsService.getParamsImageInstanceList(imagesParam,project)

        def termsName = [:]
        Term.findAllByIdInList(terms).each {
            termsName.put(it.id,it.name)
        }
        println termsName
        println images

//        def termsName = Term.findAllByIdInList(terms).collect { it.toString() }
//        def usersName = SecUser.findAllByIdInList(users).collect { it.toString() }
//        def imageInstances = ImageInstance.findAllByIdInList(images)

        def exporterIdentifier = format;
        if (exporterIdentifier == "xls") {
            exporterIdentifier = "excel"
        }
        response.contentType = grailsApplication.config.grails.mime.types[format]
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String datePrefix = simpleFormat.format(new Date())
        response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations_project${project.id}.${format}")

        AnnotationListing al = new UserAnnotationListing()
        if(type=="ALGOANNOTATION") {
            al = new UserAnnotationListing()
        } else if(type=="REVIEWEDANNOTATION") {
            al = new ReviewedAnnotationListing()
        }

        al.columnToPrint = ['basic','meta','wkt','gis','term','image','user']

        al.project = project.id
        al.images = images
        al.usersForTerm = users
        al.terms = terms

        def exportResult = []
        def annotations = annotationListingService.listGeneric(al)

        def termNameUsed = []
        def userNameUsed = []

        annotations.each { annotation ->
            def data = [:]
            println annotation
            data.id = annotation.id
            data.perimeterUnit = annotation.perimeterUnit
            data.areaUnit = annotation.areaUnit
            data.area = annotation.area
            data.perimeter = annotation.perimeter
            data.XCentroid = annotation.x
            data.YCentroid = annotation.y
            data.image = annotation.image
            data.filename = annotation.originalfilename
            data.user = annotation.creator
            data.term = annotation.term.collect{termsName.get(it)}.join(", ")
            data.cropURL = UrlApi.getAnnotationCropWithAnnotationId(annotation.id)
            data.cropGOTO = UrlApi.getAnnotationURL(annotation.project, annotation.image, annotation.id)
            exportResult.add(data)
            annotation.term.each{termNameUsed << termsName.get(it)}
            userNameUsed << annotation.creator
        }
        termNameUsed.unique()
        userNameUsed.unique()
        List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
        Map labels = ["id": "Id", "area": "Area (microns²)", "perimeter": "Perimeter (mm)", "XCentroid": "X", "YCentroid": "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View annotation picture", "cropGOTO": "View annotation on image"]
        String title = "Annotations in " + project.getName() + " created by " + userNameUsed.join(" or ") + " and associated with " + termNameUsed.join(" or ") + " @ " + (new Date()).toLocaleString()
        exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04, 0.06, 0.06, 0.04, 0.04, 0.04, 0.08, 0.06, 0.06, 0.25, 0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])


    }
}









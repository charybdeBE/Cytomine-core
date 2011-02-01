package be.cytomine.command

import grails.converters.JSON
import be.cytomine.project.Annotation
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.project.Scan

class EditAnnotationCommand extends Command implements UndoRedoCommand  {


  def execute() {
    def postData = JSON.parse(postData)
    def updatedAnnotation = Annotation.get(postData.annotation.id)
    def backup = updatedAnnotation.encodeAsJSON() //we encode as JSON otherwise hibernate will update its values

    if (!updatedAnnotation ) {
      return [data : [success : false, message : "Annotation not found with id: " + postData.annotation.id], status : 404]
    }

    for (property in postData.annotation) {
      //TODO: bad code...

      if(property.key.equals("location"))
      {
      //location is a Geometry object
        updatedAnnotation.properties.put(property.key, new WKTReader().read(property.value))
      }
      else if(property.key.equals("scan"))
      {
        //scan is a scan object and not a simple id
        updatedAnnotation.properties.put(property.key, Scan.get(property.value))
      }
      else if(!property.key.equals("class"))
      {
        //no propery class
        updatedAnnotation.properties.put(property.key, property.value)
      }
    }


    if ( updatedAnnotation.validate()) {
      data = ([ previousAnnotation : (JSON.parse(backup)), newAnnotation :  updatedAnnotation]) as JSON
      updatedAnnotation.save()
      return [data : [success : true, message:"ok", user :  updatedAnnotation], status : 200]
    } else {
      return [data : [user :  updatedAnnotation, errors : [ updatedAnnotation.errors]], status : 403]
    }


  }

  def undo() {
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.previousAnnotation.id)
    annotation.name = annotationsData.previousAnnotation.name
    annotation.location = new WKTReader().read(annotationsData.previousAnnotation.location)
    annotation.scan = Scan.get(annotationsData.previousAnnotation.scan)
    annotation.save()
    return [data : [success : true, message:"ok", annotation : annotation], status : 200]
  }

  def redo() {
    def annotationsData = JSON.parse(data)
    Annotation annotation = Annotation.findById(annotationsData.newAnnotation.id)
    annotation.name = annotationsData.newAnnotation.name
    annotation.location = new WKTReader().read(annotationsData.newAnnotation.location)
    annotation.scan = Scan.get(annotationsData.newAnnotation.scan)
    annotation.save()
    return [data : [success : true, message:"ok", nnotation : annotation], status : 200]
  }
}

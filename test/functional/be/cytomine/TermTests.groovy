package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.TermAPI
import be.cytomine.utils.JSONUtils
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class TermTests  {


  void testListOntologyTermByOntologyWithCredential() {
      Ontology ontology = BasicInstanceBuilder.getOntology()
      def result = TermAPI.listByOntology(ontology.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testListTermOntologyByOntologyWithOntologyNotExist() {
      def result = TermAPI.listByOntology(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

    void testListOntologyTermByProjectWithCredential() {
        Project project = BasicInstanceBuilder.getProject()
        def result = TermAPI.listByProject(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListTermOntologyByProjectWithProjectNotExist() {
        def result = TermAPI.listByProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testStatTerm() {
        def result = TermAPI.statsTerm(BasicInstanceBuilder.getTerm().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testStatTermNotExist() {
        def result = TermAPI.statsTerm(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


  void testListTermWithCredential() {
      def result = TermAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowTermWithCredential() {
      def result = TermAPI.show(BasicInstanceBuilder.getTerm().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddTermCorrect() {
      def termToAdd = BasicInstanceBuilder.getTermNotExist()
      def result = TermAPI.create(termToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      int idTerm = result.data.id

      result = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      result = TermAPI.undo()
      assert 200 == result.code

      result = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code

      result = TermAPI.redo()
      assert 200 == result.code

      result = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
  }
    
    void testAddTermMultipleCorrect() {
        def termToAdd1 = BasicInstanceBuilder.getTermNotExist()
        def termToAdd2 = BasicInstanceBuilder.getTermNotExist()
        def terms = []
        terms << JSON.parse(termToAdd1.encodeAsJSON())
        terms << JSON.parse(termToAdd2.encodeAsJSON())
        def result = TermAPI.create(JSONUtils.toJSONString(terms) , Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }    

    void testAddTermAlreadyExist() {
       def termToAdd = BasicInstanceBuilder.getTerm()
        println "termToAdd="+termToAdd
        println "termToAdd.o="+termToAdd.ontology
        def data = (termToAdd as JSON).toString()
        println data
        println data.class
       def result = TermAPI.create((termToAdd as JSON).toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)


       assert 409 == result.code
   }
 
   void testUpdateTermCorrect() {
       Term term = BasicInstanceBuilder.getTerm()
       def data = UpdateData.createUpdateSet(term,[name: ["OLDNAME","NEWNAME"]])
       def result = TermAPI.update(term.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       def json = JSON.parse(result.data)
       assert json instanceof JSONObject
       int idTerm = json.term.id
 
       def showResult = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       json = JSON.parse(showResult.data)
       BasicInstanceBuilder.compare(data.mapNew, json)
 
       showResult = TermAPI.undo()
       assert 200 == result.code
       showResult = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))
 
       showResult = TermAPI.redo()
       assert 200 == result.code
       showResult = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
   }
 
   void testUpdateTermNotExist() {
       Term termWithOldName = BasicInstanceBuilder.getTerm()
       Term termWithNewName = BasicInstanceBuilder.getTermNotExist()
       termWithNewName.save(flush: true)
       Term termToEdit = Term.get(termWithNewName.id)
       def jsonTerm = termToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonTerm)
       jsonUpdate.name = termWithOldName.name
       jsonUpdate.id = -99
       jsonTerm = jsonUpdate.toString()
       def result = TermAPI.update(-99, jsonTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == result.code
    }
 
   void testUpdateTermWithNameAlreadyExist() {
       Term termWithOldName = BasicInstanceBuilder.getTerm()
       Term termWithNewName = BasicInstanceBuilder.getTermNotExist()
       termWithNewName.ontology = termWithOldName.ontology
       termWithNewName.save(flush: true)
       Term termToEdit = Term.get(termWithNewName.id)
       def jsonTerm = termToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonTerm)
       jsonUpdate.name = termWithOldName.name
       jsonTerm = jsonUpdate.toString()
       def result = TermAPI.update(termToEdit.id, jsonTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 409 == result.code
   }
     
     void testEditTermWithBadName() {
         Term termToAdd = BasicInstanceBuilder.getTerm()
         Term termToEdit = Term.get(termToAdd.id)
         def jsonTerm = termToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonTerm)
         jsonUpdate.name = null
         jsonTerm = jsonUpdate.toString()
         def result = TermAPI.update(termToAdd.id, jsonTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 400 == result.code
     }
 
   void testDeleteTerm() {
       def termToDelete = BasicInstanceBuilder.getTermNotExist()
       assert termToDelete.save(flush: true)!= null
       def id = termToDelete.id
       def result = TermAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
 
       def showResult = TermAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == showResult.code
 
       result = TermAPI.undo()
       assert 200 == result.code
 
       result = TermAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
 
       result = TermAPI.redo()
       assert 200 == result.code
 
       result = TermAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == result.code
   }
 
   void testDeleteTermNotExist() {
       def result = TermAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == result.code
   }
}
